/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/ISearchService.aidl
 */
package com.joysee.adtv.aidl;
public interface ISearchService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.ISearchService
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.ISearchService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.ISearchService interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.ISearchService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.ISearchService))) {
return ((com.joysee.adtv.aidl.ISearchService)iin);
}
return new com.joysee.adtv.aidl.ISearchService.Stub.Proxy(obj);
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
case TRANSACTION_startSearch:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
com.joysee.adtv.logic.bean.Transponder _arg1;
if ((0!=data.readInt())) {
_arg1 = com.joysee.adtv.logic.bean.Transponder.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.startSearch(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_stopSearch:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.stopSearch(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setOnSearchEndListener:
{
data.enforceInterface(DESCRIPTOR);
com.joysee.adtv.aidl.OnSearchEndListener _arg0;
_arg0 = com.joysee.adtv.aidl.OnSearchEndListener.Stub.asInterface(data.readStrongBinder());
this.setOnSearchEndListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setOnSearchReceivedNewChannelsListener:
{
data.enforceInterface(DESCRIPTOR);
com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener _arg0;
_arg0 = com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener.Stub.asInterface(data.readStrongBinder());
this.setOnSearchReceivedNewChannelsListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setOnSearchNewTransponder:
{
data.enforceInterface(DESCRIPTOR);
com.joysee.adtv.aidl.OnSearchNewTransponderListener _arg0;
_arg0 = com.joysee.adtv.aidl.OnSearchNewTransponderListener.Stub.asInterface(data.readStrongBinder());
this.setOnSearchNewTransponder(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setOnSearchProgressChange:
{
data.enforceInterface(DESCRIPTOR);
com.joysee.adtv.aidl.OnSearchProgressChangeListener _arg0;
_arg0 = com.joysee.adtv.aidl.OnSearchProgressChangeListener.Stub.asInterface(data.readStrongBinder());
this.setOnSearchProgressChange(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setOnSearchTunerSignalStateListener:
{
data.enforceInterface(DESCRIPTOR);
com.joysee.adtv.aidl.OnSearchTunerSignalStateListener _arg0;
_arg0 = com.joysee.adtv.aidl.OnSearchTunerSignalStateListener.Stub.asInterface(data.readStrongBinder());
this.setOnSearchTunerSignalStateListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_saveChannels:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.joysee.adtv.logic.bean.DvbService> _arg0;
_arg0 = data.createTypedArrayList(com.joysee.adtv.logic.bean.DvbService.CREATOR);
java.util.List<com.joysee.adtv.logic.bean.ServiceType> _arg1;
_arg1 = data.createTypedArrayList(com.joysee.adtv.logic.bean.ServiceType.CREATOR);
this.saveChannels(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_deleteChannels:
{
data.enforceInterface(DESCRIPTOR);
this.deleteChannels();
reply.writeNoException();
return true;
}
case TRANSACTION_saveOldChannels:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.joysee.adtv.logic.bean.DvbService> _arg0;
_arg0 = data.createTypedArrayList(com.joysee.adtv.logic.bean.DvbService.CREATOR);
this.saveOldChannels(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.ISearchService
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
@Override public void startSearch(int searchType, com.joysee.adtv.logic.bean.Transponder tp) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(searchType);
if ((tp!=null)) {
_data.writeInt(1);
tp.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_startSearch, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopSearch(boolean isSave) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((isSave)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_stopSearch, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setOnSearchEndListener(com.joysee.adtv.aidl.OnSearchEndListener onSearchEndListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((onSearchEndListener!=null))?(onSearchEndListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setOnSearchEndListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setOnSearchReceivedNewChannelsListener(com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener onSearchReceivedNewChannelsListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((onSearchReceivedNewChannelsListener!=null))?(onSearchReceivedNewChannelsListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setOnSearchReceivedNewChannelsListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setOnSearchNewTransponder(com.joysee.adtv.aidl.OnSearchNewTransponderListener onSearchNewTransponderListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((onSearchNewTransponderListener!=null))?(onSearchNewTransponderListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setOnSearchNewTransponder, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setOnSearchProgressChange(com.joysee.adtv.aidl.OnSearchProgressChangeListener onSearchProgressChangeListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((onSearchProgressChangeListener!=null))?(onSearchProgressChangeListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setOnSearchProgressChange, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setOnSearchTunerSignalStateListener(com.joysee.adtv.aidl.OnSearchTunerSignalStateListener onSearchTunerSignalStateListener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((onSearchTunerSignalStateListener!=null))?(onSearchTunerSignalStateListener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_setOnSearchTunerSignalStateListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void saveChannels(java.util.List<com.joysee.adtv.logic.bean.DvbService> channelsList, java.util.List<com.joysee.adtv.logic.bean.ServiceType> serviceTypes) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(channelsList);
_data.writeTypedList(serviceTypes);
mRemote.transact(Stub.TRANSACTION_saveChannels, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void deleteChannels() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_deleteChannels, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void saveOldChannels(java.util.List<com.joysee.adtv.logic.bean.DvbService> channelsList) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(channelsList);
mRemote.transact(Stub.TRANSACTION_saveOldChannels, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_startSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stopSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setOnSearchEndListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setOnSearchReceivedNewChannelsListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_setOnSearchNewTransponder = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_setOnSearchProgressChange = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_setOnSearchTunerSignalStateListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_saveChannels = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_deleteChannels = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_saveOldChannels = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
}
public void startSearch(int searchType, com.joysee.adtv.logic.bean.Transponder tp) throws android.os.RemoteException;
public void stopSearch(boolean isSave) throws android.os.RemoteException;
public void setOnSearchEndListener(com.joysee.adtv.aidl.OnSearchEndListener onSearchEndListener) throws android.os.RemoteException;
public void setOnSearchReceivedNewChannelsListener(com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener onSearchReceivedNewChannelsListener) throws android.os.RemoteException;
public void setOnSearchNewTransponder(com.joysee.adtv.aidl.OnSearchNewTransponderListener onSearchNewTransponderListener) throws android.os.RemoteException;
public void setOnSearchProgressChange(com.joysee.adtv.aidl.OnSearchProgressChangeListener onSearchProgressChangeListener) throws android.os.RemoteException;
public void setOnSearchTunerSignalStateListener(com.joysee.adtv.aidl.OnSearchTunerSignalStateListener onSearchTunerSignalStateListener) throws android.os.RemoteException;
public void saveChannels(java.util.List<com.joysee.adtv.logic.bean.DvbService> channelsList, java.util.List<com.joysee.adtv.logic.bean.ServiceType> serviceTypes) throws android.os.RemoteException;
public void deleteChannels() throws android.os.RemoteException;
public void saveOldChannels(java.util.List<com.joysee.adtv.logic.bean.DvbService> channelsList) throws android.os.RemoteException;
}
