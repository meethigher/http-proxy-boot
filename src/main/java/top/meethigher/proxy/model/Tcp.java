package top.meethigher.proxy.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Tcp implements Serializable {

    private Boolean enable = false;
    private Integer port = 8090;
    //最大使用的eventloop线程数
    private Integer maxThreads = 1;
    // 目标地址
    private List<String> targets = new ArrayList<String>() {{
        add("127.0.0.1:3306");
        add("127.0.0.1:5432");
        add("127.0.0.1:1521");
    }};

}
