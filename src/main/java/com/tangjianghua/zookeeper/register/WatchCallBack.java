package com.tangjianghua.zookeeper.register;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    private Logger logger = LoggerFactory.getLogger(getClass());

    MyConf myConf;

    ZooKeeper zooKeeper;

    private CountDownLatch countDownLatch;

    private String watchPath;

    public WatchCallBack() {
        countDownLatch = new CountDownLatch(1);
    }

    /**
     * @param myConf    配置文件储存在这里
     * @param zooKeeper 监听的连接
     * @param watchPath 监听的路径
     */
    public WatchCallBack(MyConf myConf, ZooKeeper zooKeeper, String watchPath) {
        this.myConf = myConf;
        this.zooKeeper = zooKeeper;
        this.watchPath = watchPath;
        countDownLatch = new CountDownLatch(1);
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public MyConf getMyConf() {
        return myConf;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    public String getWatchPath() {
        return watchPath;
    }

    public void setWatchPath(String watchPath) {
        this.watchPath = watchPath;
    }

    public void aWait() {
        //监听路径，从watch和callback异步响应
        zooKeeper.exists(watchPath, this, this, "existCtx");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void reAWait() {
        countDownLatch = new CountDownLatch(1);
        aWait();
    }

    /**
     * Watcher
     *
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        logger.debug("wathch回调----" + event.toString());
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                logger.debug("节点被创建，获取数据");
                zooKeeper.getData(watchPath, this, this, "getDataCtx");
                break;
            case NodeDeleted:
                logger.debug("节点被删除，清空数据，重新监听并阻塞");
                //当前阻塞并未放行，重新监听即可
                if (countDownLatch.getCount() != 0) {
                    //监听路径，从watch和callback异步响应
                    zooKeeper.exists(watchPath, this, this, "existCtx");
                } else {
                    //阻塞已放行，重新阻塞
                    myConf.setValue(null);
                }
                break;
            case NodeDataChanged:
                logger.debug("节点数据修改，重新获取数据");
                zooKeeper.getData(watchPath, this, this, "getDataCtx");
                break;
            case NodeChildrenChanged:
                logger.debug("子节点数据修改，暂不处理");
                break;
        }

    }

    /**
     * exit?
     * StatCallBack
     *
     * @param rc
     * @param path
     * @param ctx
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        logger.debug("exit回调------rc--" + rc + ",ctx--" + ctx + ",path--" + path);
        if (stat != null && stat.getDataLength() > 0) {
            //存在路径，存储路径的值
            logger.debug("stat不为空，开始获取数据");
            zooKeeper.getData(path, this, this, "getDataCtx");
        }

    }

    /**
     * DataCallBack
     *
     * @param rc
     * @param path
     * @param ctx
     * @param data
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        logger.debug("getData回调-----DataCallBack,rc--" + rc + ",ctx--" + ctx + ",path--" + path);
        if (data != null) {
            logger.debug("reciveDataCallBack,data: " + new String(data));
            myConf.setValue(new String(data));
            countDownLatch.countDown();
        }
    }
}
