package top.meethigher.proxy.model;

public class TcpMuxServer {

    public Boolean enable = false;
    public String host = "0.0.0.0";
    public Integer port = 44444;
    public String secret = "hello,meethigher";
    public Integer serverIdleTimeout = 0;
    public Integer clientIdleTimeout = 0;
    public Integer maxThreads = 1;

}
