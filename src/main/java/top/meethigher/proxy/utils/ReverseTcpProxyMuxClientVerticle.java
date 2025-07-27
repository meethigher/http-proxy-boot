package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import top.meethigher.proxy.NetAddress;
import top.meethigher.proxy.model.TcpMuxClient;
import top.meethigher.proxy.tcp.mux.ReverseTcpProxyMuxClient;
import top.meethigher.proxy.tcp.mux.model.MuxNetAddress;

import java.util.Map;

public class ReverseTcpProxyMuxClientVerticle extends AbstractVerticle {

    private final TcpMuxClient muxClient;

    private final Map<MuxNetAddress, NetAddress> map;


    public ReverseTcpProxyMuxClientVerticle(TcpMuxClient muxClient, Map<MuxNetAddress, NetAddress> map) {
        this.muxClient = muxClient;
        this.map = map;
    }

    @Override
    public void start() throws Exception {
        ReverseTcpProxyMuxClient.create(vertx, muxClient.secret, map, new NetAddress(muxClient.host, muxClient.port))
                .start();
    }
}
