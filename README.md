JDK8开箱即用的反向代理工具。支持HTTP反向代理、TCP反向代理、TCP内网穿透、TCP单端口多路复用。Java 本身支持跨平台，因此相对 Nginx
来说，在各种平台上进行测试会更方便。这是我编写该工具的初心。

[使用说明](README-usage.md)

目前有三套分支

1. [1.x](https://github.com/meethigher/http-proxy-boot/tree/1.x)

    * 特点：BIO
    * 说明：借鉴 [开源 HTTP-Proxy-Servlet](https://github.com/mitre/HTTP-Proxy-Servlet)，使用 okhttp3 和 javax.servlet-api
      实现的 [proxy-servlet](https://github.com/meethigher/proxy-servlet)
2. [2.x](https://github.com/meethigher/http-proxy-boot/tree/2.x)

    * 特点：NIO
    * 说明：使用 [vert.x](https://vertx.io/) 实现的[tcp-reverse-proxy](https://github.com/meethigher/tcp-reverse-proxy)

3. [3.x](https://github.com/meethigher/http-proxy-boot/tree/3.x)

    * 特点：NIO
    * 说明：在 2.x 的基础上，脱离了依赖 springboot，更轻量。该分支也就是`master`分支的内容



   