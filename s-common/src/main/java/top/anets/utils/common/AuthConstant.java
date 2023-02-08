package top.anets.utils.common;

public interface AuthConstant {
    /**
     * 认证信息Http请求头
     */
    String JWT_TOKEN_HEADER = "Authorization";
    /**
     * JWT令牌前缀
     */
    String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT存储权限前缀
     */
    String AUTHORITY_PREFIX = "ROLE_";
    /**
     * JWT存储权限属性
     */
    String AUTHORITY_CLAIM_NAME = "authorities";

    /**
     * 用户信息Http请求头
     */
    String USER_TOKEN_HEADER = "user";

}
