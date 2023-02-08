package top.anets.top.anets.gateway.authorization;

import cn.hutool.core.convert.Convert;
import com.nimbusds.jose.JWSObject;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import reactor.core.publisher.Mono;
import top.anets.top.anets.gateway.config.IgnoreUrlsConfig;
import top.anets.utils.common.AuthConstant;
import top.anets.redis.RedisConstant;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1.0首先会经过这个AuthorizationManager，如果配置了白名单，则会跳过它
 * 这个鉴权管理器配置 主要用来鉴权
 * 会自动解析token，然后取出用户的角色 跟 配置的
 * 鉴权管理器，用于判断是否有资源的访问权限
 * Created by macro on 2020/6/19.
 */
@Component
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    @Autowired
    private RedisTemplate  redisTemplate;
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @SneakyThrows
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        ServerHttpRequest request = authorizationContext.getExchange().getRequest();
        URI uri = request.getURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        //白名单路径直接放行，这个可以在ResourceServerConfig中配置【其实这段代码配置了可不要】
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, uri.getPath())) {
                return Mono.just(new AuthorizationDecision(true));
            }
        }

        String token = request.getHeaders().getFirst(AuthConstant.JWT_TOKEN_HEADER);
        if(StringUtils.isBlank(token)){
//           认证失败
            return Mono.just(new AuthorizationDecision(false));
        }
        String username ="";
        try{
            String realToken = token.replace(AuthConstant.JWT_TOKEN_PREFIX, "");
            JWSObject jwsObject = JWSObject.parse(realToken);
            username = (String) jwsObject.getPayload().toJSONObject().get("user_name");
        }catch (Exception e){
            e.printStackTrace();
        }

//        //对应跨域的预检请求直接放行
//        if(request.getMethod()== HttpMethod.OPTIONS){
//            return Mono.just(new AuthorizationDecision(true));
//        }
//        //不同用户体系登录不允许互相访问
//        try {
//            String token = request.getHeaders().getFirst(AuthConstant.JWT_TOKEN_HEADER);
//            if(StrUtil.isEmpty(token)){
//                return Mono.just(new AuthorizationDecision(false));
//            }
//            String realToken = token.replace(AuthConstant.JWT_TOKEN_PREFIX, "");
//            JWSObject jwsObject = JWSObject.parse(realToken);
//            String userStr = jwsObject.getPayload().toString();
//            UserDto userDto = JSONUtil.toBean(userStr, UserDto.class);
//            if (AuthConstant.ADMIN_CLIENT_ID.equals(userDto.getClientId()) && !pathMatcher.match(AuthConstant.ADMIN_URL_PATTERN, uri.getPath())) {
//                return Mono.just(new AuthorizationDecision(false));
//            }
//            if (AuthConstant.PORTAL_CLIENT_ID.equals(userDto.getClientId()) && pathMatcher.match(AuthConstant.ADMIN_URL_PATTERN, uri.getPath())) {
//                return Mono.just(new AuthorizationDecision(false));
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return Mono.just(new AuthorizationDecision(false));
//        }
//        //非管理端路径直接放行
//        if (!pathMatcher.match(AuthConstant.ADMIN_URL_PATTERN, uri.getPath())) {
//            return Mono.just(new AuthorizationDecision(true));
//        }
//        //管理端路径需校验权限
//        Map<Object, Object> resourceRolesMap = redisTemplate.opsForHash().entries(AuthConstant.RESOURCE_ROLES_MAP_KEY);
//        Iterator<Object> iterator = resourceRolesMap.keySet().iterator();
//        List<String> authorities = new ArrayList<>();
//        while (iterator.hasNext()) {
//            String pattern = (String) iterator.next();
//            if (pathMatcher.match(pattern, uri.getPath())) {
//                authorities.addAll(Convert.toList(String.class, resourceRolesMap.get(pattern)));
//            }
//        }
//        authorities = authorities.stream().map(i -> i = AuthConstant.AUTHORITY_PREFIX + i).collect(Collectors.toList());
//        //认证通过且角色匹配的用户可访问当前路径
//        return mono
//                .filter(Authentication::isAuthenticated)
//                .flatMapIterable(Authentication::getAuthorities)
//                .map(GrantedAuthority::getAuthority)
//                .any(authorities::contains)
//                .map(AuthorizationDecision::new)
//                .defaultIfEmpty(new AuthorizationDecision(false));

        //从Redis中获取当前路径可访问角色列表
//        URI uri = authorizationContext.getExchange().getRequest().getURI();
//       根据路径查看访问该路径所需要的角色有哪些,比如['ADMIN']
        Object obj = redisTemplate.opsForHash().get(RedisConstant.RESOURCE_ROLES_MAP, uri.getPath());
        List<String> authorities = Convert.toList(String.class, obj);
//       给角色加前缀，比如['ROLE_ADMIN']
        authorities = authorities.stream().map(i -> i = AuthConstant.AUTHORITY_PREFIX + i).collect(Collectors.toList());

        if("admin".equals(username)){
//            Mono<AuthorizationDecision> authorizationDecisionMono = mono.filter(Authentication::isAuthenticated)
//                    .flatMapIterable(Authentication::getAuthorities)
//                    .map(GrantedAuthority::getAuthority)
//                    .any(role -> true)//拥有所有权限
//                    .map(AuthorizationDecision::new)
//                    .defaultIfEmpty(new AuthorizationDecision(false));
            Mono<AuthorizationDecision> authorizationDecisionMono = Mono.just(new AuthorizationDecision(true));
            return authorizationDecisionMono;
        }else{
            //认证通过且角色匹配的用户可访问当前路径
         return    mono.filter(Authentication::isAuthenticated)
                    .flatMapIterable(Authentication::getAuthorities)
                    .map(GrantedAuthority::getAuthority)
                    .any(authorities::contains)
//                .any(role -> true),拥有所有权限
                    .map(AuthorizationDecision::new)
                    .defaultIfEmpty(new AuthorizationDecision(false));
        }
    }

}
