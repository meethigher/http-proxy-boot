package top.meethigher.proxy.model;

import java.util.ArrayList;
import java.util.List;

public class Udp {
    public Boolean enable = false;
    public String host = "0.0.0.0";
    public Integer port = 8080;
    public Long dstTimeout = 60000L;
    public Integer maxThreads = 1;
    public List<String> targets = new ArrayList<String>();
}
