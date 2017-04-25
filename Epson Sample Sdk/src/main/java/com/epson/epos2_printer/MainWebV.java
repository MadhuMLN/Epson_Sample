package com.epson.epos2_printer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;


public class MainWebV extends AppCompatActivity {

    public static final String MyShPref_PD = "MyShPref_data";

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb1);
        setSupportActionBar(toolbar);

        webView = (WebView) findViewById(R.id.webm);

        WebSettings set = webView.getSettings();
        set.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        final ProgressDialog progressBar = ProgressDialog.show(MainWebV.this, "Please Wait", "Loading...");

      
        webView.addJavascriptInterface(new WebAppInterface(this), "MyWebResponse");
        webView.requestFocusFromTouch();
        webView.setWebViewClient(new WebViewClient()

        {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                String summary = "<html><body style='background:#fff;'><br><div ><b><p style='color: black;font-size:20px;text-align:left;'>Unable to load information.</p></b><div style='color: black;text-align:center;'>Please check if your Network connection.</div><br><div> <p style='color: black;text-align:center;'>Or<br><br> <div style='color: black;text-align:center;'>Server May be Temporarily Down\n" + "\n" +
                        "<div style='color: black;text-align:center;'>Please Try After Sometime</p></div></div></div></div></body></html>";
                view.loadData(summary, "text/html", null);
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {

                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }

            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                String summary = "<html><body style='background:#fff;'><br><div ><b><p style='color: black;font-size:20px;text-align:left;'>Unable to load information.</p></b><div style='color: black;text-align:center;'>Please check your Network Connection.</div><br><div> <p style='color: black;text-align:center;'>Or<br><div style='color: black;text-align:center;'>Server May be Temporarily Down\n" + "\n" +
                        "<div style='color: black;text-align:center;'>Please Try After Sometime</p></div></div></div></div></body></html>";
                view.loadData(summary, "text/html", null);
                alertDialog.setTitle("Error");
                alertDialog.setMessage(description);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alertDialog.show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        String url = "file:///android_asset/web_sample.html";
        try {

            if (URLUtil.isValidUrl(url)) {
                webView.loadUrl(url);

            } else {
                String e = "<html><body style='background:#fff;'><br><div ><b><p style='color: black;font-size:20px;text-align:left;'>Please Exit app and Open it After setup.</p></b></div></div></body></html>";
            }
        } catch (Exception e) {

            String s = "<html><body style='background:#fff;'><br><div ><b><p style='color: black;font-size:20px;text-align:left;'>Unable to load information.</p></b><div style='color: black;text-align:center;'>Server May be Temporarily Down\n" + "\n" +
                    "<div style='color: black;text-align:center;'>Please Try After Sometime</p></div></div></body></html>";
        }

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.m_pr_menu, menu);

        SharedPreferences s = getSharedPreferences(MyShPref_PD, 0);
        HashMap<String, String> map= (HashMap<String, String>) s.getAll();
    
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.pt1) {
            Intent mypIntent = new Intent(this, shared_DiscoveryActivity.class);
            startActivity(mypIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Exit !  My EPrint App.");
        alertDialogBuilder
                .setMessage("Click Yes to Exit!")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    public class WebAppInterface {

        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void Print(final String avalue) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Are you sure you want to leave to next screen?");
            alertDialog.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent chnIntent = new Intent(MainWebV.this, MainActivity.class);
                            chnIntent.putExtra("STRING_DATA", avalue);
                            startActivity(chnIntent);
                        }
                    });
            alertDialog.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();

        }
    }
}

