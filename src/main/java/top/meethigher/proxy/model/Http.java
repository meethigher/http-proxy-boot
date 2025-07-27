package top.meethigher.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Http {

    public Boolean enable = false;
    public Integer port = 8080;
    public Boolean preheatDns = false;
    public Integer maxThreads = 1;
    public Integer http1MaxSize = 6000;
    public Integer http2MaxSize = 2000;
    public List<Router> routers = new ArrayList<Router>();

}
