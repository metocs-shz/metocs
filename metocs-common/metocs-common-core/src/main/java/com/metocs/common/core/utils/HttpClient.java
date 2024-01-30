package com.metocs.common.core.utils;

import com.alibaba.fastjson2.JSON;
import com.metocs.common.core.response.ResponseData;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);


    public static String doGet(String url) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(3000)
                .setSocketTimeout(3000).setConnectTimeout(3000).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            String s = EntityUtils.toString(responseEntity);
            ResponseData responseData = JSON.parseObject(s, ResponseData.class);
            return responseData.getData().toString();
        } catch (IOException e) {
            logger.error("请求客户端出现错误: {}",e.getMessage(),e);
        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("请求客户端出现错误: {}",e.getMessage(),e);
            }
        }
        return "{}";
    }

    public static String doPost(String url, Map<String,String> data) {
        return doPost(url,data,null);
    }


    public static String doPost(String url, Map<String,String> data,Map<String,String> headers) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");

        if (headers!=null && !headers.isEmpty()){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }

        if (data!=null && !data.isEmpty()){
            String charSet = "UTF-8";
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(3000)
                    .setSocketTimeout(3000).setConnectTimeout(3000).build();
            httpPost.setConfig(requestConfig);
            StringEntity entity = new StringEntity(JSON.toJSONString(data), charSet);
            httpPost.setEntity(entity);

        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            String s = EntityUtils.toString(response.getEntity());
            ResponseData responseData = JSON.parseObject(s, ResponseData.class);
            return responseData.getData().toString();
        } catch (IOException e) {
            logger.error("请求客户端出现错误: {}",e.getMessage(),e);
        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("请求客户端出现错误: {}",e.getMessage(),e);
            }
        }
        return "";
    }

    public static String doFormDataPost(String url,Map<String,String> data,String Authorization)  {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization","Basic "+Authorization);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(3000)
                .setSocketTimeout(3000).setConnectTimeout(3000).build();
        httpPost.setConfig(requestConfig);
        List<NameValuePair> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(list);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        CloseableHttpResponse response = null;
        try {
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            logger.error("请求客户端出现错误: {}",e.getMessage(),e);
        }finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("请求客户端出现错误: {}",e.getMessage(),e);
            }
        }
        return "{}";
    }

}

