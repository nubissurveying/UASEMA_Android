package edu.usc.cesr.ema_uas.webview;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import edu.usc.cesr.ema_uas.Constants;
import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.ui.MainActivity;

public class MyWebViewClient extends WebViewClient {
    private  MainActivity activity;

    public MyWebViewClient(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }
    public static final String TAG = "web View Client";
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (activity.getDialog() != null && activity.getDialog().isShowing()) {
            activity.getDialog().dismiss();
        }
        // save cookie
        String cookies = CookieManager.getInstance().getCookie(url);

        Log.i("MyWebViewClient", "cookies come from url is " + cookies);
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.CookieKey, cookies);
        editor.commit();

    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        Toast.makeText(this.activity, "WebView Error",Toast.LENGTH_SHORT).show();
        // V155
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //  FirebaseCrash.report(new Exception("Webview Error:" + error.getDescription() + " at " + request.getUrl()));
        }
        this.activity.showMessage("Webview error. Please check your internet connection");
    }

    /*
      Added in API level 23
    */
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        //      Toast.makeText(getBaseContext(), "WebView Error", Toast.LENGTH_SHORT).show();
        // V155
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //  FirebaseCrash.report(new Exception("Webview Error:" + errorResponse.getReasonPhrase()  + " at " + request.getUrl()));
        }
        this.activity.showMessage("Webview error. Please check your internet connection");
    }




    @SuppressWarnings("deprecation")
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        //handleError(view, errorCode, description, uri);
       // if (android.os.Build.VERSION.SDK_INT >= 23) {
        if (errorCode == -2){ //no internet
            this.activity.showMessage("No internet connection detected. Make sure you are connected to the cellular network or wifi.");
        }
        else {
            this.activity.showMessage("Error: " + description);
//            Toast.makeText(this.activity, "WebView Error",Toast.LENGTH_SHORT).show();
        }

    }



}