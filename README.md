基于[开源HTTP-Proxy-Servlet](https://github.com/mitre/HTTP-Proxy-Servlet)实现的开箱即用的Java HTTP反向代理工具，支持根据路径代理至不同路径

创建配置文件 `application.yml` 

```yaml
logging:
  config: classpath:log/logback.xml
  file:
    path: logs
server:
  port: 8080
proxy:
  servlets:
    - name: bing
      # /* represents all interfaces below proxy /, no distinction between /* and /**
      servletUrl: /*
      targetUrl: https://cn.bing.com
      log:
        enable: true
        # configure the agent’s log format. the options are remoteAddr、remotePort、userAgent、method、source、target
        logFormat: "{remoteAddr} {method} uri: {source} --> {target}"
      corsControl:
        # true means that the cors of all requests is managed by http-proxy-boot;
        # false means that the cors of all requests is managed by the source service.
        enable: false
        # true means that the cors of all requests is allowed when enable=true
        # false means that the cors of all requests is not allowed then enable=true
        allowCORS: true
    - name: jetbrains
      servletUrl: /test/*
      targetUrl: http://jetbrains.meethigher.top
      log:
        enable: true
        logFormat: "{remoteAddr} {method} uri: {source} --> {target}"
      corsControl:
        enable: false
        allowCORS: false
```

启动

```sh
java -jar http-proxy-boot.jar --spring.config.location=application.yml
```



跨域参数说明


<img src="src/main/resources/instruction.png" alt=""/>
