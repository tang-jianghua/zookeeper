package com.tangjianghua.zookeeper.register;

import com.tangjianghua.zookeeper.ConnectionFactory;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 配置中心
 * 第一步，创建连接
 * 第二步，监听路径
 *
 * @author tangjianghua
 * @date 2020/6/28
 */
public class ConfigurationCenter {

    private String watchPath;

    private String url;

    private int timeout;

    WatchCallBack watch;
    //放配置数据的地方
    final MyConf myConf = new MyConf();

    private Logger logger = LoggerFactory.getLogger(getClass());


    public ConfigurationCenter(String url, String watchPath, int timeout) {
        this.watchPath = watchPath;
        this.url = url;
        this.timeout = timeout;

        init();
    }

    /**
     * 初始化 建立连接
     */
    private void init() {
        //创建连接
        ZooKeeper zooKeeper = ConnectionFactory.createConnection(url, timeout);

        //监控配置变动
        watch = new WatchCallBack(myConf, zooKeeper, watchPath);
    }


    /**
     * 开始监听
     */
    public void listen() {
        //监听并阻塞
        try {
            watch.aWait();
            while (true) {
                if (myConf.getValue() != null && !myConf.getValue().isEmpty()) {
                    logger.warn(watchPath + " current value: " + myConf.getValue());
                } else {
                    logger.warn("暂无配置");
                    watch.reAWait();
                }
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                watch.getZooKeeper().close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ConfigurationCenter configurationCenter = new ConfigurationCenter("192.168.25.66:2181,192.168.25.67:2181,192.168.25.68:2181/root", "/app1", 50000);
        configurationCenter.listen();
    }
}