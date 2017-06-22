
package com.joysee.adtv.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DefaultTpValue;
import com.joysee.adtv.common.DefaultParameter.DisplayMode;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbKeyEvent;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DvbUtil;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.doc.ADTVEpgDoc;
import com.joysee.adtv.doc.ADTVResource;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.Program;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.server.ADTVService;
import com.joysee.adtv.webview.LookBackActivity;

public class EpgGuideWindow extends BasicWindow implements IDvbBaseView {

    private static final DvbLog log = new DvbLog(
            "EPGGuideWindow", DvbLog.DebugType.D);

    public static final int RES_Bitmap=1;
    public static final int RES_Actor=2;//主演或播出
    public static final int RES_Type=3;//类型或主持人
    public static final int RES_About=4;//简介
    public static final int RES_Nibble=5;//
    public static final int RES_ProgramList=6;//
    
    public static final int Status_End=1;
    public static final int Status_Ing=2;
    public static final int Status_Future=3;
    public static final int Status_Error=4;
    
    public static final int Type_Move=1;
    public static final int Type_Other=2;
    
    protected boolean isShow;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private EpgWeekView epgWeek = null;
    private EpgChannelFrame epgChannel = null;
    private LinearLayout epgGuideLayout=null;
    private PopupWindow epgGuideWindow;
    private PopupWindow mAlertPopupWindow;
    private ViewController mViewController;
    private SettingManager mSettingManager;
    private Dialog mAlertDialog;
    public View mBcBackground;
    public static double mTimeZone;
    public static final int MSG_Close_Window = 1;
    public static final int MSG_Get_Detail = 2;
    public static final int MSG_Get_ProgramList = 3;
    public static long TimeOffset;
    
    private TextView actor_con,type_con,about_con,actor,type;
    private ImageView poster;
    private int programId=0;
    ADTVEpgDoc doc;
    
    public static final boolean isTSMode = true;
    
    
    private static final int RESERVE_STATUS_OFF = 0;
    private static final int RESERVE_STATUS_ON = 1;
    
    public EpgGuideWindow(Activity activity){
        mActivity=activity;
        mInflater=mActivity.getLayoutInflater();
        mSettingManager = SettingManager.getSettingManager();
        mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
        Log.d("songwenxuan","time zone = " + mTimeZone);
        TimeOffset=(long)((8-mTimeZone)*3600*1000);
        doc=new ADTVEpgDoc();
        setDoc(doc);
    }
    
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_Close_Window:
                    if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
                        mAlertPopupWindow.dismiss();
                    }
                    break;
                case MSG_Get_Detail:
//                    getDetail(msg.arg1);
                    break;
                case MSG_Get_ProgramList:
                    doc.getProgramidList(msg.arg2,msg.arg1, EpgWeekView.beginTime, EpgWeekView.endTime);
                    break;
            }
        }
    };
    
    public void getDetail(int programId){
        log.D("**************getDetail*********   programId="+programId);
        clearProgramInfo(programId);
        if(programId>0)
            doc.getProgramDetail(programId);
    }

    @Override
    public void processMessage(Object sender, DvbMessage msg) {
        switch (msg.what) {
            case ViewMessage.SHOW_PROGRAM_GUIDE:
                mViewController = (ViewController) sender;
                checkOrderList();
                showProgramGuide();
                break;
            case ViewMessage.SHOW_PROGRAM_RESERVE_ALERT:
                dismiss(false);
                break;
            case ViewMessage.RECEIVED_SEARCH_EPG_COMPLETED:
            	Log.d("songwenxuan","RECEIVE_EPG_CALLBACK---------------------------------------");
            	int message = 0;
            	if(msg.obj!=null){
            		message = (Integer) msg.obj;
            	}
            	if(message == -1){
            	    if(mLoadingDialog != null && mLoadingDialog.isShowing()){
            	        mLoadingDialog.dismiss();
            	        showErrorPop(R.string.error_main_timeout);
            	    }
            	}else{
            		if(mLoadingDialog!= null && mLoadingDialog.isShowing()){
            			mLoadingDialog.dismiss();
            			mFocusImageView.setVisibility(View.VISIBLE);
            			epgChannel.init();
            			epgChannel.onfocusView();
            			String mode = getCurrentOutputResolution();
                        int[] position = getPosition(mode, TV_LEFT, TV_TOP, TV_RIGHT, TV_BOTTOM);
                        mViewController.playFromEpg(position[0],position[1],position[2],position[3],DisplayMode.DISPLAYMODE_16TO9);
            			isPlay = true;
            		}
            	}
                break;
            case ViewMessage.EXIT_PROGRAM_GUIDE:
            	dismiss(false);
            	break;
            case ViewMessage.EPG_RECEIVE_NOTIFY:
                Object [] objs = (Object[]) msg.obj;
                refreshNotify(objs);
                break;
            case ViewMessage.START_PLAY_BC:
                if(mBcBackground != null)
                    mBcBackground.setVisibility(View.VISIBLE);
                break;
            case ViewMessage.START_PLAY_TV:
                if(mBcBackground!=null)
                    mBcBackground.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void showProgramGuide() {
        if(epgGuideLayout!=null){
            epgChannel.removeAllViews();
            epgWeek.removeAllViews();
            epgGuideLayout=null;
        }
        epgGuideLayout=(LinearLayout) mInflater.inflate(R.layout.epg_guide, null);
        epgWeek=(EpgWeekView)epgGuideLayout.findViewById(R.id.week_day);
        epgChannel=(EpgChannelFrame)epgGuideLayout.findViewById(R.id.channel);
        actor_con=(TextView)epgGuideLayout.findViewById(R.id.actor_con);
        actor=(TextView)epgGuideLayout.findViewById(R.id.actor);
        type_con=(TextView)epgGuideLayout.findViewById(R.id.type_con);
        type=(TextView)epgGuideLayout.findViewById(R.id.type);
        about_con=(TextView)epgGuideLayout.findViewById(R.id.about_con);
        poster=(ImageView)epgGuideLayout.findViewById(R.id.poster);
        mTime=(TextView)epgGuideLayout.findViewById(R.id.time);
        mNotify = (TextView)epgGuideLayout.findViewById(R.id.ca_tuner_notify);
        mBcBackground = epgGuideLayout.findViewById(R.id.epg_bc_bg);
        
        tip_layout = (RelativeLayout) epgGuideLayout.findViewById(R.id.tip_layout);
        epgWeek.setChannelView(epgChannel);
        epgChannel.doc=doc;
        epgChannel.setEpgWeekView(epgWeek);
        epgChannel.setViewController(mViewController);
        epgChannel.setGuideWindow(this);
        epgChannel.setActivity(mActivity);
        epgWeek.setGuideWindow(this);
        epgWeek.setActivity(mActivity);
        epgWeek.init();
        if(isTSMode){
        	TextView dataFromNetTextView = (TextView) epgGuideLayout.findViewById(R.id.data_from_net_text);
        	mFocusImageView = (ImageView) epgGuideLayout.findViewById(R.id.flow_img);
        	View view = epgGuideLayout.findViewById(R.id.detail_layout);
        	view.setVisibility(View.GONE);
        	dataFromNetTextView.setVisibility(View.GONE);
        	mFocusImageView.setVisibility(View.INVISIBLE);
        	isPlay = false;
        }else{
        	tip_layout.setVisibility(View.GONE);
        	epgChannel.init();
        	epgChannel.onfocusView();
        }
        if(epgGuideWindow == null)
            epgGuideWindow = new PopupWindow();
        epgGuideWindow.setContentView(epgGuideLayout);
        epgGuideWindow.setWidth((int)mActivity.getResources().getDimension(R.dimen.program_guide_popupwindow_width));
        epgGuideWindow.setHeight((int)mActivity.getResources().getDimension(R.dimen.program_guide_popupwindow_height));
        epgGuideWindow.setFocusable(true);
        epgGuideWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        setIsShow(true);
        if(isTSMode){
        	show();
        }
        new TimeThread().start();
    }
    
    /**
     * 检查缓存中预约是否过期
     */
    public void checkOrderList(){
        refreshUtcTime=true;
        long utc=getUtcTime()/1000;
        ADTVService.getService().getEpg().checkReservesList((int)utc);
    }
    
    protected void dismiss(boolean isToHome) {
    	if(mAlertDialog != null && mAlertDialog.isShowing()){
    		mAlertDialog.dismiss();
    	}
    	if(mExitDialog != null && mExitDialog.isShowing()){
    		mExitDialog.dismiss();
    	}
        if(epgGuideWindow!=null && epgGuideWindow.isShowing()){
            setIsShow(false);
            clearDialog();
            mViewController.cancelEPGSearch();
            String mode = getCurrentOutputResolution();
            final int[] position = DvbUtil.getPosition(mode);

            if(!isPlay){
                log.D("!isPlay,call play()");
            	mViewController.playFromEpg(position[0],position[1],position[0] + position[2],position[1] + position[3],DisplayMode.DISPLAYMODE_NORMAL);
            }else{
                log.D("isPlay,call setWindowSize().");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!mViewController.isPause()){
                            mViewController.resetDvbDisplayMode();
                            mSettingManager.nativeSetVideoWindow(position[0],position[1],position[0] + position[2],position[1] + position[3]);
                        }
                    }
                }, 300);
            }
            mViewController.byPassDeinterlace(false);
            mViewController.resetMonitor();
            mViewController.showBlankView();
            if(mLoadingDialog!= null && mLoadingDialog.isShowing()){
                mLoadingDialog.dismiss();
            }
            epgGuideWindow.dismiss();
            if(isToHome){
                mViewController.finish();
            }
            else	//chaidandan
            {
                mViewController.refreshDVBNotify();
            }
        }
    }
    
    public void setIsShow(boolean flag) {
        this.isShow = flag;
    }
    
    public void clearProgramInfo(int programId){
        this.programId=programId;
        actor_con.setText("");
        type_con.setText("");
        about_con.setText("");
        poster.setImageBitmap(null);
    }
    
    public void clearDialog(){
        if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
            mAlertPopupWindow.dismiss();
        }
    }
    
    public void showMenu(){
        dismiss(false);
        if(mViewController!=null)
            mViewController.showMainMenu();
    }
    
    public int programStatus(NETEventInfo info){
        if(null==info){
            log.D("----------------------------error---info is null");
            return Status_Error;
        }
        long startTime=info.getBegintime();
        long endTime=info.getBegintime()+info.getDuration();
        long tsTime=getUtcTime();
        long utcTime =tsTime + TimeOffset;
//        Log.d("songwenxuan","duration = " + info.getDuration());
//        Log.d("songwenxuan","startTime = " +startTime +"  format =" + DateFormatUtil.getTimeFromMillis(startTime) + "***************************** program name = " + info.getEname());
//        Log.d("songwenxuan","endTime = " + endTime +" format ="+ DateFormatUtil.getTimeFromMillis(endTime) + "*****************************");
//        Log.d("songwenxuan","tsTime = " + tsTime +" format ="+ DateFormatUtil.getTimeFromMillis(tsTime) + "*****************************");
//        Log.d("songwenxuan","utcTime = " + utcTime +" format ="+ DateFormatUtil.getTimeFromMillis(utcTime) + "*****************************");
        
        if(endTime <= utcTime){//过期节目
//        	Log.d("songwenxuan","过期节目");
            return Status_End;
        }else if(startTime < utcTime && endTime >= utcTime ){//正在播放
//        	Log.d("songwenxuan","正在播放");
            return Status_Ing;
        }else{//可以预约
//        	Log.d("songwenxuan","可以预约");
            return Status_Future;
        }
    }
    
    public void showAlertDialog(final NETEventInfo info,final View program){
        refreshUtcTime=true;
        //||info.getProgramId()<=0
        if(info==null||info.getEname()==null||info.getEname().trim().equals("")){
            showAlertPop(R.string.program_reserve_no_program_text,false);
            return;
        }
        Cursor tProgramReserveCursor = null;
        Cursor query =null;
        try{
		tProgramReserveCursor = mActivity.getContentResolver().query(
                Channel.URI.TABLE_RESERVES,null, null, null, null);
        int tProgramReserveCount = tProgramReserveCursor.getCount();
        long startTime=info.getBegintime();
        long endTime=info.getBegintime()+info.getDuration();
        long tsTime=getUtcTime();
        long utcTime =tsTime + TimeOffset;
        log.D("-------startTime="+startTime+";endTime="+endTime+";utcTime="+utcTime);
        log.D("-------startTime="+DateFormatUtil.getStringFromMillis(startTime)+";endTime="+DateFormatUtil.getStringFromMillis(endTime)+";utcTime="+DateFormatUtil.getStringFromMillis(utcTime));
        int status=programStatus(info);
        log.D("----------------status="+status);
        if(status==Status_End){//过期节目
            showAlertPop(R.string.program_reserve_time_out,false);
        }else if(status==Status_Ing){//正在播放
            dismiss(false);
            mViewController.switchChannelFromNum(info.getLogicNumer());
        }else{//可以预约
        		query = mActivity.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, "startTime=? and serviceId=?",new String[]{""+((startTime-TimeOffset)/1000),""+info.getServiceId()}, null);
            View epgNotifyView=null;
            epgNotifyView = mActivity.getLayoutInflater().inflate(R.layout.order_dialog_layout, null);
            TextView textView = (TextView) epgNotifyView.findViewById(R.id.epg_alert_title);
            Button confirmBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_confirm_btn);
            Button cancleBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_cancle_btn);
            log.D("---------query.getCount="+query.getCount());
            if(query.getCount() == 0){//没有预约
            	if(tProgramReserveCount >= 40){
            		showAlertPop(R.string.program_reserve_max_text,false);
//            		mAlertDialog.dismiss();
            		tProgramReserveCursor.close();
            		return;
            	}
                confirmBtn.setText(mActivity.getResources().getString(R.string.program_reverse_alert));
                textView.setText(R.string.program_reverse_alert_message);
                confirmBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        log.D("----------serviceId="+info.getServiceId()+";channelName="+info.getChannelName()+";programName="+info.getEname());
                        
                        long startTime=info.getBegintime();
                        long endTime=info.getBegintime()+info.getDuration();
                        String programName = info.getEname();
                        ContentValues values = new ContentValues();
                        values.put(Channel.TableReservesColumns.PROGRAMNAME, programName);
                        values.put(Channel.TableReservesColumns.SERVICEID, info.getServiceId());
                        values.put(Channel.TableReservesColumns.CHANNELNUMBER, info.getLogicNumer());
                        values.put(Channel.TableReservesColumns.CHANNELNAME, info.getChannelName());
                        values.put(Channel.TableReservesColumns.ENDTIME, endTime);
                        values.put(Channel.TableReservesColumns.STARTTIME, (startTime-TimeOffset)/1000);
                        values.put(Channel.TableReservesColumns.PROGRAMID, info.getProgramId());
                        values.put(Channel.TableReservesColumns.RESERVESTATUS, RESERVE_STATUS_ON);

                        long utcTime = getUtcTime() + TimeOffset;
                        long timeCompensate = System.currentTimeMillis() - utcTime;
                        log.D("current time=" + System.currentTimeMillis());
                        log.D("utctime=" + utcTime);
                        log.D("timecompensate=" + timeCompensate);
                        Uri uri = mActivity.getContentResolver().insert(Channel.URI.TABLE_RESERVES, values);
                        ImageView programReserveTagView = (ImageView) program.findViewById(R.id.order_icon);
                        programReserveTagView.setImageResource(R.drawable.order);
                        programReserveTagView.setVisibility(View.VISIBLE);
                        
                        Program pro=new Program();
                        pro.setId(Integer.valueOf(uri.getLastPathSegment()));
                        pro.setChannelName(info.getChannelName());
                        pro.setChannelNumber(info.getLogicNumer());
                        pro.setEndTime(endTime);
                        pro.setName(programName);
                        pro.setProgramId(info.getProgramId());
                        pro.setStartTime(startTime);
                        pro.setServiceId(info.getServiceId());
                        pro.setStatus(RESERVE_STATUS_ON);
                        ADTVService.getService().getEpg().addProgram(String.valueOf(info.getServiceId())+String.valueOf((startTime-TimeOffset)/1000), pro);
                        
                        addReServeProgramToAlam(Integer.valueOf(uri.getLastPathSegment()),startTime + timeCompensate- 60 * 1000);
                        log.D("reserve success! reserve date="+new Date(startTime + timeCompensate - 60 * 1000).toLocaleString());
                        //
                        log.D("current timee=" + new Date().toLocaleString());
                        mAlertDialog.dismiss();
                        showAlertPop(R.string.program_reserve_success,true);
                    }
                });
            }else{//已预约
                query.moveToFirst();
                final int tProgramReserveId = query.getInt(query.getColumnIndex(Channel.TableReservesColumns.ID));
                log.D("----------have order tProgramReserveId="+tProgramReserveId);
                confirmBtn.setText(mActivity.getResources().getString(R.string.program_reverse_alert_cancel));
                textView.setText(R.string.program_reserve_list_cancel);
                confirmBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mActivity.getContentResolver().delete(Channel.URI.TABLE_RESERVES, Channel.TableReservesColumns.ID+"=?", new String[]{tProgramReserveId+""});
                        removeReserveProgramFromAlarm(tProgramReserveId);
                        ADTVService.getService().getEpg().removeProgram(String.valueOf(info.getServiceId())+String.valueOf((info.getBegintime()-TimeOffset)/1000));
                        mAlertDialog.dismiss();
                        View programReserveTagView = program.findViewById(R.id.order_icon);
                        programReserveTagView.setVisibility(View.INVISIBLE);
                        showAlertPop(R.string.program_reverse_alert_cancel_success,true);
                    }
                });
            }
            cancleBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
            if(mAlertDialog == null){
                mAlertDialog = new Dialog(mActivity,R.style.epgActivityTheme);
            }
                final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_width);
                final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_height);
//                mAlertDialog.setContentView(epgNotifyView,new LayoutParams(windowWidth, windowHeight));
            mAlertDialog.setContentView(epgNotifyView,new LinearLayout.LayoutParams(windowHeight, windowWidth));
            mAlertDialog.show();
        	}
        }finally{
        	if(null!=tProgramReserveCursor&&!tProgramReserveCursor.isClosed()){
        		tProgramReserveCursor.close();
        	}
        	if(null!=query&&!query.isClosed()){
        		query.close();
        	}
        }
        
    }
    
    public static long UTCTime;
    public static boolean refreshUtcTime=true;
    
    public long getUtcTime() {
        if(refreshUtcTime){
            refreshUtcTime=false;
            String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
            String[] utcTime = utcTimeStr.split(":");
            long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
            UTCTime=currentTimeMillis;
            return UTCTime;            
        }else{
            return UTCTime;
        }
//        log.D("---getUtcTime--currentTimeMillis="+currentTimeMillis);
//        return currentTimeMillis;
    }
    
    /** 添加预约闹钟 */
    private void addReServeProgramToAlam(int id, long startTime){
        log.D("addReServeProgramToAlam() -- reserveId=" + id + ", startTime=" + startTime);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }
    /** 删除预约闹钟 */
    private void removeReserveProgramFromAlarm(int id) {
        Log.d("songwenxuan","remove alarm id = "+id);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    /** 提示 */
    private void showAlertPop(int id,boolean showIcon){ 
        if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
            mAlertPopupWindow.dismiss();
        }
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View tView = inflater.inflate(R.layout.order_result_dialog_layout, null);
//        ImageView img=(ImageView)tView.findViewById(R.id.result_img);
//        if(!showIcon){
//            img.setVisibility(View.GONE);
//        }
        TextView text = (TextView) tView.findViewById(R.id.result_txt);
        text.setText(mActivity.getResources().getString(id));
        if(mAlertPopupWindow == null){
            mAlertPopupWindow = new PopupWindow(tView);
        }
        mAlertPopupWindow.setContentView(tView);
        mAlertPopupWindow.setWidth((int)mActivity.getResources().getDimension(R.dimen.dvb_notify_window_width));
        mAlertPopupWindow.setHeight((int)mActivity.getResources().getDimension(R.dimen.dvb_notify_window_height));
        mAlertPopupWindow.setFocusable(false);
        log.D("show alert toast");
        mAlertPopupWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER_HORIZONTAL, 0, 0);
        handler.removeMessages(MSG_Close_Window);
        handler.sendEmptyMessageDelayed(MSG_Close_Window, 3000);
    }
    
    protected void onDocGotResource(ADTVResource res){
        log.D("--------onDocGotResource-----servicesid="+res.getID()+";programList.size()="+res.getArrayList().size()+";programId="+programId);
        if (isShow) {
            if(res.getType()==RES_ProgramList){
                epgChannel.refreshProgramFrame(res.getID(),res.getArrayList(),res.getHashMap());
                return;
            }
            if(res.getID()!=programId)
                return;
            switch (res.getType()) {
                case RES_Bitmap:
                    poster.setImageBitmap(res.getBitmap());
                    break;
                case RES_Actor:
                    actor_con.setText(res.getString());
                    break;
                case RES_Type:
                    type_con.setText(res.getString());
                    break;
                case RES_About:
                    about_con.setText(res.getString());
                    break;
                case RES_Nibble:
                    if(res.getInt()==Type_Move){
//                        actor.setText(mActivity.getResources().getString(R.string.actor));
                        type.setText(mActivity.getResources().getString(R.string.type));
                    }else if(res.getInt()==Type_Other){
//                        actor.setText(mActivity.getResources().getString(R.string.playout));
                        type.setText(mActivity.getResources().getString(R.string.presenter));
                    }
                    break;
            }
        }
    }
    
//    public void test(){
//    	mSettingManager.nativeSetVideoWindow(98,527, 254, 139);
//    }
    
    private Dialog mLoadingDialog;
    private boolean isPlay;
    private static final int MAIN_FRE_PARAMS_ERROR = -1;
    private static final int MAIN_FRE_LOCK_ERROR = -2;
    private static final int ALREADY_SEARCHED = -3;
    private static final int TV_LEFT = 82;
    private static final int TV_TOP = 520; 
    private static final int TV_RIGHT = 364;
    private static final int TV_BOTTOM = 670;
    private void show(){
    	mViewController.setEPGSourceMode(isTSMode);
    	mViewController.stopFromEpg();
    	Log.d("songwenxuan","startEPGSearch()  start.....");
        int ret = mViewController.startEPGSearch(new Transponder(DefaultTpValue.FREQUENCY,
                DefaultTpValue.SYMBOL_RATE, DefaultTpValue.MODULATION), 1);
    	Log.d("songwenxuan","startEPGSearch()======ret = " + ret);
    	switch (ret) {
			case MAIN_FRE_PARAMS_ERROR:
				showErrorPop(R.string.error_main_fre);
				return;
			case MAIN_FRE_LOCK_ERROR:
				showErrorPop(R.string.error_main_lock);
				return;
			case ALREADY_SEARCHED:
				epgChannel.init();
				epgChannel.onfocusView();
				String mode = getCurrentOutputResolution();
				final int[] position = getPosition(mode, TV_LEFT, TV_TOP, TV_RIGHT, TV_BOTTOM);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mViewController.playFromEpg(position[0],position[1],position[2],position[3],DisplayMode.DISPLAYMODE_16TO9);
						isPlay = true;
					}
				}, 500);
//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						log.D("mSettingManager.nativeSetVideoWindow(98,527,352,666)");
//						mSettingManager.nativeSetVideoWindow(98,527,352,666);
//					}
//				}, 600);
				return;
		}
    	Log.d("songwenxuan","startEPGSearch()  end.....");
    	mLoadingDialog = new Dialog(mActivity,R.style.alertDialogTheme);
    	View view = mInflater.inflate(R.layout.alert_dialog_no_button_layout, null);
    	TextView textview = (TextView) view.findViewById(R.id.alert_text);
    	textview.setText(R.string.week_epg_loading);
    	int width = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
    	int height = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
    	mLoadingDialog.setContentView(view, new LinearLayout.LayoutParams(width, height));
    	mLoadingDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_UP){
					if(keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK){
//					mViewController.cancelEPGSearch();
						String mode = getCurrentOutputResolution();
						int[] position = getPosition(mode, TV_LEFT, TV_TOP, TV_RIGHT, TV_BOTTOM);
						mViewController.playFromEpg(position[0],position[1],position[2],position[3],DisplayMode.DISPLAYMODE_16TO9);
						isPlay = true;
//		        	new Handler().postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							mSettingManager.nativeSetVideoWindow(98,527,352,666);
//						}
//					}, 300);
//					dialog.dismiss();
						return true;
					}else if(keyCode == 268){
						dismiss(false);
						return true;
					} else if (keyCode == 269) {
						dismiss(false);
						Intent lookBackIntent = new Intent();
						Bundle lookBackBundle = new Bundle();
						lookBackBundle.putInt(LookBackActivity.FROM_WHERE,
								LookBackActivity.DVB_MAIN_ACTIVITY);
						lookBackIntent.putExtras(lookBackBundle);
						if (mActivity != null) {
							lookBackIntent.setClass(mActivity, LookBackActivity.class);
							mActivity.startActivity(lookBackIntent);
						}
						return true;
					} else {
						return true;
					}
				}else{
					return false;
				}
			}
		});
    	mLoadingDialog.show();
    }
    
    public void refreshNotify(Object[] caTunerStatus) {
        boolean isTunerEnable = (Boolean) caTunerStatus[2];
        int caParam = (Integer) caTunerStatus[3];
        mNotify.setText("");
        if (isTunerEnable && caParam==0) {
            log.D("DVBErrorNotify. dismiss ErrorWindow");
            mNotify.setText("");
        } else {
            if (!isTunerEnable) {
                mNotify.setText(R.string.dvb_no_signal);
            } else if (caParam != 0 && caParam != -1) {
                String notifyStr = getCaNotifyString(caParam);
                mNotify.setText(notifyStr);
            }
            log.D("DVBErrorNotifyFromEpg. show ErrorWindow");
        }
    }
    
    /** 提示 */
    private void showErrorPop(int id){ 
        if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
            mAlertPopupWindow.dismiss();
        }
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View tView = inflater.inflate(R.layout.order_result_dialog_layout, null);
        TextView text = (TextView) tView.findViewById(R.id.result_txt);
        text.setText(mActivity.getResources().getString(id));
//        ImageView img=(ImageView)tView.findViewById(R.id.result_img);
//        img.setVisibility(View.GONE);
        if(mAlertPopupWindow == null){
            mAlertPopupWindow = new PopupWindow(tView);
        }
        mAlertPopupWindow.setContentView(tView);
        mAlertPopupWindow.setWidth((int)mActivity.getResources().getDimension(R.dimen.dvb_notify_window_width));
        mAlertPopupWindow.setHeight((int)mActivity.getResources().getDimension(R.dimen.dvb_notify_window_height));
        mAlertPopupWindow.setFocusable(false);
        log.D("show alert toast");
        mAlertPopupWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER_HORIZONTAL, 0, 0);
        handler.removeMessages(MSG_Close_Window);
    }
    
    
    private static final int UPDATE_TIME=1;
    public static long time;
    private TextView mTime;
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TIME:
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
                    EpgGuideWindow.refreshUtcTime=true;
                    Date currentDate = new Date(getUtcTime()+EpgGuideWindow.TimeOffset);
                    mTime.setText(sdf.format(currentDate));
                    break;
                default:
                    break;
            }
        }
    };

	private ImageView mFocusImageView;

	private RelativeLayout tip_layout;

    private TextView mNotify;
    
    public class TimeThread extends Thread {
        @Override
        public void run () {
            do {
                try {
                    Message msg = new Message();
                    msg.what = UPDATE_TIME;
                    mHandler.sendMessage(msg);
                    Thread.sleep(60000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(true);
        }
    }
    
    public String getCaNotifyString(int notify){
        
        switch(notify){
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_badcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE:
            
            return mActivity.getString(R.string.ca_message_blackout_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE:
            
            return mActivity.getString(R.string.ca_message_callback_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE:
            
            return mActivity.getString(R.string.ca_message_cancel_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE:
            
            return mActivity.getString(R.string.ca_message_decryptfail_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_errcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE:
            
            return mActivity.getString(R.string.ca_message_errregion_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_expicard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE:
            
            return mActivity.getString(R.string.ca_message_freeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_insertcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE:
            
            return mActivity.getString(R.string.ca_message_lowcardver_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE:
            
            return mActivity.getString(R.string.ca_message_maxrestart_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE:
            
            return mActivity.getString(R.string.ca_message_needfeed_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE:
            
            return mActivity.getString(R.string.ca_message_noentitle_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE:
            
            return mActivity.getString(R.string.ca_message_nomoney_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE:
            
            return mActivity.getString(R.string.ca_message_nooper_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE:
            
            return mActivity.getString(R.string.ca_message_outworktime_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE:
            
            return mActivity.getString(R.string.ca_message_pairing_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE:
            
            return mActivity.getString(R.string.ca_message_stbfreeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE:
            
            return mActivity.getString(R.string.ca_message_stblocked_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE:
            
            return mActivity.getString(R.string.ca_message_update_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE:
            
            return mActivity.getString(R.string.ca_message_viewlock_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE:
            
            return mActivity.getString(R.string.ca_message_watchlevel_type);
        }
        
        return null;
    }
    
    private final String[] mOutputModeList = 
        {
            "480i",  
            "480p",
            "576i",  
            "576p",         
            "720p",
            "1080i", 
            "1080p",
            "720p50hz",
            "1080i50hz",
            "1080p50hz"
        };
    
    private int[] getPosition(String mode,int left,int top,int right,int bottom) {
        int[] curPosition = {
                left, top, right, bottom
        };
        int index = 4; // 720p
        for (int i = 0; i < mOutputModeList.length; i++) {
            if (mode.equalsIgnoreCase(mOutputModeList[i]))
                index = i;
        }
        switch (index) {
            case 0: // 480i
                break;
            case 1: // 480p
                break;
            case 2: // 576i
                curPosition[0] = (int)(left/1.777777778);
                curPosition[1] = (int)(top/1.25);
                curPosition[2] = (int)(right/1.777777778);//720*576
                curPosition[3] = (int)(bottom/1.25);
                break;
            case 3: // 576p
                curPosition[0] = (int)(left/1.777777778);
                curPosition[1] = (int)(top/1.25);
                curPosition[2] = (int)(right/1.777777778);
                curPosition[3] = (int)(bottom/1.25);
                break;
            case 4: // 720p
            case 7:
                curPosition[0] = left;
                curPosition[1] = top;
                curPosition[2] = right;
                curPosition[3] = bottom;
                break;
            case 5: // 1080i
            case 8:
                curPosition[0] = (int) (left*1.5);
                curPosition[1] = (int) (top*1.5);
                curPosition[2] = (int) (right*1.5);
                curPosition[3] = (int) (bottom*1.5);
                break;
            case 6: // 1080p
            case 9:
                curPosition[0] = (int) (left*1.5);
                curPosition[1] = (int) (top*1.5);
                curPosition[2] = (int) (right*1.5);
                curPosition[3] = (int) (bottom*1.5);
                break;
            default: // 720p
                curPosition[0] = left;
                curPosition[1] = top;
                curPosition[2] = right;
                curPosition[3] = bottom;
                break;
        }
        return curPosition;
    }
    
    public final String mCurrentResolution = "/sys/class/display/mode";
    public String getCurrentOutputResolution()
    {
        String currentMode = null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(mCurrentResolution);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(fileReader);

        try
        {
            currentMode = bufferedReader.readLine();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            bufferedReader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            fileReader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return currentMode;
    }
    private Dialog mExitDialog;

    private Button btOk;
  	public void showExitDialog() {
  		log.D("===========enter showExitDialog==================");
  		if(mExitDialog == null){
  			mExitDialog = new Dialog(mActivity,R.style.alertDialogTheme);
  			View view  = View.inflate(mActivity, R.layout.order_dialog_layout, null);
  			TextView tv = (TextView) view.findViewById(R.id.epg_alert_title);
  			tv.setText(R.string.epg_confirm_exit);
  			btOk = (Button) view.findViewById(R.id.epg_alert_confirm_btn);
  			Button btCancel = (Button) view.findViewById(R.id.epg_alert_cancle_btn);
  			btOk.setText(R.string.ok);
  			btOk.setOnClickListener(new OnClickListener() {
  				@Override
  				public void onClick(View v) {
  					log.D("===========start dismiss EpgGuideWindow==================");
  					mExitDialog.dismiss();
  					dismiss(false);
  					log.D("===========end dismiss EpgGuideWindow==================");
  				}
  			});
  			btCancel.setOnClickListener(new OnClickListener() {
  				@Override
  				public void onClick(View v) {
  					mExitDialog.dismiss();
  				}
  			});
  			int width = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_width);
  			int height = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_height);
  			mExitDialog.setContentView(view, new LayoutParams(width, height));
  		}
  		if(mExitDialog.isShowing())
  			return;
  		btOk.requestFocus();
		mExitDialog.show();
	}
}
