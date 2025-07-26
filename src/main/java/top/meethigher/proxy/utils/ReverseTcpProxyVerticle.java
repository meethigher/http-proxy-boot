package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import top.meethigher.proxy.LoadBalancer;
import top.meethigher.proxy.NetAddress;
import top.meethigher.proxy.tcp.ReverseTcpProxy;

import java.util.List;

public class ReverseTcpProxyVerticle extends AbstractVerticle {

    private final LoadBalancer<NetAddress> lb;
    private final List<NetAddress> nodes;
    private final int port;

    public ReverseTcpProxyVerticle(LoadBalancer<NetAddress> lb, List<NetAddress> nodes, int port) {
        this.lb = lb;
        this.nodes = nodes;
        this.port = port;
    }


    @Override
    public void start() throws Exception {
        NetServer netServer = vertx.createNetServer();
        NetClient netClient = vertx.createNetClient();
        String name = ReverseTcpProxy.generateName();
        ReverseTcpProxy.create(netServer, netClient, lb, nodes, name)
                .port(port)
                .start();
    }
}
