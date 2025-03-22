package top.meethigher.proxy.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class Reverse implements Serializable {
    private Tcp tcp;
    private Http http;

}
