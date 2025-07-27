package top.meethigher.proxy.utils;

import java.util.regex.Pattern;

public class IPv4Validator {
    // 预编译正则表达式，提升性能
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    /**
     * 判断输入字符串是否为合法的 IPv4 地址
     * @param addr 可能包含端口号或方括号的地址（如 "192.168.1.1"、"192.168.1.1:8080"、"[192.168.1.1]"）
     * @return true=是合法IPv4，false=不是
     */
    public static boolean isIPv4Address(String addr) {
        if (addr == null || addr.isEmpty()) {
            return false;
        }

        // 1. 去除方括号（兼容 "[192.168.1.1]" 格式）
        String stripped = addr.replaceAll("[\\[\\]]", "");

        // 2. 分离地址和端口（兼容 "192.168.1.1:8080" 格式）
        String[] parts = stripped.split(":");
        String ipPart = parts[0]; // 取冒号前的部分

        // 3. 正则匹配IPv4格式
        return IPV4_PATTERN.matcher(ipPart).matches();
    }

    // 测试用例
    public static void main(String[] args) {
        System.out.println(isIPv4Address("192.168.1.1"));         // true
        System.out.println(isIPv4Address("255.255.255.255"));     // true
        System.out.println(isIPv4Address("0.0.0.0"));             // true
        System.out.println(isIPv4Address("192.168.1.1:8080"));    // true
        System.out.println(isIPv4Address("[192.168.1.1]"));        // true
        System.out.println(isIPv4Address("256.0.0.1"));           // false
        System.out.println(isIPv4Address("192.168.1"));           // false
        System.out.println(isIPv4Address("2001:db8::1"));         // false (IPv6)
        System.out.println(isIPv4Address("abc.def.ghi.jkl"));     // false
        System.out.println(isIPv4Address(null));                  // false
        System.out.println(isIPv4Address("reqres.in"));                  // false
    }
}