package com.woniu.sncp.pay.cas;

import javax.net.ssl.SSLSession;

//hostnameVerifier
public class HostnameVerifier  implements javax.net.ssl.HostnameVerifier {
    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}
