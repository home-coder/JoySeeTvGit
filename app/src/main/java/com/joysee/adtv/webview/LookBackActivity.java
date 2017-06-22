package com.joysee.adtv.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.joysee.adtv.R;
import com.joysee.adtv.activity.DvbMainActivity;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DisplayMode;
import com.joysee.adtv.common.DefaultParameter.NotificationAction.TunerStatus;
import com.joysee.adtv.common.DialogTools;
import com.joysee.adtv.common.DvbUtil;
import com.joysee.adtv.controller.TimeShiftController;
import com.joysee.adtv.controller.TimeShiftController.MediaInfoUpdateListener;
import com.joysee.adtv.controller.TimeShiftController.OnPlayListener;
import com.joysee.adtv.logic.ChannelManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.DVBPlayManager.OnMonitorListener;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.MediaInfo;
import com.joysee.adtv.ui.PlayControlPopupWindow;

public class LookBackActivity extends Activity implements OnMonitorListener,
		OnKeyListener, OnPlayListener, MediaInfoUpdateListener {
	public static final String WEB_URL = "http://192.168.40.34";
//	public static final String WEB_URL = "http://www.sina.com";
	private static final String TAG = "LookBackActivity";
	public static String LOOK_BACK_URL = "serviceUrl";
	public static String LOOK_BACK_SERVICE = "service";
	public static String FROM_WHERE="from_where";
	public static final String RESULT_DATA = "result_data";
	public static final int DVB_MAIN_ACTIVITY=1;
	public static final int LAUNCHER=2;
	private static final int SHOW_SYMBOL_WINDOW=1;
	private static final int SHOW_PLAY_END_DIALOG=2;
	private static final int SHOW_STARTING_PLAY_DIALOG=3;
	private static final int TIME_SHIFT_PLAY_FAILE=4;
	private static final int SHOW_EXIT_DIALOG=5;
	private static final int SHOW_NET_ERROR_DIALOG=6;
	private static final int SHOW_NET_DISCONNECTED_DIALOG=7;
	private static final int SHOW_EXE_STOP_DIALOG=8;
	private static final int SHOW_OPERATE_FAILE_DIALOG=9;
	private static final int SHOW_SEEKTO_SUCCESS_DIALOG=10;
	
	private static final int NO_SINGLE_EXIT_PLAY_TIME=10000;
	private int from_where;
	protected WebView mWebView;
//	private boolean isShowing = false;
	private boolean isPlaying = false;
	private PlayControlPopupWindow mPlayControlPopupWindow;
	private static int mScreenWidth = 1280;
	private static int mScreenHeight = 720;
	private DvbService service;
	private FrameLayout root_view;
	private DVBPlayManager mDvbPlayManager;
	private ChannelManager mChannelManager;
	private VideoView mVideoView;
	private boolean isFirst=true;
	private String firstUrl=null;
	private boolean isFinished = false;
	private ProgressDialog dialog = null; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.look_back_activity);
		getParamsFromIntent();
		initView();
		setWebview();
		initMetrics();
		managerInit();
		mPlayControlPopupWindow = new PlayControlPopupWindow(
				LookBackActivity.this, mScreenWidth, mScreenHeight,false,null);
	}

	private void initMetrics() {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(mDisplayMetrics);
		mScreenHeight = mDisplayMetrics.heightPixels;
		mScreenWidth = mDisplayMetrics.widthPixels;
	}
	
	private void managerInit() {
		mDvbPlayManager = DVBPlayManager.getInstance(this);
		mDvbPlayManager.setOnMonitorListener(this);
		mDvbPlayManager.init();
		mChannelManager = ChannelManager.getInstance();
		service = new DvbService();
		mChannelManager.nativeGetCurrentService(service);
	}
	
	private void getParamsFromIntent(){
		Intent intent=this.getIntent();
		if(intent!=null){
			Bundle b=intent.getExtras();
			if(b!=null){
				from_where=b.getInt(FROM_WHERE, LAUNCHER);
			}else{
				from_where=LAUNCHER;
			}
		}else{
			from_where=LAUNCHER;
		}
	}
	
	private void initView() {
		root_view=(FrameLayout)findViewById(R.id.root_view);
		root_view.setOnKeyListener(this);
		
		mVideoView = (VideoView) findViewById(R.id.video_view);
		mVideoView.post(new Runnable(){

			@Override
			public void run() {
				mVideoView.requestFocus();
			}
			
		});
        mVideoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
//                mController.resetScreenMode();
                Log.d(TAG,"surfaceDestroyed");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
//                mController.resetWindowSize();
            	Log.d(TAG,"surfaceCreated");
                initSurface(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            	Log.d(TAG,"surfaceChanged");
            }

            private void initSurface(SurfaceHolder h) {
                Canvas c = null;
                try {
                	Log.d(TAG,"initSurface");
                    c = h.lockCanvas();
                } finally {
                    if (c != null) {
                        h.unlockCanvasAndPost(c);
                    }
                }
            }
        });
        mVideoView.getHolder().setFormat(257);
	}

	
	private void setWebview() {
		mWebView = (WebView) findViewById(R.id.webView);

		// mController = new ActivityController(this.getApplicationContext());
		mWebView.getSettings().setNeedInitialFocus(false);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		// 不保存表单数据
		mWebView.getSettings().setSaveFormData(false);
		// 不保存密码
		mWebView.getSettings().setSavePassword(false);
		// 支持页面放大功能
		mWebView.getSettings().setSupportZoom(true);
		mWebView.setInitialScale(100);
		// mWebView.getSettings().setLoadWithOverviewMode(true);

		// 设置webview自适应屏幕大小
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);

		// 不显示页面拖动条
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.getSettings().setDefaultTextEncodingName("utf-8");
		mWebView.addJavascriptInterface(new LookBackJs(this), "joysee");
		// 设置WebViewClient对象
		mWebView.setWebViewClient(new JoyseeWebViewClient());
		// 设置setWebChromeClient对象
		mWebView.setWebChromeClient(new JoyseeWebChromeClient());
//		mWebView.setVisibility(View.INVISIBLE);
		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		mWebView.setBackgroundColor(Color.TRANSPARENT);
		mWebView.loadUrl(WEB_URL);
//		mWebView.requestFocus();
	}
	
	class JoyseeWebViewClient extends WebViewClient{
		
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	System.out.println("shouldOverrideUrlLoading  url--->"+url);
	        view.loadUrl(url);
	        return true;
	    }

	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap favicon) {
	    	Log.d(TAG,"onPageStarted--->"+view.getUrl());
	    	Log.d(TAG,"onPageStarted  url---->"+url);
	    	try{
	    		if(LookBackActivity.this.isFirst()){
	    			LookBackActivity.this.setFirstUrl(url);
	    			LookBackActivity.this.setFirst(false);
	        	}
	    		String msg=LookBackActivity.this.getResources().getString(R.string.page_loading);
//	    		dialog = new ProgressDialog(LookBackActivity.this, R.style.CustomDialog);
	    		dialog = ProgressDialog.show(LookBackActivity.this,null,msg);
	        	dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
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
	        Log.d(TAG, "----------------onPageFinished");
	        if (dialog != null && dialog.isShowing()) {
	            dialog.dismiss();
	        }
	    }

		@Override
		public void onReceivedError(WebView arg0, int arg1, String arg2,
				String arg3) {
//			super.onReceivedError(arg0, arg1, arg2, arg3);
//			mWebView.setBackgroundColor(Color.WHITE);
			Log.d(TAG,"================onReceivedError start =========");
			String data = "<HTML>" +
					"<head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>" +
					"<h3 style='margin-top: 200px' align='center'>出错啦</h3>" +
					"<script type='text/javascript'> function eventHandle2(keycode)"+
						"{if(keycode==111||keycode==4){ joysee.ExitBrowser();" +
						"}}"+
					"</script>"+
					"</head>" +
					"<BODY bgcolor='#FFFFFF' align='center'>节目信息加载失败，请检查网络或联系运营商。</BODY></HTML>";

		    mWebView.loadDataWithBaseURL("about:blank", data, "text/html", "utf-8", null);
//		    mWebView.loadData(data, "text/html","utf-8");
//			mWebView.loadData(data, "txt/html","UTF-8");
			if (dialog != null && dialog.isShowing()) {
	            dialog.dismiss();
	        }
//			final AlertDialog alertDialog = new AlertDialog.Builder(LookBackActivity.this).create();
//			 alertDialog.setTitle("Error");
//             alertDialog.setMessage(description);
//             alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//                 public void onClick(DialogInterface dialog, int which) {
//                     return;
//                 }
//             });
//             alertDialog.show();
		
		}

		@Override
		public void onReceivedSslError(WebView arg0, SslErrorHandler handler,
				SslError arg2) {
			handler.proceed();
		}
	  
	}
	
	
	class JoyseeWebChromeClient extends WebChromeClient{
		
		@Override
		public void onProgressChanged(final WebView view, int newProgress) {
			Log.e("onProgressChanged", " Value is: " + newProgress);
	        if (newProgress >= 90) {
	        	if (dialog != null) {
                dialog.dismiss();
	        	}
//	        	 mHandler.postDelayed(new Runnable() {
//	 				@Override
//	 				public void run() {
//	 					mWebView.setVisibility(View.VISIBLE);
//	 				}
//	 			}, 100);
	        }
	        
	        super.onProgressChanged(view, newProgress);
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
	                		LookBackActivity.this,
	                		LookBackActivity.this.getString(R.string.joysee_prompt),
	                        new String[] { message },
	                        LookBackActivity.this
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
	                		LookBackActivity.this,
	                		LookBackActivity.this.getString(R.string.joysee_prompt),
	                        new String[] { message },
	                        LookBackActivity.this
	                                .getString(R.string.launcher_check_permission_window_ok_btn),
	                       LookBackActivity.this
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

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume ------------------------- isPlaying = " + isPlaying);
		DvbUtil.resetWindowSize();
		resetDvbDisplayMode();
        DvbUtil.switchDeinterlace(true);
        DvbUtil.setRealVideoOnOff(true);
		TimeShiftController.getInstance().setMediaInfoUpdateListener(this);
		TimeShiftController.getInstance().initAudioManager(this);
		isFinished = false;
        if (isPlaying) {
            exitPlay();
            isPlaying = false;
        }
	}

	@Override
    protected void onPause() {
		super.onPause();
		isFinished = true;
        mPlayControlPopupWindow.isDestory(true);
        Log.d(TAG, "onPause -------------------------begin ");
		TimeShiftController.getInstance().stop();
		SettingManager.getSettingManager().nativeSetVideoAspectRatio(
				DisplayMode.DISPLAYMODE_16TO9);
        DvbUtil.writeFile(DVB_PLAYED,"1");
        DvbUtil.writeFile(BACK_TO_LAUNCHER, "0");
        DvbUtil.switchDeinterlace(false);
        DvbUtil.setRealVideoOnOff(false);
        mChannelManager.nativeSetCurrentService(service);
        Log.d(TAG, "onPause -------------------------end ");
    }

    private static final String BACK_TO_LAUNCHER = "/cache/launcher_needplay";
    private static final String DVB_PLAYED = "/cache/dvb_played";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int tKeyCode = event.getKeyCode();
        Log.d(TAG, " dispatchKeyEvent tKeyCode = " + tKeyCode);
        if (isFinished) {
            Log.d(TAG, " isFinished so return true ");
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (tKeyCode == KeyEvent.KEYCODE_HOME || tKeyCode == KeyEvent.KEYCODE_PROG_RED) {
                exitPlay();
                finish();
            }
            if (tKeyCode == 268) {
                exitPlay();
                Intent intent = new Intent(LookBackActivity.this,
                        DvbMainActivity.class);
                startActivity(intent);
                finish();
            }
        }
        if (mWebView.getVisibility() == View.VISIBLE
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            try {
                if (tKeyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || tKeyCode == KeyEvent.KEYCODE_ENTER) {
                    tKeyCode = KeyEvent.KEYCODE_DPAD_CENTER;
                }
                if (tKeyCode == KeyEvent.KEYCODE_BACK
                        || tKeyCode == KeyEvent.KEYCODE_ESCAPE) {
                    tKeyCode = KeyEvent.KEYCODE_BACK;
                }
                mWebView.loadUrl("javascript: eventHandle2(" + tKeyCode + ")");
                Log.d(TAG, "javascript: eventHandle2(" + tKeyCode + ")");
                isFirst = false;
                return true;
            } catch (Exception e) {
                Log.e(TAG,
                        "this page is not support javascript:keyCode(code) function");
            }
        } else {
            Log.d(TAG,
                    " dispatchKeyEvent isPlaying = " + isPlaying
                            + " mPlayControlPopupWindow.isShowing() = "
                            + mPlayControlPopupWindow.isShowing());
            if (isPlaying) {
                if (mPlayControlPopupWindow != null
                        && mPlayControlPopupWindow.isShowing()) {
                    if ((tKeyCode == KeyEvent.KEYCODE_BACK || tKeyCode == KeyEvent.KEYCODE_ESCAPE)) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            TimeShiftController.getInstance().pause();
                            showDialog(SHOW_EXIT_DIALOG);
                        }
                        return true;
                    } else {
                        mPlayControlPopupWindow.dispatchKeyEvent(event);
                    }
                } else {
                    if (mPlayControlPopupWindow == null) {
                        mPlayControlPopupWindow = new PlayControlPopupWindow(
                                LookBackActivity.this, mScreenWidth,
                                mScreenHeight, false, null);
//                        mPlayControlPopupWindow.show();
//                        return true;
                    }
                    if ((tKeyCode == KeyEvent.KEYCODE_BACK || tKeyCode == KeyEvent.KEYCODE_ESCAPE)) {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            TimeShiftController.getInstance().pause();
                            showDialog(SHOW_EXIT_DIALOG);
                        }
                        return true;
                    }
                    if (tKeyCode == KeyEvent.KEYCODE_DPAD_CENTER
                            || tKeyCode == KeyEvent.KEYCODE_ENTER
                            || tKeyCode == KeyEvent.KEYCODE_DPAD_LEFT
                            || tKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                            || tKeyCode == KeyEvent.KEYCODE_DPAD_DOWN
                            || tKeyCode == KeyEvent.KEYCODE_DPAD_UP
                            || tKeyCode == KeyEvent.KEYCODE_PROG_GREEN) {
                        if (mPlayControlPopupWindow.isShowing()) {
                            mPlayControlPopupWindow.dispatchKeyEvent(event);
                        } else {
                            if (event.getAction() == KeyEvent.ACTION_UP
                                    && (mWebView.getVisibility() != View.VISIBLE)) {
                                mPlayControlPopupWindow.show();
                            }
                        }
                        return true;
                    }
                }
            }
        }
        Log.d(TAG, " return dispatchKeyEvent");
        return super.dispatchKeyEvent(event);
    }

	private Dialog showExitDialog() {
		final Dialog mExitDialog;
    	mExitDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.timeshift_exit_dialog_layout, null);
    	TextView tv = (TextView) view.findViewById(R.id.alert_title);
    	tv.setText(R.string.confirm_exit_timeshift);
    	final Button btOk = (Button) view.findViewById(R.id.alert_confirm_btn);
    	Button btCancel = (Button) view.findViewById(R.id.alert_cancle_btn);
    	
    	btOk.setText(R.string.time_shift_confirm);
    	btCancel.setText(R.string.time_shift_cancel);
    	btOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitPlay();
				mExitDialog.dismiss();
				disDialog();
			}
		});
    	btOk.post(new Runnable(){

			@Override
			public void run() {
				btOk.requestFocus();
				System.out.println("post  ------------");
			}
    		
    	});
        btCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mExitDialog.dismiss();
                // TimeShiftController.getInstance().resume_play();
                if (mPlayControlPopupWindow != null
                        && !mPlayControlPopupWindow.isShowing()
                        && (mWebView.getVisibility() != View.VISIBLE)) {
                    mPlayControlPopupWindow.show();
                }
            }
        });
    	mExitDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				btOk.requestFocus();
				System.out.println("onShow  ------------");
			}
		});
    	mExitDialog.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
    	int width = (int) getResources().getDimension(R.dimen.timeshift_exit_width);
        int height = (int) getResources().getDimension(R.dimen.timeshift_exit_heigth);
    	mExitDialog.setContentView(view, new LayoutParams(width, height));
    	return mExitDialog;
	}

    private void exitPlay() {
        Log.d(TAG, " ---- exitPlay ----- isPlaying = " + isPlaying);
        if (isPlaying) {
            if (mPlayControlPopupWindow != null) {
                mPlayControlPopupWindow.resetView();
                mPlayControlPopupWindow.dismiss();
            }
            dismissDialog();
            disDialog();
            mWebView.setVisibility(View.VISIBLE);
//            mWebView.requestFocus();
            isPlaying = false;
            TimeShiftController.getInstance().stop();
        }
    }
	
	
//	private void onBack() {
//	}

	private boolean setSingle(){
		if(mDvbPlayManager.getTunerSignalStatus()==0){
			//有信号
			haveSignal=true;
		}else if(mDvbPlayManager.getTunerSignalStatus()==1){
			haveSignal=false;
		}else{
			//失败
		    haveSignal=false;
		}
		Log.d(TAG, "----- setSingle " + haveSignal);
		return true;
	}
	public static final int PLAY = 0;
	public static final int NO_SINGLE_EXIT_PLAY = 1;
	public static final int EXIT_PLAY = 2;
	public static final int DISMIS_DIALOG = 3;
	public static final int DISMIS_SEEKTO_SUCCESS_DIALOG=4;
	public static final int EXIT_BROWSER=5;
//	private boolean symbol_dialog_isshowing=false;
//	private boolean starting_play_dialog_isshowing=false;
//	private boolean show_play_end_dialog=false;
//	private boolean play_fail_didalog_isshowing=false;
//	private boolean show_net_error_dialog=false;
	
	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PLAY:
				mWebView.setVisibility(View.GONE);
				String url = (String) msg.obj;
				int start = url.indexOf('=');
				int end = url.lastIndexOf('&');
				String temp_url = url.substring(start + 1, end);
				if(mPlayControlPopupWindow==null){
					mPlayControlPopupWindow = new PlayControlPopupWindow(
							LookBackActivity.this, mScreenWidth, mScreenHeight,true,null);
					System.out.println("mScreenWidth="+mScreenWidth+"  mScreenHeight="+mScreenHeight);
				}
				isPlaying = true;
				// 开始播放,不管有没有信号都得调播放
				TimeShiftController.getInstance().play_look_back(temp_url,
						LookBackActivity.this);
				showDialog(SHOW_STARTING_PLAY_DIALOG);
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setSingle();
                        if(!haveSignal){
                            showDialog(SHOW_SYMBOL_WINDOW);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    removeDialog(SHOW_SYMBOL_WINDOW);
                                    exitPlay();
                                }
                            }, 3000);
                        }
                    }
                }, 1000);
				break;
			case NO_SINGLE_EXIT_PLAY:
				System.out.println("cccccccccccccccccccccccccccccccc  isPlaying="+isPlaying);
				removeDialog(SHOW_SYMBOL_WINDOW);
				exitPlay();
				break;
			case EXIT_PLAY:
				disDialog();
				exitPlay();
				break;
			case DISMIS_DIALOG:
				removeDialog(SHOW_OPERATE_FAILE_DIALOG);
				break;
			case DISMIS_SEEKTO_SUCCESS_DIALOG:
				removeDialog(SHOW_SEEKTO_SUCCESS_DIALOG);
				break;
			case EXIT_BROWSER:
				exitLoopBackActivity();
				break;
			}
		}

	};
	/**
	 * 清除屏蔽返回键的对话框
	 */
	private void dismissDialog(){
		removeDialog(SHOW_EXIT_DIALOG);//7
	}
	/**
	 * 清除一般的消息对话框
	 */
	private void disDialog(){
		try{
			removeDialog(SHOW_STARTING_PLAY_DIALOG);//3
			removeDialog(TIME_SHIFT_PLAY_FAILE);//4
			removeDialog(SHOW_PLAY_END_DIALOG);//2
			removeDialog(SHOW_SYMBOL_WINDOW);//1
			removeDialog(SHOW_NET_ERROR_DIALOG);//6
			removeDialog(SHOW_NET_DISCONNECTED_DIALOG);//7
			removeDialog(SHOW_EXE_STOP_DIALOG);//8
			removeDialog(SHOW_OPERATE_FAILE_DIALOG);//9
			removeDialog(SHOW_SEEKTO_SUCCESS_DIALOG);//10
//			removeDialog(SHOW_EXIT_DIALOG);//5
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) { // 如果为后退按钮的作用，替换成WebView里的查看历史页面
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				// if (mWebView.canGoBack() &&
				// !mWebView.getUrl().endsWith("index.php")) {
				// mWebView.goBack();
				// } else {
				// if(from_where==LAUNCHER){
				// this.finish();
				// }else{
				// Intent intent = new Intent(LookBackActivity.this,
				// DvbMainActivity.class);
				// startActivity(intent);
				// LookBackActivity.this.finish();
				// }
				// }
				mWebView.loadUrl("javascript: eventHandle2('" + 27 + "')");
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
				mWebView.loadUrl("javascript: eventHandle2('" + 33 + "')");
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
				mWebView.loadUrl("javascript: eventHandle2('" + 34 + "')");
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_HOME) {
				Log.d("songwenxuan", "keyCode == KeyEvent.KEYCODE_HOME");
				finish();
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			mWebView.loadUrl("javascript:KEYCODE_MENU()");
			return true;
		}
		return false;
	}
	
	public void exitLoopBackActivity(){
	    Log.d(TAG, "----exitLoopBackActivity-----");
		if(from_where==LAUNCHER){
			this.finish();
		}else{
			Intent intent = new Intent(LookBackActivity.this, DvbMainActivity.class);
			startActivity(intent);
			LookBackActivity.this.finish();
		}
	}

	private boolean isNull(Object inputStr) {
		if (null == inputStr || "".equals(inputStr) || "null".equals(inputStr))
			return true;
		return false;
	}

//	public ProgressBar getProgressBar() {
//		return webview_progressbar;
//	}

    public DvbService getService() {
        DvbService dvbService = new DvbService();
        mChannelManager.nativeGetCurrentService(dvbService);
        Log.d(TAG, " getService " + dvbService.toString());
        return dvbService;
    }

	@Override
	protected void onStop() {
		Log.d(TAG, "-----onStop---");
		super.onStop();
	}

	private boolean haveSignal=true;
	@Override
	public void onMonitor(int monitorType, Object message) {
		switch (monitorType) {
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_TUNER_SIGNAL:
			int code = (Integer) message;
			if(code == TunerStatus.ACTION_TUNER_UNLOCKED){/** tuner 有信号*/
				haveSignal=true;
				mHandler.removeMessages(NO_SINGLE_EXIT_PLAY);
				removeDialog(SHOW_SYMBOL_WINDOW);
			}else{/** tuner 无信号*/
				haveSignal=false;
				if(mWebView.getVisibility()!=View.VISIBLE){
//					symbol_dialog_isshowing=true;
					this.showDialog(SHOW_SYMBOL_WINDOW);
					System.out.println("无信号");
					Message msg=Message.obtain();
					msg.what=NO_SINGLE_EXIT_PLAY;
					mHandler.sendMessageDelayed(msg, NO_SINGLE_EXIT_PLAY_TIME);
				}
			}
			break;
		}
	}
	
	private Dialog showSymbolWindow(){
		View mSymbolNotifyView;
		Dialog mErrorNotifyWindow;
		mErrorNotifyWindow = new Dialog(this, R.style.dvbErrorDialogTheme);
		mSymbolNotifyView = this.getLayoutInflater().inflate(R.layout.dvb_symbol_notify_layout, null);
		TextView textview=(TextView)mSymbolNotifyView.findViewById(R.id.test);
		textview.setText(R.string.timeshift_no_signal);
		final int windowHeight = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mSymbolNotifyView, new LayoutParams(windowWidth, windowHeight));
		return mErrorNotifyWindow;
	}
	
	/**
	 * 
	 * @return
	 */
	private Dialog showMessageDialog(String msg){
		if(msg==null){
			return null;
		}
		View mSymbolNotifyView;
		Dialog mErrorNotifyWindow;
		mErrorNotifyWindow = new Dialog(this, R.style.dvbErrorDialogTheme);
		mSymbolNotifyView = this.getLayoutInflater().inflate(R.layout.dvb_symbol_notify_layout, null);
		TextView textview=(TextView)mSymbolNotifyView.findViewById(R.id.test);
		textview.setText(msg);
		final int windowHeight = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mSymbolNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
		return mErrorNotifyWindow;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case SHOW_SYMBOL_WINDOW:
			return showSymbolWindow();
		case SHOW_PLAY_END_DIALOG:
			String look_back_play_end=this.getResources().getString(R.string.look_back_play_end);
			return showMessageDialog(look_back_play_end);
		case SHOW_STARTING_PLAY_DIALOG:
			String start_play=this.getResources().getString(R.string.start_play);
			return showMessageDialog(start_play);
		case TIME_SHIFT_PLAY_FAILE:
			String time_shift_play_faile=this.getResources().getString(R.string.time_shift_play_faile);
			return showMessageDialog(time_shift_play_faile);
		case SHOW_EXIT_DIALOG:
			return showExitDialog();
		case SHOW_NET_ERROR_DIALOG:
//			show_net_error_dialog=true;
			String text=this.getResources().getString(R.string.net_errr);
			return showMessageDialog(text);
		case SHOW_NET_DISCONNECTED_DIALOG:
			String net_stop=this.getResources().getString(R.string.net_stop);
			return showMessageDialog(net_stop);
		case SHOW_EXE_STOP_DIALOG:
			net_stop=this.getResources().getString(R.string.net_stop);
			return showMessageDialog(net_stop);
		case SHOW_OPERATE_FAILE_DIALOG:
			String operate_faile=this.getResources().getString(R.string.timeshift_operate_faile);
			return showMessageDialog(operate_faile);
		case SHOW_SEEKTO_SUCCESS_DIALOG:
			String timeshift_seekto_success=this.getResources().getString(R.string.timeshift_seekto_success);
			return showMessageDialog(timeshift_seekto_success);
		}
		return super.onCreateDialog(id);
	}

	private Dialog mBlankDialog;
	private View mBlankView;
	private void showBlankView(){
		if(mBlankDialog ==null)
			mBlankDialog = new Dialog(this, R.style.dvbErrorDialogTheme);
		if(mBlankView == null)
			mBlankView = this.getLayoutInflater().inflate(R.layout.blank, null);
        final int windowHeight = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
        final int windowWidth = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
        mBlankDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mBlankDialog.setContentView(mBlankView, new LayoutParams(windowWidth, windowHeight));
        mBlankDialog.show();
    }
	private void dismissBlankView(){
		if(mBlankDialog != null && mBlankDialog.isShowing()){
			mBlankDialog.dismiss();
		}
	}

	private void sendMessage(int what,int time){
		Message msg=Message.obtain();
		msg.what=what;
		mHandler.sendMessageDelayed(msg, time);
	}
	@Override
	public void onMonitorTimeShift(int operate, int result) {
		Log.d(TAG, " onMonitorTimeShift operate = " + operate + " result = "
				+ result);
		if (isFinished) {
			Log.d(TAG, " onMonitor isFinished = " + isFinished);
			return;
		}
		switch (operate) {
		case TimeShiftController.NotificationAction.VOD_TO_END:// 影片结束
			// System.out.println("影片结束-------------------------");
			if (mWebView.getVisibility() != View.VISIBLE) {
				// show_play_end_dialog=true;
				showDialog(SHOW_PLAY_END_DIALOG);
				sendMessage(EXIT_PLAY, 3000);
			}
		case TimeShiftController.NotificationAction.PLAY:
			if (result == TimeShiftController.NotificationAction.SUCCESS) {
				removeDialog(SHOW_STARTING_PLAY_DIALOG);
				MediaInfo mediaInfo = TimeShiftController.getInstance()
						.getLocalMediaInfo();
//				mPlayControlPopupWindow.setMediaInfo(mediaInfo);
//				mPlayControlPopupWindow.isDestory(false);
//				if (mPlayControlPopupWindow != null
//						&& !mPlayControlPopupWindow.isShowing()) {
//					mPlayControlPopupWindow.show();
//				}
				if (mediaInfo != null) {
					Log.d(TAG,
							"play success  mMediaInfo = "
									+ mediaInfo.toString());
				} else {
					Log.d(TAG, "play success  mMediaInfo = " + mediaInfo);
				}
			} else if (result == TimeShiftController.NotificationAction.FAIL) {
				Log.d(TAG, "play fail");
				removeDialog(SHOW_STARTING_PLAY_DIALOG);
				showDialog(TIME_SHIFT_PLAY_FAILE);
				sendMessage(EXIT_PLAY, 3000);
			}

			break;
		case TimeShiftController.NotificationAction.FAST_PLAY:
			/**
			 * 快进、快退时，0是失败，非0是成功，成功时result就是当前的倍速，result<0是快退，result>0是快进
			 */
			if(result==TimeShiftController.NotificationAction.SUCCESS){
				showDialog(SHOW_OPERATE_FAILE_DIALOG);
				sendMessage(DISMIS_DIALOG,5000);
			}
			break;
		case TimeShiftController.NotificationAction.NET_ERROR://网路错误、断开（操作中，土司）
			showDialog(SHOW_NET_ERROR_DIALOG);
			sendMessage(EXIT_PLAY,3000);
			break;
		case TimeShiftController.NotificationAction.NET_DISCONNECTED://心跳断开
			showDialog(SHOW_NET_DISCONNECTED_DIALOG);
			sendMessage(EXIT_PLAY,3000);
			break;
		case TimeShiftController.NotificationAction.PLAY_BACK_BEGIN://快退到头
//			exitPlay();
			break;
		case TimeShiftController.NotificationAction.EXE_STOP://需要客户端停止服务
			showDialog(SHOW_EXE_STOP_DIALOG);
			sendMessage(EXIT_PLAY,3000);
			break;
		case TimeShiftController.NotificationAction.SEEKTO:
			if(result==0){//跳转成功
				showDialog(SHOW_SEEKTO_SUCCESS_DIALOG);
				sendMessage(DISMIS_SEEKTO_SUCCESS_DIALOG,3000);
				if(mPlayControlPopupWindow!=null && mPlayControlPopupWindow.isShowing()){
					mPlayControlPopupWindow.disMissJumpView(5000);
				}
				MediaInfo mediaInfo = TimeShiftController.getInstance()
						.getLocalMediaInfo();
				mPlayControlPopupWindow.setMediaInfo(mediaInfo);
			}else{
				showDialog(SHOW_OPERATE_FAILE_DIALOG);
				sendMessage(DISMIS_DIALOG,3000);
			}
			break;
		}
	}

	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public String getFirstUrl() {
		return firstUrl;
	}

	public void setFirstUrl(String firstUrl) {
		this.firstUrl = firstUrl;
	}

	@Override
    public void onGetMediaInfo(MediaInfo mediaInfo) {
        Log.d(TAG, " onGetMediaInfo mediaInfo = " + mediaInfo);
        if (isFinished) {
            Log.d(TAG, " onGetMediaInfo isFinished = " + isFinished);
            return;
        }
        if (mPlayControlPopupWindow != null) {
            mPlayControlPopupWindow.setMediaInfo(mediaInfo);
            mPlayControlPopupWindow.isDestory(false);
            if (mPlayControlPopupWindow != null
                    && !mPlayControlPopupWindow.isShowing()
                    && (mWebView.getVisibility() != View.VISIBLE)) {
                mPlayControlPopupWindow.show();
            }
        }
    }
	 public void resetDvbDisplayMode() {
         String cur_output = DvbUtil.getCurrentOutputResolution();
         if ("576i".equals(cur_output) || "576p".equals(cur_output)) {
             mDvbPlayManager.setDisplayMode(DefaultParameter.DisplayMode.DISPLAYMODE_4TO3);
         } else {
             mDvbPlayManager.setDisplayMode(mDvbPlayManager.getDisplayMode());
         }
     }
}
