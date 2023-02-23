package com.nexi.rabbitmq.vertex.test.smphr;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class Smphr implements Runnable{
    Semaphore smp = new Semaphore(10);
    int j;

    public Smphr(){
        IntStream.range(0, 100000).forEach(i -> {
            try {
                System.out.print("Acquiring %s - ".formatted(i));
                smp.acquire();
                j = i;
                new Thread(this).start();
                System.out.print("Release %s - Printed: ".formatted(i));
                smp.release();
                //Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }
    /*

     */
    public static void main(String[] args) {
        new Smphr();
    }

    @Override
    public void run() {
        System.out.println("Some " + j);
    }
}
