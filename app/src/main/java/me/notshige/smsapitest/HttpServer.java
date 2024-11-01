package me.notshige.smsapitest;

import android.telephony.SmsManager;
import android.util.Log;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;

public class HttpServer extends NanoHTTPD {

    private static final String TAG = "HttpServer";

    public HttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG, "Received request: " + session.getUri() + " Method: " + session.getMethod());

        if (Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);
                String json = files.get("postData");

                Log.d(TAG, "Request body: " + json);

                Gson gson = new Gson();
                SmsRequest smsRequest = gson.fromJson(json, SmsRequest.class);

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(smsRequest.number, null, smsRequest.content, null, null);

                Log.d(TAG, "SMS sent to: " + smsRequest.number);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"success\"}");
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
                Log.e(TAG, "Error processing request", e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"status\":\"error\"}");
            }
        } else {
            Log.w(TAG, "Method not allowed: " + session.getMethod());
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "application/json", "{\"status\":\"method not allowed\"}");
        }
    }

    private static class SmsRequest {
        String number;
        String content;
    }
}