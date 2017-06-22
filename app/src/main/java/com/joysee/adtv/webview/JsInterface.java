package com.joysee.adtv.webview;

public interface JsInterface {
	/**
	 * 播放回看
	 * @param url
	 */
	public void setplayurl(String url);
	/**
	 * 
	 * @return 全小写的MAC地址字符串
	 */
	public String GetMACAddress();
	/**
	 * 
	 * @param freq
	 * @param sym
	 * @param qam
	 * @return TSID
	 */
	public String GetTSID(int freq,int sym,int qam); 
	
	public void ExitBrowser();
	
	public void onMainPageReady();
}
