/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/OnSearchEndListener.aidl
 */
package com.joysee.adtv.aidl;
public interface OnSearchEndListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.OnSearchEndListener
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.OnSearchEndListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.OnSearchEndListener interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.OnSearchEndListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.OnSearchEndListener))) {
return ((com.joysee.adtv.aidl.OnSearchEndListener)iin);
}
return new com.joysee.adtv.aidl.OnSearchEndListener.Stub.Proxy(obj);
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
case TRANSACTION_onSearchEnd:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.joysee.adtv.logic.bean.DvbService> _arg0;
_arg0 = data.createTypedArrayList(com.joysee.adtv.logic.bean.DvbService.CREATOR);
this.onSearchEnd(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.OnSearchEndListener
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
@Override public void onSearchEnd(java.util.List<com.joysee.adtv.logic.bean.DvbService> services) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(services);
mRemote.transact(Stub.TRANSACTION_onSearchEnd, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSearchEnd = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onSearchEnd(java.util.List<com.joysee.adtv.logic.bean.DvbService> services) throws android.os.RemoteException;
}
