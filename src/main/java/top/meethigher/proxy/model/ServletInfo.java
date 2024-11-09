package top.meethigher.proxy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务信息
 *
 * @author chenchuancheng
 * @since 2024/07/17 23:21
 */
public class ServletInfo {

    private String name;

    /**
     * /* represents all interfaces below proxy /, no distinction between /* and /**
     */
    private String servletUrl;

    private String targetUrl;

    private boolean xForwardedFor;

    private boolean preserveCookies;

    private boolean preserveHost;

    private LOG log;

    private CORSControl corsControl;


    public String getServletUrl() {
        return servletUrl;
    }

    public void setServletUrl(String servletUrl) {
        this.servletUrl = servletUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public LOG getLog() {
        return log;
    }

    public void setLog(LOG log) {
        this.log = log;
    }

    public CORSControl getCorsControl() {
        return corsControl;
    }

    public void setCorsControl(CORSControl corsControl) {
        this.corsControl = corsControl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isxForwardedFor() {
        return xForwardedFor;
    }

    public void setxForwardedFor(boolean xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    public boolean isPreserveCookies() {
        return preserveCookies;
    }

    public void setPreserveCookies(boolean preserveCookies) {
        this.preserveCookies = preserveCookies;
    }

    public boolean isPreserveHost() {
        return preserveHost;
    }

    public void setPreserveHost(boolean preserveHost) {
        this.preserveHost = preserveHost;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("proxy.name", getName());
        map.put("proxy.servletUrl", getServletUrl());
        map.put("proxy.targetUrl", getTargetUrl());
        map.put("proxy.xForwardedFor", String.valueOf(isxForwardedFor()));
        map.put("proxy.preserveCookies", String.valueOf(isPreserveCookies()));
        map.put("proxy.preserveHost", String.valueOf(isPreserveHost()));
        map.put("proxy.log.enable", String.valueOf(getLog().isEnable()));
        map.put("proxy.log.logFormat", String.valueOf(getLog().getLogFormat()));
        map.put("proxy.corsControl.enable", String.valueOf(getCorsControl().isEnable()));
        map.put("proxy.corsControl.allowCORS", String.valueOf(getCorsControl().isAllowCORS()));
        return map;
    }


    /**
     * 跨域控制
     *
     * @author chenchuancheng
     * @since 2024/06/29 11:59
     */
    public static class CORSControl {
        /**
         * true表示所有的代理请求的跨域都由自己管理
         * false表示所有的代理请求的跨域由被代理方控制
         */
        private boolean enable;

        /**
         * 当enable=true时，该参数才会生效
         * 如果该参数为true表示所有经过代理的服务都允许跨域
         * 如果该参数为true表示所有经过代理的服务均不允许跨域
         */
        private boolean allowCORS;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean isAllowCORS() {
            return allowCORS;
        }

        public void setAllowCORS(boolean allowCORS) {
            this.allowCORS = allowCORS;
        }
    }

    public static class LOG {
        private boolean enable;
        /**
         * Configure the agent’s log format. The options are remoteAddr、remotePort、userAgent、method、source、target
         */
        private String logFormat;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getLogFormat() {
            return logFormat;
        }

        public void setLogFormat(String logFormat) {
            this.logFormat = logFormat;
        }
    }
}