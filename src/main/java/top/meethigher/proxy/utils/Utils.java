package top.meethigher.proxy.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import top.meethigher.proxy.NetAddress;
import top.meethigher.proxy.RoundRobinLoadBalancer;
import top.meethigher.proxy.model.*;
import top.meethigher.proxy.tcp.mux.model.MuxNetAddress;
import top.meethigher.proxy.tcp.tunnel.ReverseTcpProxyTunnelServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Utils {


    private static final String CONFIG_FILE_NAME = "application.yml";

    private static final String CONFIG_EXAMPLE_FILE_NAME = "application-full.yml";

    private volatile static Vertx vertx;


    public static void registerReverseTcpProxyMuxClient(TcpMuxClient muxClient) {
        Map<MuxNetAddress, NetAddress> map = new LinkedHashMap<>();
        try {
            for (String service : muxClient.services) {
                String[] arr = service.split("-");
                NetAddress local = NetAddress.parse(arr[1]);
                MuxNetAddress muxNetAddress = new MuxNetAddress(local.getHost(), local.getPort(), arr[0]);
                NetAddress backend = NetAddress.parse(arr[2]);
                map.put(muxNetAddress, backend);
            }
        } catch (Exception e) {
            log.error("parsing from {} occurred exception", CONFIG_FILE_NAME, e);
            System.exit(1);
        }
        for (int i = 0; i < muxClient.maxThreads; i++) {
            vertx().deployVerticle(new ReverseTcpProxyMuxClientVerticle(muxClient, map)).onFailure(e -> {
                log.error("deploy tcp mux client failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseTcpProxyMuxServer(TcpMuxServer muxServer) {
        for (int i = 0; i < muxServer.maxThreads; i++) {
            vertx().deployVerticle(new ReverseTcpProxyMuxServerVerticle(muxServer)).onFailure(e -> {
                log.error("deploy tcp mux server failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseHttpProxy(Http http) {
        for (int i = 0; i < http.maxThreads; i++) {
            vertx().deployVerticle(new ReverseHttpProxyVerticle(http)).onFailure(e -> {
                log.error("deploy reverse http proxy failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseUdpProxy(Udp udp) {
        List<String> targets = udp.targets;
        List<NetAddress> nodes = new ArrayList<>();
        for (String target : targets) {
            try {
                String[] addr = target.split(":");
                nodes.add(new NetAddress(addr[0], Integer.parseInt(addr[1])));
            } catch (Exception e) {
                log.error("udp targes format is incorrect. The correct format is host:port.");
                System.exit(1);
            }
        }
        RoundRobinLoadBalancer lb = RoundRobinLoadBalancer.create(nodes);
        for (int i = 0; i < udp.maxThreads; i++) {
            vertx().deployVerticle(new ReverseUdpProxyVerticle(lb, udp)).onFailure(e -> {
                log.error("deploy reverse udp proxy failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseTcpProxy(Tcp tcp) {
        List<String> targets = tcp.targets;
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
        RoundRobinLoadBalancer lb = RoundRobinLoadBalancer.create(nodes);
        for (int i = 0; i < tcp.maxThreads; i++) {
            vertx().deployVerticle(new ReverseTcpProxyVerticle(lb, tcp)).onFailure(e -> {
                log.error("deploy reverse tcp proxy failed", e);
                System.exit(1);
            });
        }
    }

    public static void registerReverseTcpProxyTunnelClient(TcpTunnelClient tc) {
        vertx().deployVerticle(new ReverseTcpProxyTunnelClientVerticle(tc)).onFailure(e -> {
            log.error("deplay reverse tcp tunnel client failed", e);
            System.exit(1);
        });
    }

    public static void registerReverseTcpProxyTunnelServer(TcpTunnelServer server) {
        final ConcurrentHashMap<NetSocket, ReverseTcpProxyTunnelServer.DataProxyServer> authedSockets = new ConcurrentHashMap<>();
        for (int i = 0; i < server.maxThreads; i++) {
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
                                    // 该参数默认是永不刷新，也就是hosts内容，程序缓存后永不更新。此处配置每5秒更新
                                    // @see https://github.com/eclipse-vertx/vert.x/pull/4843
                                    .setHostsRefreshPeriod(5)
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

}
