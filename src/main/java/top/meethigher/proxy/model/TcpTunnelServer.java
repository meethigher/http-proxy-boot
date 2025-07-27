package top.meethigher.proxy.model;

public class TcpTunnelServer {

    public Boolean enable = false;
    public String host = "0.0.0.0";
    public Integer port = 44444;
    public String secret = "hello,meethigher";
    public Integer judgeDelay = 300;
    public Integer heartbeatDelay = 30000;
    public Boolean serverTcpKeepAlive = false;
    public Integer serverIdleTimeout = 0;
    public Integer maxThreads = 1;

}
