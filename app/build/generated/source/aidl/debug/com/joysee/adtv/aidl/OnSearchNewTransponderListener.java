/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/OnSearchNewTransponderListener.aidl
 */
package com.joysee.adtv.aidl;
public interface OnSearchNewTransponderListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.OnSearchNewTransponderListener
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.OnSearchNewTransponderListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.OnSearchNewTransponderListener interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.OnSearchNewTransponderListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.OnSearchNewTransponderListener))) {
return ((com.joysee.adtv.aidl.OnSearchNewTransponderListener)iin);
}
return new com.joysee.adtv.aidl.OnSearchNewTransponderListener.Stub.Proxy(obj);
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
case TRANSACTION_onSearchNewTransponder:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onSearchNewTransponder(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.OnSearchNewTransponderListener
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
@Override public void onSearchNewTransponder(int frequency) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(frequency);
mRemote.transact(Stub.TRANSACTION_onSearchNewTransponder, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onSearchNewTransponder = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onSearchNewTransponder(int frequency) throws android.os.RemoteException;
}
