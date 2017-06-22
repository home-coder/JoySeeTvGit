/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/ca/ICaSettingService.aidl
 */
package com.joysee.adtv.aidl.ca;
/**
 * 在这里定义了一些于Ca卡有关的接口用于在CaService中调用
 */
public interface ICaSettingService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.ca.ICaSettingService
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.ca.ICaSettingService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.ca.ICaSettingService interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.ca.ICaSettingService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.ca.ICaSettingService))) {
return ((com.joysee.adtv.aidl.ca.ICaSettingService)iin);
}
return new com.joysee.adtv.aidl.ca.ICaSettingService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getWatchLevel:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getWatchLevel();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setWatchLevel:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _result = this.setWatchLevel(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setWatchTime:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
int _result = this.setWatchTime(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getWatchTime:
{
data.enforceInterface(DESCRIPTOR);
int[] _result = this.getWatchTime();
reply.writeNoException();
reply.writeIntArray(_result);
return true;
}
case TRANSACTION_changePincode:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.changePincode(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getCardSN:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCardSN();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getAuthorization:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.util.List _result = this.getAuthorization(_arg0);
reply.writeNoException();
reply.writeList(_result);
return true;
}
case TRANSACTION_getOperatorID:
{
data.enforceInterface(DESCRIPTOR);
java.util.List _result = this.getOperatorID();
reply.writeNoException();
reply.writeList(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.ca.ICaSettingService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * 获取观看等级
     * @return 等级
     */
@Override public int getWatchLevel() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWatchLevel, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 设置观看等级
     * 
     * @param pin pin密码
     * @param level
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
@Override public int setWatchLevel(java.lang.String pin, int level) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pin);
_data.writeInt(level);
mRemote.transact(Stub.TRANSACTION_setWatchLevel, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 设置工作时段 时分 需密码
     * @param pwd pin密码
     * @param iStarthour
     * @param iStartMin
     * @param iStartSec
     * @param iEndHour
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
@Override public int setWatchTime(java.lang.String pwd, int iStarthour, int iStartMin, int iEndHour, int iEndMin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pwd);
_data.writeInt(iStarthour);
_data.writeInt(iStartMin);
_data.writeInt(iEndHour);
_data.writeInt(iEndMin);
mRemote.transact(Stub.TRANSACTION_setWatchTime, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 获取工作时段
     * @return int [flag,startHour,startMin,endHour,endMin]
     */
@Override public int[] getWatchTime() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getWatchTime, _data, _reply, 0);
_reply.readException();
_result = _reply.createIntArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
@Override public int changePincode(java.lang.String oldPwd, java.lang.String newPwd) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(oldPwd);
_data.writeString(newPwd);
mRemote.transact(Stub.TRANSACTION_changePincode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 获取卡序列号，成功返回序号字符串，失败返回空字符串
     */
@Override public java.lang.String getCardSN() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCardSN, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 获取授权信息列表
     * @param operID 运营商ID
     * @return Map
     */
@Override public java.util.List getAuthorization(int operID) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(operID);
mRemote.transact(Stub.TRANSACTION_getAuthorization, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readArrayList(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * 获取运营商ID 
     * @return 运营商ID数组
     */
@Override public java.util.List getOperatorID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getOperatorID, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readArrayList(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getWatchLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_setWatchLevel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setWatchTime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getWatchTime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_changePincode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getCardSN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getAuthorization = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getOperatorID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
}
/**
     * 获取观看等级
     * @return 等级
     */
public int getWatchLevel() throws android.os.RemoteException;
/**
     * 设置观看等级
     * 
     * @param pin pin密码
     * @param level
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
public int setWatchLevel(java.lang.String pin, int level) throws android.os.RemoteException;
/**
     * 设置工作时段 时分 需密码
     * @param pwd pin密码
     * @param iStarthour
     * @param iStartMin
     * @param iStartSec
     * @param iEndHour
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
public int setWatchTime(java.lang.String pwd, int iStarthour, int iStartMin, int iEndHour, int iEndMin) throws android.os.RemoteException;
/**
     * 获取工作时段
     * @return int [flag,startHour,startMin,endHour,endMin]
     */
public int[] getWatchTime() throws android.os.RemoteException;
/**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
public int changePincode(java.lang.String oldPwd, java.lang.String newPwd) throws android.os.RemoteException;
/**
     * 获取卡序列号，成功返回序号字符串，失败返回空字符串
     */
public java.lang.String getCardSN() throws android.os.RemoteException;
/**
     * 获取授权信息列表
     * @param operID 运营商ID
     * @return Map
     */
public java.util.List getAuthorization(int operID) throws android.os.RemoteException;
/**
     * 获取运营商ID 
     * @return 运营商ID数组
     */
public java.util.List getOperatorID() throws android.os.RemoteException;
}
