package com.predatum.predatoid;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.loopj.android.http.*;

import org.apache.http.entity.StringEntity;

/**
 * Created by mpena on 3/30/2015.
 */
public class PredatumRestClient
{

    private static final String BASE_URL = "http://predatum.net/api/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, String userAgent, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.setUserAgent(userAgent);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, String userAgent, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.setUserAgent(userAgent);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }
    public static void post(Context context, String url, StringEntity entity, String userAgent, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.setUserAgent(userAgent);
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }
    public static void put(String url, RequestParams params, String userAgent, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.setUserAgent(userAgent);
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    private String getPredatoidUserAgent(Context context){
        String userAgent = "";
        try {
            userAgent = "Predatoid-";
            userAgent = userAgent + context.getPackageManager().getPackageInfo(context.getPackageName(), 0 ).versionName;
            userAgent = userAgent + " (" + Build.DEVICE + "/" + Build.VERSION.RELEASE + ")";
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return userAgent;
    }
}
