package fun.bookish.vertx.api.wall.core;

import fun.bookish.vertx.api.wall.config.ApiAccessOptions;
import fun.bookish.vertx.api.wall.utils.RouterContextUtil;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ApiAccessHandlerImpl implements ApiAccessHandler {

    private final ApiAccessOptions apiAccessOptions;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAccessHandlerImpl.class);
    private static final Map<String,List<LocalDateTime>> ACCESS_INTERVAL = new ConcurrentHashMap<>();
    private static final Map<String,LocalDateTime> IP_BLACKLIST = new ConcurrentHashMap<>();

    public ApiAccessHandlerImpl(ApiAccessOptions apiAccessOptions) {
        this.apiAccessOptions = apiAccessOptions;
    }

    @Override
    public void handle(RoutingContext context) {
        LocalDateTime now = LocalDateTime.now();
        String ip = RouterContextUtil.getRequestIp(context);
        if(!checkBlackList(ip,now)){
            LOGGER.info(ip + "处于访问黑名单中, 直接拒绝其访问");
            // 如果当前请求的ip已在黑名单中，直接拒绝
            context.response().putHeader("Content-Type", "application/json;charset=UTF-8");
            context.response().setStatusCode(400).end("您的访问过于频繁, 已被服务器限制访问, 请稍后再试");
            return;
        }
        checkAccessInterval(ip,now,context);
    }

    private boolean checkBlackList(String ip, LocalDateTime now) {
        // 当前ip在配置的白名单中，允许访问
        if(apiAccessOptions.getWhiteList() != null && apiAccessOptions.getWhiteList().contains(ip)){
            return true;
        }
        // 当前ip在配置的黑名单中，直接拒绝访问
        if(apiAccessOptions.getBlackList() != null && apiAccessOptions.getBlackList().contains(ip)){
            return false;
        }
        // 当前ip未在黑名单中，允许访问
        if(!IP_BLACKLIST.containsKey(ip)){
            return true;
        }
        LocalDateTime lockedTime = IP_BLACKLIST.get(ip);
        if(now.minus(apiAccessOptions.getIpLockTime(),ChronoUnit.SECONDS).isAfter(lockedTime)){
            // 当前时间减去配置的ip封禁时间，如果最后得到的时间在该ip被封禁的时间之后，说明封禁时间已过，解封该ip
            IP_BLACKLIST.remove(ip);
            return true;
        }
        return false;
    }

    private void checkAccessInterval(String ip, LocalDateTime now, RoutingContext context) {
        List<LocalDateTime> accessTimes = ACCESS_INTERVAL.get(ip);
        if(accessTimes == null){
            List<LocalDateTime> newAccessTimes = new ArrayList<>();
            List<LocalDateTime> putResult = ACCESS_INTERVAL.putIfAbsent(ip, newAccessTimes);
            accessTimes = putResult == null ? newAccessTimes : ACCESS_INTERVAL.get(ip);
        }
        LocalDateTime acceptableFirstAccessTime = now.minus(apiAccessOptions.getAccessInterval(), ChronoUnit.SECONDS);
        List<LocalDateTime> recentlyAccess = accessTimes.stream().filter(item -> item.isAfter(acceptableFirstAccessTime)).collect(Collectors.toList());
        if(recentlyAccess.size() >= apiAccessOptions.getAccessLimit()){
            // 如果当前ip请求频率超过限制，那么将其加入黑名单
            IP_BLACKLIST.put(ip,now);
            LOGGER.info(ip + "访问过于频繁, 暂时加入访问黑名单");
            // 同时删除当前ip之前的访问记录
            ACCESS_INTERVAL.remove(ip);
            context.response().putHeader("Content-Type", "application/json;charset=UTF-8");
            context.response().setStatusCode(400).end("您的访问过于频繁, 已被服务器限制访问, 请稍后再试");
            return;
        }
        accessTimes.add(now);
        context.next();
    }


}
