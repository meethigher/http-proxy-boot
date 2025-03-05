package top.meethigher.proxy.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.meethigher.proxy.http.ProxyRoute;
import top.meethigher.proxy.http.ReverseHttpProxy;
import top.meethigher.proxy.model.ServletInfo;
import top.meethigher.proxy.utils.VertxUtils;

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


    protected List<ServletInfo> servletInfos = new ArrayList<>();

    protected Integer port;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // order越小，优先级越高。配置文件中的效果就是越靠后优先级高。
        for (int i = servletInfos.size() - 1, j = 0; i >= 0; i--, j++) {
            addRoute(registry, servletInfos.get(i), j);
        }
        VertxUtils.httpProxy().port(port).start();
    }

    /**
     * 注册ProxyServlet
     */
    private void addRoute(BeanDefinitionRegistry registry, ServletInfo servletInfo, Integer order) {
        try {
            ReverseHttpProxy reverseHttpProxy = VertxUtils.httpProxy();
            ProxyRoute proxyRoute = new ProxyRoute()
                    .setName(servletInfo.getName())
                    .setSourceUrl(servletInfo.getServletUrl())
                    .setTargetUrl(servletInfo.getTargetUrl())
                    .setFollowRedirects(false)
                    .setForwardIp(servletInfo.isxForwardedFor())
                    .setHttpKeepAlive(servletInfo.isHttpKeepAlive())
                    .setPreserveCookies(servletInfo.isPreserveCookies())
                    .setPreserveHost(servletInfo.isPreserveHost());
            proxyRoute.getLog().setEnable(servletInfo.getLog().isEnable()).setLogFormat(servletInfo.getLog().getLogFormat());
            proxyRoute.getCorsControl().setEnable(servletInfo.getCorsControl().isEnable()).setAllowCors(servletInfo.getCorsControl().isAllowCORS());
            reverseHttpProxy.addRoute(proxyRoute, order);
        } catch (Exception e) {
            log.error("addRoute error", e);
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
        port = environment.getProperty("server.port", Integer.class, 8080);

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
            servletInfo.setHttpKeepAlive(environment.getProperty(prefix + ".httpKeepAlive", Boolean.class, Boolean.FALSE));

            ServletInfo.LOG log = new ServletInfo.LOG();
            log.setEnable(Boolean.parseBoolean(environment.getProperty(prefix + ".log.enable", "true")));
            log.setLogFormat(environment.getProperty(prefix + ".log.logFormat", "{name} -- {method} -- {userAgent} -- {remoteAddr}:{remotePort} -- {source} --> {target} -- {statusCode} consumed {consumedMills} ms"));
            servletInfo.setLog(log);

            ServletInfo.CORSControl corsControl = new ServletInfo.CORSControl();
            corsControl.setEnable(Boolean.parseBoolean(environment.getProperty(prefix + ".corsControl.enable")));
            corsControl.setAllowCORS(Boolean.parseBoolean(environment.getProperty(prefix + ".corsControl.allowCORS")));
            servletInfo.setCorsControl(corsControl);

            servletInfos.add(servletInfo);
            index++;
        }
    }
}
