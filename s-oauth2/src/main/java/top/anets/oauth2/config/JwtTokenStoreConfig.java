package top.anets.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

@Configuration
public class JwtTokenStoreConfig {


    @Resource // 不要少了
    private RedisTemplate redisTemplate;

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter =
                new JwtAccessTokenConverter();
        // 采用非对称加密文件
//        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(
//                new ClassPathResource("oauth2.jks"), "oauth2".toCharArray());
//        converter.setKeyPair( factory.getKeyPair("oauth2") );
        converter.setKeyPair( keyPair() );
        return converter;
    }



    @Bean
    public KeyPair keyPair() {
        // 从classpath下的证书中获取秘钥对
        String password = "oauth2";
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("oauth2.jks"), password.toCharArray());
//        return keyStoreKeyFactory.getKeyPair("jwt", password.toCharArray());
        return keyStoreKeyFactory.getKeyPair("oauth2", password.toCharArray());
    }


    /**
     * 主要作用token的增删改查
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        // Jwt管理令牌========================
//        return new JwtTokenStore(jwtAccessTokenConverter());


        // 采用jwt管理信息-redis管理
        return new JwtTokenStore( jwtAccessTokenConverter() ) {
            // redis存储jwt令牌
            @Override
            public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
                // 将jwt中的唯一标识 jti 作为redis中的 key 值 ，value值是 accessToken 访问令牌
                if(token.getAdditionalInformation().containsKey("jti")) {
                    String jti = token.getAdditionalInformation().get("jti").toString();
                    // 存储到redis中 (key, value, 有效时间，时间单位)
                    redisTemplate.opsForValue()
                            .set(jti, token.getValue(), token.getExpiresIn(), TimeUnit.SECONDS);
                }
                super.storeAccessToken(token, authentication);
            }

            // 删除redis中的jwt令牌
            @Override
            public void removeAccessToken(OAuth2AccessToken token) {
                if(token.getAdditionalInformation().containsKey("jti")) {
                    String jti = token.getAdditionalInformation().get("jti").toString();
                    // 将redis中对应jti的记录删除
                    redisTemplate.delete(jti);
                }
                super.removeAccessToken(token);
            }
        };
    }


}
