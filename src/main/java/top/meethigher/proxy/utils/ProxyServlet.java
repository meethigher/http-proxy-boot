package top.meethigher.proxy.utils;

import okhttp3.*;
import okio.BufferedSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 该内容主要是想着学习一下底层HTTP反向代理的实现
 *
 * @author <a href="https://meethigher.top">chenchuancheng</a>
 * @see <a href="https://github.com/mitre/HTTP-Proxy-Servlet">mitre/HTTP-Proxy-Servlet: Smiley&#39;s HTTP Proxy implemented as a Java servlet</a>
 * @see <a href="https://www.mnot.net/blog/2011/07/11/what_proxies_must_do">What Proxies Must Do</a>
 * @since 2024/11/09 22:43
 */
public class ProxyServlet extends HttpServlet {
    protected static final Logger log = LoggerFactory.getLogger(ProxyServlet.class);


    protected final OkHttpClient client;
    protected final String targetUrl; // 目标服务器信息
    protected final boolean corsControl; // 跨域控制。当为true时，跨域信息都由自身服务管理
    protected final boolean allowCORS; // 是否允许跨域。当corsControl为true时，该参数方可生效。
    protected final boolean logEnable; // 启用日志
    protected final boolean forwardIp; // 遵循代理规范，将实际调用方的ip和protocol传给目标服务器
    protected final boolean preserveHost; // 保留原host，这个仅对请求头有效。
    protected final boolean preserveCookie; // 保留原cookie。这个对请求头和响应头均有效。
    protected final String logFormat; // 日志格式

    /**
     * 跨域相关的响应头
     */
    protected final List<String> allowCORSHeaders = Arrays.asList(
            "access-control-allow-origin",//指定哪些域可以访问资源。可以是特定域名，也可以是通配符 *，表示允许所有域访问。
            "access-control-allow-methods",//指定允许的HTTP方法，如 GET、POST、PUT、DELETE 等。
            "access-control-allow-headers",//指定允许的请求头。
            "access-control-allow-credentials",//指定是否允许发送凭据（如Cookies）。值为 true 表示允许，且不能使用通配符 *。
            "access-control-expose-headers",//指定哪些响应头可以被浏览器访问。
            "access-control-max-age",//指定预检请求的结果可以被缓存的时间（以秒为单位）。
            "access-control-request-method",//在预检请求中使用，指示实际请求将使用的方法。
            "access-control-request-headers"//在预检请求中使用，指示实际请求将使用的自定义头。
    );

    /**
     * 不应该被复制的逐跳标头
     */
    protected final String[] hopByHopHeaders = new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade"};


    /**
     * 默认的日志格式
     */
    public static final String LOG_FORMAT_DEFAULT = "{method} -- {userAgent} -- {remoteAddr}:{remotePort} -- {source} --> {target} -- {statusCode} consumed {consumedMills} ms";


    protected void doLog(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long startMills) {
        if (logEnable) {
            String queryString = httpServletRequest.getQueryString();
            String logInfo = logFormat.replace("{method}", httpServletRequest.getMethod())
                    .replace("{userAgent}", httpServletRequest.getHeader("User-Agent"))
                    .replace("{remoteAddr}", httpServletRequest.getRemoteAddr())
                    .replace("{remotePort}", String.valueOf(httpServletRequest.getRemotePort()))
                    .replace("{source}", queryString == null ? httpServletRequest.getRequestURL() : httpServletRequest.getRequestURL() + "?" + queryString)
                    .replace("{target}", rewriteUrlFromRequest(httpServletRequest))
                    .replace("{statusCode}", String.valueOf(httpServletResponse.getStatus()))
                    .replace("{consumedMills}", String.valueOf(System.currentTimeMillis() - startMills));
            log.info("{}: {}", getServletName(), logInfo);
        }
    }

    /**
     * 是否包含逐跳标头
     */
    protected boolean containsHopByHopHeader(String name) {
        for (String header : hopByHopHeaders) {
            if (header.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 不考虑contextPath
     * 获取代理请求url，不包含queryparams
     */
    protected String getTargetUrl(HttpServletRequest request) {
        //request.getRequestURI();//包含contextPath的uri
        String uri = request.getPathInfo();//不包含contextPath的uri
        if (uri == null || uri.isEmpty()) {
            return targetUrl;
        } else {
            return targetUrl + uri;
        }
    }

    /**
     * 获取代理请求完整url，包含queryparams
     */
    protected String rewriteUrlFromRequest(HttpServletRequest request) {
        String targetUrl = getTargetUrl(request);
        String queryString = request.getQueryString();
        return queryString == null ? targetUrl : targetUrl + "?" + queryString;
    }

    /**
     * 将重定向的url，重写为代理服务器的地址
     */
    protected String rewriteUrlFromResponse(HttpServletRequest request, String locationUrl) {
        String targetUrl = getTargetUrl(request);
        if (locationUrl != null && locationUrl.startsWith(targetUrl)) {
            StringBuffer curUrl = request.getRequestURL();
            int pos;
            if ((pos = curUrl.indexOf("://")) >= 0) {
                if ((pos = curUrl.indexOf("/", pos + 3)) >= 0) {
                    curUrl.setLength(pos);
                }
            }
            curUrl.append(request.getContextPath());
            curUrl.append(request.getServletPath());
            curUrl.append(locationUrl, targetUrl.length(), locationUrl.length());
            return curUrl.toString();
        }
        return locationUrl;
    }

    public ProxyServlet(OkHttpClient client, String targetUrl, boolean corsControl, boolean allowCORS, boolean logEnable, String logFormat, boolean forwardIp, boolean preserveHost, boolean preserveCookie) {
        this.client = client;
        this.targetUrl = targetUrl;
        this.corsControl = corsControl;
        this.allowCORS = allowCORS;
        this.logEnable = logEnable;
        this.forwardIp = forwardIp;
        this.preserveHost = preserveHost;
        this.preserveCookie = preserveCookie;
        this.logFormat = logFormat;
    }

    public ProxyServlet(OkHttpClient client, String targetUrl) {
        this(client, targetUrl, false, false, true, LOG_FORMAT_DEFAULT, false, false, false);
    }

    /**
     * 根据代理的规定，通过请求头进行真实信息的传递
     * X-Forwarded-For: 传输实际调用者ip
     * X-Forwarded-Proto: 传输实际调用者请求协议
     */
    public void setXForwardedForHeader(HttpServletRequest request, Request.Builder requestBuilder) {
        if (forwardIp) {
            String forHeaderName = "X-Forwarded-For";
            String forHeader = request.getRemoteAddr();
            String existingForHeader = request.getHeader(forHeader);
            if (existingForHeader != null) {
                forHeader = existingForHeader + ", " + forHeader;
            }
            requestBuilder.header(forHeaderName, forHeader);
            String protoHeaderName = "X-Forwarded-Proto";
            String protoHeader = request.getScheme();
            requestBuilder.header(protoHeaderName, protoHeader);
        }
    }


    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            if (httpServletRequest.getMethod().equalsIgnoreCase("options") && corsControl && allowCORS) {
                httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("origin"));
                httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
                httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
                httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "*");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            String method = httpServletRequest.getMethod();
            String proxyRequestUrl = rewriteUrlFromRequest(httpServletRequest);
            Request.Builder requestBuilder = getInitRequestBuilder(httpServletRequest, httpServletResponse);
            requestBuilder.url(proxyRequestUrl);
            if ("get".equalsIgnoreCase(method)) {
                requestBuilder.method(method, null);
            } else {
                // 根据HTTP规定，复制请求体
                RequestBody requestBody;
                if (httpServletRequest.getHeader("Content-Length") != null || httpServletRequest.getHeader("Transfer-Encoding") != null) {
                    try {
                        ServletInputStream inputStream = httpServletRequest.getInputStream();
                        requestBody = new StreamingRequestBody(MediaType.parse(httpServletRequest.getContentType()), inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                        writeGatewayError(httpServletResponse, e.getMessage());
                        return;
                    }
                } else {
                    requestBody = RequestBody.create(null, new byte[0]);
                }
                requestBuilder.method(method, requestBody);
            }

            copyRequestHeaders(httpServletRequest, requestBuilder);
            setXForwardedForHeader(httpServletRequest, requestBuilder);
            try (Response response = client.newCall(requestBuilder.build()).execute()) {
                httpServletResponse.setStatus(response.code());
                copyResponseHeaders(httpServletRequest, httpServletResponse, response);
                if (response.code() == 304) {
                    // http状态码为304时，表示当客户端发起请求时，如果服务器发现请求的资源并没有自上次请求后发生任何更改，就会返回 304 状态码，同时不包含请求资源的实体内容。这意味着客户端可以继续使用缓存中的资源，从而避免不必要的数据传输，减少服务器负载和带宽消耗。
                    httpServletResponse.setIntHeader("Content-Length", 0);
                } else {
                    // 复制响应体
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        ServletOutputStream os = httpServletResponse.getOutputStream();
                        InputStream is = responseBody.byteStream();
                        int len;
                        byte[] buffer = new byte[8192];
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeGatewayError(httpServletResponse, e.getMessage());
                return;
            }
        } finally {
            doLog(httpServletRequest, httpServletResponse, start);
        }
    }

    protected Request.Builder getInitRequestBuilder(HttpServletRequest request, HttpServletResponse response) {
        Request.Builder builder = new Request.Builder();
        // 弱网情况下，使用keep-alive会遇到一些问题。使用短连接会影响性能，该工具的宗旨是 稳定 > 性能
        builder.header("Connection", "close");
        return builder;
    }


    /**
     * 复制响应头
     */
    protected void copyResponseHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Response response) {
        Set<String> names = response.headers().names();
        // httpServletResponse不支持移除请求头，因此按需添加头
        Map<String, String> needSetHeaderMap = new LinkedHashMap<>();
        for (String name : names) {
            if (containsHopByHopHeader(name)) {
                continue;
            }
            if ("Location".equalsIgnoreCase(name)) {
                // 重写重定向头
                needSetHeaderMap.put(name, rewriteUrlFromResponse(httpServletRequest, response.header(name)));
            } else if ("Set-Cookie".equalsIgnoreCase(name) || "Set-Cookie2".equalsIgnoreCase(name)) {
                // 保存Cookie信息
                if (preserveCookie) {
                    needSetHeaderMap.put(name, response.header(name));
                }
            } else {
                needSetHeaderMap.put(name, response.header(name));
            }
        }

        /**
         * 跨域控制
         */
        if (corsControl) {
            /**
             * 1. 清空所有与跨域相关的响应头
             * 2. 如果允许跨域，则添加跨域允许响应头
             */
            Iterator<Map.Entry<String, String>> iterator = needSetHeaderMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String keyIgnoreCase = entry.getKey().toLowerCase(Locale.ROOT);
                if (allowCORSHeaders.contains(keyIgnoreCase)) {
                    iterator.remove();
                }
            }
            if (allowCORS) {
                needSetHeaderMap.put("Access-Control-Allow-Origin", httpServletRequest.getHeader("origin"));
                needSetHeaderMap.put("Access-Control-Allow-Methods", "*");
                needSetHeaderMap.put("Access-Control-Allow-Headers", "*");
                needSetHeaderMap.put("Access-Control-Allow-Credentials", "true");
                needSetHeaderMap.put("Access-Control-Expose-Headers", "*");
            }
        }
        for (String header : needSetHeaderMap.keySet()) {
            httpServletResponse.setHeader(header, needSetHeaderMap.get(header));
        }
    }

    /**
     * 复制请求头
     */
    protected void copyRequestHeaders(HttpServletRequest httpServletRequest, Request.Builder requestBuilder) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            if (containsHopByHopHeader(key)) {
                continue;
            }
            if ("host".equalsIgnoreCase(key)) {
                if (preserveHost) {
                    requestBuilder.header(key, httpServletRequest.getHeader(key));
                }
            } else if ("cookie".equalsIgnoreCase(key)) {
                if (preserveCookie) {
                    requestBuilder.header(key, httpServletRequest.getHeader(key));
                }
            } else {
                requestBuilder.header(key, httpServletRequest.getHeader(key));
            }
        }
    }


    protected void writeGatewayError(HttpServletResponse httpServletResponse, String msg) {
        httpServletResponse.setStatus(502);
        httpServletResponse.setContentType("text/html;charset=utf-8");
        try {
            httpServletResponse.getWriter().write(msg);
        } catch (Exception ignore) {

        }
    }


    /**
     * okhttpclient的流式请求体
     * 节省应用内存，通过流式传输数据，只会在网络传输过程中按需读取数据，而不会将整个请求体加载到内存。
     */
    class StreamingRequestBody extends RequestBody {

        private final MediaType contentType;
        private final InputStream inputStream;

        public StreamingRequestBody(MediaType contentType, InputStream inputStream) {
            this.contentType = contentType;
            this.inputStream = inputStream;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            // 每次读8KB的数据
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                sink.write(buffer, 0, bytesRead);
            }
        }
    }


    private static OkHttpClient okHttpClient;

    public synchronized static OkHttpClient okHttpClient() {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(false)
                    .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);
            builder.followRedirects(false);
            builder.followSslRedirects(true);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(final String s, final SSLSession sslSession) {
                    return true;
                }

                @Override
                public final String toString() {
                    return "NO_OP";
                }
            });
            try {
                X509TrustManager x509TrustManager = new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                };
                TrustManager[] trustManagers = {
                        x509TrustManager
                };
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustManagers, new SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager);
            } catch (Exception ignore) {

            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }
}
