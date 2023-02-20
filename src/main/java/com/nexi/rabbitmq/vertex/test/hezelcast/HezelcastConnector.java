package com.nexi.rabbitmq.vertex.test.hezelcast;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HezelcastConnector {
    HazelcastInstance hz;
    IMap map;
    public HezelcastConnector(){
        hz = HazelcastClient.newHazelcastClient();
        map = hz.getMap("Nexi-Map-1");
    }
    public void connector(String k, String v) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        // Get the Distributed Map from Cluster.
        //String kS = Integer.toString(k);
        //Standard Put and Get.
        //ContentDoc contentDoc =  new ContentDoc(1, v);
        //map.putIfAbsent(kS, new ContentDoc(1, v));
        //if (k%10000==0) System.out.println("Get kS Message from Hezelcast: " + ((ContentDoc) map.get(kS)).getDoc());
        map.put(k, v);
        //Concurrent Map methods, optimistic updating
        /*map.putIfAbsent("somekey", "somevalue");
        map.replace("key", "value", "newvalue");
        System.out.println(map.get("key"));
        System.out.println(map.get("somekey"));
        map.remove("key");*/
        //map.remove(kS);
        // Shutdown this Hazelcast client
        //hz.shutdown();
    }

    public static void main(String[] args) {
        new HezelcastConnector().connector("1","some text");
    }
}

class ContentDoc {
    public ContentDoc(int status, String doc) {
        this.status = status;
        this.doc = doc;
    }

    int status;
    String doc;

    public int getStatus() {
        return status;
    }

    public String getDoc() {
        return doc;
    }
}