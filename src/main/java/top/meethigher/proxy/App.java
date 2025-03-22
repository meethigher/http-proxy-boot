package top.meethigher.proxy;

import io.vertx.core.Vertx;
import top.meethigher.proxy.model.Http;
import top.meethigher.proxy.model.Reverse;
import top.meethigher.proxy.model.Tcp;
import top.meethigher.proxy.utils.ReverseHttpProxyVerticle;
import top.meethigher.proxy.utils.ReverseTcpProxyVerticle;

import static top.meethigher.proxy.utils.Utils.*;

public class App {


    public static void main(String[] args) {
        // 指定logback使用的环境变量
        System.setProperty("LOG_HOME", "logs");
        loadLogConfig();
        Reverse reverse = loadApplicationConfig();
        Tcp tcp = reverse.getTcp();
        Http http = reverse.getHttp();
        Vertx vertx = vertx();
        if (tcp.getEnable()) {
            registerReverseTcpProxy(vertx, tcp);
        } else if (http.getEnable()) {
            if (http.getPreheatDns()) {
                preheatDns(http);
            }
            registerReverseHttpProxy(vertx, http);
        } else {
            throw new IllegalArgumentException("you need to enable tcp or http");
        }
    }

    private static void registerReverseHttpProxy(Vertx vertx, Http http) {
        for (int i = 0; i < http.getMaxThreads(); i++) {
            vertx.deployVerticle(new ReverseHttpProxyVerticle(http.getPort(), http));
        }
    }

    private static void registerReverseTcpProxy(Vertx vertx, Tcp tcp) {
        for (int i = 0; i < tcp.getMaxThreads(); i++) {
            vertx.deployVerticle(new ReverseTcpProxyVerticle(tcp.getPort(), tcp));
        }
    }
}
