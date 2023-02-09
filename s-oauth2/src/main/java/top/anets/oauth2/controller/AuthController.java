package top.anets.oauth2.controller;

import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import top.anets.oauth2.domain.Oauth2TokenDto;
import top.anets.oauth2.service.AuthService;
import top.anets.base.RequestUtil;
import top.anets.base.Result;
import top.anets.utils.common.AuthConstant;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
public class AuthController {
    Logger logger = LoggerFactory.getLogger(getClass());

    private static final String HEADER_TYPE = "Basic ";

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private AuthService authService;

    @Autowired
    private ConsumerTokenServices consumerTokenServices;

    @GetMapping("/user/refreshToken") // localhost:7001/auth/user/refreshToken?refreshToken=xxxx
    public Result refreshToken(HttpServletRequest request) {
        try {
            // 获取请求中的刷新令牌
            String refreshToken = request.getParameter("refreshToken");
            Preconditions.checkArgument(StringUtils.isNotEmpty(refreshToken), "刷新令牌不能为空");
            // 获取请求头
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if(header == null || !header.startsWith(HEADER_TYPE)) {
                throw new UnsupportedOperationException("请求头中无client信息");
            }
            // 解析请求头的客户端信息
            String[] tokens = RequestUtil.extractAndDecodeHeader(header);
            assert tokens.length == 2;

            String clientId = tokens[0];
            String clientSecret = tokens[1];

            // 查询客户端信息，核对是否有效
            ClientDetails clientDetails =
                    clientDetailsService.loadClientByClientId(clientId);
            if(clientDetails == null) {
                throw new UnsupportedOperationException("clientId对应的配置信息不存在：" + clientId);
            }
            // 校验客户端密码是否有效
            if( !passwordEncoder.matches(clientSecret, clientDetails.getClientSecret())) {
                throw new UnsupportedOperationException("无效clientSecret");
            }
            // 获取新的认证信息
            return authService.refreshToken(header, refreshToken);
        } catch(Exception e) {
            logger.error("refreshToken={}", e.getMessage(), e);
            return Result.error("新令牌获取失败：" + e.getMessage());
        }
    }

    /**
     * 自定义登录授权
     * @param request
     * @param grant_type
     * @param client_id
     * @param client_secret
     * @param refresh_token
     * @param username
     * @param password
     * @return
     * @throws HttpRequestMethodNotSupportedException
     */
    @RequestMapping("/oauth/token")
    public Result postAccessToken(HttpServletRequest request,
                                                        @ApiParam("授权模式") @RequestParam String grant_type,
                                                        @ApiParam("Oauth2客户端ID") @RequestParam String client_id,
                                                        @ApiParam("Oauth2客户端秘钥") @RequestParam String client_secret,
                                                        @ApiParam("刷新token") @RequestParam(required = false) String refresh_token,
                                                        @ApiParam("登录用户名") @RequestParam(required = false) String username,
                                                        @ApiParam("登录密码") @RequestParam(required = false) String password,
                                  @ApiParam("授权码") @RequestParam(required = false) String code

       ) throws HttpRequestMethodNotSupportedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("grant_type",grant_type);
//        parameters.put("client_id",client_id);
//        parameters.put("client_secret",client_secret);
        parameters.putIfAbsent("refresh_token",refresh_token);
        parameters.putIfAbsent("username",username);
        parameters.putIfAbsent("password",password);
        parameters.putIfAbsent("code",code );


//      在这里，可以对grant_type,进行拦截，比如说如果是微信登录，可以先进行登录，然后再调下面的方法进行颁发令牌
//      当然，也可以真实的把微信登录这个授权类型继承过去
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(request.getUserPrincipal(), parameters).getBody();
        Oauth2TokenDto oauth2TokenDto = Oauth2TokenDto.builder()
                .token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .tokenHead(AuthConstant.JWT_TOKEN_PREFIX).build();
        return Result.success(oauth2TokenDto);
    }


    /**
     * 这个要结合网关
     * @param
     * @return
     */
    @RequestMapping("/oauth/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader(AuthConstant.JWT_TOKEN_HEADER);
        String realToken = token.replace(AuthConstant.JWT_TOKEN_PREFIX, "");
        if (consumerTokenServices.revokeToken(realToken)) {
            return Result.success( "登出成功");
        } else {
            return Result.error( "登出失败");
        }
    }


//    /**
//     * 获取当前用户
//     */
//    @RequestMapping("/oauth/user")
//    public Result logout(){
//        return Result.success(AuthUtil.getUserInfo());
//    }


}
