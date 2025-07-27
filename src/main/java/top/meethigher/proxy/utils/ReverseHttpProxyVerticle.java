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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ReverseHttpProxyVerticle extends AbstractVerticle {

    private final Http http;

    public ReverseHttpProxyVerticle(Http http) {
        this.http = http;
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setTcpKeepAlive(http.clientTcpKeepAlive)
                .setIdleTimeout(http.clientIdleTimeout)
                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                .setKeepAliveTimeout(http.clientHttpKeepAliveTimeout)
                // 设置客户端默认使用的HTTP协议版本是http1.1，并且开启alpn支持协商http1.1和http2Add commentMore actions
                // alpn基于tls，若对方没有开启tls，则不支持alpn
                .setProtocolVersion(HttpVersion.HTTP_1_1)
                .setUseAlpn(true)
                .setAlpnVersions(new ArrayList<HttpVersion>() {{
                    add(HttpVersion.HTTP_1_1);
                    add(HttpVersion.HTTP_2);
                }});
        PoolOptions poolOptions = new PoolOptions()
                .setHttp1MaxSize(http.http1MaxSize)
                .setHttp2MaxSize(http.http2MaxSize);
        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setTcpKeepAlive(http.serverTcpKeepAlive)
                .setIdleTimeout(http.serverIdleTimeout)
                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                // 服务端支持与客户端进行协商，支持通过alpn用于协商客户端和服务端使用http1.1还是http2
                // 开启h2c，使其支持http2，默认情况下http2只在开启了tls使用。如果不开启tls还想使用http2，那么需要开启h2c
                // alpn基于tls，若未开启tls，则不支持alpn
                .setAlpnVersions(Collections.unmodifiableList(Arrays.asList(HttpVersion.HTTP_1_1, HttpVersion.HTTP_2)))
                .setUseAlpn(true)
                .setHttp2ClearTextEnabled(true);
        ReverseHttpProxy httpProxy = ReverseHttpProxy.create(router,
                        vertx.createHttpServer(httpServerOptions),
                        vertx.createHttpClient(httpClientOptions, poolOptions))
                .port(http.port)
                .host(http.host);
        for (int i = 0, order = Integer.MAX_VALUE; i < http.routers.size(); i++, order--) {
            top.meethigher.proxy.model.Router r = http.routers.get(i);
            ProxyRoute proxyRoute = new ProxyRoute()
                    .setName(r.name)
                    .setSourceUrl(r.sourceUrl)
                    .setTargetUrl(r.targetUrl)
                    .setForwardIp(r.forwardIp)
                    .setPreserveCookies(r.preserveCookies)
                    .setPreserveHost(r.preserveHost)
                    .setFollowRedirects(r.followRedirects)
                    .setHttpKeepAlive(r.httpKeepAlive)
                    .setLog(new ProxyRoute.Log().setEnable(r.logEnable).setLogFormat(r.logFormat))
                    .setCorsControl(new ProxyRoute.CorsControl().setEnable(r.corsControl).setAllowCors(r.corsAllow));
            httpProxy.addRoute(proxyRoute, order, false);
        }
        httpProxy.start();
    }
}
