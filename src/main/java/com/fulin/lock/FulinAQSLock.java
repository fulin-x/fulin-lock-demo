package com.fulin.lock;

import com.fulin.lock.template.AbstractFulinLock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author: Fulin
 * @Description: AQS锁实现
 * @DateTime: 2025/4/8 下午11:50
 **/
public class FulinAQSLock extends AbstractFulinLock {
    AtomicInteger state = new AtomicInteger(0);

    Thread owner = null;

    // 对引用的变化进行原子操作
    AtomicReference<Node> head = new AtomicReference<>(new Node());

    AtomicReference<Node> tail = new AtomicReference<>(head.get());

    public void lock() {
        if(state.get() == 0){
            // 此处存在则该锁为非公平锁，此处不存在该锁为公平锁
            if (state.compareAndSet(0, 1)) {
                System.out.println(Thread.currentThread().getName() + "获得锁");
                owner = Thread.currentThread();
                return;
            }
        }else{
            if(owner == Thread.currentThread()){
                state.incrementAndGet();
                System.out.println(Thread.currentThread().getName() + "获得重入锁，重入次数为"+  state.get());
                return;
            }
        }

        Node current = new Node();
        current.thread = Thread.currentThread();
        while (true) {
            Node currentTail = tail.get();
            if (tail.compareAndSet(currentTail, current)) {
                System.out.println(Thread.currentThread().getName() + "加入到链表尾");
                current.pre = currentTail;
                currentTail.next = current;
                break;
            }
        }
        while(true){
            if(current.pre == head.get() && state.compareAndSet(0, 1)){
                owner = Thread.currentThread();
                // 只有持有锁的线程能进入，本就是线程安全的
                head.set(current);
                current.pre.next = null;
                current.pre = null;
                System.out.println(Thread.currentThread().getName() + "被唤醒后，拿到了锁");
                return;
            }
            LockSupport.park();
        }
    }

    public void unlock() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("当前线程不是锁的拥有者，无法解锁！");
        }

        int i = state.get();
        if (i > 1){
            // 因为这里只有同一个线程进行操作，不需要关心线程安全
            state.set(i-1);
            System.out.println(Thread.currentThread().getName() + "解锁重入锁，重入次数为"+  state.get());
            return;
        }

        if(i<=0){
            throw new IllegalStateException("没有重入次数，重入锁解锁错误！");
        }

        Node headNode = head.get();
        Node next = headNode.next;
        state.set(0);
        if(next != null){
            System.out.println(Thread.currentThread().getName() + "释放锁，唤醒下一个线程"+ next.thread.getName());
            LockSupport.unpark(next.thread);
        }
    }

    class Node {
        Node pre;
        Node next;
        Thread thread;
    }
}
