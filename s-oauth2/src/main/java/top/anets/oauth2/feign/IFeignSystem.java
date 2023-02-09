package top.anets.oauth2.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import top.anets.oauth2.domain.SysUserDto;
import top.anets.oauth2.feign.fallback.FeignSystemFallback;

/**
 * @author ftm
 * @date 2023/2/6 0006 11:16
 * contextId: 相当于一个标识，避免注册到spring容器后，bean名称重复
 * fallbackFactory: 服务触发降级后执行哪个类的方法
 */
@FeignClient(value="system-server", contextId = "webflux")
//@FeignClient(value="system-server", contextId = "webflux" ,fallbackFactory = FeignSystemFallback.class)
public interface IFeignSystem {
    @RequestMapping("/feign/user/loadUserByUsername")
    SysUserDto loadUserByUsername(@RequestParam("username") String username);
}