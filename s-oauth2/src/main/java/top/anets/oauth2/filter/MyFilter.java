package top.anets.oauth2.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Component
@Order(Integer.MIN_VALUE)
public class MyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        System.out.println("filter:接收到请求："+request.getRequestURL());
        //在这前后写一些过滤逻辑
        filterChain.doFilter(servletRequest,servletResponse);

    }
}
