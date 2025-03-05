package top.meethigher.proxy.utils;

import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import top.meethigher.proxy.App;
import top.meethigher.proxy.http.ReverseHttpProxy;

public class VertxUtils {

    private volatile static ReverseHttpProxy httpProxy;

    public static ReverseHttpProxy httpProxy() {
        //第一次检查
        if (httpProxy == null) {
            //加锁第二次检查
            synchronized (App.class) {
                if (httpProxy == null) {
                    Vertx vertx = Vertx.vertx();
                    Router router = Router.router(vertx);
                    // 遗憾的是，vertx的httpclient没有读超时、写超时。
                    HttpClientOptions httpClientOptions = new HttpClientOptions()
                            .setVerifyHost(false)
                            .setTrustAll(true)
                            .setConnectTimeout(10000);
                    // 创建HttpClient时指定的PoolOptions里面的EventLoopSize不会生效。以Vertx的EventLoopSize为主。默认http/1为5并发，http/2为1并发
                    PoolOptions poolOptions = new PoolOptions().setHttp1MaxSize(2000).setHttp2MaxSize(1000);
                    HttpClient httpClient = vertx.createHttpClient(httpClientOptions, poolOptions);
                    HttpServerOptions httpServerOptions = new HttpServerOptions();
                    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

                    httpProxy = ReverseHttpProxy.create(router, httpServer, httpClient, "httpProxy");
                }
            }
        }
        return httpProxy;
    }
}
