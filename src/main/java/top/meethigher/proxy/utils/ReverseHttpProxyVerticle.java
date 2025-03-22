package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.PoolOptions;
import io.vertx.ext.web.Router;
import top.meethigher.proxy.http.ProxyRoute;
import top.meethigher.proxy.http.ReverseHttpProxy;
import top.meethigher.proxy.model.Http;

public class ReverseHttpProxyVerticle extends AbstractVerticle {

    private final int port;
    private final Http http;

    public ReverseHttpProxyVerticle(int port, Http http) {
        this.port = port;
        this.http = http;
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setTrustAll(true)
                .setVerifyHost(false)
                .setUseAlpn(true)
                .setProtocolVersion(HttpVersion.HTTP_2);
        PoolOptions poolOptions = new PoolOptions()
                .setHttp1MaxSize(http.getHttp1MaxSize())
                .setHttp2MaxSize(http.getHttp2MaxSize());
        ReverseHttpProxy httpProxy = ReverseHttpProxy.create(router,
                        vertx.createHttpServer(),
                        vertx.createHttpClient(httpClientOptions, poolOptions))
                .port(port);
        httpProxy.start();
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
            httpProxy.addRoute(proxyRoute, order);
        }
    }
}
