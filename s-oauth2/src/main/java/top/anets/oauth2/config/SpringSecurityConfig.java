package top.anets.oauth2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import top.anets.oauth2.module.wechat.WechatAuthenticationProvider;
import top.anets.oauth2.handle.CustomAuthenticationFailureHandler;
import top.anets.oauth2.handle.CustomAuthenticationSuccessHandler;
import top.anets.oauth2.handle.CustomLogoutSuccessHandler;

/**
 * 安全配置类
 */
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WechatAuthenticationProvider wechatAuthenticationProvider;



    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 指定使用自定义查询用户信息来完成身份认证
        auth.userDetailsService(userDetailsService);


        // 配置具体的认证类------这是是自定义授权类型比如微信才配置的
//        auth.authenticationProvider(wechatAuthenticationProvider)
//                .userDetailsService(userDetailsService)
//                .passwordEncoder(passwordEncoder);
    }

    @Bean // 使用 password模块时需要此bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }


    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 关闭csrf攻击
//        http.csrf().disable();
//        登录功能配置=============================================S



        http.formLogin()
                // 成功处理器
                .successHandler(customAuthenticationSuccessHandler())
                .failureHandler(customAuthenticationFailureHandler())
                .and()
                .logout()
                .logoutSuccessHandler(customLogoutSuccessHandler())
                .and()
                .csrf().disable();
//        登录功能配置=============================================E


//        允许获取公钥接口的访问；
        http.authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .antMatchers("/rsa/publicKey").permitAll()
//               健康检查
                .antMatchers("/assets/**").permitAll()
                .antMatchers(  "/instances/**").permitAll()
                .antMatchers(  "/actuator/**").permitAll()
                .antMatchers("/manual/oauth/token").permitAll()
                .antMatchers("/oauth/logout").permitAll()
                .antMatchers("/oauth/token").permitAll()
                .antMatchers("/oauth/user").permitAll()
                .anyRequest().authenticated();


////        模拟
//        http
//                // 必须配置，不然OAuth2的http配置不生效----不明觉厉
//                .requestMatchers()
//                .antMatchers("/login", "/authorize", "/oauth/authorize")
//                .and()
//                .authorizeRequests()
//                // 自定义页面或处理url是，如果不配置全局允许，浏览器会提示服务器将页面转发多次
//                .antMatchers("/login", "/authorize","/custom/authorize","/auth/custom/authorize")
//                .permitAll()
//                .anyRequest()
//                .authenticated();
//
//        // 表单登录
//        http.formLogin()
////                .failureHandler(failureLoginHandler)
//                .successHandler(customAuthenticationSuccessHandler())
//                // 页面
//                .loginPage("http://localhost:10001/#/oauth/login") //登陆界面
//                .loginProcessingUrl("/auth/custom/authorize");//登陆访问路径：提交表单之后跳转的地址,可以看作一个中转站，这个步骤就是验证user的一个过程
////                .defaultSuccessUrl("/test/index",true).permitAll() ;   //登陆成功之后跳转的路径
//
//        http.httpBasic().disable();
    }





}
