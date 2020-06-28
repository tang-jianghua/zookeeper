package com.tangjianghua.zookeeper.lock;

import com.tangjianghua.zookeeper.ConnectionFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class ZkLock implements Lock {

    private ZooKeeper zooKeeper;

    private String path;

    private static final byte[] LOCK = "lock".getBytes();

    private CountDownLatch countDownLatch;

    private LockWatcher lockWatcher;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ZkLock(ZooKeeper zooKeeper, String path) {
        this.zooKeeper = zooKeeper;
        this.path = path;
    }

    @Override
    public void lock() {
        countDownLatch = new CountDownLatch(1);
        lockWatcher = new LockWatcher(countDownLatch, zooKeeper, Thread.currentThread().getName());
        zooKeeper.create(path, LOCK, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, lockWatcher, "lockCtx");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        lockWatcher.unlock();
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                ZkLock zkLock = new ZkLock(ConnectionFactory.createConnection("192.168.25.66:2181,192.168.25.67:2181,192.168.25.68:2181/root", 600000), "/lock");
                zkLock.lock();
                try {
                    TimeUnit.SECONDS.sleep(2L);
                    zkLock.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
