package top.meethigher.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Http {

    public Boolean enable = false;
    public String host = "0.0.0.0";
    public Integer port = 8080;
    public Integer maxThreads = 1;
    public Integer http1MaxSize = 6000;
    public Integer http2MaxSize = 2000;
    public Boolean serverTcpKeepAlive = false;
    public Boolean clientTcpKeepAlive = false;
    public Integer serverIdleTimeout = 0;
    public Integer clientIdleTimeout = 0;
    public Integer clientConnectTimeout = 0;
    public Integer clientHttpKeepAliveTimeout = 60;
    public List<Router> routers = new ArrayList<Router>();

}
