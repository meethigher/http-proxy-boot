package top.meethigher.proxy.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class TcpTunnelClient implements Serializable {

    private Boolean enable = false;
    // 失败重连最小延迟，单位毫秒
    private Integer minDelay = 1000;
    // 失败重连最大延迟，单位毫秒
    private Integer maxDelay = 64000;
    // 控制服务地址
    private String host = "127.0.0.1";
    // 控制服务端口
    private Integer port = 44444;
    // 鉴权密钥
    private String secret = "0123456789";
    // 穿透后的服务名称
    private String dataProxyName = "ssh-proxy";
    // 穿透后的服务地址
    private String dataProxyHost = "127.0.0.1";
    // 穿透后的服务端口
    private Integer dataProxyPort = 22;
    // 需要穿透的后端服务地址
    private String backendHost = "meethigher.top";
    // 需要穿透的后端端口
    private Integer backendPort = 22;

}
