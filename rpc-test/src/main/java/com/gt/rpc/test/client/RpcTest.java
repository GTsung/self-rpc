package com.gt.rpc.test.client;

import com.gt.rpc.client.RpcClient;
import com.gt.rpc.test.service.HelloService;

import java.util.concurrent.CountDownLatch;

/**
 * @author GTsung
 * @date 2022/1/21
 */
public class RpcTest {

    public static void main(String[] args) throws InterruptedException {

        final RpcClient rpcClient = new RpcClient("192.168.11.1:2181");

        int threadNum = 1;
        final int requestNum = 50;
        Thread[] threads = new Thread[threadNum];

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadNum; i++) {
            threads[i]= new Thread(() -> {
                for (int j = 0; j < requestNum; j++) {
                    try {
                        final HelloService syncClient = rpcClient.createService(HelloService.class, "1.0");
                        String result = syncClient.hello(Integer.toString(j));
                        if (!result.equals("Hello " + j)) {
                            System.out.println("error = " + result);
                        } else {
                            System.out.println("result = " + result);
                        }
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
                countDownLatch.countDown();
            });
            threads[i].start();
        }
        countDownLatch.await();
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Sync call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();
    }
}
