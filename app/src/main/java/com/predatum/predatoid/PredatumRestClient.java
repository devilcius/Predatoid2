package com.predatum.predatoid;

import com.loopj.android.http.*;

/**
 * Created by mpena on 3/30/2015.
 */
public class PredatumRestClient
{

    private static final String BASE_URL = "http://predatum.net/api/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }
    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, PersistentCookieStore cookieStore) {
        client.setCookieStore(cookieStore);
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
