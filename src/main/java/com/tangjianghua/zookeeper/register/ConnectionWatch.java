package com.tangjianghua.zookeeper.register;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class ConnectionWatch implements Watcher {

    private Logger logger = LoggerFactory.getLogger(getClass());

    CountDownLatch countDownLatch;

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getState()) {
            case Disconnected:
                logger.warn("zookeeper Disconnected...");
                countDownLatch = new CountDownLatch(1);
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                logger.warn("zookeeper SyncConnected...");
                countDownLatch.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
        }
    }
}
