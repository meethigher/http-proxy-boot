package top.meethigher.proxy.utils;


import org.junit.Assert;
import org.junit.Test;
import top.meethigher.proxy.model.Reverse;

import java.util.Map;

public class UtilsTest {


    @Test
    public void loadYaml() {
        Map<String, Object> map = Utils.loadYaml();
        Assert.assertNotNull(map);
    }

    @Test
    public void loadApplicationConfig() {
        Reverse reverse = Utils.loadApplicationConfig();
        Assert.assertNotNull(reverse);
    }

    @Test
    public void loadLogConfig() {

    }

    @Test
    public void vertx() {
    }
}