package top.meethigher.proxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.meethigher.proxy.model.ServletInfo;
import top.meethigher.proxy.utils.ProxyServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态ProxyServlet注册器
 *
 * @author chenchuancheng
 * @since 2024/07/14 14:17
 */
@Component
public class DynamicProxyServletRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private final static Logger log = LoggerFactory.getLogger(DynamicProxyServletRegistrar.class);


    private List<ServletInfo> servletInfos = new ArrayList<>();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (ServletInfo servletInfo : servletInfos) {
            addServlet(registry, servletInfo);
        }
    }

    /**
     * 注册ProxyServlet
     */
    private void addServlet(BeanDefinitionRegistry registry, ServletInfo servletInfo) {
        try {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ServletRegistrationBean.class);
            ProxyServlet proxyServlet = new ProxyServlet(ProxyServlet.okHttpClient(), servletInfo.getTargetUrl(),
                    servletInfo.getCorsControl().isEnable(), servletInfo.getCorsControl().isAllowCORS(),
                    servletInfo.getLog().isEnable(), servletInfo.getLog().getLogFormat(),
                    servletInfo.isxForwardedFor(), servletInfo.isPreserveHost(), servletInfo.isPreserveCookies());
            beanDefinitionBuilder.addConstructorArgValue(proxyServlet);
            beanDefinitionBuilder.addConstructorArgValue(servletInfo.getServletUrl());
            beanDefinitionBuilder.addPropertyValue("name", servletInfo.getName());
            registry.registerBeanDefinition(servletInfo.getName() + "ServletRegistrationBean", beanDefinitionBuilder.getBeanDefinition());
            log.info("all requests to {} will be proxied to {}", servletInfo.getServletUrl(), servletInfo.getTargetUrl());
            log.info("the configuration parameters are as follows {} {}", System.lineSeparator(), new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(servletInfo.toMap()));
        } catch (Exception e) {
            log.error("addServlet error", e);
        }
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        /**
         * 通过 Environment 将 yaml 配置文件转换成实体类
         */
        int index = 0;
        while (true) {
            String prefix = "proxy.servlets[" + index + "]";
            String name = environment.getProperty(prefix + ".name");
            if (name == null) {
                break;
            }
            ServletInfo servletInfo = new ServletInfo();
            servletInfo.setName(name);
            servletInfo.setServletUrl(environment.getProperty(prefix + ".servletUrl"));
            servletInfo.setTargetUrl(environment.getProperty(prefix + ".targetUrl"));
            servletInfo.setPreserveHost(environment.getProperty(prefix + ".preserveHost", Boolean.class, Boolean.FALSE));
            servletInfo.setPreserveCookies(environment.getProperty(prefix + ".preserveCookies", Boolean.class, Boolean.TRUE));
            servletInfo.setxForwardedFor(environment.getProperty(prefix + ".xForwardedFor", Boolean.class, Boolean.FALSE));

            ServletInfo.LOG log = new ServletInfo.LOG();
            log.setEnable(Boolean.parseBoolean(environment.getProperty(prefix + ".log.enable", "true")));
            log.setLogFormat(environment.getProperty(prefix + ".log.logFormat", "{remoteAddr} {method} uri: {source} --> {target} consumed: {consumedMills} ms"));
            servletInfo.setLog(log);

            ServletInfo.CORSControl corsControl = new ServletInfo.CORSControl();
            corsControl.setEnable(Boolean.parseBoolean(environment.getProperty(prefix + ".corsControl.enable")));
            corsControl.setAllowCORS(Boolean.parseBoolean(environment.getProperty(prefix + ".corsControl.allowCORS")));
            servletInfo.setCorsControl(corsControl);

            servletInfos.add(servletInfo);
            index++;
        }
    }

    public List<ServletInfo> getServletInfos() {
        return servletInfos;
    }
}
