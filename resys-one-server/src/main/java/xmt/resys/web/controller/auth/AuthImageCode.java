package xmt.resys.web.controller.auth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.dao.l1.LocalCacheDao;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.controller.BaseController;

/**
 * 登陆的校验图片
 */

@Controller
@RequestMapping(value = "/${api.version}/f/auth/img")
public class AuthImageCode extends BaseController {
    @Resource
    private LocalCacheDao localCacheDao;

    @RequestMapping(value = "/getAuthCode")
    @ResponseStatus(HttpStatus.OK)
    public void getAuthCode(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {
        String sessionCode = getSessionid();
        if (HBStringUtil.isBlank(sessionCode)) {
            return;
        }
        int width = 63;
        int height = 37;
        Random random = new Random();
        // 设置response头信息
        // 禁止缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 生成缓冲区image类
        BufferedImage image = new BufferedImage(width, height, 1);
        // 产生image类的Graphics用于绘制操作
        Graphics g = image.getGraphics();
        // Graphics类的样式
        g.setColor(this.getRandColor(200, 250));
        g.setFont(new Font("Papyrus", 0, 28));
        g.fillRect(0, 0, width, height);
        // 绘制干扰线
        for (int i = 0; i < 100; i++) {
            g.setColor(this.getRandColor(130, 200));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int x1 = random.nextInt(12);
            int y1 = random.nextInt(12);
            g.drawLine(x, y, x + x1, y + y1);
        }
        // 绘制字符
        String strCode = "";
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(random.nextInt(10));
            strCode = strCode + rand;
            g.setColor(new Color(20 + random.nextInt(110),
                                 20 + random.nextInt(110),
                                 20 + random.nextInt(110)));
            g.drawString(rand, 13 * i + 6, 28);
        }
        // 把image的信号存到redis中，5分钟内输入有效
        localCacheDao.put(sessionCode + ".auth_img_code", strCode);
        request.getSession().setAttribute("auth_code", strCode);
        g.dispose();
        ImageIO.write(image, "JPEG", response.getOutputStream());
        response.getOutputStream().flush();
    }

    @RequestMapping(value = "/checkAuthCode/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public JSONObject checkAuthCode(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable String code)
            throws IOException {
        JSONObject rsJson = new JSONObject();
        String sessionCode = request.getHeader("sessionCode");
        if (StringUtils.isEmpty(sessionCode)) {
            rsJson.put("code", ApiCode.PARAM_CONTENT_ERROR);
            return rsJson;
        }
        if (StringUtils.isEmpty(code)) {
            rsJson.put("code", ApiCode.PARAM_CONTENT_ERROR);
            return rsJson;
        }
        Object authCode = request.getSession().getAttribute("auth_code");
        if (authCode == null) {
            rsJson.put("code", ApiCode.PARAM_CONTENT_ERROR);
            return rsJson;
        }
        if (authCode.toString().equals(code.trim())) {
            rsJson.put("code", ApiCode.PARAM_CONTENT_ERROR);
        } else {
            rsJson.put("code", ApiCode.PARAM_CONTENT_ERROR);
        }
        return rsJson;
    }

    Color getRandColor(int fc,
                       int bc) {
        Random random = new Random();
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
