package top.meethigher.proxy.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import top.meethigher.proxy.model.Reverse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@Slf4j
public class Utils {


    private static final String CONFIG_FILE_NAME = "application.yml";

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
        return Vertx.vertx(new VertxOptions().setUseDaemonThread(false));
    }

}
