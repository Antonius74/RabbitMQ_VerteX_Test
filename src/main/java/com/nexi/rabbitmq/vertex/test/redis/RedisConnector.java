package com.nexi.rabbitmq.vertex.test.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisConnector {
    private JedisPool pool;
    private Jedis jedis;

    public RedisConnector() {
        pool = new JedisPool("localhost", 6379);
        jedis = pool.getResource();
        System.out.println("connect to Redis " + jedis.memoryStats());

    }

    public void set(String k, String v) {
        jedis.set(k, v);
    }

    public String get(String k) {
        return jedis.get(k);
    }

    public void del(String k) {
        jedis.del(k);
    }

    public static void main(String[] args) {
        RedisConnector jpm = new RedisConnector();
        jpm.set("key", "value  1");
        System.out.println(jpm.get("key"));
    }
}
