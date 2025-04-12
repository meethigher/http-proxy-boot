package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServerOptions;
import top.meethigher.proxy.model.TunnelServer;
import top.meethigher.proxy.tcp.tunnel.ReverseTcpProxyTunnelServer;

import java.util.concurrent.TimeUnit;

public class ReverseTcpProxyTunnelServerVerticle extends AbstractVerticle {

    private final TunnelServer ts;

    public ReverseTcpProxyTunnelServerVerticle(TunnelServer ts) {
        this.ts = ts;
    }

    @Override
    public void start() throws Exception {
        ReverseTcpProxyTunnelServer.create(
                        vertx,
                        vertx.createNetServer(new NetServerOptions()
                                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                                .setIdleTimeout(ts.getIdleTimeout())),
                        ts.getSecret())
                .heartbeatDelay(ts.getHeartbeatDelay())
                .judgeDelay(ts.getJudgeDelay())
                .host(ts.getHost())
                .port(ts.getPort())
                .start();

    }
}
