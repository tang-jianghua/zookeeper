package com.tangjianghua.zookeeper;

import com.tangjianghua.zookeeper.register.ConnectionWatch;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class ConnectionFactory {

    /**
     * 创建连接
     * @param url
     * @param timeout
     * @return
     */
    public static ZooKeeper createConnection(String url,int timeout){
        ZooKeeper zooKeeper = null;
        try {
            //创建连接，等待连接建立完成后在继续
            CountDownLatch countDownLatch = new CountDownLatch(1);
            //在Watch中监听连接状态，当状态建立完成后countDownLatch.countDown()
            ConnectionWatch connectionWatch = new ConnectionWatch();
            connectionWatch.setCountDownLatch(countDownLatch);
            zooKeeper = new ZooKeeper(url, timeout, connectionWatch);
            //连接建立完成前阻塞当前线程
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }
}

