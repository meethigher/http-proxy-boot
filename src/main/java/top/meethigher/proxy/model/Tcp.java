package top.meethigher.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Tcp {

    public Boolean enable = false;
    public String host = "0.0.0.0";
    public Integer port = 8080;
    public Boolean serverTcpKeepAlive = false;
    public Boolean clientTcpKeepAlive = false;
    public Integer serverIdleTimeout = 0;
    public Integer clientIdleTimeout = 0;
    public Integer clientConnectTimeout = 0;
    public Integer maxThreads = 1;
    public List<String> targets = new ArrayList<String>();

}
