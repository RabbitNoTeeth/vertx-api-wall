package fun.bookish.vertx.api.wall.config;

import java.util.List;

public class ApiAccessOptions {

    private long accessInterval = 60;
    private int accessLimit = 60;
    private long ipLockTime = 60 * 2;
    private List<String> whiteList;
    private List<String> blackList;

    public long getAccessInterval() {
        return accessInterval;
    }

    public void setAccessInterval(long accessInterval) {
        this.accessInterval = accessInterval;
    }

    public int getAccessLimit() {
        return accessLimit;
    }

    public void setAccessLimit(int accessLimit) {
        this.accessLimit = accessLimit;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }

    public long getIpLockTime() {
        return ipLockTime;
    }

    public void setIpLockTime(long ipLockTime) {
        this.ipLockTime = ipLockTime;
    }
}
