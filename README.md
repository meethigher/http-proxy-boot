基于[开源HTTP-Proxy-Servlet](https://github.com/mitre/HTTP-Proxy-Servlet)实现的开箱即用的Java HTTP反向代理工具

启动命令

```sh
java -jar http-proxy-boot.jar --server.port=80  --proxy.corsControl.enable=true --proxy.target_url=https://cn.bing.com
```

或者

```sh
java -jar http-proxy-boot.jar --spring.config.location=application.yml
```

跨域参数说明

<img src="src/main/resources/instruction.png" alt=""/>