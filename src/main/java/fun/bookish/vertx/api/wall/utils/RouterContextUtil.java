package fun.bookish.vertx.api.wall.utils;

import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

public class RouterContextUtil {

    private RouterContextUtil(){}

    public static String getRequestIp(RoutingContext context){
        String ip = context.request().getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(ip) && !ip.equals("unknown")) {
            return ip;
        }else{
            ip = context.request().getHeader("X-Forwarded-For");
            if (StringUtils.isNotBlank(ip) && !ip.equals("unknown")) {
                int index = ip.indexOf(',');
                ip = index == -1 ? ip : ip.substring(0, ip.indexOf(','));
                return ip;
            }else{
                return context.request().remoteAddress().host();
            }
        }
    }

}
