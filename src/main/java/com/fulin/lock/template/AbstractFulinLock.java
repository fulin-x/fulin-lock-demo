package com.fulin.lock.template;

/**
 * @Author: Fulin
 * @Description: 锁模板
 * @DateTime: 2025/4/8 下午11:46
 **/
public abstract class AbstractFulinLock {
    public abstract void lock();
    public abstract void unlock();
}
