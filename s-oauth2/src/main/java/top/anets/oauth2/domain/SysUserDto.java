package top.anets.oauth2.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author ftm
 * @since 2023-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonIgnoreProperties解决反序列化报错
@ApiModel(value="User对象", description="用户信息表")
public class SysUserDto  {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private String id;


    @ApiModelProperty(value = "用户账号")
    private String userName;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户类型（0系统用户）")
    private String userType;

    @ApiModelProperty(value = "用户邮箱")
    private String email;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "用户性别（0男 1女 2未知）")
    private String sex;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "密码")
    private String password;

    private String salt;

    @ApiModelProperty(value = "帐号状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "删除标志（0代表存在 1代表删除）")
    private String deleteFlag;

    @ApiModelProperty(value = "最后登录IP")
    private String loginIp;


    @ApiModelProperty(value = "创建者")
    private String createBy;


    @ApiModelProperty(value = "更新者")
    private String updateBy;


    @ApiModelProperty(value = "备注")
    private String remark;



//    private Collection<? extends GrantedAuthority> authorities;

    private List<String> roles;


//    public SysUserDto(String username, String password, String salt, Collection<? extends GrantedAuthority> authorities) {
//        this.userName = username;
//        this.password = password;
//        this.salt = salt;
//        this.authorities = authorities;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
//    }
}
