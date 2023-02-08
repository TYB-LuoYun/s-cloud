package top.anets.oauth2.service;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.anets.exception.ServiceException;
import top.anets.oauth2.domain.SysUserDto;
import top.anets.oauth2.feign.IFeignSystem;

import java.util.List;
import java.util.Set;

@Slf4j
@Service // 不一定不要少了
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IFeignSystem iFeignSystem;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserDto userDto = null;
//        try{
//            上层的不用管，上层捕捉了，主要是这层
             userDto = iFeignSystem.loadUserByUsername(username);
//        }catch (Exception e){
////            看这里
//            e.printStackTrace();
//            throw  new ServiceException(e.getMessage());
//        }
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(userDto, sysUser);
        if(sysUser!=null&&sysUser.getRoles()!=null){
            List<String> authorities = userDto.getRoles();
            Set<MyGrantedAuthority> grantedAuthoritys = Sets.newHashSet();
            authorities.forEach(item->{
                grantedAuthoritys.add( new MyGrantedAuthority( item));
            });
            sysUser.setAuthorities(grantedAuthoritys);
        }
        return sysUser;
    }
}
