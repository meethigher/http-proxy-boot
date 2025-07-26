package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import top.meethigher.proxy.model.TcpTunnelServer;
import top.meethigher.proxy.tcp.tunnel.ReverseTcpProxyTunnelServer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReverseTcpProxyTunnelServerVerticle extends AbstractVerticle {

    private final TcpTunnelServer ts;

    private final Map<NetSocket, ReverseTcpProxyTunnelServer.DataProxyServer> authedSockets;

    public ReverseTcpProxyTunnelServerVerticle(TcpTunnelServer ts, Map<NetSocket, ReverseTcpProxyTunnelServer.DataProxyServer> authedSockets) {
        this.ts = ts;
        this.authedSockets = authedSockets;
    }

    @Override
    public void start() throws Exception {
        ReverseTcpProxyTunnelServer.create(
                        vertx,
                        vertx.createNetServer(new NetServerOptions()
                                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                                .setIdleTimeout(ts.getIdleTimeout())),
                        ts.getSecret(),
                        authedSockets)
                .heartbeatDelay(ts.getHeartbeatDelay())
                .judgeDelay(ts.getJudgeDelay())
                .host(ts.getHost())
                .port(ts.getPort())
                .start();

    }
}
