package com.gotoy.debugopen.hook;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class TrustAllSSLSocketFactory extends SSLSocketFactory {
    private static final TrustManager[] TRUST_ALL_CERTS = {new TrustAllX509TrustManager()};
    private final SSLSocketFactory delegate = getSocketFactory();

    public Socket createSocket() throws IOException {
        return this.delegate.createSocket();
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.delegate.createSocket(host, port);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.delegate.createSocket(address, port, localAddress, localPort);
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return this.delegate.createSocket(s, host, port, autoClose);
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.delegate.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return this.delegate.createSocket(host, port, localHost, localPort);
    }

    public boolean equals(Object o) {
        return this.delegate.equals(o);
    }

    public String[] getDefaultCipherSuites() {
        return this.delegate.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return this.delegate.getSupportedCipherSuites();
    }

    public static SSLSocketFactory getSocketFactory() {
        boolean z = false;
        System.setProperty("http.keepAlive", "false");
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}
