package top.meethigher.proxy.model;


public class Reverse {
    public TcpMuxClient tcpMuxClient = new TcpMuxClient();
    public TcpMuxServer tcpMuxServer = new TcpMuxServer();
    public TcpTunnelClient tcpTunnelClient = new TcpTunnelClient();
    public TcpTunnelServer tcpTunnelServer = new TcpTunnelServer();
    public Tcp tcp = new Tcp();
    public Http http = new Http();
}
