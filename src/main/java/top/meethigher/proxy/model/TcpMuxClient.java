package top.meethigher.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class TcpMuxClient {

    public Boolean enable = false;
    public String host = "127.0.0.1";
    public Integer port = 44444;
    public String secret = "hello,meethigher";
    public Integer serverIdleTimeout = 0;
    public Integer clientIdleTimeout = 0;
    public Integer maxThreads = 1;
    public List<String> services = new ArrayList<String>();

}
