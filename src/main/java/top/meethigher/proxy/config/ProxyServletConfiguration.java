package top.meethigher.proxy.config;


import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import top.meethigher.proxy.utils.CorsProxyServlet;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.*;

/**
 * SpringBoot使用ProxyServlet实现HTTP反向代理-阿里云开发者社区: https://developer.aliyun.com/article/1373393
 * mitre/HTTP-Proxy-Servlet: Smiley's HTTP Proxy implemented as a Java servlet: https://github.com/mitre/HTTP-Proxy-Servlet
 *
 * @author chenchuancheng
 * @since 2024/01/09 18:26
 */
@Configuration
public class ProxyServletConfiguration {


    @Resource
    private ProxyProperties proxyProperties;


    /**
     * 创建新的ProxyServlet
     */
    @Bean
    public Servlet proxyServlet() {
        return new CorsProxyServlet(proxyProperties.getCorsControl().isEnable(),
                proxyProperties.getCorsControl().isAllowCORS(), proxyProperties.getLog().getLogFormat());
    }

    @Bean
    public ServletRegistrationBean<ProxyServlet> proxyServletRegistration(@Qualifier("proxyServlet") Servlet proxyServlet) {
        ServletRegistrationBean<ProxyServlet> registrationBean = new ServletRegistrationBean(proxyServlet, proxyProperties.getServletUrl());
        //设置网址以及参数
        Map<String, String> params = new HashMap<>();
        params.put("targetUri", proxyProperties.getTargetUrl());
        params.put("log", String.valueOf(proxyProperties.getLog().isEnable()));
        registrationBean.setInitParameters(params);
        registrationBean.setName(proxyProperties.getName());
        return registrationBean;
    }

    @Bean
    @ConditionalOnProperty(name = {"proxy.corsControl.enable", "proxy.corsControl.allowCORS"}, havingValue = "true")
    public FilterRegistrationBean<Filter> optionsFilterRegistration() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter((servletRequest, servletResponse, filterChain) -> {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            //预检请求无脑放行
            if ("options".equalsIgnoreCase(httpServletRequest.getMethod())) {
                httpServletResponse.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader("origin"));
                httpServletResponse.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "*");
                httpServletResponse.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, "*");
                httpServletResponse.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                httpServletResponse.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);
        });
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

//    /**
//     * fix springboot中使用proxyservlet的 bug.
//     * https://github.com/mitre/HTTP-Proxy-Servlet/issues/83
//     * https://stackoverflow.com/questions/8522568/why-is-httpservletrequest-inputstream-empty
//     *
//     * @param filter
//     * @return 关闭springboot 1.x 自带的 HiddenHttpMethodFilter 防止post提交的form数据流被提前消费
//     */
//    @Bean
//    public FilterRegistrationBean registration(HiddenHttpMethodFilter filter) {
//        FilterRegistrationBean<HiddenHttpMethodFilter> registrationBean = new FilterRegistrationBean<>(filter);
//        registrationBean.setEnabled(false);
//        return registrationBean;
//    }
}