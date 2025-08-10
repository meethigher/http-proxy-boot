package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import top.meethigher.proxy.LoadBalancer;
import top.meethigher.proxy.NetAddress;
import top.meethigher.proxy.model.Tcp;
import top.meethigher.proxy.tcp.ReverseTcpProxy;

import java.util.concurrent.TimeUnit;

public class ReverseTcpProxyVerticle extends AbstractVerticle {

    private final LoadBalancer<NetAddress> lb;
    private final Tcp tcp;

    public ReverseTcpProxyVerticle(LoadBalancer<NetAddress> lb, Tcp tcp) {
        this.lb = lb;
        this.tcp = tcp;
    }


    @Override
    public void start() throws Exception {
        NetServerOptions netServerOptions = new NetServerOptions()
                .setTcpKeepAlive(tcp.serverTcpKeepAlive)
                .setIdleTimeout(tcp.serverIdleTimeout).setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
        NetClientOptions netClientOptions = new NetClientOptions()
                .setTcpKeepAlive(tcp.clientTcpKeepAlive)
                .setConnectTimeout(tcp.clientConnectTimeout).setIdleTimeout(tcp.clientIdleTimeout).setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
        NetServer netServer = vertx.createNetServer(netServerOptions);
        NetClient netClient = vertx.createNetClient(netClientOptions);
        String name = ReverseTcpProxy.generateName();
        ReverseTcpProxy.create(netServer, netClient, lb, name)
                .host(tcp.host)
                .port(tcp.port)
                .start();
    }
}
