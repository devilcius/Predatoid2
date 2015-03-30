package com.predatum.predatoid;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Predatum.com connector
 */
public class Predatum {

    private static final String PREDATUM_URL = "http://predatum.net";
    private static final String PREDATUM_LOGIN_CONTEXT = "user/authenticate";
    private static final String PREDATUM_USER_CHECK = "user/check";
    private static final String PREDATUM_SONG_POST_CONTEXT = "scrobbler";

    private static Predatum instance = null;
    private static int currentSongId = -1;

    final String TAG = "Predatum connector";

    /**
     * Returns singleton instance of this class
     */
    public static synchronized Predatum getInstance() {
        if (instance == null) {
            instance = new Predatum();
        }
        return instance;
    }

    public void authenticateToPredatum(final String userName,
                                       final String userPassword, final Context context) throws JSONException {

        final PersistentCookieStore predatumPersistentCookieStore = new PersistentCookieStore(
                context);
        if (!userIsLoggedIn(predatumPersistentCookieStore)) {
            this.login(userName, userPassword, context);
        } else { //double check user is logged in
            PredatumRestClient.get(PREDATUM_USER_CHECK, null, this.getPredatoidUserAgent(context),
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject message) {
                            try {
                                if (!message.getBoolean("error")) {
                                    Log.i(getClass().getSimpleName(),
                                            message.getString("message"));
                                    Toast toast = Toast.makeText(context,
                                            message.getString("message"),
                                            Toast.LENGTH_LONG);
                                    toast.show();
                                } else {
                                    //cookie not valid
                                    predatumPersistentCookieStore.clear();
                                    login(userName, userPassword, context);
                                }

                            } catch (JSONException ex) {
                                Log.e(getClass().getSimpleName(),
                                        ex.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                                error) {
                            Log.i(getClass().getSimpleName(), "async task failed!!!! " + error.getMessage());
                            if(responseBody != null) {
                                String errorMessage = new String(responseBody);
                                Log.i(getClass().getSimpleName(), "Response body " + errorMessage);
                            }
                            error.printStackTrace(System.out);
                        }

                        @Override
                        public void onStart() {
                            String buh = "buh";
                            Log.i(getClass().getSimpleName(), "async task started!!!!");

                        }

                    }, predatumPersistentCookieStore);
        }
    }

    public void updateNowPlaying(HashMap<String, Object> song, Context context)
            throws ClientProtocolException, IOException {

        Iterator<Entry<String, Object>> iterator = song.entrySet().iterator();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        while (iterator.hasNext()) {
            @SuppressWarnings("rawtypes")
            HashMap.Entry pairs = (HashMap.Entry) iterator.next();
            nameValuePairs.add(new BasicNameValuePair(
                    pairs.getKey().toString(), pairs.getValue().toString()));
            iterator.remove(); // avoids a ConcurrentModificationException
        }

        JSONObject serverResponse = predatumPost(nameValuePairs,
                PREDATUM_SONG_POST_CONTEXT, context);
        try {

            if (serverResponse.has("processed")) {
                // process different response
                if (serverResponse.has("np_data")) {
                    Log.d(getClass().getSimpleName(),
                            serverResponse.getString("np_data"));
                    currentSongId = serverResponse.getJSONObject("np_data")
                            .getInt("user_track");
                }
            } else {
                Toast toast = Toast.makeText(context,
                        serverResponse.getString("error"), Toast.LENGTH_LONG);
                toast.show();
            }

        } catch (JSONException jsonException) {
            Log.e(TAG,
                    jsonException.getMessage());
        }
    }

    private boolean userIsLoggedIn(PersistentCookieStore cookieStore) {

        List<Cookie> predatumCookies = cookieStore.getCookies();
        return predatumCookies.size() >= 1;
    }

    private JSONObject predatumPost(List<NameValuePair> params,
                                    String controller, Context context) {

        PersistentCookieStore predatumPersistentCookieStore = new PersistentCookieStore(
                context);
        DefaultHttpClient mHttpClient = new DefaultHttpClient();
        JSONObject jResponse = null;
        try {
            HttpPost httppost = new HttpPost(PREDATUM_URL + controller);
            httppost.setHeader("User-Agent", getPredatoidUserAgent(context));
            httppost.setEntity(new UrlEncodedFormEntity(params));
            BasicHttpContext mHttpContext = new BasicHttpContext();
            mHttpContext.setAttribute(ClientContext.COOKIE_STORE,
                    predatumPersistentCookieStore);
            HttpResponse response = mHttpClient.execute(httppost, mHttpContext);
            String responseBody = EntityUtils.toString(response.getEntity());
            jResponse = new JSONObject(responseBody);
        } catch (ClientProtocolException cpEx) {
            Log.e(getClass().getSimpleName(), cpEx.toString());
        } catch (IOException ioEx) {
            Log.e(getClass().getSimpleName(), ioEx.toString());
        } catch (JSONException jsonEx) {
            Log.e(getClass().getSimpleName(), jsonEx.toString());
        }
        return jResponse;
    }

    private String getPredatoidUserAgent(Context context) {
        String userAgent = "";
        try {
            userAgent = "Predatoid-";
            userAgent = userAgent + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            userAgent = userAgent + " (" + Build.DEVICE + "/" + Build.VERSION.RELEASE + ")";
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return userAgent;
    }

    private void login(String email, String password, final Context context) {


        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        params.put("remember", "1");
        PersistentCookieStore predatumPersistentCookieStore = new PersistentCookieStore(
                context);
        if (!userIsLoggedIn(predatumPersistentCookieStore)) {
            predatumPersistentCookieStore.clear();
        }
        PredatumRestClient.post(PREDATUM_LOGIN_CONTEXT, params, getPredatoidUserAgent(context),
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject message) {
                        try {
                            if (!message.getBoolean("error")) {
                                Log.d(context.getClass().getSimpleName(),
                                        message.getString("message"));
                                Toast toast = Toast.makeText(context,
                                        message.getString("message"),
                                        Toast.LENGTH_LONG);
                                toast.show();
                            } else {
                                Log.e(this.getClass().getSimpleName(),
                                        message.getString("message"));
                            }

                        } catch (JSONException jsonException) {
                            JSONObject dumpMessage = message;
                            Log.e(TAG, jsonException.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                            error) {
                        for (StackTraceElement ste : error.getStackTrace()) {
                            Log.e(TAG, responseBody.toString());
                            Log.e(this.getClass().getName(), ste.toString());
                        }
                    }
                }, predatumPersistentCookieStore);
    }

    public int getCurrentSongID() {
        return currentSongId;
    }
}
