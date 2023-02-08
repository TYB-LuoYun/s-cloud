package top.anets.config.feign;

/**
 * @author ftm
 * @date 2023/2/7 0007 9:59
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import top.anets.base.Result;
import top.anets.exception.ServiceException;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader(Charset.defaultCharset()));
                log.error(body);
                Result exceptionInfo = JSON.parseObject(body, new TypeReference<Result>() {
                });
//              这个异常才不会被feign 捕获
                return new HystrixBadRequestException(exceptionInfo.getMessage());
//                return new ServiceException(exceptionInfo.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return new InternalException(e.getMessage());
        }
        return new InternalException("system error");
    }
}

