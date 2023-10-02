package team.aphroraletters.gateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

/**
 * 工具类：处理客户端请求
 */
public class RequestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";

    /**
     * 访客IP工具方法
     *
     * @param request
     *
     * @return ip
     */
    public static String getRemoteIp(ServerHttpRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = null;
        List<String> ipRes = request.getHeaders().get("x-forwarded-for");
        if (ipRes == null || ipRes.isEmpty() || UNKNOWN.equalsIgnoreCase(ipRes.get(0))) {
            ipRes = request.getHeaders().get("Proxy-Client-IP");
        }
        if (ipRes == null || ipRes.isEmpty() || UNKNOWN.equalsIgnoreCase(ipRes.get(0))) {
            ipRes = request.getHeaders().get("X-Forwarded-For");
        }
        if (ipRes == null || ipRes.isEmpty() || UNKNOWN.equalsIgnoreCase(ipRes.get(0))) {
            ipRes = request.getHeaders().get("WL-Proxy-Client-IP");
        }
        if (ipRes == null || ipRes.isEmpty() || UNKNOWN.equalsIgnoreCase(ipRes.get(0))) {
            ipRes = request.getHeaders().get("X-Real-IP");
        }
        if (ipRes == null || ipRes.isEmpty() || UNKNOWN.equalsIgnoreCase(ipRes.get(0))) {
            ip = Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
            if (LOCALHOST_IP.equalsIgnoreCase(ip) || LOCALHOST_IPV6.equalsIgnoreCase(ip)) {
                // 根据网卡取本机配置的IP地址:用于将0:0:0:0:0:0:0:1转变为正常的本地IPV4
                /* 当需要记录本机操作时启用 */
                InetAddress iNet = null;
                try {
                    iNet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    LOGGER.error(e.getMessage());
                }
                if (iNet != null) {
                    ip = iNet.getHostAddress();
                }
            }
        } else {
            ip = ipRes.get(0);
        }
        // 对于通过多个代理的情况，分割出第一个 IP
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(SEPARATOR) > 0) {
                ip = ip.substring(0, ip.indexOf(SEPARATOR));
            }
        }
        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IP : ip;
    }

    /**
     * 获取操作系统,浏览器及浏览器版本信息
     *
     * @param request
     * @return
     */
    public static String getOsAndBrowserInfo(ServerHttpRequest request) {
        String userAgent = Objects.requireNonNull(request.getHeaders().get("User-Agent")).get(0);
        String user = userAgent.toLowerCase();

        String os = "";
        String browser = "";

        //=================OS Info=======================
        if (userAgent.toLowerCase().contains("windows")) {
            os = "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            os = "Mac";
        } else if (userAgent.toLowerCase().contains("x11")) {
            os = "Unix";
        } else if (userAgent.toLowerCase().contains("android")) {
            os = "Android";
        } else if (userAgent.toLowerCase().contains("iphone")) {
            os = "IPhone";
        } else {
            os = "UnKnown, More-Info: " + userAgent;
        }
        //===============Browser===========================
        if (user.contains("edge")) {
            browser = (userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("msie")) {
            String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
            browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
        } else {
            String[] split = userAgent.substring(userAgent.indexOf("Version")).split(" ");
            if (user.contains("safari") && user.contains("version")) {
                browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
                        + "-" + (split[0]).split("/")[1];
            } else if (user.contains("opr") || user.contains("opera")) {
                if (user.contains("opera")) {
                    browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
                            + "-" + (split[0]).split("/")[1];
                } else if (user.contains("opr")) {
                    browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
                            .replace("OPR", "Opera");
                }

            } else if (user.contains("chrome")) {
                browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
            } else if ((user.contains("mozilla/7.0")) || (user.contains("netscape6")) ||
                    (user.contains("mozilla/4.7")) || (user.contains("mozilla/4.78")) ||
                    (user.contains("mozilla/4.08")) || (user.contains("mozilla/3"))) {
                browser = "Netscape-?";

            } else if (user.contains("firefox")) {
                browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("rv")) {
                String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
                browser = "IE" + IEVersion.substring(0, IEVersion.length() - 1);
            } else {
                browser = "UnKnown, More-Info: " + userAgent;
            }
        }

        return os + " --- " + browser;
    }
}
