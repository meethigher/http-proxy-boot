package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import top.meethigher.proxy.model.TcpMuxServer;
import top.meethigher.proxy.tcp.mux.ReverseTcpProxyMuxServer;

import java.util.concurrent.TimeUnit;

public class ReverseTcpProxyMuxServerVerticle extends AbstractVerticle {

    private final TcpMuxServer muxServer;

    public ReverseTcpProxyMuxServerVerticle(TcpMuxServer muxServer) {
        this.muxServer = muxServer;
    }

    @Override
    public void start() throws Exception {
        NetServer netServer = vertx.createNetServer(
                new NetServerOptions()
                        .setTcpKeepAlive(muxServer.serverTcpKeepAlive)
                        .setIdleTimeout(muxServer.serverIdleTimeout).setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
        );
        NetClient netClient = vertx.createNetClient(
                new NetClientOptions()
                        .setTcpKeepAlive(muxServer.clientTcpKeepAlive)
                        .setConnectTimeout(muxServer.clientConnectTimeout)
                        .setIdleTimeout(muxServer.clientIdleTimeout).setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
        );
        ReverseTcpProxyMuxServer.create(vertx, muxServer.secret, netServer, netClient)
                .host(muxServer.host)
                .port(muxServer.port)
                .start();
    }
}
