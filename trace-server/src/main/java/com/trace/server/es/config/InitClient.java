package com.trace.server.es.config;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 王柱星
 * @version 1.0
 * @title
 * @time 2018年11月13日
 * @since 1.0
 */
public class InitClient {
    public final static String HOST = "172.22.51.117";
    // http请求的端口是9200，客户端是9300
    public final static int PORT = 9300;

    /**
     * getConnection:(获取连接).
     *
     * @throws Exception
     * @author xbq Date:2018年3月21日下午4:03:32
     */
    public static TransportClient getClient() throws UnknownHostException {

        Settings settings = Settings.builder()
                .put("client.transport.sniff", false).build();

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(HOST), PORT));
        return client;

    }

    public static RestHighLevelClient getHighClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(HOST, 9200, "http")));
        return client;
    }


    public static void main(String[] args) throws UnknownHostException {
        InitClient.getClient();
    }
}
