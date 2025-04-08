package com.fulin.lock;

import com.fulin.lock.template.AbstractFulinLock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author: Fulin
 * @Description: AQS锁实现
 * @DateTime: 2025/4/8 下午11:50
 **/
public class FulinAQSLock extends AbstractFulinLock {
    AtomicBoolean flag = new AtomicBoolean(false);

    Thread owner = null;

    // 对引用的变化进行原子操作
    AtomicReference<Node> head = new AtomicReference<>(new Node());

    AtomicReference<Node> tail = new AtomicReference<>(head.get());

    public void lock() {
        // 此处存在则该锁为非公平锁，此处不存在该锁为公平锁
        if (flag.compareAndSet(false, true)) {
            System.out.println(Thread.currentThread().getName() + "获得锁");
            owner = Thread.currentThread();
            return;
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
            if(current.pre == head.get() && flag.compareAndSet(false,true)){
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

        Node headNode = head.get();
        Node next = headNode.next;
        flag.set(false);
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
