/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/jiangxiujie/AndroidStudioProjects/JoySeeTvGit/app/src/main/aidl/com/joysee/adtv/aidl/email/IEmailService.aidl
 */
package com.joysee.adtv.aidl.email;
public interface IEmailService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.joysee.adtv.aidl.email.IEmailService
{
private static final java.lang.String DESCRIPTOR = "com.joysee.adtv.aidl.email.IEmailService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.joysee.adtv.aidl.email.IEmailService interface,
 * generating a proxy if needed.
 */
public static com.joysee.adtv.aidl.email.IEmailService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.joysee.adtv.aidl.email.IEmailService))) {
return ((com.joysee.adtv.aidl.email.IEmailService)iin);
}
return new com.joysee.adtv.aidl.email.IEmailService.Stub.Proxy(obj);
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
case TRANSACTION_getEmailIdleSpace:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getEmailIdleSpace();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getEmailUsedSpace:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getEmailUsedSpace();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getEmailHeads:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.joysee.adtv.logic.bean.EmailHead> _result = this.getEmailHeads();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getEmailContent:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getEmailContent(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_DelEmailByID:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.DelEmailByID(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.joysee.adtv.aidl.email.IEmailService
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
     * 查看邮件空间
     * @return 可用空间
     */
@Override public int getEmailIdleSpace() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getEmailIdleSpace, _data, _reply, 0);
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
     * 得到当前邮件数量
     * @return 当前邮件总数
     */
@Override public int getEmailUsedSpace() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getEmailUsedSpace, _data, _reply, 0);
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
     * 获取邮件头列表.
     * @return 邮件头列表
     */
@Override public java.util.List<com.joysee.adtv.logic.bean.EmailHead> getEmailHeads() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.joysee.adtv.logic.bean.EmailHead> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getEmailHeads, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.joysee.adtv.logic.bean.EmailHead.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * @param id 邮件ID
     * @return content 邮件内容
     */
@Override public java.lang.String getEmailContent(int id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
mRemote.transact(Stub.TRANSACTION_getEmailContent, _data, _reply, 0);
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
     * @param id 删除邮件
     * @return >=0成功
     */
@Override public int DelEmailByID(int id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
mRemote.transact(Stub.TRANSACTION_DelEmailByID, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getEmailIdleSpace = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getEmailUsedSpace = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getEmailHeads = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getEmailContent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_DelEmailByID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
/**
     * 查看邮件空间
     * @return 可用空间
     */
public int getEmailIdleSpace() throws android.os.RemoteException;
/**
     * 得到当前邮件数量
     * @return 当前邮件总数
     */
public int getEmailUsedSpace() throws android.os.RemoteException;
/**
     * 获取邮件头列表.
     * @return 邮件头列表
     */
public java.util.List<com.joysee.adtv.logic.bean.EmailHead> getEmailHeads() throws android.os.RemoteException;
/**
     * @param id 邮件ID
     * @return content 邮件内容
     */
public java.lang.String getEmailContent(int id) throws android.os.RemoteException;
/**
     * @param id 删除邮件
     * @return >=0成功
     */
public int DelEmailByID(int id) throws android.os.RemoteException;
}
