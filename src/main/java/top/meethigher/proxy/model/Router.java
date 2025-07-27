package top.meethigher.proxy.model;

public class Router {

    public String name = "route-default";
    public String sourceUrl = "/*";
    public String targetUrl = "http://127.0.0.1:80";
    public Boolean forwardIp = false;
    public Boolean preserveCookies = true;
    public Boolean preserveHost = false;
    public Boolean followRedirects = true;
    public Boolean httpKeepAlive = true;
    public Boolean logEnable = true;
    public String logFormat = "{name} -- {serverHttpVersion} -- {clientHttpVersion} -- {method} -- {userAgent} -- {serverRemoteAddr} -- {serverLocalAddr} -- {clientLocalAddr} -- {clientRemoteAddr} -- {sourceUri} -- {proxyUrl} -- {statusCode} -- consumed {consumedMills} ms";
    public Boolean corsControl = false;
    public Boolean corsAllow = true;

}
