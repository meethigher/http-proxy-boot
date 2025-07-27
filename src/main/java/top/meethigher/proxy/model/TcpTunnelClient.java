package top.meethigher.proxy.model;

public class TcpTunnelClient {

    public Boolean enable = false;
    public Integer minDelay = 1000;
    public Integer maxDelay = 64000;
    public String host = "127.0.0.1";
    public Integer port = 44444;
    public Boolean clientTcpKeepAlive = false;
    public Integer clientIdleTimeout = 0;
    public Integer clientConnectTimeout = 0;
    public String secret = "hello,meethigher";
    public String dataProxyName = "ssh-proxy";
    public String dataProxyHost = "127.0.0.1";
    public Integer dataProxyPort = 2222;
    public String backendHost = "127.0.0.1";
    public Integer backendPort = 22;

}
