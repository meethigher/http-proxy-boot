开箱即用的Java HTTP反向代理工具，支持根据路径代理至不同路径。

该工具仅为了方便我在开发环境进行开箱即用的测试。 内部基于两套代理库。

1. [1.x](https://github.com/meethigher/http-proxy-boot/tree/1.x)

   特点：BIO

   说明：借鉴[开源HTTP-Proxy-Servlet](https://github.com/mitre/HTTP-Proxy-Servlet)，使用okhttp3和javax.servlet-api实现的[proxy-servlet](https://github.com/meethigher/proxy-servlet)

2. [2.x](https://github.com/meethigher/http-proxy-boot/tree/2.x)

   特点：NIO

   说明：使用[vert.x](https://vertx.io/)实现的[tcp-reverse-proxy](https://github.com/meethigher/tcp-reverse-proxy)