package com.nexi.rabbitmq.vertex.test;

import com.mongodb.DB;
import com.nexi.rabbitmq.vertex.test.hezelcast.HezelcastConnector;
import com.nexi.rabbitmq.vertex.test.mongodb.MongoDBTest;
import com.nexi.rabbitmq.vertex.test.redis.RedisConnector;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.rabbitmq.RabbitMQPublisher;
import io.vertx.rabbitmq.RabbitMQPublisherOptions;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RabbitMQExamples {
    MongoDBTest mongoDBTest = new MongoDBTest();
    DB db;
    RedisConnector redisConnector;
    public RabbitMQExamples(){
        DB db = mongoDBTest.getConnection();
        redisConnector = new RedisConnector();
    }
    public static void main(String[] args) {
        //new RabbitMQExamples().createClientWithManualParams(Vertx.vertx());
        new RabbitMQExamples().createClientWithMultipleHost(Vertx.vertx());
    }

    public void createClientWithUri(Vertx vertx) {
        RabbitMQOptions config = new RabbitMQOptions();
        // full amqp uri
        config.setUri("amqp://xvjvsrrc:VbuL1atClKt7zVNQha0bnnScbNvGiqgb@moose.rmq.cloudamqp.com/xvjvsrrc");
        RabbitMQClient client = RabbitMQClient.create(vertx, config);

        // Connect
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("RabbitMQ successfully connected!");
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
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
        config.setConnectionTimeout(6000);
        config.setRequestedHeartbeat(60);
        config.setHandshakeTimeout(6000);
        config.setRequestedChannelMax(5);
        config.setNetworkRecoveryInterval(500);
        config.setAutomaticRecoveryEnabled(true);

        RabbitMQClient client = RabbitMQClient.create(vertx, config);

        // Connect
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("RabbitMQ successfully connected!");
                //basicPublish(client);
                for (int i=0; i<1; i++){
                    basicPublishWithConfirm(client, 10000);
                }
                //basicConsumer(Vertx.vertx(), client);
            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });


    }

    //pass multiple hosts to client, allow to use a clustered RabbitMQ
    public void createClientWithMultipleHost(Vertx vertx) {
        RabbitMQOptions config = new RabbitMQOptions();
        config.setUser("guest");
        config.setPassword("guest");
        config.setVirtualHost("/");

        config.setAddresses(Arrays.asList(Address.parseAddresses("localhost:5672")));

        RabbitMQClient client = RabbitMQClient.create(vertx, config);

        // Connect
        client.start(asyncResult -> {
            if (asyncResult.succeeded()) {
                System.out.println("RabbitMQ successfully connected!");
                basicPublish(client);

            } else {
                System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
            }
        });
    }

    public void basicPublish(RabbitMQClient client) {
        AtomicLong start = new AtomicLong(new Date().getTime());
        HezelcastConnector hezelcastConnector = new HezelcastConnector();
        for (int i = 0; i<900000; i++) {
            Buffer message = Buffer.buffer("Nexi text message " + i);
            int finalI = i;
            client.basicPublish("NexiExchange", "", message, pubResult -> {
                if (pubResult.succeeded()) {
                    //hezelcastConnector.connector(String.valueOf(finalI), message.toString());
                    redisConnector.set(String.valueOf(finalI), message.toString());
                    //System.out.println(redisConnector.get(String.valueOf(finalI)));
                    //redisConnector.del(String.valueOf(finalI));

                    //This method returns the time in millis
                    //System.out.println(start);
                    //System.out.println(start);
                    if (finalI%10000==0){
                        System.out.println("Published " + finalI + " messages " + message.toString() + " in " + (new Date().getTime() - start.get()));
                        start.set(new Date().getTime());
                    }
                } else {
                    pubResult.cause().printStackTrace();
                }
            });
        }
    }

    public void basicPublishWithConfirm(RabbitMQClient client, int i) {
            String message = ("Nexi Test Message ");
            // Put the channel in confirm mode. This can be done once at init.
        for (int j=0; j<i; j++) {
            int finalJ = j;
            client.confirmSelect(confirmResult -> {
                if (confirmResult.succeeded()) {
                    client.basicPublish("NexiExchange", "", Buffer.buffer(message + (finalJ)), pubResult -> {
                        if (pubResult.succeeded()) {
                            // Check the message got confirmed by the broker.
                            client.waitForConfirms(waitResult -> {
                                if (waitResult.succeeded()) {
                                    System.out.println("Published " + finalJ + " messages");
                                } else {
                                    waitResult.cause().printStackTrace();
                                }
                            });
                        } else {
                            pubResult.cause().printStackTrace();
                        }
                    });
                } else {
                    confirmResult.cause().printStackTrace();
                }
            });
        }
    }

    public void basicConsumer(Vertx vertx, RabbitMQClient client) {
        client.basicConsumer("VertexQueue", rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer created !");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    //System.out.println("Got message: " + message.body().toString());
                    //System.out.println("written msg " + message.body().toString());
                    mongoDBTest.MongoTestWrite("1", message.body().toString());
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });
    }

    public void basicConsumerOptions(Vertx vertx, RabbitMQClient client) {
        QueueOptions options = new QueueOptions()
                .setMaxInternalQueueSize(1000)
                .setKeepMostRecent(true);

        client.basicConsumer("my.queue", options, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer created !");
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });
    }

    public void pauseAndResumeConsumer(RabbitMQConsumer consumer){
        consumer.pause();
        consumer.resume();
    }

    public void endHandlerConsumer(RabbitMQConsumer rabbitMQConsumer) {
        rabbitMQConsumer.endHandler(v -> {
            System.out.println("It is the end of the stream");
        });
    }

    public void cancelConsumer(RabbitMQConsumer rabbitMQConsumer) {
        rabbitMQConsumer.cancel(cancelResult -> {
            if (cancelResult.succeeded()) {
                System.out.println("Consumption successfully stopped");
            } else {
                System.out.println("Tired in attempt to stop consumption");
                cancelResult.cause().printStackTrace();
            }
        });
    }

    public void exceptionHandler(RabbitMQConsumer consumer) {
        consumer.exceptionHandler(e -> {
            System.out.println("An exception occurred in the process of message handling");
            e.printStackTrace();
        });
    }

    public void consumerTag(RabbitMQConsumer consumer) {
        String consumerTag = consumer.consumerTag();
        System.out.println("Consumer tag is: " + consumerTag);
    }

    public void getMessage(RabbitMQClient client) {
        client.basicGet("my.queue", true, getResult -> {
            if (getResult.succeeded()) {
                RabbitMQMessage msg = getResult.result();
                System.out.println("Got message: " + msg.body());
            } else {
                getResult.cause().printStackTrace();
            }
        });
    }

    //pass the additional config for the exchange as JSON, check RabbitMQ documentation for specific config parameters
    public void exchangeDeclareWithConfig(RabbitMQClient client) {

        JsonObject config = new JsonObject();

        config.put("x-dead-letter-exchange", "my.deadletter.exchange");
        config.put("alternate-exchange", "my.alternate.exchange");
        // ...
        client.exchangeDeclare("my.exchange", "fanout", true, false, config, onResult -> {
            if (onResult.succeeded()) {
                System.out.println("Exchange successfully declared with config");
            } else {
                onResult.cause().printStackTrace();
            }
        });
    }

    public void consumeWithManualAck(Vertx vertx, RabbitMQClient client) {
        // Setup the rabbitmq consumer
        client.basicConsumer("my.queue", new QueueOptions().setAutoAck(false), consumeResult -> {
            if (consumeResult.succeeded()) {
                System.out.println("RabbitMQ consumer created !");
                RabbitMQConsumer consumer = consumeResult.result();

                // Set the handler which messages will be sent to
                consumer.handler(msg -> {
                    JsonObject json = (JsonObject) msg.body();
                    System.out.println("Got message: " + json.getString("body"));
                    // ack
                    client.basicAck(json.getLong("deliveryTag"), false, asyncResult -> {
                    });
                });
            } else {
                consumeResult.cause().printStackTrace();
            }
        });
    }

    //pass the additional config for the queue as JSON, check RabbitMQ documentation for specific config parameters
    public void queueDeclareWithConfig(RabbitMQClient client) {
        JsonObject config = new JsonObject();
        config.put("x-message-ttl", 10_000L);

        client.queueDeclare("my-queue", true, false, true, config, queueResult -> {
            if (queueResult.succeeded()) {
                System.out.println("Queue declared!");
            } else {
                System.err.println("Queue failed to be declared!");
                queueResult.cause().printStackTrace();
            }
        });
    }

    // Use the connectionEstablishedCallback to declare an Exchange
    public void connectionEstablishedCallback(Vertx vertx, RabbitMQOptions config) {
        RabbitMQClient client = RabbitMQClient.create(vertx, config);
        client.addConnectionEstablishedCallback(promise -> {
            client.exchangeDeclare("exchange", "fanout", true, false)
                    .compose(v -> {
                        return client.queueDeclare("queue", false, true, true);
                    })
                    .compose(declareOk -> {
                        return client.queueBind(declareOk.getQueue(), "exchange", "");
                    })
                    .onComplete(promise);
        });

        // At this point the exchange, queue and binding will have been declared even if the client connects to a new server
        client.basicConsumer("queue", rabbitMQConsumerAsyncResult -> {
        });
    }

    public void connectionEstablishedCallbackForServerNamedAutoDeleteQueue(Vertx vertx, RabbitMQOptions config) {
        RabbitMQClient client = RabbitMQClient.create(vertx, config);
        AtomicReference<RabbitMQConsumer> consumer = new AtomicReference<>();
        AtomicReference<String> queueName = new AtomicReference<>();
        client.addConnectionEstablishedCallback(promise -> {
            client.exchangeDeclare("exchange", "fanout", true, false)
                    .compose(v -> client.queueDeclare("", false, true, true))
                    .compose(dok -> {
                        queueName.set(dok.getQueue());
                        // The first time this runs there will be no existing consumer
                        // on subsequent connections the consumer needs to be update with the new queue name
                        RabbitMQConsumer currentConsumer = consumer.get();
                        if (currentConsumer != null) {
                            currentConsumer.setQueueName(queueName.get());
                        }
                        return client.queueBind(queueName.get(), "exchange", "");
                    })
                    .onComplete(promise);
        });

        client.start()
                .onSuccess(v -> {
                    // At this point the exchange, queue and binding will have been declared even if the client connects to a new server
                    client.basicConsumer(queueName.get(), rabbitMQConsumerAsyncResult -> {
                        if (rabbitMQConsumerAsyncResult.succeeded()) {
                            consumer.set(rabbitMQConsumerAsyncResult.result());
                        }
                    });
                })
                .onFailure(ex -> {
                    System.out.println("It went wrong: " + ex.getMessage());
                });
    }

    public void rabbitMqPublisher(Vertx vertx, RabbitMQClient client, RabbitMQPublisherOptions options, Map<String, JsonObject> messages) {

        RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx, client, options);

        messages.forEach((k,v) -> {
            com.rabbitmq.client.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .messageId(k)
                    .build();
            publisher.publish("exchange", "routingKey", properties, v.toBuffer());
        });

        publisher.getConfirmationStream().handler(conf -> {
            if (conf.isSucceeded()) {
                messages.remove(conf.getMessageId());
            }
        });

        // messages should eventually be empty

    }


}

class test{
    public static void main(String[] args) {

    }
}