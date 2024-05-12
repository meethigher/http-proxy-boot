package top.meethigher.proxy.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@ConditionalOnProperty(name = "allowCrossOrigin", havingValue = "true")
public class CrossOriginConfig {

    @Bean
    public FilterRegistrationBean cross() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        //注入过滤器
        registrationBean.setFilter((servletRequest, servletResponse, filterChain) -> {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            //响应头设置
            httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
            //响应类型
            httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
            //允许跨越发送cookie
            httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
            if (httpServletRequest.getMethod().equalsIgnoreCase("options")) {
                //通过响应头告诉预检请求，我是支持跨域的。并且返回状态码是200
                httpServletResponse.setStatus(200);
                return;
            }

            filterChain.doFilter(servletRequest, servletResponse);
        });
        //过滤器名称
        registrationBean.setName("CrossOrigin");
        //拦截规则
        registrationBean.addUrlPatterns("/*");
        //过滤器顺序
        registrationBean.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);

        return registrationBean;
    }
}
