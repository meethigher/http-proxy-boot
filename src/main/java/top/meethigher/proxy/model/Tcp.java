package top.meethigher.proxy.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Tcp implements Serializable {

    private Boolean enable = false;
    private Integer port = 8090;
    private Integer maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    private String targetHost = "127.0.0.1";
    private Integer targetPort = 8080;

}
