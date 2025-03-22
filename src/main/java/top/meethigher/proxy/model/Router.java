package top.meethigher.proxy.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Router  implements Serializable {

    private String name = "proxyRoute";
    private String sourceUrl = "/*";
    private String targetUrl = "http://127.0.0.1:8080";
    private Boolean forwardIp = false;
    private Boolean preserveCookies = true;
    private Boolean preserveHost = false;
    private Boolean followRedirects = true;
    private Boolean httpKeepAlive = true;
    private Boolean logEnable = true;
    private String logFormat = "{name} -- {serverHttpVersion} -- {clientHttpVersion} -- {method} -- {userAgent} -- {serverRemoteAddr} -- {clientLocalAddr} -- {sourceUri} -- {proxyUrl} -- {statusCode} -- consumed {consumedMills} ms";
    private Boolean corsControl = false;
    private Boolean corsAllow = true;

}
