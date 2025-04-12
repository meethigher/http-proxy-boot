package top.meethigher.proxy.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class TunnelServer implements Serializable {

    private Boolean enable = false;
    // 控制服务监听地址
    private String host = "0.0.0.0";
    // 控制服务监听端口
    private Integer port = 44444;
    // 授权密钥
    private String secret = "0123456789";
    // 连接类型的延迟判定，在弱网情况下，该参数需要调大。单位毫秒
    private Integer judgeDelay = 2000;
    // 心跳间隔。单位毫秒
    private Integer heartbeatDelay = 30000;
    // 空闲连接超时端口。单位毫秒。该值要比心跳值要大方可
    private Integer idleTimeout = 60000;
    // 最大使用的eventloop线程数
    private Integer maxThreads = 1;

}
