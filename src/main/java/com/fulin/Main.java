package com.fulin;

import com.fulin.lock.FulinSpinLock;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Fulin
 * @Description: 主函数
 * @DateTime: 2025/4/8 下午11:29
 **/
public class Main {
    public static void main(String[] args) throws InterruptedException{
        int[] count = new int[]{1000};
        List<Thread> threadList = new ArrayList<>();

        FulinSpinLock lock = new FulinSpinLock();
        for(int i = 0; i < 100; i++){
            threadList.add(new Thread(()->{
                lock.lock();
                for(int j = 0; j < 10; j++){
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e){
                        throw new RuntimeException(e);
                    }
                    count[0]--;
                }
                lock.unlock();
            }));
        }

        for(Thread thread : threadList){
            thread.start();
        }

        for(Thread thread : threadList){
            thread.join();
        }

        System.out.println(count[0]);
    }
}