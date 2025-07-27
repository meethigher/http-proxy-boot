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

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReverseTcpProxyVerticle extends AbstractVerticle {

    private final LoadBalancer<NetAddress> lb;
    private final List<NetAddress> nodes;
    private final Tcp tcp;

    public ReverseTcpProxyVerticle(LoadBalancer<NetAddress> lb, List<NetAddress> nodes, Tcp tcp) {
        this.lb = lb;
        this.nodes = nodes;
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
        ReverseTcpProxy.create(netServer, netClient, lb, nodes, name)
                .host(tcp.host)
                .port(tcp.port)
                .start();
    }
}
