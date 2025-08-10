package top.meethigher.proxy.utils;

import io.vertx.core.AbstractVerticle;
import top.meethigher.proxy.LoadBalancer;
import top.meethigher.proxy.NetAddress;
import top.meethigher.proxy.model.Udp;
import top.meethigher.proxy.udp.ReverseUdpProxy;

public class ReverseUdpProxyVerticle extends AbstractVerticle {
    private final LoadBalancer<NetAddress> lb;
    private final Udp udp;


    public ReverseUdpProxyVerticle(LoadBalancer<NetAddress> lb, Udp udp) {
        this.lb = lb;
        this.udp = udp;
    }

    @Override
    public void start() throws Exception {
        ReverseUdpProxy.create(vertx, udp.dstTimeout, lb)
                .host(udp.host)
                .port(udp.port)
                .start();
    }
}
