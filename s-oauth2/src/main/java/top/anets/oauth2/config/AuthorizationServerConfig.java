package top.anets.oauth2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import top.anets.oauth2.module.wechat.WechatTokenGranter;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAuthorizationServer // 标识为认证服务器
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Bean // 客户端使用jdbc管理
    public ClientDetailsService jdbcClientDetailsService() {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * 配置被允许访问认证服务的客户端信息：数据库方式管理客户端信息
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails( jdbcClientDetailsService() );

    }



    @Autowired // 在SpringSecurityConfig中已经添加到容器中了
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Resource
    private TokenStore tokenStore;

    @Resource
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Resource // 注入增强器
    private TokenEnhancer jwtTokenEnhancer;
    /**
     * 关于认证服务器端点配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 密码模块必须使用这个authenticationManager实例
        endpoints.authenticationManager(authenticationManager);
        // 刷新令牌需要 使用userDetailsService
        endpoints.userDetailsService(userDetailsService);
        // 令牌管理方式，如果不需要token过期时间用下面这句话
//        endpoints.tokenStore(tokenStore).accessTokenConverter(jwtAccessTokenConverter);
        endpoints.tokenServices(defaultTokenServices());
//      token转换器
        // 添加增强器,扩展器（客户端需要其他用户信息，则可以进行扩展）S============================
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        // 组合 增强器和jwt转换器
        List<TokenEnhancer> enhancerList = new ArrayList<>();
        enhancerList.add(jwtTokenEnhancer);
        enhancerList.add(jwtAccessTokenConverter);
        enhancerChain.setTokenEnhancers(enhancerList);
        // 将认证信息的增强器添加到端点上
        endpoints.tokenEnhancer(enhancerChain).accessTokenConverter(jwtAccessTokenConverter);
        // 添加增强器,扩展器（客户端需要其他用户信息，则可以进行扩展）E============================


//      授权模式配置自定义授权的接受入口
        endpoints.pathMapping("/oauth/confirm_access","/custom/confirm_access");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // /oauth/check_token 解析令牌，默认情况 下拒绝访问
        security.checkTokenAccess("permitAll()");

//      这句话很重要,如果没有这句话，client_id 会走basic auth认证 保护 ； 有这个话则会取url中 client_id和client_secret  来保护
//        security.allowFormAuthenticationForClients();


    }





    /**
     * 创建grant_type列表  -------这个如果有自定义授权类型才配置
     * @param endpoints
     * @return
     */
    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> list = new ArrayList<>();
        // 这里配置密码模式
        if (authenticationManager != null) {
            list.add(new ResourceOwnerPasswordTokenGranter(authenticationManager,
                    endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(),
                    endpoints.getOAuth2RequestFactory()));
        }

        //刷新token模式、
        list.add(new RefreshTokenGranter
                (endpoints.getTokenServices(),
                        endpoints.getClientDetailsService(),
                        endpoints.getOAuth2RequestFactory()));

        //授权码模式、
        list.add(new AuthorizationCodeTokenGranter(
                endpoints.getTokenServices(),
                endpoints.getAuthorizationCodeServices(),
                endpoints.getClientDetailsService(),
                endpoints.getOAuth2RequestFactory()));
        //、简化模式
        list.add(new ImplicitTokenGranter(
                endpoints.getTokenServices(),
                endpoints.getClientDetailsService(),
                endpoints.getOAuth2RequestFactory()));

        //客户端模式
        list.add(new ClientCredentialsTokenGranter(
                endpoints.getTokenServices(),
                endpoints.getClientDetailsService(),
                endpoints.getOAuth2RequestFactory()));

        //  以上为oauth自带的认证，需再次加一遍

        // 微信小程序验证模式（自定义）、
        list.add(new WechatTokenGranter(
                authenticationManager,
                endpoints.getTokenServices(),
                endpoints.getClientDetailsService(),
                endpoints.getOAuth2RequestFactory()));

        return new CompositeTokenGranter(list);
    }





    //  过期时间
    @Bean
    @Primary
    public DefaultTokenServices defaultTokenServices(){
        DefaultTokenServices services=new DefaultTokenServices();
        services.setSupportRefreshToken(true);
        services.setAccessTokenValiditySeconds(60*60*24);//设置20秒过期
        services.setRefreshTokenValiditySeconds(60*60*24);//设置刷新token的过期时间
        services.setTokenStore(tokenStore);
        services.setTokenEnhancer(jwtAccessTokenConverter);
        return services;
    }



}
