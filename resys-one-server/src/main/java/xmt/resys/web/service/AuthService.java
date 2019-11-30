package xmt.resys.web.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xmt.resys.common.dao.l1.LocalCacheDao;
import xmt.resys.common.server.MainServer;
import xmt.resys.common.service.BaseService;
import xmt.resys.common.service.TimerService;
import xmt.resys.user.bean.mongo.HBModule;
import xmt.resys.user.bean.mongo.HBUser;
import xmt.resys.user.dao.UserDao;
import xmt.resys.user.service.ModuleService;
import xmt.resys.util.encrypt.AESUtil;
import xmt.resys.util.encrypt.MD5Util;
import xmt.resys.util.encrypt.coder.BASE64Encoder;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.bean.auth.HBUserSession;

/**
 * 以前对服务进行过拆分，现在都归总到这里实现
 */
@Service
public class AuthService extends BaseService {
    @Resource
    private UserDao userDao;
    @Resource
    private LocalCacheDao localCacheDao;
    @Autowired
    private MainServer mainServer;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    protected HttpServletRequest request;

    /**
     * 对用户输入的密码进行MD5加密
     */
    public String encodePassword(String rawPassword) {
        return new String(MD5Util.EncodeByMd5(mainServer.conf().getTokenSaltKey() + rawPassword));
    }

    private final String encryptTok = ",,";

    /**
     * 生成用户校验用的token，不用jwt之后，我们可以在这个方法体中直接运用加密
     * @INFO 秘钥由两部分组成，前半部分将用户id+创建时间加在一起进行AES加密作为sessionid
     * @INFO session每次生成的时候，如果不是从缓存中获取的，那么都要调用这个方法作为对session的初始化
     */
    public HBUserSession generateToken(HBUserSession session) {
        String firstToken = null;
        String secondToken = null;
        { // 对秘钥的前半部分加密
            StringBuilder sb = new StringBuilder();
            // 增加userid
            sb.append(session.getUser().getId()).append(encryptTok);
            // 增加创建时间
            sb.append(session.getLastVisitTime());
            // 生成token的初始值
            String rawToken = sb.toString();
            try {
                // token加密
                byte[] aesTok = AESUtil.encrypt(rawToken, mainServer.conf().getTokenFstAesSalt());
                // Base64序列化
                firstToken = BASE64Encoder.encode(aesTok);
            } catch (Exception e) {
                logger.error("token加密过程失败", e);
            }
        }
        { // 对秘钥的后半部分加密
            StringBuilder sb = new StringBuilder();
            // 增加userid
            sb.append(session.getUser().getId()).append(encryptTok);
            // 增加username
            sb.append(session.getUser().getUserName()).append(encryptTok);
            // 增加
            sb.append(HBCollectionUtil.listToString(session.getRole())).append(encryptTok);
            // 生成token的初始值
            String rawToken = sb.toString();
            try {
                // token加密
                byte[] aesTok = AESUtil.encrypt(rawToken, mainServer.conf().getTokenSndAesSalt());
                // Base64序列化
                secondToken = BASE64Encoder.encode(aesTok);
            } catch (Exception e) {
                logger.error("token加密过程失败", e);
            }
        }
        if (HBStringUtil.isNotBlank(firstToken) && HBStringUtil.isNotBlank(secondToken)) {
            session.setId(firstToken); // 注意在这里对session进行的赋值
            session.setToken(firstToken + "." + secondToken);
        }
        return session;
    }

    /**
     * 检查用户输入的密码是否有效
     */
    public boolean checkPasswdValid(String inputPwd,
                                    String userPwd) {
        String inputPwdEncode = encodePassword(inputPwd);
        return HBStringUtil.checkIsEqual(inputPwdEncode, userPwd);
    }

    /**
     * 按照用户名密码登陆
     */
    public HBUserSession login(String username,
                               String password) {
        HBUser user = userDao.findUserByName(username);
        if (user != null && user.getValid()) {
            HBUserSession session = new HBUserSession(user);
            session.setLastVisitUrl(request.getRemoteAddr());
            if (checkPasswdValid(password, session.getUser().getPassword())) {
                // 登陆成功，返回用户Session
                generateToken(session);
                localCacheDao.put(session.getId(), session);
                return session;
            } else {
                // 登陆失败，返回null
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 按照用户id直接登录
     */
    public HBUserSession loginById(String userid) {
        // 先去session中查询有没有session，如果有，直接拿出来比对即可
        HBUserSession session = (HBUserSession) localCacheDao.get(userid);
        if (session == null) {
            HBUser user = userDao.findOne(userid);
            if (user != null && user.getValid()) {
                session = new HBUserSession(user);
                localCacheDao.put(session.getId(), session);
            }
        }
        return generateToken(session);
    }

    /**
     * 检查用户输入的密码是否准确
     */
    public HBUserSession checkPasswordValid(String userid,
                                            String password) {
        // 先去session中查询有没有session，如果有，直接拿出来比对即可
        HBUserSession session = (HBUserSession) localCacheDao.get(userid);
        if (session == null) {
            HBUser user = userDao.findOne(userid);
            if (user != null) {
                session = new HBUserSession(user);
                if (checkPasswdValid(password, session.getUser().getPassword())) {
                    // 登陆成功，返回用户Session
                    generateToken(session);
                    localCacheDao.put(session.getId(), session);
                    return session;
                } else {
                    // 登陆失败，返回null
                    return null;
                }
            } else {
                return null;
            }
        } else {
            if (checkPasswdValid(password, session.getUser().getPassword())) {
                // 登陆成功，返回用户Session
                return generateToken(session);
            } else {
                // 登陆失败，返回null
                return null;
            }
        }
    }

    /**
     * 检查session是否过期，如果存在session，可以直接返回
     * @FIXME 注意这里正常情况下会对session进行两次查询，似乎都走redis了，这样是很慢的，所以后面需要在redis和local之间再封装一层
     */
    public HBUserSession checkSessionValid(String sessionKey) {
        HBUserSession session = (HBUserSession) localCacheDao.get(sessionKey);
        if (session != null) {
            if ((TimerService.now_to_timestamp
                    - session.getLastVisitTime() < mainServer.conf().getSessionExpiration())
                    && session.getLastVisitUrl().equals(request.getRemoteAddr())) {
                session.setExpired(false);
            } else {
                localCacheDao.remove(sessionKey);
                session.setExpired(true);
            }
        }
        return session;
    }

    /**
     * 检查用户从前端送来的token是否合法
     * @INFO 如果用户的session已经存在，说明用户还在登录状态，那么检查用户的url和上次访问的url是否一致即可，如果url发生了变化，那么session失效，需要重新校验token
     * @INFO 如果合法，检查完之后返回布尔值，且在request中添加userid字段
     */
    public HBUserSession checkTokenValid(String encryToken) {
        try {
            String[] sessionkey = encryToken.split("\\.");
            // 先查询当前有没有用户的session，如果session有，通过session检查
            HBUserSession session = checkSessionValid(sessionkey[0]);
            if (session != null && !session.isExpired()) {
                return session;
            } else {
                // 如果session不存在或已经过期，那么token中进行检查
                // Base64反序列化
                byte[] tokenBytes = BASE64Encoder.decode(encryToken.split("\\.")[0]);
                // token解密
                String aesTok = AESUtil.decrypt(tokenBytes, mainServer.conf().getTokenFstAesSalt());
                // 检查用户的时间是否有效
                String[] fstToken = aesTok.split(encryptTok);
                if (TimerService.now_to_timestamp
                        - Long.parseLong(fstToken[1]) > mainServer.conf().getTokenExpiration()) {
                    return null;
                } else {
                    return rebuildSessionByToken(encryToken);
                }
            }
        } catch (Exception e) {
            logger.error("token加密过程失败", e);
            return null;
        }
    }

    /**
     * 获取用户的Session，获取过程会对token合法性进行校验，如果获取不为空也意味着token合法
     */
    public HBUserSession getUserSessionByToken(String encryToken) {
        try {
            String[] sessionkey = encryToken.split("\\.");
            // 先查询当前有没有用户的session，如果session有，通过session检查
            HBUserSession session = checkSessionValid(sessionkey[0]);
            if (session != null && !session.isExpired()) {
                return session;
            } else {
                // 如果session不存在或已经过期，那么由token进行重建
                return rebuildSessionByToken(encryToken);
            }
        } catch (Exception e) {
            logger.error("token解密过程失败", e);
            return null;
        }
    }

    public HBUserSession rebuildSessionByToken(String encryToken) throws Exception {
        String[] sessionkey = encryToken.split("\\.");
        // Base64反序列化
        byte[] tokenBytes = BASE64Encoder.decode(sessionkey[1]);
        // token解密
        String aesTok = AESUtil.decrypt(tokenBytes, mainServer.conf().getTokenSndAesSalt());
        // 重建session，重建并不从数据库恢复session下的用户对象，如果需要进行高级的操作才需要从数据库读取user对象
        HBUserSession session = new HBUserSession();
        session.setId(sessionkey[0]);
        String[] tokValues = aesTok.split(encryptTok);
        session.setUserid(tokValues[0]);
        session.setUsername(tokValues[1]);
        session.setRole(HBCollectionUtil.stringToList(tokValues[2]));
        //session.setPhoneNum(tokValues[3]);
        session.setToken(encryToken);
        session.setLastVisitTime(TimerService.now_to_timestamp);
        session.setLastVisitUrl(request.getRemoteAddr());
        request.setAttribute("usersession", session);
        localCacheDao.put(session.getId(), session);
        return session;
    }

    /**
     * 将session中信息填充到request中，如果没有填充也不返回错误，有多少能填充的填充多少
     */
    public void fullfillRequestBySession(String sessionkey) {
        HBUserSession session = (HBUserSession) localCacheDao.get(sessionkey);
        if (session != null) {
            if (session.getUserid() != null) {
                request.setAttribute("userid", session.getUserid());
            }
            if (session.getUsername() != null) {
                request.setAttribute("username", session.getUsername());
            }
            if (session.getRole() != null) {
                request.setAttribute("userrole", session.getRole());
            }
            if (session.getPhoneNum() != null) {
                request.setAttribute("phoneNum", session.getPhoneNum());
            }
            if (session.getToken() != null) {
                request.setAttribute("usertoken", session.getToken());
            }
        }
    }

    /**
     * 将token中的字段放入request
     * @INFO 因为session无关，不再需要对token进行校验，不过解密成功意味着传入的token是合法的
     */
    public void fullfillRequestByToken(String encryToken) {
        try {
            // Base64反序列化
            byte[] tokenBytes = BASE64Encoder.decode(encryToken.split("\\.")[1]);
            // token解密
            String aesTok = AESUtil.decrypt(tokenBytes, mainServer.conf().getTokenSndAesSalt());
            // 重建session，重建并不从数据库恢复session下的用户对象，如果需要进行高级的操作才需要从数据库读取user对象
            String[] tokValues = aesTok.split(encryptTok);
            request.setAttribute("userid", tokValues[0]);
            request.setAttribute("username", tokValues[1]);
            request.setAttribute("userrole", HBCollectionUtil.stringToList(tokValues[2]));
            // request.setAttribute("phoneNum", tokValues[3]);
            request.setAttribute("usertoken", encryToken);
        } catch (Exception e) {
            logger.error("token解密过程失败", e);
        }
    }

    /**
     * 传入的url是可以匿名访问
     */
    public boolean isUrlAnonymously() {
        HBModule anonymouslyModule = moduleService.getAnonymouslyModule();
        return anonymouslyModule != null && moduleService.matches(anonymouslyModule, request);
    }
}
