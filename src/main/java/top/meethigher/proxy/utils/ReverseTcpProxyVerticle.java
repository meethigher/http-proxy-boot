package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import top.meethigher.proxy.model.Tcp;
import top.meethigher.proxy.tcp.ReverseTcpProxy;

public class ReverseTcpProxyVerticle extends AbstractVerticle {

    private final int port;
    private final Tcp tcp;

    public ReverseTcpProxyVerticle(int port, Tcp tcp) {
        this.port = port;
        this.tcp = tcp;
    }

    @Override
    public void start() throws Exception {
        ReverseTcpProxy.create(vertx, tcp.getTargetHost(), tcp.getTargetPort())
                .port(port)
                .start();
    }
}
