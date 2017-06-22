/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/OnSearchReceivedNewChannelsListener.aidl
 */
package com.joysee.adtv.aidl;
public interface OnSearchReceivedNewChannelsListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener))) {
return ((com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener)iin);
}
return new com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener.Stub.Proxy(obj);
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
case TRANSACTION_onSearchReceivedNewChannelsListener:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.joysee.adtv.logic.bean.DvbService> _arg0;
_arg0 = data.createTypedArrayList(com.joysee.adtv.logic.bean.DvbService.CREATOR);
boolean _result = this.onSearchReceivedNewChannelsListener(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener
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
@Override public boolean onSearchReceivedNewChannelsListener(java.util.List<com.joysee.adtv.logic.bean.DvbService> services) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(services);
mRemote.transact(Stub.TRANSACTION_onSearchReceivedNewChannelsListener, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_onSearchReceivedNewChannelsListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public boolean onSearchReceivedNewChannelsListener(java.util.List<com.joysee.adtv.logic.bean.DvbService> services) throws android.os.RemoteException;
}
