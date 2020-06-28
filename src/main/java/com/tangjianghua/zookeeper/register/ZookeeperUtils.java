package com.tangjianghua.zookeeper.register;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class ZookeeperUtils {

    static ZooKeeper zk;

    private static final String url="192.168.25.66:2181,192.168.25.67:2181,192.168.25.68:2181/root";

    public static ZooKeeper getZooKeeper(){
        WatchCallBack watchCallBack = new WatchCallBack();
        try {
            zk=new ZooKeeper(url,3000, watchCallBack);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zk;
    }


    public static void close(ZooKeeper zk){
        if(zk!=null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
