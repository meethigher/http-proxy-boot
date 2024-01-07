package top.meethigher.proxy.config;


import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

/**
 * SpringBoot使用ProxyServlet实现HTTP反向代理-阿里云开发者社区: https://developer.aliyun.com/article/1373393
 * mitre/HTTP-Proxy-Servlet: Smiley's HTTP Proxy implemented as a Java servlet: https://github.com/mitre/HTTP-Proxy-Servlet
 *
 * @author chenchuancheng
 * @since 2024/01/09 18:26
 */
@Configuration
public class ProxyServletConfiguration {
    /**
     * 读取配置文件中路由设置
     */
    @Value("${proxy.servlet_url}")
    private String servlet_url;
    /**
     * 读取配置中代理目标地址
     */
    @Value("${proxy.target_url}")
    private String target_url;

    /**
     * 创建新的ProxyServlet
     */
    @Bean
    public Servlet proxyServlet() {
        return new ProxyServlet();
    }

    @Bean
    public ServletRegistrationBean proxyServletRegistration(@Qualifier("proxyServlet") Servlet proxyServlet) {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(proxyServlet, servlet_url);
        //设置网址以及参数
        Map<String, String> params = new HashMap<>();
        params.put("targetUri", target_url);
        params.put("log", "true");
        registrationBean.setInitParameters(params);
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