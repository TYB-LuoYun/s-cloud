package top.anets.oauth2.feign.fallback;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.anets.exception.ServiceException;
import top.anets.oauth2.domain.SysUserDto;
import top.anets.oauth2.feign.IFeignSystem;

@Slf4j
@Component
//自定义的
public class FeignSystemFallback implements FallbackFactory<IFeignSystem> {


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
//
                return  dto;
            }
        };
    }
}