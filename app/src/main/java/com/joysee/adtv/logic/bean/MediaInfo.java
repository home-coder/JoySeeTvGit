package com.joysee.adtv.logic.bean;

public class MediaInfo {
	private String begintime;
	private String endtime;
	private String curtime;//当前直播时间
	private String preprg;//上一个节目名称
	private String nextprg;//下一个节目名称
	private String curprg;//当前节目名称
	public String getBegintime() {
		return begintime;
	}
	public void setBegintime(String begintime) {
		this.begintime = begintime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getCurtime() {
		return curtime;
	}
	public void setCurtime(String curtime) {
		this.curtime = curtime;
	}
	public String getPreprg() {
		return preprg;
	}
	public void setPreprg(String preprg) {
		this.preprg = preprg;
	}
	public String getNextprg() {
		return nextprg;
	}
	public void setNextprg(String nextprg) {
		this.nextprg = nextprg;
	}
	public String getCurprg() {
		return curprg;
	}
	public void setCurprg(String curprg) {
		this.curprg = curprg;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return " begintime = " + begintime + " endtime = " + endtime
				+ " curtime = " + curtime + " curprg = " + curprg
				+ " nextprg = " + nextprg + " preprg = " + preprg;
	}
}
