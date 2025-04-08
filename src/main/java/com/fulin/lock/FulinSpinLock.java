package com.fulin.lock;

import com.fulin.lock.template.AbstractFulinLock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: Fulin
 * @Description: 自旋锁实现
 * @DateTime: 2025/4/8 下午11:40
 **/
public class FulinSpinLock extends AbstractFulinLock {

    AtomicBoolean flag = new AtomicBoolean(false);

    public void lock() {
        while(true){
            if(flag.compareAndSet(false, true)){
                return;
            }
        }
    }

    public void unlock() {
        while(true){
            if(flag.compareAndSet(true, false)){
                return;
            }
        }
    }
}
