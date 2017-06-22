package com.joysee.adtv.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.activity.DvbMainActivity;
import com.joysee.adtv.activity.SearchMainActivity;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.EmailStatus;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DefaultParameter.OsdShowType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ToastUtil;

public class OsdPopupWindow implements IDvbBaseView {
	private DvbLog log = new DvbLog("OsdPopupWindow", DvbLog.DebugType.D);
	private Context mContext;
	private Activity mActivity;
    /* 显示OSD和Email */
	private PopupWindow mOsdPopWindowTop,mOsdPopWindowBottom;
	private AutoScrollTextView mOsdPopWindowViewTop,mOsdPopWindowViewBottom;
	private ImageView mEmailIcon;
	private int mCurrentEmailIconStatus = EmailStatus.EMAIL_HIDE;
	
	private PopupWindow mGotoSearchNotifyWindow;
	
	/* 指纹 */
	private TextView mFingerInfoTv = null;
	/*智能卡升级*/
    private PopupWindow mCaUpdateProgress;
    private ProgressBar mCaUpdateProgressBar;
    private TextView mCaUpdateProgress_Title;
    private TextView mCaUpdateProgress_Rate;
    public static int sLastProcess = 0;
    private boolean mCanDispatchKey = true;
	
	public OsdPopupWindow(Activity activity){
		mContext = activity.getApplicationContext();
		mActivity = activity;
	}
	public void setEmailcon(ImageView emailIcom){
		mEmailIcon = emailIcom;
	}
	public void setFingerInfoTv(TextView fingerInfoTv){
		mFingerInfoTv = fingerInfoTv;
	}
	
	/**
	 * 显示OSD信息View
	 * @param osdMsg    OSD信息
	 * @param showType OSD的显示方式 0x01: 显示在屏幕上方 0x02：显示在屏幕下方 0x03：整屏显示 0x04：半屏显示
	 */
    private void showOsdView(String osdMsg, int showType) {
        log.D(" showOsdView osdMsg = " + osdMsg + " showType = " + showType);
        switch (showType) {
        case OsdShowType.OSD_SHOW_BOTTOM_FULL:
            if (null == mOsdPopWindowViewBottom) {
                mOsdPopWindowViewBottom = new AutoScrollTextView(mContext);
                mOsdPopWindowBottom = new PopupWindow(mOsdPopWindowViewBottom);
            }
            mOsdPopWindowBottom.setWidth((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_width));
            mOsdPopWindowBottom.setHeight((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_height));
            mOsdPopWindowViewBottom.setText(osdMsg);
            mOsdPopWindowViewBottom.invalidate();
            mOsdPopWindowViewBottom.init(mActivity.getWindowManager());
            mOsdPopWindowBottom.showAtLocation(mActivity.getWindow().getDecorView(),
                    Gravity.BOTTOM | Gravity.RIGHT, 0, 10);
            mOsdPopWindowViewBottom.startScroll();
            break;
        case OsdShowType.OSD_SHOW_TOP_FULL:
            if (null == mOsdPopWindowViewTop) {
                mOsdPopWindowViewTop = new AutoScrollTextView(
                        mContext);
                mOsdPopWindowTop = new PopupWindow(mOsdPopWindowViewTop);
            }
            mOsdPopWindowTop.setWidth((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_width));
            mOsdPopWindowTop.setHeight((int) mContext.getResources().getDimension(R.dimen.osd_popupwindow_height));
            mOsdPopWindowViewTop.setText(osdMsg);
            mOsdPopWindowViewTop.invalidate();
            mOsdPopWindowViewTop.init(mActivity.getWindowManager());
            mOsdPopWindowTop.showAtLocation(mActivity.getWindow().getDecorView(),
                    Gravity.TOP | Gravity.RIGHT, 0, 10);
            mOsdPopWindowViewTop.startScroll();
            break;
        }
    }
    
    /**
  	 * 隐藏OSD信息View
  	 */
    private void hideOsdView(int type) {
          switch (type) {
          case OsdShowType.OSD_SHOW_BOTTOM_FULL:
              if (null != mOsdPopWindowBottom) {
                  mOsdPopWindowViewBottom.stopScroll();
                  mOsdPopWindowBottom.dismiss();
              } else {
                  log.E(" ------  HideOsd error !!! mOsdPopWindowBottom = "
                          + mOsdPopWindowBottom);
              }
              break;
          case OsdShowType.OSD_SHOW_TOP_FULL:
              if (null != mOsdPopWindowTop) {
                  mOsdPopWindowViewTop.stopScroll();
                  mOsdPopWindowTop.dismiss();
              } else {
                  log.E(" ------  HideOsd error !!! mOsdPopWindowTop = "
                          + mOsdPopWindowTop);
              }
              break;
          }
      }
      
    private void hideEmail() {
  		if (mEmailIcon != null) {
  			mEmailIcon.setVisibility(View.GONE);
  			mCurrentEmailIconStatus = EmailStatus.EMAIL_HIDE;
  		}
  	}
    private void showEmail() {
  		if (mEmailIcon != null) {
  			mEmailIcon.setVisibility(View.VISIBLE);
  			mCurrentEmailIconStatus = EmailStatus.EMAIL_SHOW;
  		}
  	}
      /**
  	 * 邮件图标闪烁
  	 */
    private void showTimerEmailIcon() {
  		mHandler.postDelayed(mSwitchEIconRunnable, 1000);
  	}
  	// E Email
  	private Runnable mSwitchEIconRunnable = new Runnable() {
  		public void run() {
  			if (mCurrentEmailIconStatus == EmailStatus.EMAIL_HIDE) {
  				showEmail();
  			} else {
  				hideEmail();
  			}
  			mHandler.postDelayed(this, 1000);
  		}
  	};
  	
	 /**显示指纹信息*/
  	private void showFingerInfo(int cardid,int ecmpid){
        log.D( " showFingerInfo  cardid = " + cardid + " ecmpid = " + ecmpid);
        if (cardid == 0) {
            mFingerInfoTv.setVisibility(View.INVISIBLE);
        }else{
            mFingerInfoTv.setText(""+cardid);
            mFingerInfoTv.setVisibility(View.VISIBLE);
        }
    }
    
  	private static final int HIDE_GOTO_SEACH_NOTIFY = 2;
  	private static final int GOTO_SEARCH = 3;
    /**隐藏升级框*/
    public static final int MSG_DISMISS_CAUPDATE = 4;
  	private Handler mHandler = new Handler(){
  		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_GOTO_SEACH_NOTIFY:
				if (mGotoSearchNotifyWindow != null && mGotoSearchNotifyWindow.isShowing())
					mGotoSearchNotifyWindow.dismiss();
				break;
			case GOTO_SEARCH:
				if (mGotoSearchNotifyWindow != null && mGotoSearchNotifyWindow.isShowing())
					mGotoSearchNotifyWindow.dismiss();
//				Intent tIntent = new Intent(mContext, SearchMainActivity.class);
//				tIntent.putExtra(DefaultParameter.TVNOTIFY_TO_SEARCH, true);
//				mContext.startActivity(tIntent);
				Intent intent = new Intent();
                intent.setClassName("com.lenovo.settings",
                        "com.lenovo.settings.LenovoSettingsActivity");
                Bundle bundle = new Bundle();
                bundle.putInt("curChoice", 2);
                bundle.putString("subChoice", "cctv-search");
                intent.putExtras(bundle);
                DvbMainActivity dvbMainActivity = (DvbMainActivity)mActivity;
                dvbMainActivity.isTurnOffDeinterlace = true;
                mActivity.startActivity(intent);
                mActivity.finish();
				break;
            case MSG_DISMISS_CAUPDATE:
                if (mCaUpdateProgress != null) {
                    if (mCaUpdateProgress.isShowing()) {
                        mCaUpdateProgress.dismiss();
                        mCaUpdateProgress = null;
                        sLastProcess = 0;
                    }
                }
                break;
			}
		};
  	};

    @Override
    public void processMessage(Object sender, DvbMessage msg) {
        log.D(" processMessage msg = " + msg.toString());
        switch (msg.what) {
            case ViewMessage.RECEIVED_OSD_INFO_SHOW:
                String osdMsg = (String) msg.obj;
                int type = msg.arg1;
                showOsdView(osdMsg, type);
                break;
            case ViewMessage.RECEIVED_OSD_INFO_HIDE:
                int osdType = msg.arg1;
                hideOsdView(osdType);
                break;
            case ViewMessage.RECEIVED_EMAIL_SHOW:
                showEmail();
                break;
            case ViewMessage.RECEIVED_EMAIL_HIDE:
                hideEmail();
                break;
            case ViewMessage.RECEIVED_EMAIL_BLINK:
                showTimerEmailIcon();
                break;
            case ViewMessage.RECEIVED_FINGER_INFO_SHOW:
                int cardid = msg.arg1;
                int emcpid = msg.arg2;
                showFingerInfo(cardid, emcpid);
                break;
            case ViewMessage.STOP_PLAY:
                hideOsdView(OsdShowType.OSD_SHOW_BOTTOM_FULL);
                hideOsdView(OsdShowType.OSD_SHOW_TOP_FULL);
                mHandler.removeCallbacks(mSwitchEIconRunnable);
                mHandler.removeMessages(HIDE_GOTO_SEACH_NOTIFY);
                mHandler.removeMessages(GOTO_SEARCH);
                break;
            case ViewMessage.RECEIVED_UPDATE_PROGRAM_NB_CHANGE:
                if (mGotoSearchNotifyWindow == null)
                    mGotoSearchNotifyWindow = ToastUtil.showPopToast(mContext,
                            mContext.getResources().getString(R.string.dvb_update_program));
                mGotoSearchNotifyWindow.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mCanDispatchKey = true;
                    }
                });
                mGotoSearchNotifyWindow.showAtLocation(mActivity.getWindow().getDecorView(),
                        Gravity.CENTER, 0, 0);
                mCanDispatchKey = false;
                mHandler.sendEmptyMessageDelayed(HIDE_GOTO_SEACH_NOTIFY, 3000);
                mHandler.sendEmptyMessageDelayed(GOTO_SEARCH, 5000);
                break;
            case ViewMessage.MSG_SHOW_CAUPDATE:
                showCaUpdate(msg.arg1, msg.arg2);
                break;
        }
    }

    /**
     * 显示智能卡升级进度
     * @param type 升级类型
     * @param progress 进度值
     */
    private void showCaUpdate(int type, int progress) {
        log.D(" showCaUpdate  type = " + type + " progress = " + progress);
        try {
            if (mCaUpdateProgress == null) {
                mCaUpdateProgress = new PopupWindow();
                mCaUpdateProgress.setWidth(600);
                mCaUpdateProgress.setHeight(200);
                View view = mActivity.getLayoutInflater().inflate(
                        R.layout.updateprogress_ca, null);
                mCaUpdateProgressBar = (ProgressBar) view
                        .findViewById(R.id.ca_updateprogress_probar);
                mCaUpdateProgress_Title = (TextView) view
                        .findViewById(R.id.ca_updateprogress_title);
                mCaUpdateProgress_Rate = (TextView) view.findViewById(R.id.ca_updateprogress_rate);
                mCaUpdateProgress.setContentView(view);
            }
            if (!mCaUpdateProgress.isShowing()) {
                mCaUpdateProgress.showAtLocation(mActivity.getWindow()
                        .getDecorView(), Gravity.CENTER, 0, 300);
            }
            if (progress >= 101 && sLastProcess >= 100) {
                mCaUpdateProgress_Rate.setVisibility(View.GONE);
                mCaUpdateProgressBar.setVisibility(View.GONE);
                mCaUpdateProgress_Title.setText("");
                if (type == 1) {/* 升级数据接收 */
                    mCaUpdateProgress_Title.setText(mActivity.getResources().getString(
                            R.string.ca_update_type1_end));
                } else if (type == 2) {/* 智能卡升级 */
                    mCaUpdateProgress_Title.setText(mActivity.getResources().getString(
                            R.string.ca_update_type2_end));
                }
                mHandler.sendEmptyMessageDelayed(MSG_DISMISS_CAUPDATE, 2000);
                return;
            } else if (progress >= 101) {
                mHandler.sendEmptyMessageDelayed(MSG_DISMISS_CAUPDATE, 1000);
                return;
            } else {
                mCaUpdateProgress_Title.setText("");
                if (type == 1) {/* 升级数据接收 */
                    mCaUpdateProgress_Title.setText(mActivity.getResources().getString(
                            R.string.ca_update_type1));
                } else if (type == 2) {/* 智能卡升级 */
                    mCaUpdateProgress_Title.setText(mActivity.getResources().getString(
                            R.string.ca_update_type2));
                }
            }
            if (progress < 0) {
                progress = 0;
            }
            if (progress > 100) {
                progress = 100;
            }
            mCaUpdateProgress_Rate.setText("");
            mCaUpdateProgress_Rate.setText(mActivity.getResources().getString(
                    R.string.ca_update_rate, progress));
            mCaUpdateProgressBar.setProgress(progress);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    /**
     * 是否接收按键消息
     * @return true 可以接收按键消息
     */
    public boolean canDispatchKey(){
        return mCanDispatchKey ;
    }
}
