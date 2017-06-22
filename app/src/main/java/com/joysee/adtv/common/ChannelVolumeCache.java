
package com.joysee.adtv.common;

import com.joysee.adtv.logic.bean.DvbService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class ChannelVolumeCache {
	private static final DvbLog log = new DvbLog(
            "ChannelVolumeCache", DvbLog.DebugType.D);

	public final static String PREFERENCE_NAME = "ChannelVolumeCache";
	

	public static int getVolume(Context context, DvbService service) {
		log.D("getVersion begin...");
		String channelName = service.getChannelName();
		String serviceId = service.getServiceId() + "";
		log.D("channelName = " + channelName + " serviceId = " + serviceId);
		try {
			context = context.createPackageContext(context.getPackageName(), Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			log.D("Cound not find application...");
		}
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_WORLD_READABLE);
		int volume = share.getInt(channelName, 0);
		log.D("getVolume from xml Volume = " + volume);
		return volume;
	}

	public static void saveVolume(Context context, DvbService service, int volume) {
		log.D("saveVolume begin...");
		String channelName = service.getChannelName();
		String serviceId = service.getServiceId() + "";
		log.D("channelName = " + channelName + " serviceId = " + serviceId);
		try {
			context = context.createPackageContext(context.getPackageName(), Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			log.D("Cound not find application...");
		}
		SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = share.edit();

		editor.putInt(channelName, volume);
		editor.commit();

		log.D("saveVolume to xml Volume = " + volume);
	}
}
