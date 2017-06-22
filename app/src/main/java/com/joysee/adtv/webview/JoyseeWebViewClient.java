package com.joysee.adtv.webview;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.joysee.adtv.R;

public class JoyseeWebViewClient extends WebViewClient{
	
	private static final String TAG = JoyseeWebViewClient.class.getCanonicalName();
	
	private LookBackActivity mActivity;
	private ProgressDialog dialog = null; 
    public JoyseeWebViewClient(LookBackActivity mActivity) {
        this.mActivity = mActivity;
    }
    /**
     * 重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
    	System.out.println("shouldOverrideUrlLoading  url--->"+url);
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    	System.out.println("onPageStarted--->"+view.getUrl());
    	System.out.println("onPageStarted  url---->"+url);
    	try{
    		if(mActivity.isFirst()){
    			mActivity.setFirstUrl(url);
    			mActivity.setFirst(false);
        	}
    		String msg=mActivity.getResources().getString(R.string.page_loading);
        	dialog = ProgressDialog.show(mActivity,null,msg);
        	dialog.setOnKeyListener(new OnKeyListener(){

    			@Override
    			public boolean onKey(DialogInterface dialog, int keyCode,
    					KeyEvent event) {
    				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
    					if(dialog!=null){
    						dialog.dismiss();
    					}
    				}
    				return false;
    			}
        		
        	});
    	}catch(BadTokenException e){
    		e.printStackTrace();
    	}
    	
        super.onPageStarted(view, url, favicon);
        
    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        super.onPageFinished(view, url);
        System.out.println("onPageFinished");
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
//        view.requestFocus();
//        view.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                view.setFocusable(true);
//                view.getSettings().setNeedInitialFocus(true);
//                view.requestFocus(View.FOCUS_DOWN);
//            }
//        }, 2000);
    }
    
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        view.loadUrl("file:///android_asset/Nothingfoundfor404.html");
        Log.d(TAG, " onReceivedError errorCode = " + errorCode);
        Log.d(TAG, " onReceivedError description = " + description);
        Log.d(TAG, " onReceivedError failingUrl = " + failingUrl);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                mActivity.finish();
            }
        }, 6000);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
            SslError error) {
        super.onReceivedSslError(view, handler, error);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        Log.d(TAG, " onReceivedSslError error = " + error);
    }

//    @Override
//    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
////        view.setFocusable(true);
////        view.setSelected(true);
//        int keyCode = event.getKeyCode();
//        Log.e(TAG, "shouldOverrideKeyEvent:(" + keyCode + ")" + event.getAction());
////        if (event.getAction() == KeyEvent.ACTION_DOWN) {
////            try {
////                view.loadUrl("javascript: eventHandle2(" + keyCode + ")");
////                Log.e(TAG, "javascript: onkeydown(" + keyCode + ")");
////            } catch (Exception e) {
////                Log.e(TAG,
////                        "this page is not support javascript:keyCode(code) function");
////                return super.shouldOverrideKeyEvent(view, event);
////            }
////        }
//        return super.shouldOverrideKeyEvent(view, event);
//    }

}
