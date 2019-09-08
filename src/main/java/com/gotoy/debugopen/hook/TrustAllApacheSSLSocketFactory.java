package com.gotoy.debugopen.hook;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

public final class TrustAllApacheSSLSocketFactory extends SSLSocketFactory {
    private final SSLContext sslContext = SSLContext.getInstance("TLS");

    public TrustAllApacheSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
        this.sslContext.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, null);
    }

    public Socket createSocket() throws IOException {
        return this.sslContext.getSocketFactory().createSocket();
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return this.sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    public static SSLSocketFactory getSocketFactory() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory factory = new TrustAllApacheSSLSocketFactory(trustStore);
            factory.setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
            return factory;
        } catch (IOException | GeneralSecurityException e) {
            return null;
        }
    }
}
