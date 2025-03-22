下载[Releases · meethigher/http-proxy-boot](https://github.com/meethigher/http-proxy-boot/releases)

使用方式

```java
java -jar http-proxy-boot.jar
```

配置文件 application.yml 示例

```yml
# 反向代理。支持tcp与http两种反代模式。应用启动时只支持一种模式
reverse:
  # tcp反向代理
  tcp:
    enable: true
    port: 8080
    # 最大使用的eventloop线程数
    maxThreads: 1
    # 目标host
    targetHost: reqres.in
    # 目标端口
    targetPort: 443
  # http反向代理
  http:
    enable: false
    port: 8080
    # DNS解析预热。当一台机器有多个DNS服务时，建议将该参数开启，可以预热缓存
    preheatDns: false
    # 最大使用的eventloop线程数
    maxThreads: 1
    # 单个eventloop最大可以处理的http1请求
    http1MaxSize: 6000
    # 单个eventloop最大可以处理的http2请求
    http2MaxSize: 2000
    # 代理路由
    # 路由配置越靠后，优先级越高
    routers:
      - name: route1
        # 代理路径。支持单路径匹配（如 /test ）、多路径匹配（如 /* ）
        sourceUrl: /*
        # 代理目标地址
        targetUrl: https://reqres.in
        # 转发客户端请求IP与协议
        forwardIp: false
        # 保留响应头的Cookie与SetCookie
        preserveCookies: true
        # 保留请求头的Host
        preserveHost: false
        # 跟随跳转
        followRedirects: true
        # 代理服务内部请求是否使用keepalive长连接
        httpKeepAlive: true
        # 记录http日志，日志格式由logFormat参数决定
        logEnable: true
        # 日志格式，当logEnable开启时生效。用花括号引起来，表示关键字。关键字可选，格式支持自定义
        logFormat: "{name} -- {serverHttpVersion} -- {clientHttpVersion} -- {method} -- {userAgent} -- {serverRemoteAddr} -- {clientLocalAddr} -- {sourceUri} -- {proxyUrl} -- {statusCode} -- consumed {consumedMills} ms"
        # 由代理服务管理跨域。当该参数为true时，是否允许跨域由allowCors决定
        corsControl: false
        # 是否允许跨域。当corsControl开启时生效
        corsAllow: true
      - name: route2
        sourceUrl: /test/*
        targetUrl: https://meethigher.top
      - name: route3
        sourceUrl: /test1/*
        targetUrl: https://reqres.in
```

