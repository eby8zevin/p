package com.ekosp.indoweb.epesantren.laporan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ekosp.indoweb.epesantren.MainActivity;
import com.ekosp.indoweb.epesantren.R;
import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.GlobalVar;

public class LaporanWeb extends Activity {

    private WebView mWebView;
    private String userName;
    private String kodess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_web);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        Intent intent = getIntent();
        if (intent.getStringExtra(GlobalVar.PARAM_DATA_USER) != null){
            userName = intent.getStringExtra(GlobalVar.PARAM_DATA_USER);
        }
        if (intent.getStringExtra(GlobalVar.PARAM_KODES_USER) != null){
            kodess = intent.getStringExtra(GlobalVar.PARAM_KODES_USER);
        }

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

      //  mWebView.getSettings().setUserAgentString(newUA);

        // Stop local links and redirects from opening in browser instead of WebView
        mWebView.setWebViewClient(new HelloWebViewClient());
        mWebView.getSettings().setDomStorageEnabled(true);
        Log.i("LAPORAN", "url:"+ApiClient.BASE_REPORT_URL+userName);
        Log.i("LAPORAN", "id:"+userName);
        Log.i("LAPORAN", "kode:"+kodess);
        mWebView.loadUrl(ApiClient.BASE_REPORT_URL+userName+"&kode_sekolah="+kodess);
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void backToHome (View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private class HelloWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url)
        {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

          //  progressBar.setVisibility(View.GONE);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            Toast.makeText(LaporanWeb.this, "Terjadi kesalahan koneksi internet!", Toast.LENGTH_SHORT).show();
            String summary = "<html><body><h1>Oops!</h1><h2>Terjadi Kesalahan</h2><div>Maaf, terjadi gangguan silakan cek koneksi internet anda!</div></body></html>";

            mWebView.loadData(summary, "text/html; charset=utf-8", "utf-8");

            if (mWebView.canGoBack()) {
                mWebView.goBack();
            }
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

}
