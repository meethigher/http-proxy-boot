package top.meethigher.proxy.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import top.meethigher.proxy.model.Http;
import top.meethigher.proxy.model.Reverse;
import top.meethigher.proxy.model.Router;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class Utils {


    private static final String CONFIG_FILE_NAME = "application.yml";

    private volatile static Vertx vertx;

    public static Map<String, Object> loadYaml() {
        Yaml yaml = new Yaml();
        // 优先加载外部配置文件，其次加载类路径下配置文件
        String userDirPath = System.getProperty("user.dir").replace("\\", "/");
        File file = new File(userDirPath, CONFIG_FILE_NAME);
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
            if (file.exists()) {
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

            log.info("Logback configuration loaded successfully: " + logConfigFile);
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
        try {
            HttpClient httpClient = vertx().createHttpClient(new HttpClientOptions().setVerifyHost(false).setTrustAll(true));
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
            log.info("preheat dns end");
        }
    }

}
