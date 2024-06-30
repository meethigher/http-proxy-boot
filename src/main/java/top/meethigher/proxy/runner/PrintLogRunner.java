package top.meethigher.proxy.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import top.meethigher.proxy.config.ProxyProperties;

import javax.annotation.Resource;

@Component
@ConditionalOnBean(ProxyProperties.class)
public class PrintLogRunner implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(PrintLogRunner.class);

    @Resource
    private ProxyProperties proxyProperties;

    @Override
    public void run(String... args) throws Exception {
        String banner = "    //    / / /__  ___/ /__  ___/ //   ) )         //   ) ) //   ) )  //   ) ) \\\\ / / \\\\    / /         //   ) )  //   ) ) //   ) ) /__  ___/ \n" +
                "   //___ / /    / /       / /    //___/ /         //___/ / //___/ /  //   / /   \\  /   \\\\  / /         //___/ /  //   / / //   / /    / /     \n" +
                "  / ___   /    / /       / /    / ____ /   ____  / ____ / / ___ (   //   / /    / /     \\\\/ /   ____  / __  (   //   / / //   / /    / /      \n" +
                " //    / /    / /       / /    //               //       //   | |  //   / /    / /\\\\     / /         //    ) ) //   / / //   / /    / /       \n" +
                "//    / /    / /       / /    //               //       //    | | ((___/ /    / /  \\\\   / /         //____/ / ((___/ / ((___/ /    / /        \n" +
                "\n" +
                "                                      Source code address is https://github.com/meethigher/http-proxy-boot\n" +
                "                                             Have fun! Author's website is https://meethigher.top \n";
        log.info("all requests to {} will be proxied to {} \n\n{}", proxyProperties.getServletUrl(), proxyProperties.getTargetUrl(), banner);
        log.info("the configuration parameters are as follows {} {}", System.lineSeparator(), new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(proxyProperties.toMap()));


    }
}
