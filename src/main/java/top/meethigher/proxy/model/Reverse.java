package top.meethigher.proxy.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class Reverse implements Serializable {
    private TcpTunnelClient tcpTunnelClient = new TcpTunnelClient();
    private TcpTunnelServer tcpTunnelServer = new TcpTunnelServer();
    private Tcp tcp = new Tcp();
    private Http http = new Http();
}
