package com.ssl.t1;

import com.rabbitmq.client.*;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by shi_senlin on 2017/6/28.
 */
public class MulThreadConsumerTest {
    private final static String QUEUE_NAME = "uid";
    private Connection connection;

    private void setConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void consumer() throws Exception {
        setConnection();


        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(1);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                channel.basicAck(envelope.getDeliveryTag(), false);
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }


//多线程测试

    @Test
    public void testMulThread() throws Throwable {
        long startTime = System.currentTimeMillis();
        TestRunnable[] testRunnable = new TestRunnable[100];
        for (int i = 0; i < testRunnable.length; i++) {
            testRunnable[i] = new SingleThread();
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(testRunnable);
        try {
            mttr.runTestRunnables();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("总共耗时：" + (endTime - startTime) + "ms");
        Thread.sleep(5000);
    }

    private class SingleThread extends TestRunnable {

        /*
         * (non-Javadoc)
         *
         * @see net.sourceforge.groboutils.junit.v1.TestRunnable#runTest()
         */
        @Override
        public void runTest() throws Throwable {
            consumer();
        }

    }
}
