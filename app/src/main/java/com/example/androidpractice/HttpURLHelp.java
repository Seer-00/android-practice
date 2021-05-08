package com.example.androidpractice;

import android.util.Log;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpURLHelp {

    private static final String TAG = HttpURLHelp.class.getName();

    private static final int TIME_OUT = 5 * 1000;

    // 信任所有主机，忽略Https的证书
    private static TrustManager trustAllManager = new X509TrustManager() {

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1) {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1) {
        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    public static HttpsURLConnection getHttpsConnection(String url) {
        HttpsURLConnection conn = null;
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustAllManager}, null);
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            conn = (HttpsURLConnection) new URL(url).openConnection();
            //设置加密协议
            conn.setSSLSocketFactory(ssf);

            conn.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static String downloadStringFromURL(String url) {
        StringBuffer buffer = new StringBuffer();
        HttpURLConnection conn = null;

        try {
            URL realUrl = new URL(url);
            // 通过请求地址判断请求类型(http或者是https)
            if (realUrl.getProtocol().toLowerCase().equals("https")) {
                Log.i(TAG, "Download data via https...");
                conn = getHttpsConnection(url);
            } else {
                Log.i(TAG, "Download data via http...");
                conn = (HttpURLConnection) realUrl.openConnection();
            }

            //设置超时时长
            conn.setConnectTimeout(TIME_OUT);
            conn.setReadTimeout(TIME_OUT);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "text/plain;charset=utf-8");

            conn.connect();

            int code = conn.getResponseCode();
            Log.i(TAG, "downloadStringFromURL: ResponseCode: " + code);

            if (HttpURLConnection.HTTP_OK == code) {
                // 取得该连接的输入流，以读取响应内容
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                int s;
                while ((s = reader.read()) != -1) {
                    buffer.append((char) s);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return buffer.toString();
    }
}
