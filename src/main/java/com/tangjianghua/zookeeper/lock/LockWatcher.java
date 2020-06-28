package com.tangjianghua.zookeeper.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class LockWatcher implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback {

    private CountDownLatch countDownLatch;

    private ZooKeeper zooKeeper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String lockName;

    private String threadName;

    public LockWatcher(CountDownLatch countDownLatch, ZooKeeper zooKeeper, String threadName) {
        this.countDownLatch = countDownLatch;
        this.zooKeeper = zooKeeper;
        this.threadName = threadName;
    }

    @Override
    public void process(WatchedEvent event) {
        logger.debug(event.toString());
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zooKeeper.getChildren("/", this, this, lockName);
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }

    }

    public void unlock() {
        try {
            zooKeeper.delete(lockName, -1);
            logger.warn(threadName + "----------释放锁------" + lockName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * StringCallback
     *
     * @param rc
     * @param path
     * @param ctx
     * @param name
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        logger.debug("StringCallback:rc--" + rc + ",path--" + path + ",ctx--" + ctx + ",name=" + name);
        if (name != null) {
            lockName = name;
            zooKeeper.getChildren("/", this, this, name);
        }
    }

    /**
     * Children2Callback
     *
     * @param rc
     * @param path
     * @param ctx
     * @param children
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        logger.debug("StringCallback:rc--" + rc + ",path--" + path + ",ctx--" + ctx);
        Collections.sort(children);
        int i = children.indexOf(ctx.toString().substring(1));
        if (i == 0) {
            countDownLatch.countDown();
            logger.warn(threadName + "----------获取到锁------" + lockName);
        } else {
            try {
                zooKeeper.exists("/" + children.get(--i), this);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
