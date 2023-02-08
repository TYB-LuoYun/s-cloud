package top.anets.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author ftm
 * @date 2023/2/7 0007 11:49
 */
@Slf4j
@Configuration
public class MyFeignInterceptor implements RequestInterceptor {
    private static final String FEIGN_REQUEST_ID = "FEIGN_REQUEST_ID";

    @Override
    public void apply(RequestTemplate requestTemplate) {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null) {
            // 获取请求对象
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            if(StringUtils.isNotEmpty(token)) { // Bearer xxx
                // 在使用feign远程调用时，请求头就会带上访问令牌
                requestTemplate.header(HttpHeaders.AUTHORIZATION, token);
            }
        }
        // 微服务之间传递的唯一标识,区分大小写所以通过httpServletRequest获取,如何防止外部访问模拟出特定header 字段，如上图，我们在网关层会擦除该特定header属性
        String sid = String.valueOf(UUID.randomUUID());
        requestTemplate.header(FEIGN_REQUEST_ID, sid);
        System.out.println("远程request: "+requestTemplate.url()  );
    }
}