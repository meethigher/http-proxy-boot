package top.meethigher.proxy;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.vertx.core.Vertx;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.meethigher.proxy.tcp.ReverseTcpProxy;

import java.net.URL;
import java.util.Scanner;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        String model = System.getProperty("proxy.mode", "http");
        if (!"http".equalsIgnoreCase(model)) {
            loadConfigFromClasspath("log/logback.xml");
            System.out.println("The mode you are using now is TCP Reverse Proxy");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the target host: ");
            String targetHost = scanner.next();
            System.out.print("Enter the target port: ");
            int targetPort = scanner.nextInt();
            System.out.print("Enter the listening port: ");
            int port = scanner.nextInt();
            ReverseTcpProxy.create(Vertx.vertx(), targetHost, targetPort)
                    .port(port)
                    .start();

        } else {
            SpringApplication.run(App.class, args);
        }
    }


    public static void loadConfigFromClasspath(String classpathConfigFile) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();  // 先重置当前的 Logback 配置
        context.putProperty("test", "logs");
        try {
            // 获取 classpath 下的配置文件
            URL configFile = App.class.getClassLoader().getResource(classpathConfigFile);
            if (configFile == null) {
                throw new IllegalArgumentException("Logback configuration file not found: " + classpathConfigFile);
            }

            // 使用 JoranConfigurator 加载配置
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(configFile);

            System.out.println("Logback configuration loaded successfully: " + classpathConfigFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Logback configuration", e);
        }
    }
}
