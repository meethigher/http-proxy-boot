package top.meethigher.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.meethigher.proxy.model.*;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static top.meethigher.proxy.utils.Utils.*;

public class App {

    public static String pid() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String name = runtimeMXBean.getName();
        return name.split("@")[0];
    }

    public static void main(String[] args) {
        // 指定logback使用的环境变量
        System.setProperty("LOG_HOME", "logs");
        //PID的获取，要在所有log对象生成之前
        System.setProperty("PID", pid());
        loadLogConfig();
        Reverse reverse = loadApplicationConfig();
        Tcp tcp = reverse.tcp;
        Http http = reverse.http;
        TcpTunnelClient tc = reverse.tcpTunnelClient;
        TcpTunnelServer ts = reverse.tcpTunnelServer;
        TcpMuxClient mc = reverse.tcpMuxClient;
        TcpMuxServer ms = reverse.tcpMuxServer;
        final Logger log = LoggerFactory.getLogger(App.class);
        if (tcp.enable) {
            log.info("current mode: ReverseTcpProxy");
            registerReverseTcpProxy(tcp);
        } else if (http.enable) {
            log.info("current mode: ReverseHttpProxy");
            registerReverseHttpProxy(http);
        } else if (tc.enable) {
            log.info("current mode: ReverseTcpProxyTunnelClient");
            registerReverseTcpProxyTunnelClient(tc);
        } else if (ts.enable) {
            log.info("current mode: ReverseTcpProxyTunnelServer");
            registerReverseTcpProxyTunnelServer(ts);
        } else if (mc.enable) {
            log.info("current mode: ReverseTcpProxyMuxClient");
            registerReverseTcpProxyMuxClient(mc);
        } else if (ms.enable) {
            log.info("current mode: ReverseTcpProxyMuxServer");
            registerReverseTcpProxyMuxServer(ms);
        } else {
            throw new IllegalArgumentException("you need to enable one mode");
        }
    }

}
