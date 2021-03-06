package com.predatum.predatoid.com.predatum.predatoid.net;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Predatum.com connector
 */
public class Predatum {

    private static final String PREDATUM_URL = "https://predatum.com";
    private static final String PREDATUM_LOGIN_CONTEXT = "user/authenticate";
    private static final String PREDATUM_USER_CHECK = "user/check";
    private static final String PREDATUM_SONG_POST_CONTEXT = "scrobbler";
    private static Predatum instance = null;
    private static int currentSongId = -1;

    /**
     * Returns singleton instance of this class
     */
    public static synchronized Predatum getInstance() {
        if (instance == null) {
            instance = new Predatum();
        }
        return instance;
    }

    public void checkPredatumConnection(final String userName,
                                        final String userPassword, final Context context) throws JSONException {

        final PersistentCookieStore predatumPersistentCookieStore = new PersistentCookieStore(
                context);
        if (!cookieExists(predatumPersistentCookieStore)) {
            this.login(userName, userPassword, context);
        } else { //double check user is indeed logged in
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
                                Log.e(getClass().getSimpleName(), "Response body " + errorMessage);
                            } else {
                                Log.e(getClass().getSimpleName(), error.toString());
                            }
                        }

                    }, predatumPersistentCookieStore);
        }
    }
    public void updateNowPlaying(HashMap<String, Object> song, final Context context)
            throws IOException, JSONException {

        JSONObject jsonParams = new JSONObject(song);
        StringEntity entity = new StringEntity(jsonParams.toString());
        final PersistentCookieStore predatumPersistentCookieStore = new PersistentCookieStore(
                context);

        PredatumRestClient.post(context, PREDATUM_SONG_POST_CONTEXT, entity, this.getPredatoidUserAgent(context),
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject message) {
                        try {
                            if (!message.getBoolean("error")) {
                                if (message.has("np_data")) {
                                    Log.d(getClass().getSimpleName(),
                                            message.getString("np_data"));
                                    currentSongId = message.getJSONObject("np_data")
                                            .getInt("user_track");
                                }
                            } else if (message.getBoolean("error") && message.getJSONArray("message").getString(0) == "login_error") { //cookie not valid
                                Toast toast = Toast.makeText(context,
                                        "You're not authenticated, check your settings",
                                        Toast.LENGTH_LONG);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(context,
                                        message.getJSONArray("message").getString(0),
                                        Toast.LENGTH_LONG);
                                toast.show();
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
                            Log.e(getClass().getSimpleName(), "Response body " + errorMessage);
                        }
                        error.printStackTrace(System.out);
                    }

                }, predatumPersistentCookieStore
        );
    }

    private boolean cookieExists(PersistentCookieStore cookieStore) {

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
        if (!cookieExists(predatumPersistentCookieStore)) {
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
                            Log.e(getClass().getSimpleName(), jsonException.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                            error) {
                        for (StackTraceElement ste : error.getStackTrace()) {
                            Log.e(getClass().getSimpleName(), responseBody.toString());
                            Log.e(this.getClass().getName(), ste.toString());
                        }
                    }
                }, predatumPersistentCookieStore);
    }

    public int getCurrentSongID() {
        return currentSongId;
    }
}
