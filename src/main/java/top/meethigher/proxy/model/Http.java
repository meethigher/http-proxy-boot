package top.meethigher.proxy.model;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Http implements Serializable {

    private Boolean enable = false;
    private Integer port = 8080;
    // 最大使用的eventloop线程数
    private Integer maxThreads = 1;
    // 单个eventloop最大可以处理的http1请求
    private Integer http1MaxSize = 6000;
    // 单个eventloop最大可以处理的http2请求
    private Integer http2MaxSize = 2000;
    // DNS解析预热。当一台机器有多个DNS服务时，建议将该参数开启，可以预热缓存
    private Boolean preheatDns = false;

    private List<Router> routers = new ArrayList<>();

}
