package com.joysee.adtv.server;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.logic.EPGManager;
import com.joysee.adtv.ui.EpgGuideWindow;
import com.joysee.adtv.ui.EpgWeekView;

/**
 * 获取频道某时间段内所有节目id
 * @author chenggang
 *
 */
abstract public class ADTVProgramListTask extends ADTVTask{
	private static final String TAG="ADTVProgramListTask";
	
	public static final HashMap<String,ADTVProgramListTask> taskMap=new HashMap<String,ADTVProgramListTask>();
	
	public ArrayList<Integer> pidList=new ArrayList<Integer>();
	public int serviceId;
	public int channelNumber;
	public long startTime;
	public long endTime;
	EPGManager mEPGManager=null;

	public ADTVProgramListTask(int channelNumber,int serviceId,long begin,long end){
		super(null, ADTVTask.PRIO_JNI);
		mEPGManager = EPGManager.getInstance();
		this.serviceId=serviceId;
		this.endTime=end;
		this.startTime=begin;
		this.channelNumber=channelNumber;
		String key=String.valueOf(this.serviceId)+String.valueOf(startTime)+String.valueOf(endTime);
		ArrayList<Integer> list=getEpg().getProgramIdList(key);
//		Log.d(TAG, "----------list is "+list);
		if(taskMap.get(key)!=null){
		    return;
		}
		if(list!=null&&list.size()>0){
		    Log.d(TAG, "-----ADTVProgramListTask--have--serviceId="+serviceId+"------------list.size="+list.size());
		    pidList=list;
		    onSingal();
		    return;
		}
		taskMap.put(key, this);
		start();
	}

	void onCancel(){
	    String key=String.valueOf(this.serviceId)+String.valueOf(startTime)+String.valueOf(endTime);
	    taskMap.remove(key);
	}

	boolean process(){
		boolean ret = true;
		if(status==Status_Cancel){
		    Log.d(TAG, "----------is cancel- -----------");
		    return ret;
		}
		mEPGManager.nativeGetProgramIdListBySid(serviceId, startTime, endTime, pidList);
		Log.d(TAG, "startTime = " + DateFormatUtil.getTimeFromMillis(startTime) + " endTime = " + DateFormatUtil.getTimeFromMillis(endTime));
		String key=String.valueOf(serviceId)+String.valueOf(startTime)+String.valueOf(endTime);
		if(EpgGuideWindow.isTSMode){
			for (int i = 0; i < pidList.size(); i++) {
				int temp = pidList.get(i);
				temp = temp | ((serviceId & 0xffff) << 16);
				pidList.set(i, temp);
			}
		}
		getEpg().addProgramIdList(key, pidList);
//		Log.d(TAG, "---------process--key="+key+";pidList.size="+pidList.size());
		return ret;
	}
}

