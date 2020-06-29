package com.tangjianghua.zookeeper.register;

/**
 * @author tangjianghua
 * @date 2020/6/28
 */
public class MyConf {

    private volatile String value;

    public String getValue() {
        return value;
    }

    public synchronized void setValue(String value) {
        this.value = value;
    }
}
