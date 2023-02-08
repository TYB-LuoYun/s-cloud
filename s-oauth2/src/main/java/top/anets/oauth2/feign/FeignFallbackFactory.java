package top.anets.oauth2.feign;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import feign.Target;
import feign.hystrix.FallbackFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.stereotype.Component;
import top.anets.exception.ServiceException;
import top.anets.oauth2.domain.SysUserDto;

@Slf4j
@Component
//自定义的
public class FeignFallbackFactory implements FallbackFactory<IFeignSystem> {


    /**
     * Returns an instance of the fallback appropriate for the given cause
     *
     * @param cause corresponds to
     */
    @Override
    public IFeignSystem create(Throwable cause) {
        return new IFeignSystem() {
            @Override
            public SysUserDto loadUserByUsername(String username) {
                System.out.println("服务挂了");
                SysUserDto dto = new SysUserDto();
                dto.setId("11");
                dto.setUserName("aaa");
                dto.setPassword("bbb");
                if(true){
                    throw new ServiceException("服务错误");
                }
                return  dto;
            }
        };
    }
}