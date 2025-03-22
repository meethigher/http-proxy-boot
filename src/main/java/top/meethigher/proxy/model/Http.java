package top.meethigher.proxy.model;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Http implements Serializable {

    private Boolean enable = false;
    private Integer port = 8080;
    private Integer maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    private Integer http1MaxSize = 5000;
    private Integer http2MaxSize = 1000;
    private List<Router> routers = new ArrayList<Router>();
    private Boolean preheatDns = false;

}
