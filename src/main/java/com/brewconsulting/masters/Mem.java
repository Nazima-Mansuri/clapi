package com.brewconsulting.masters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Future;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

public class Mem {

//    static String configEndpoint = "rollacache.wvhp0d.cfg.use1.cache.amazonaws.com";
//    static Integer clusterPort = 11211;
    static String node1 = "localhost";
    static Integer port = 11211;
    static MemcachedClient client = null;

      public static boolean getToken(String key,String value) throws IOException {

//          client = new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
          client = new MemcachedClient(new InetSocketAddress(node1, port));


          Object myObject = client.get(key);
          if (client == null) { // the object does not exist
              return false;
          } else {
              if (myObject == value) {
                  return true;
              } else {
                  return false;
              }
          }
      }

    public static boolean getData(String key) throws IOException {

//        client =  new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
        client =  new MemcachedClient(new InetSocketAddress(node1, port));


        Object myObject = client.get(key);
        if(client == null)
        { // the object does not exist
            return false;
        }
        else
        {
            return  true;

        }


//		List<InetSocketAddress> cluster = new ArrayList<InetSocketAddress>();
//		cluster.add(new InetSocketAddress(node1, port));
//		ConnectionFactory cf = new DefaultConnectionFactory(ClientMode.Static);
//		MemcachedClient client = new MemcachedClient(cf, cluster);
//
//		// Store a data item for an hour. The client will decide which cache host will store this item.


//		{"username":"lanet@rolla.com","password":"lanet2016","isPublic":false}
//		{"accessToken":"eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0NzYwMjA4MjUsImlzcyI6ImJyZXdjb25zdWx0aW5nLmNvbSIsInN1YiI6ImxhbmV0QHJvbGxhLmNvbSIsImp0aSI6IjYwOWQ5MTQ2LTVlY2MtNGM5Ni1iOTkyLTcyZmE5ZmNjZWY2ZSIsImV4cCI6MTQ3NjAzMjgyNSwidXNlciI6IntcImNsaWVudElkXCI6MixcImlkXCI6MixcInVzZXJuYW1lXCI6XCJsYW5ldEByb2xsYS5jb21cIixcInNjaGVtYU5hbWVcIjpcImNsaWVudDFcIixcImZpcnN0TmFtZVwiOlwicG9vamFcIixcImxhc3ROYW1lXCI6XCJzaGFoXCIsXCJyb2xlc1wiOlt7XCJyb2xlaWRcIjoyLFwicm9sZW5hbWVcIjpcIk1BUktFVElOR1wifV0sXCJkZXNpZ25hdGlvblwiOm51bGx9IiwidG9rZW5UeXBlIjoiQUNDRVNTIn0.6dUcTeGAXQOT1cwzYBflBtYIsN-lZri15O0W-nZ2_q0","refreshToken":"eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE0NzYwMjA4MjUsImlzcyI6ImJyZXdjb25zdWx0aW5nLmNvbSIsInN1YiI6ImxhbmV0QHJvbGxhLmNvbSIsImp0aSI6IjFhNDJlYTlmLTNkNGUtNGUzZC1iODcwLWM4OTVjOWJmYjZkOCIsImV4cCI6MTQ3Njc0MDgyNSwidXNlciI6IntcImNsaWVudElkXCI6MixcImlkXCI6MixcInVzZXJuYW1lXCI6XCJsYW5ldEByb2xsYS5jb21cIixcInNjaGVtYU5hbWVcIjpcImNsaWVudDFcIixcImZpcnN0TmFtZVwiOlwicG9vamFcIixcImxhc3ROYW1lXCI6XCJzaGFoXCIsXCJyb2xlc1wiOlt7XCJyb2xlaWRcIjoyLFwicm9sZW5hbWVcIjpcIk1BUktFVElOR1wifV0sXCJkZXNpZ25hdGlvblwiOm51bGx9IiwidG9rZW5UeXBlIjoiUkVGUkVTSCJ9.9tD54dWR1P8j42yBy_UriWs__ku9VimanTL2lnnsIT0"}

    }

    public static void setData(String key, int time) throws IOException {
//        client =  new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
        client =  new MemcachedClient(new InetSocketAddress(node1, port));
        client.set(key, time, "");
        client.shutdown();
    }

    public static void deleteData(String key) throws IOException {
//        client =  new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
        client =  new MemcachedClient(new InetSocketAddress(node1, port));
        if(client.get(key)!=null) {
          client.delete(key);
        }
    }
}