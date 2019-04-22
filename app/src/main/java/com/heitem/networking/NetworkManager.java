package com.heitem.networking;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

public class NetworkManager {

    public static final String TAG = NetworkManager.class.getSimpleName();

    private static NetworkManager instance = null;

    public static NetworkManager getInstance()
    {
        if (instance == null)
        {
            synchronized (NetworkManager.class)
            {
                if (instance == null)
                {
                    instance = new NetworkManager();
                }
            }
        }

        return instance;
    }

    public RequestGetJson get(final Context context, final String urlApi, final RequestGetJson.OnGetRequestListener listener) {
        final RequestGetJson request = new RequestGetJson(context, urlApi, null,
                (Response.Listener<String>) response -> {
                    Log.d(TAG, "GET => " + urlApi);
                    listener.onGetRequestSuccess(response);
                },
                error -> {
                    Log.e("/// SERVER ///", "GET => *** ERROR *** : " + error);
                    listener.onGetRequestError(error);
                }
        );
        Volley.newRequestQueue(context.getApplicationContext(), new HurlStack()).add(request);
        return request;
    }
}
