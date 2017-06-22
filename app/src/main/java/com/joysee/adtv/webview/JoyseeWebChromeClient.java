package com.joysee.adtv.webview;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DialogTools;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class JoyseeWebChromeClient extends WebChromeClient {
    private LookBackActivity mActivity;

    public JoyseeWebChromeClient(LookBackActivity mActivity) {
        this.mActivity = mActivity;
    }

	@Override
	public void onProgressChanged(final WebView view, int newProgress) {
		mActivity.setTitle(mActivity.getString(R.string.page_loading));
		mActivity.setProgress(newProgress * 100);
		if (newProgress == 100) {
			if (mActivity.getIntent() != null) {
				String title = mActivity.getIntent().getStringExtra("title"); // 从上个页面获取页面的title
				mActivity.setTitle(title);
			}
		}
//		if (newProgress == 100) {
//			view.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					view.requestFocus(View.FOCUS_DOWN);
//				}
//			}, 300);
//		}
		Log.d(" JoyseeWebChromeClient", " onProgressChanged newProgress = "
				+ newProgress);
	}

    @Override
    public void onRequestFocus(WebView view) {
        super.onRequestFocus(view);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message,
            final JsResult result) {
        // 构建一个Builder来显示网页中的alert对话框
        DialogTools
                .dialogList(
                        mActivity,
                        mActivity.getString(R.string.joysee_prompt),
                        new String[] { message },
                        mActivity
                                .getString(R.string.launcher_check_permission_window_ok_btn),
                        null, null, new DialogTools.DialogOnClickListener() {
                            @Override
                            public void onDialogClick(DialogInterface dialog,
                                    int whichButton, int source) {
                                result.confirm();
                            }
                        });
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
            final JsResult result) {
        // 构建一个Builder来显示网页中的Confirm对话框
        DialogTools
                .dialogList(
                        mActivity,
                        mActivity.getString(R.string.joysee_prompt),
                        new String[] { message },
                        mActivity
                                .getString(R.string.launcher_check_permission_window_ok_btn),
                        mActivity
                                .getString(R.string.launcher_check_permission_window_cancle_btn),
                        null, new DialogTools.DialogOnClickListener() {
                            @Override
                            public void onDialogClick(DialogInterface dialog,
                                    int whichButton, int source) {
                                switch (whichButton) {
                                case Dialog.BUTTON1: // 处理确定按钮
                                    result.confirm();
                                    break;
                                case Dialog.BUTTON2: // 处理取消按钮
                                    result.cancel();
                                    break;
                                }
                            }
                        });
        return true;
    }

    @Override
    public boolean onJsTimeout() {
        return super.onJsTimeout();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
            boolean isUserGesture, Message resultMsg) {
        // TODO Auto-generated method stub
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onCloseWindow(WebView window) {
        // TODO Auto-generated method stub
        super.onCloseWindow(window);
    }

}
