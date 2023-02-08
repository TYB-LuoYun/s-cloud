package top.anets.oauth2.module.wechat;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author ftm
 * @date 2023/1/20 0020 12:48
 * 微信认证具体实现类
 */
@Slf4j
@Component
public class WechatAuthenticationProvider implements AuthenticationProvider {

//    @Autowired
//    private RestTemplate restTemplate;

//    @Autowired
//    private WechatConfig wechatConfig;
//
//    @Autowired
//    private IWechatUserService webchatUserService;

//    @Autowired
//    private UserManager userManager;

    @Override
    @SneakyThrows
    public Authentication authenticate(Authentication authentication) {
//        HttpServletRequest httpServletRequest = DciUtil.getHttpServletRequest();
//
//        WechatAuthenticationToken wechatAuthenticationToken = (WechatAuthenticationToken) authentication;
//
//        String code = wechatAuthenticationToken.getPrincipal().toString();
//        System.out.println(code);
//
//        // 调用微信请求接口，获取openId的值
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
//        Map<String, String> requestMap = new HashMap<>();
//        requestMap.put("appid", wechatConfig.getAppid());
//        requestMap.put("secret", wechatConfig.getSecret());
//        requestMap.put("code", code);
//
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class,requestMap);
//        JSONObject jsonObject= JSONObject.parseObject(responseEntity.getBody());
//        System.out.println(jsonObject);
//        String openId=jsonObject.getString("openid");
////        String session_key=jsonObject.getString("session_key");
//
//        // 项目码
//        String projectCode = httpServletRequest.getParameter("projectCode");
//
//        if(StringUtils.isEmpty(openId)) {
//            throw new AuthenticationServiceException("Wechat Invalid Code");
//        }
//
//        WechatUser wechatUser = new WechatUser();
//        wechatUser.setProjectCode(projectCode);
//        wechatUser.setOpenid(openId);
//        WechatUser weUser = webchatUserService.getWechatUserByOpenId(wechatUser);
//
//        // 如果是第一次登录，进行信息添加
//        if(weUser == null) {
//            wechatUser.setCreateTime(new Date());
//            webchatUserService.save(wechatUser);
//            httpServletRequest.setAttribute("wxid", wechatUser.getWxid());
//            throw new AuthenticationServiceException("Wechat No Bind Phone");
//        } else {
//            String phone = weUser.getPhone();
//            if(StringUtils.isEmpty(phone)) {
//                httpServletRequest.setAttribute("wxid", weUser.getWxid());
//                throw new AuthenticationServiceException("Wechat No Bind Phone");
//            }
//        }
//
//        // 预留权限控制，防止以后需要
//        String permissions = "";
//
//        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.NO_AUTHORITIES;
//        if (StringUtils.isNotBlank(permissions)) {
//            grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(permissions);
//        }
//
//        DciAuthUser authUser = new DciAuthUser(weUser.getPhone(), "", grantedAuthorities);
//        BeanUtils.copyProperties(weUser, authUser);
//        authUser.setCurrProjectCode("2");
//
//        WechatAuthenticationToken authenticationToken = new WechatAuthenticationToken(authUser,
//                authUser.getAuthorities());
//        LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
//        linkedHashMap.put("principal", authenticationToken.getPrincipal());
//        authenticationToken.setDetails(linkedHashMap);
//        return authenticationToken;
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WechatAuthenticationToken.class.isAssignableFrom(authentication);
    }
}