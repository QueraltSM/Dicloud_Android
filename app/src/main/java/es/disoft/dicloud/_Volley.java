package es.disoft.dicloud;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class _Volley {

    public static void getResponse(
            Context context,
            String url,
            final Map data,
            final _VolleyCallback callback) {

        RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(context);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccessResponse(response);
                    }
                }, null) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                return new JSONObject(data).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String strUTF8 = null;
                try { strUTF8 = new String(response.data, "UTF-8"); }
                catch (UnsupportedEncodingException e) { e.printStackTrace(); }
                return Response.success(strUTF8, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(postRequest);
    }
}
