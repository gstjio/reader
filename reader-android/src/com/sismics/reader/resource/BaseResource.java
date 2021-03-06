package com.sismics.reader.resource;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.sismics.reader.util.PreferenceUtil;

/**
 * Base class for API access.
 * 
 * @author bgamard
 */
public class BaseResource {
    
    /**
     * User-Agent to use.
     */
    protected static String USER_AGENT = null;
    
    /**
     * HTTP client.
     */
    protected static AsyncHttpClient client = new AsyncHttpClient();
    
    static {
        // 20sec default timeout
        client.setTimeout(20000);
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);   
        } catch (Exception e) {
            // NOP
        }
    }
    
    /**
     * Socket factory to allow self-signed certificates.
     * 
     * @author bgamard
     */
    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
    
    /**
     * Returns cleaned API URL.
     * @param context
     * @return
     */
    protected static String getApiUrl(Context context) {
        String serverUrl = PreferenceUtil.getServerUrl(context);
        
        if (serverUrl == null) {
            return null;
        }
        
        return serverUrl + "/api";
    }
}
