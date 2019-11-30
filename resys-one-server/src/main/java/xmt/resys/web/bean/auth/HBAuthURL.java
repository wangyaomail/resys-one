package xmt.resys.web.bean.auth;

import lombok.Data;

/**
 * 一个判断权限有一个url和method
 */
@Data
public class HBAuthURL {
    private String url;
    private String method; // GET, POST, PUT, DELETE, ALL
}
