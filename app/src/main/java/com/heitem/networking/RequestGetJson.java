package com.heitem.networking;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

public class RequestGetJson<T> extends Request<T> {
    public interface OnGetRequestListener {
        void onGetRequestSuccess(String response);

        void onGetRequestError(VolleyError volleyError);
    }

    protected Context context;
    protected String url;
    protected Response.Listener listener;

    private static final String TAG = RequestGetJson.class.getSimpleName();

    public RequestGetJson(Context context, String url, String requestBody, Response.Listener listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.context = context;
        this.url = url;
        this.listener = listener;

        setRetryPolicy(new DefaultRetryPolicy(5000, 3, 3));
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, "UTF-8");

            return (Response<T>) Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {

        }

        return null;
    }
}