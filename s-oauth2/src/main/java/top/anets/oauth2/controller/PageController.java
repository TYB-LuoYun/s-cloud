package top.anets.oauth2.controller;

/**
 * @author ftm
 * @date 2023/2/8 0008 15:56
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
/**
 * @author lvhaibao
 * @description 处理登录和授权的控制器
 * @date 2018/12/26 0026 17:31
 */
@Slf4j
@Controller
public class  PageController {

    /**
     * thymeleaf指定登录页面
     *
     * @param model
     * @return
     */
    @GetMapping("/loginPage")
    public String index(Model model) {
        return "/login";
    }
}