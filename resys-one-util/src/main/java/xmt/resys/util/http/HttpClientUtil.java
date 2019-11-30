package xmt.resys.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * http 工具类
 * @author kevinliu
 */
public class HttpClientUtil {
    private static RequestConfig requestConfig;
    // private static PoolingNHttpClientConnectionManager cnm;
    private static PoolingHttpClientConnectionManager cm;
    private static CloseableHttpClient httpClient = null;
    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";

    private static void init() {
        if (requestConfig == null) {
            synchronized (RequestConfig.class) {
                if (requestConfig == null) {
                    requestConfig = RequestConfig.custom()
                                                 // .setConnectionRequestTimeout(1000)
                                                 .setSocketTimeout(1000)
                                                 .setConnectTimeout(1000)
                                                 .build();
                }
            }
        }
        if (cm == null) {
            synchronized (PoolingHttpClientConnectionManager.class) {
                if (cm == null) {
                    cm = new PoolingHttpClientConnectionManager();
                    cm.setMaxTotal(500);// 整个连接池最大连接数
                    cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
                    cm.closeExpiredConnections();
                    cm.closeIdleConnections(200, TimeUnit.MILLISECONDS);
                }
            }
        }
        /*
         * if (cnm == null) { ConnectingIOReactor ioReactor; try { ioReactor = new
         * DefaultConnectingIOReactor(); cnm = new
         * PoolingNHttpClientConnectionManager(ioReactor); cnm.setMaxTotal(100);
         * cnm.setDefaultMaxPerRoute(5); } catch (IOReactorException e) {
         * e.printStackTrace(); } }
         */
    }

    /**
     * 通过连接池获取HttpClient
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        init();
        if (httpClient == null) {
            synchronized (CloseableHttpClient.class) {
                if (httpClient == null) {
                    httpClient = HttpClients.custom()
                                            // .setConnectionManagerShared(true)
                                            .setConnectionManager(cm)
                                            .setDefaultRequestConfig(requestConfig)
                                            .build();
                }
            }
        }
        return httpClient;
    }

    /*
     * public static CloseableHttpAsyncClient getHttpAsyncClient(){ init(); return
     * HttpAsyncClients.custom() .setConnectionManager(cnm)
     * //.setConnectionManagerShared(true) .setDefaultRequestConfig(requestConfig)
     * .build(); }
     */
    /**
     * @param url
     * @return
     */
    public static String httpGetRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    public static String httpGetRequest(String url,
                                        Map<String, Object> params)
            throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);
        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    public static String httpGetRequest(String url,
                                        Map<String, Object> headers,
                                        Map<String, Object> params)
            throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);
        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), (String) param.getValue());
        }
        return getResult(httpGet);
    }

    public static String httpGet(String url,
                                 Map<String, String> params)
            throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        if (null != params) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                pairs.add(new BasicNameValuePair(param.getKey(), (String) param.getValue()));
            }
            ub.setParameters(pairs);
        }
        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet);
    }

    public static String httpPostRequest(String url) {
        HttpPost httpPost = new HttpPost(url);
        return getResult(httpPost);
    }

    public static String httpPostRequest(String url,
                                         Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    public static String httpPost(String url,
                                  Map<String, String> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    public static String httpPostRequest(String url,
                                         Map<String, Object> headers,
                                         Map<String, Object> params)
            throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), (String) param.getValue());
        }
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost);
    }

    /**
     * 不需要证书的请求
     * @param strUrl String
     * @param reqBody 向wxpay post的请求xml数据
     * @param connectTimeoutMs 超时时间，单位是毫秒
     * @param readTimeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public static String requestWithoutCert(String strUrl,
                                            String reqBody,
                                            int connectTimeoutMs,
                                            int readTimeoutMs)
            throws Exception {
        String UTF8 = "UTF-8";
        URL httpUrl = new URL(strUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setConnectTimeout(connectTimeoutMs);
        httpURLConnection.setReadTimeout(readTimeoutMs);
        httpURLConnection.connect();
        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(reqBody.getBytes(UTF8));
        // 获取内容
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                                                                                 UTF8));
        final StringBuffer stringBuffer = new StringBuffer();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        String resp = stringBuffer.toString();
        if (stringBuffer != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // if (httpURLConnection!=null) {
        // httpURLConnection.disconnect();
        // }
        return resp;
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), (String) param.getValue()));
        }
        return pairs;
    }

    /**
     * 处理Http请求
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request) {
        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            // response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                String result = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                response.close();
                // httpClient.close();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.abort();
        } finally {
            request.releaseConnection();
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return EMPTY_STR;
    }

    public static String httpPostMethod(String url,
                                        String data) {
        String licenseStr = "";
        HttpClient httpClient = null;
        HttpPost method = null;
        HttpResponse response = null;
        try {
            // 1.建立httpclient
            httpClient = getHttpClient();
            // httpClient = new DefaultHttpClient();
            // 2.根据url建立请求方式，设置请求方式，添加发送参数
            method = new HttpPost(url);
            method.addHeader("Content-type", "application/json; charset=utf-8");
            method.setHeader("Accept", "application/json");
            // 设置参数，官方给出必须是GBK编码格式
            method.setEntity(new StringEntity(data, Charset.forName("GBK")));
            // 3.发送请求，得到响应
            response = httpClient.execute(method);
            // 4.获取响应码，判断是否请求成功
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                // 5.获取响应信息
                licenseStr = EntityUtils.toString(response.getEntity(), "GBK");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return licenseStr;
    }

    public static void main(String... strings) throws Exception {
        // String url = "http://192.168.1.71:38080/bk/v1/b/auth/token/refresh";
        // System.out.println(httpGetRequest(url,
        // HBCollectionUtil.getMapSplit("hbjwtauth",
        // "hbtoken_eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ6aGFuZ3NhbiIsImhiaWQiOiIxMjQ3OTA4MzMxNzA5NyIsImNyZWF0ZWQiOjE1MzAzMjMyODE3MzAsImhiZW5jcnlwdCI6ImJRZFdyYlRIMWZFSnBBTXBMczZEZTdraTFRcE9UQ3FXbFdsWGlVUHpGc2tnTHdkZFdoU293U0tyTDc4YmI0ZUo3aEhiMlR1NG1SeDJlYW1GdjA2RC93PT0iLCJleHAiOjE1MzA5MjgwODF9.K2sJ5IeCCwt_14RYkL8H93q3oaPY6mZiBwz-LEfGeMIgGurQu4qsvw76b8USwT5UsDEDXLKgNcTL0jUwcDPQFA"),
        // new HashMap<>()));
        String url = "http://ip.taobao.com/service/getIpInfo.php?ip=117.159.8.147";
        System.out.println(httpGetRequest(url));
    }
}
