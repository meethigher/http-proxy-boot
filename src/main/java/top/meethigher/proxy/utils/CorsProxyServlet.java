package top.meethigher.proxy.utils;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpHeaders.*;

/**
 * 该类用于在ProxyServlet的基础上，自行管理跨域相关的请求头，是否允许跨域，均由自己管理
 * 如果想要自行管理全局代理服务的跨域能力，而不受被代理节点的影响，则使用CorsProxyServlet
 * 如果想要让被代理节点自行决定是否允许跨域，则使用ProxyServlet
 *
 * @author chenchuancheng
 * @since 2024/06/29 10:58
 */
public class CorsProxyServlet extends ProxyServlet {


    private final boolean corsControl;

    /**
     * true表示允许跨域
     */
    private final boolean allowCORS;

    /**
     * Configure the agent’s log format. The options are remoteAddr、remotePort、userAgent、method、source、target
     */
    private final String logFormat;


    /**
     * 跨域相关的响应头
     */
    private final List<String> allowCORSHeaders = Arrays.asList(
            "access-control-allow-origin",//指定哪些域可以访问资源。可以是特定域名，也可以是通配符 *，表示允许所有域访问。
            "access-control-allow-methods",//指定允许的HTTP方法，如 GET、POST、PUT、DELETE 等。
            "access-control-allow-headers",//指定允许的请求头。
            "access-control-allow-credentials",//指定是否允许发送凭据（如Cookies）。值为 true 表示允许，且不能使用通配符 *。
            "access-control-expose-headers",//指定哪些响应头可以被浏览器访问。
            "access-control-max-age",//指定预检请求的结果可以被缓存的时间（以秒为单位）。
            "access-control-request-method",//在预检请求中使用，指示实际请求将使用的方法。
            "access-control-request-headers"//在预检请求中使用，指示实际请求将使用的自定义头。
    );

    public CorsProxyServlet(boolean crosControl, boolean allowCORS, String logFormat) {
        this.corsControl = crosControl;
        this.allowCORS = allowCORS;
        this.logFormat = logFormat;
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        if (servletRequest.getMethod().equalsIgnoreCase("options") && corsControl && allowCORS) {
            servletResponse.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, servletRequest.getHeader("origin"));
            servletResponse.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "*");
            servletResponse.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, "*");
            servletResponse.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            servletResponse.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            servletResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        super.service(servletRequest, servletResponse);
    }

    @Override
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        corsControl(proxyResponse, servletRequest, servletResponse);
        for (Header header : proxyResponse.getAllHeaders()) {
            copyResponseHeader(servletRequest, servletResponse, header);
        }
    }

    /**
     * 跨域控制
     *
     * @param response HttpResponse
     */
    protected void corsControl(HttpResponse response, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        if (corsControl) {
            /**
             * 1. 清空所有与跨域相关的响应头
             * 2. 如果允许跨域，则添加跨域允许响应头
             */
            List<String> needRemoveHeaderList = new ArrayList<>();
            for (Header header : response.getAllHeaders()) {
                String originName = header.getName();
                String name = originName.toLowerCase(Locale.ROOT);
                if (allowCORSHeaders.contains(name)) {
                    needRemoveHeaderList.add(name);
                }
            }
            for (String s : needRemoveHeaderList) {
                response.removeHeaders(s);
            }
            if (allowCORS) {
                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, servletRequest.getHeader("origin"));
                response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "*");
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, "*");
                response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, "*");
//                response.setHeader(ACCESS_CONTROL_MAX_AGE, "");
//                response.setHeader(ACCESS_CONTROL_REQUEST_METHOD, "");
//                response.setHeader(ACCESS_CONTROL_REQUEST_HEADERS, "");
            }
        }
    }


    @Override
    protected void copyResponseHeader(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Header header) {
        String headerName = header.getName();
        /**
         * End-to-End and Hop-by-Hop Headers 区别
         * End-to-End 头字段是指那些在客户端和最终服务器之间传递的头字段。这些头字段应该在整个请求/响应链条上保留和传递，包括所有的中间代理服务器。
         * Hop-by-Hop 头字段是指那些在每一跳（每个节点之间）传递时会被处理的头字段，而不会在客户端和最终服务器之间完整传递。代理服务器和其他中间设备在处理这些头字段后，不会将它们转发给下一个节点。
         */
        if (hopByHopHeaders.containsHeader(headerName)) {
            return;
        }
        String headerValue = header.getValue();
        if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE) ||
                headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
            copyProxyCookie(servletRequest, servletResponse, headerValue);
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            // 重写Location响应头
            servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
        } else {
            servletResponse.addHeader(headerName, headerValue);
        }
    }

    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                                     HttpRequest proxyRequest) throws IOException {
        long start = System.currentTimeMillis();
        try {
            return getProxyClient().execute(getTargetHost(servletRequest), proxyRequest);
        } finally {
            long end = System.currentTimeMillis();
            if (doLog) {
                String msg = this.logFormat
                        .replace("{consumedMills}", String.valueOf(end - start))
                        .replace("{remoteAddr}", servletRequest.getRemoteAddr())
                        .replace("{remotePort}", String.valueOf(servletRequest.getRemotePort()))
                        .replace("{userAgent}", servletRequest.getHeader("User-Agent"))
                        .replace("{method}", servletRequest.getMethod())
                        .replace("{source}", servletRequest.getRequestURI())
                        .replace("{target}", proxyRequest.getRequestLine().getUri());
                log(msg);
            }
        }
    }
}
