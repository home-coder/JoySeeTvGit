/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/OnSearchTunerSignalStateListener.aidl
 */
package com.joysee.adtv.aidl;
public interface OnSearchTunerSignalStateListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.OnSearchTunerSignalStateListener
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.OnSearchTunerSignalStateListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.OnSearchTunerSignalStateListener interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.OnSearchTunerSignalStateListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.OnSearchTunerSignalStateListener))) {
return ((com.joysee.adtv.aidl.OnSearchTunerSignalStateListener)iin);
}
return new com.joysee.adtv.aidl.OnSearchTunerSignalStateListener.Stub.Proxy(obj);
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
case TRANSACTION_onSearchTunerSignalState:
{
data.enforceInterface(DESCRIPTOR);
com.joysee.adtv.logic.bean.TunerSignal _arg0;
if ((0!=data.readInt())) {
_arg0 = com.joysee.adtv.logic.bean.TunerSignal.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onSearchTunerSignalState(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.OnSearchTunerSignalStateListener
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
@Override public void onSearchTunerSignalState(com.joysee.adtv.logic.bean.TunerSignal tunerSignal) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((tunerSignal!=null)) {
_data.writeInt(1);
tunerSignal.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onSearchTunerSignalState, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSearchTunerSignalState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onSearchTunerSignalState(com.joysee.adtv.logic.bean.TunerSignal tunerSignal) throws android.os.RemoteException;
}
