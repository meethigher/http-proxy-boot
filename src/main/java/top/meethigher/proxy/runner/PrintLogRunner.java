package top.meethigher.proxy.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PrintLogRunner implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(PrintLogRunner.class);

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
        log.info("\n{}", banner);
    }
}
