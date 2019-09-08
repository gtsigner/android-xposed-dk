package com.gotoy.debugopen;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.gotoy.debugopen.hook.TrustAllApacheSSLSocketFactory;
import com.gotoy.debugopen.hook.TrustAllSSLSocketFactory;
import com.gotoy.debugopen.hook.TrustAllX509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    public static final String PACKAGENAME = "com.chinatelecom.bestpayclient";
    public XC_MethodHook getSocketFactoryHook = new XC_MethodHook() {
        /* access modifiers changed from: protected */
        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.setResult(TrustAllApacheSSLSocketFactory.getSocketFactory());
        }
    };
    public XC_MethodHook hostNameVerifierHook = new XC_MethodHook() {
        /* access modifiers changed from: protected */
        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        }
    };
    public XC_MethodHook setSSLSocketFactoryHook = new XC_MethodHook() {
        /* access modifiers changed from: protected */
        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.args[0] = new TrustAllSSLSocketFactory();
        }
    };
    public XC_MethodHook sslContextInitHook = new XC_MethodHook() {
        /* access modifiers changed from: protected */
        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.args[1] = MainHook.this.trustManagers;
        }
    };
    public TrustManager[] trustManagers = {new TrustAllX509TrustManager()};
    public XC_MethodHook urlConnectionHook = new XC_MethodHook() {
        /* access modifiers changed from: protected */
        public void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.setResult(new TrustAllSSLSocketFactory());
        }
    };


    /**
     * 组合
     *
     * @param lpparam
     */
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod(SSLSocketFactory.class, "getSocketFactory", new Object[]{this.getSocketFactoryHook});
        XposedHelpers.findAndHookMethod(SSLContext.class, "init", new Object[]{"javax.net.ssl.KeyManager[]", "javax.net.ssl.TrustManager[]", SecureRandom.class, this.sslContextInitHook});
        XposedHelpers.findAndHookMethod(SSLSocketFactory.class, "setHostnameVerifier", new Object[]{X509HostnameVerifier.class, this.hostNameVerifierHook});
        XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setHostnameVerifier", new Object[]{HostnameVerifier.class, this.hostNameVerifierHook});
        XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setDefaultHostnameVerifier", new Object[]{HostnameVerifier.class, this.hostNameVerifierHook});
        XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setSSLSocketFactory", new Object[]{javax.net.ssl.SSLSocketFactory.class, this.setSSLSocketFactoryHook});
        XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setDefaultSSLSocketFactory", new Object[]{javax.net.ssl.SSLSocketFactory.class, this.setSSLSocketFactoryHook});
        XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "getDefaultSSLSocketFactory", new Object[]{this.urlConnectionHook});
        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader), "getPackageInfo", new XC_MethodHook() {
            /* access modifiers changed from: protected */
            public void afterHookedMethod(MethodHookParam param) throws Throwable {
                PackageInfo packageInfo = (PackageInfo) param.getResult();
                if (packageInfo != null) {
                    ApplicationInfo appinfo = packageInfo.applicationInfo;
                    int flags = appinfo.flags;
                    Log.i("BDOpener", "Load App : " + appinfo.packageName);
                    if ((flags & 32768) == 0) {
                        flags |= 32768;
                    }
                    if ((flags & 2) == 0) {
                        flags |= 2;
                    }
                    appinfo.flags = flags;
                    param.setResult(packageInfo);
                    MainHook.isDebugable(appinfo);
                    MainHook.isBackup(appinfo);
                }
            }
        });
    }

    public static boolean isDebugable(ApplicationInfo info) {
        try {
            if ((info.flags & 2) != 0) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isBackup(ApplicationInfo info) {
        try {
            if ((info.flags & 32768) != 0) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
