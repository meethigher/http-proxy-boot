package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.PoolOptions;
import io.vertx.ext.web.Router;
import top.meethigher.proxy.http.ProxyRoute;
import top.meethigher.proxy.http.ReverseHttpProxy;
import top.meethigher.proxy.model.Http;

import java.util.Arrays;
import java.util.Collections;

public class ReverseHttpProxyVerticle extends AbstractVerticle {

    private final Http http;

    public ReverseHttpProxyVerticle(Http http) {
        this.http = http;
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setTrustAll(true)
                .setVerifyHost(false)
                // httpclient支持与后端服务进行协商使用使用http2
                .setUseAlpn(true)
                .setProtocolVersion(HttpVersion.HTTP_2);
        PoolOptions poolOptions = new PoolOptions()
                .setHttp1MaxSize(http.getHttp1MaxSize())
                .setHttp2MaxSize(http.getHttp2MaxSize());
        HttpServerOptions httpServerOptions = new HttpServerOptions()
                // 服务端支持与客户端进行协商，使用h2c（HTTP/2 cleartext）
                // 常规情况下，h2只在开启了tls使用。如果不开启tls，需要指定使用的是h2c
                .setAlpnVersions(Collections.unmodifiableList(Arrays.asList(HttpVersion.HTTP_1_1, HttpVersion.HTTP_2)))
                .setUseAlpn(true)
                .setHttp2ClearTextEnabled(true);
        ReverseHttpProxy httpProxy = ReverseHttpProxy.create(router,
                        vertx.createHttpServer(httpServerOptions),
                        vertx.createHttpClient(httpClientOptions, poolOptions))
                .port(http.getPort());
        for (int i = 0, order = Integer.MAX_VALUE; i < http.getRouters().size(); i++, order--) {
            top.meethigher.proxy.model.Router r = http.getRouters().get(i);
            ProxyRoute proxyRoute = new ProxyRoute()
                    .setName(r.getName())
                    .setSourceUrl(r.getSourceUrl())
                    .setTargetUrl(r.getTargetUrl())
                    .setForwardIp(r.getForwardIp())
                    .setPreserveCookies(r.getPreserveCookies())
                    .setPreserveHost(r.getPreserveHost())
                    .setFollowRedirects(r.getFollowRedirects())
                    .setHttpKeepAlive(r.getHttpKeepAlive())
                    .setLog(new ProxyRoute.Log().setEnable(r.getLogEnable()).setLogFormat(r.getLogFormat()))
                    .setCorsControl(new ProxyRoute.CorsControl().setEnable(r.getCorsControl()).setAllowCors(r.getCorsAllow()));
            httpProxy.addRoute(proxyRoute, order, false);
        }
        httpProxy.start();
    }
}
