package xmt.resys.common.bean.http;

import java.util.HashMap;

import lombok.Data;
import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.util.set.HBCollectionUtil;

@Data
public class ResponseBean {
    private String code = ApiCode.SUCCESS.getCode();
    private String errMsg = "";
    private Object data;
    private String stackMsg;

    /**
     * 如果data里只是一个keyvalue，这样就足够了
     */
    public ResponseBean setOneData(String key,
                                   Object value) {
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(key, value);
        data = map;
        return this;
    }

    public ResponseBean end() {
        if (data == null) {
            data = HBCollectionUtil.emptyList;
        }
        if (!ApiCode.SUCCESS.getCode().equals(code)) {
            errMsg += "\n若您操作无误，请联系网站管理人员";
        }
        return this;
    }

    public void setCodeAndErrMsg(String code,
                                 String errMsg) {
        this.code = code;
        this.errMsg = errMsg;
    }

    public void setCodeEnum(ApiCode code) {
        this.code = code.getCode();
        this.errMsg = code.getName();
    }
}
