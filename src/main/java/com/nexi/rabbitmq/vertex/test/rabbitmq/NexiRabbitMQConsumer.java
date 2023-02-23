package com.nexi.rabbitmq.vertex.test.rabbitmq;

import com.nexi.rabbitmq.vertex.test.RabbitMQExamples;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class NexiRabbitMQConsumer {
    public static void main(String[] args) {
        new NexiRabbitMQConsumer().createClientWithManualParams(Vertx.vertx());
    }
    public void createClientWithManualParams(Vertx vertx) {
        RabbitMQOptions config = new RabbitMQOptions();
        // Each parameter is optional
        // The default parameter with be used if the parameter is not set
        config.setUser("guest");
        config.setPassword("guest");
        config.setHost("localhost");
        config.setPort(5672);
        config.setVirtualHost("/");
        config.setConnectionTimeout(6000); // in milliseconds
        config.setRequestedHeartbeat(60); // in seconds
        config.setHandshakeTimeout(6000); // in milliseconds
        config.setRequestedChannelMax(5);
        config.setNetworkRecoveryInterval(500); // in milliseconds
        config.setAutomaticRecoveryEnabled(true);

        RabbitMQClient client = RabbitMQClient.create(vertx, config);

        // Connect
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("RabbitMQ successfully connected!");
                //basicPublishWithConfirm(client);
                new RabbitMQExamples().basicConsumer(Vertx.vertx(), client);

            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });


    }

}
