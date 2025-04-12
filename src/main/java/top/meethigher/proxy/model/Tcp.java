package top.meethigher.proxy.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Tcp implements Serializable {

    private Boolean enable = false;
    private Integer port = 8090;
    //最大使用的eventloop线程数
    private Integer maxThreads = 1;
    // 目标host
    private String targetHost = "reqres.in";
    // 目标端口
    private Integer targetPort = 443;

}
