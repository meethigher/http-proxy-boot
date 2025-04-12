package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import top.meethigher.proxy.model.Tcp;
import top.meethigher.proxy.tcp.ReverseTcpProxy;

public class ReverseTcpProxyVerticle extends AbstractVerticle {

    private final Tcp tcp;

    public ReverseTcpProxyVerticle(Tcp tcp) {

        this.tcp = tcp;
    }

    @Override
    public void start() throws Exception {
        ReverseTcpProxy.create(vertx, tcp.getTargetHost(), tcp.getTargetPort())
                .port(tcp.getPort())
                .start();
    }
}
