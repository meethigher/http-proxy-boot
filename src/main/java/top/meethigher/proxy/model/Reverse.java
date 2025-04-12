package top.meethigher.proxy.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class Reverse implements Serializable {
    private TunnelClient tunnelClient = new TunnelClient();
    private TunnelServer tunnelServer = new TunnelServer();
    private Tcp tcp = new Tcp();
    private Http http = new Http();
}
