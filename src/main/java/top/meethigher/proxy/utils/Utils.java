package top.meethigher.proxy.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import top.meethigher.proxy.NetAddress;
import top.meethigher.proxy.model.*;
import top.meethigher.proxy.tcp.TcpRoundRobinLoadBalancer;
import top.meethigher.proxy.tcp.tunnel.ReverseTcpProxyTunnelClient;
import top.meethigher.proxy.tcp.tunnel.ReverseTcpProxyTunnelServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class Utils {


    private static final String CONFIG_FILE_NAME = "application.yml";

    private static final String CONFIG_EXAMPLE_FILE_NAME = "application-full.yml";

    private volatile static Vertx vertx;

    public static void registerReverseHttpProxy(Http http) {
        for (int i = 0; i < http.getMaxThreads(); i++) {
            vertx().deployVerticle(new ReverseHttpProxyVerticle(http)).onFailure(e -> {
                log.error("deploy reverse http proxy failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseTcpProxy(Tcp tcp) {
        List<String> targets = tcp.getTargets();
        List<NetAddress> nodes = new ArrayList<>();
        for (String target : targets) {
            try {
                String[] addr = target.split(":");
                nodes.add(new NetAddress(addr[0], Integer.parseInt(addr[1])));
            } catch (Exception e) {
                log.error("tcp targets format is incorrect. The correct format is host:port.");
                System.exit(1);
            }
        }
        TcpRoundRobinLoadBalancer lb = TcpRoundRobinLoadBalancer.create(nodes);
        for (int i = 0; i < tcp.getMaxThreads(); i++) {
            vertx().deployVerticle(new ReverseTcpProxyVerticle(lb, nodes,tcp.getPort())).onFailure(e -> {
                log.error("deploy reverse tcp proxy failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseTcpProxyTunnelClient(TcpTunnelClient tc) {
        ReverseTcpProxyTunnelClient.create(vertx(), vertx().createNetClient(), tc.getMinDelay(), tc.getMaxDelay(),
                        tc.getSecret())
                .backendPort(tc.getBackendPort())
                .backendHost(tc.getBackendHost())
                .dataProxyName(tc.getDataProxyName())
                .dataProxyHost(tc.getDataProxyHost())
                .dataProxyPort(tc.getDataProxyPort())
                .connect(tc.getHost(), tc.getPort());
    }

    public static void registerReverseTcpProxyTunnelServer(TcpTunnelServer server) {
        final ConcurrentHashMap<NetSocket, ReverseTcpProxyTunnelServer.DataProxyServer> authedSockets = new ConcurrentHashMap<>();
        for (int i = 0; i < server.getMaxThreads(); i++) {
            vertx().deployVerticle(new ReverseTcpProxyTunnelServerVerticle(server, authedSockets)).onFailure(e -> {
                log.error("deplay reverse tcp tunnel server failed", e);
                System.exit(1);
            });
        }
    }

    public static Map<String, Object> loadYaml() {
        Yaml yaml = new Yaml();
        // 优先加载外部配置文件，其次加载类路径下配置文件
        String userDirPath = System.getProperty("user.dir").replace("\\", "/");
        File file = new File(userDirPath, CONFIG_FILE_NAME);
        File exampleFile = new File(userDirPath, CONFIG_EXAMPLE_FILE_NAME);
        Map<String, Object> map = null;
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                map = yaml.load(fis);
            } catch (Exception e) {
                log.error("load external config file error", e);
            }
            log.info("load external config file {}", file.getAbsoluteFile());
        } else {
            try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
                map = yaml.load(is);
            } catch (Exception e) {
                log.error("load internal config file error", e);
            }
            log.info("load internal config file {}", CONFIG_FILE_NAME);
            try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
                 InputStream eIs = Utils.class.getClassLoader().getResourceAsStream(CONFIG_EXAMPLE_FILE_NAME)) {
                // 若外部配置文件不存在，则将内部配置文件向外部复制一份。
                Files.copy(is, file.toPath());
                Files.copy(eIs, exampleFile.toPath());
            } catch (Exception e) {
                log.error("create config file error", e);
            }
            log.info("create config file {}, example config file {}", file.getAbsoluteFile(),
                    exampleFile.getAbsoluteFile());
        }
        return map;
    }

    public static Reverse loadApplicationConfig() {
        Map<String, Object> map = loadYaml();
        JsonObject jsonObject = JsonObject.mapFrom(map);
        JsonObject reverseJson = jsonObject.getJsonObject("reverse");
        return reverseJson.mapTo(Reverse.class);
    }


    public static void loadLogConfig() {
        String logConfigFile = "logback.xml";
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();  // 先重置当前的 Logback 配置
        context.putProperty("LOG_HOME", "logs");
        try {
            // 优先加载外部配置文件，其次加载类路径下配置文件
            String userDirPath = System.getProperty("user.dir").replace("\\", "/");
            File file = new File(userDirPath, logConfigFile);
            URL configFile = null;
            boolean exists = file.exists();
            if (exists) {
                configFile = file.toURI().toURL();
            } else {
                configFile = Utils.class.getClassLoader().getResource(logConfigFile);
            }
            // 获取 classpath 下的配置文件
            if (configFile == null) {
                throw new IllegalArgumentException("Logback configuration file not found: " + logConfigFile);
            }
            // 使用 JoranConfigurator 加载配置
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(configFile);
            if (exists) {
                log.info("logback configuration loaded successfully: {}", file.getAbsoluteFile());
            } else {
                log.info("logback configuration loaded successfully: {}", logConfigFile);
                try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(logConfigFile)) {
                    Files.copy(is, file.toPath());
                } catch (Exception e) {
                    log.error("create log config file error", e);
                }
                log.info("create log config file {}", file.getAbsoluteFile());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Logback configuration", e);
        }
    }

    public static Vertx vertx() {
        //第一次检查
        if (vertx == null) {
            //加锁第二次检查
            synchronized (Utils.class) {
                if (vertx == null) {
                    VertxOptions options = new VertxOptions()
                            .setAddressResolverOptions(new AddressResolverOptions()
                                    .setQueryTimeout(2000))
                            .setUseDaemonThread(false);
                    vertx = Vertx.vertx(options);
                }
            }
        }
        return vertx;
    }


    /**
     * 域名解析成ipv4
     */
    public static String domain2Ipv4(String domain) {
        if (IPv4Validator.isIPv4Address(domain)) {
            return domain;
        } else {
            try {
                return InetAddress.getByName(domain).getHostAddress();
            } catch (Exception e) {
                return domain;
            }
        }
    }


    /**
     * netty的dns解析是顺序解析的，若dns有一个存在问题，会导致整体响应变慢。因此需要预热成ip地址
     * 参考https://github.com/meethigher/bug-test/tree/vertx-http-dns
     */
    public static void preheatDns(Http http) {
        log.info("preheat dns start");
        long startTimestamp = System.currentTimeMillis();
        HttpClient httpClient = vertx().createHttpClient(new HttpClientOptions().setVerifyHost(false).setTrustAll(true));
        try {
            Set<String> domains = new HashSet<>();
            for (Router router : http.getRouters()) {
                try {
                    String targetUrl = router.getTargetUrl();
                    String host = new URL(targetUrl).getHost();
                    if (IPv4Validator.isIPv4Address(host)) {
                        continue;
                    }
                    domains.add(host);
                } catch (Exception e) {
                }
            }
            CountDownLatch countDownLatch = new CountDownLatch(domains.size());
            log.info("{} domains need to preheat", domains.size());
            for (String domain : domains) {
                Future<HttpClientRequest> requestFuture = httpClient.request(new RequestOptions().setHost(domain).setSsl(false).setMethod(HttpMethod.HEAD));
                requestFuture.compose(HttpClientRequest::send).onSuccess(t -> {
                    log.info("{} preheat dns success", domain);
                    countDownLatch.countDown();
                }).onFailure(throwable -> {
                    log.error("{} preheat dns failure", domain);
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            httpClient.close();
        } catch (Exception e) {
            log.error("preheat dns failure", e);
        } finally {
            httpClient.close();
            log.info("preheat dns end, consumed {} ms", System.currentTimeMillis() - startTimestamp);
        }
    }

}
