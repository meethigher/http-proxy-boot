package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClientOptions;
import top.meethigher.proxy.model.TcpTunnelClient;
import top.meethigher.proxy.tcp.tunnel.ReverseTcpProxyTunnelClient;

import java.util.concurrent.TimeUnit;

public class ReverseTcpProxyTunnelClientVerticle extends AbstractVerticle {


    private final TcpTunnelClient tc;

    public ReverseTcpProxyTunnelClientVerticle(TcpTunnelClient tc) {
        this.tc = tc;
    }

    @Override
    public void start() throws Exception {
        ReverseTcpProxyTunnelClient.create(vertx, vertx.createNetClient(new NetClientOptions()
                                .setTcpKeepAlive(tc.clientTcpKeepAlive)
                                .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                                .setIdleTimeout(tc.clientIdleTimeout)
                                .setConnectTimeout(tc.clientConnectTimeout)), tc.minDelay, tc.maxDelay,
                        tc.secret)
                .backendPort(tc.backendPort)
                .backendHost(tc.backendHost)
                .dataProxyName(tc.dataProxyName)
                .dataProxyHost(tc.dataProxyHost)
                .dataProxyPort(tc.dataProxyPort)
                .connect(tc.host, tc.port);
    }
}
