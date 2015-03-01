package com.mao.util;

import java.util.ArrayList;

import com.mao.myclass.Account;
import com.mao.myclass.Fouse;
import com.mao.myclass.InformationItem;
import com.mao.myclass.MemorandumItem;
import com.mao.myclass.MyDate;
import com.mao.recorder.RecordActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseTool extends SQLiteOpenHelper{
	private final static String tag = "DatabaseTool";
	private static int version = 1;//���ݿ�汾
	
	public final int REGISTER_SUCCESS = 1;//ע��ɹ�
	public final int REGISTER_SAMENAME = 2;//����ͬ��
	public final int REGISTER_UNKNOWERROR = 3;//δ֪����
	public DatabaseTool(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	public DatabaseTool(Context context, String name, CursorFactory factory) {
		super(context, name, factory, version);
	}
	public DatabaseTool(Context context, String name) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table if not exists accounts (id integer primary key autoincrement,username varchar(50),password varchar(50),picturepath varchar(50))";
		db.execSQL(sql);//����������˻����򴴽�
		sql = "create table if not exists dynamic_information (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000))";
		db.execSQL(sql);//�����̬��Ϣ�������򴴽�
		sql = "create table if not exists information_common (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000))";
		db.execSQL(sql);//���������Ϣ�������򴴽�
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	/**
	 * �ж��Ƿ��¼�ɹ�
	 * */
	public boolean isLoginSuccess(String name,String password)
	{
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try
		{
			db = this.getReadableDatabase();
			String sql = "select password from accounts where username = ?";
			cursor = db.rawQuery(sql, new String[]{name});
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				if(cursor.getString(0).equals(password))
				{
					if(cursor!=null) cursor.close();
					return true;
				}
				cursor.moveToNext();
			}
			if(cursor!=null) cursor.close();
			return false;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(cursor!=null) cursor.close();
			return false;
		}
	}
	/**
	 * �ж�ע���Ƿ�ɹ�(�Ƿ����ͬ��)
	 * */
	public int isRegisterSuccess(String name,String password)
	{
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try
		{
			db = this.getReadableDatabase();
			String sql = "select * from accounts where username = ?";
			cursor = db.rawQuery(sql, new String[]{name});
			int count = cursor.getCount();
			if(cursor!=null) cursor.close();
			if(count==0) 
			{
				Log.i(tag,name);
				Log.i(tag,password);
				if(writeAccountToDB(name,password))
					return REGISTER_SUCCESS;
				else return REGISTER_UNKNOWERROR;
			}
			else return REGISTER_SAMENAME;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(cursor!=null) cursor.close();
			return REGISTER_UNKNOWERROR;
		}
	}
	/**
	 * ע��ɹ����˻���Ϣд�����ݿ�
	 * */
	public boolean writeAccountToDB(String name,String password)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				//db.beginTransaction();
				String sql = "insert into accounts (username,password,picturepath) values (?,?,?)";
				db.execSQL(sql,new String[]{name,password,"#"});
				//db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * ��dynamic_�û��� ���ȡ��Ϣ
	 * */
	public ArrayList<InformationItem> readDynamicInfo(String name)
	{
		createDynamicTable(name);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ArrayList<InformationItem> arrayList = new ArrayList<InformationItem>();
		try
		{
			db = this.getReadableDatabase();
			String sql = "select * from dynamic_"+name;
			cursor = db.rawQuery(sql, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				InformationItem item = new InformationItem();
				item.picturePath = cursor.getString(1);
				item.username = cursor.getString(2);
				item.publishTime = cursor.getString(3);
				item.fromDevice = cursor.getString(4);
				item.text = cursor.getString(5);
				arrayList.add(item);
				cursor.moveToNext();
			}
			if(cursor!=null) cursor.close();
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(cursor!=null) cursor.close();
			return null;
		}
	}
	/**
	 * ����dynamic_�û��� ��
	 * */
	public boolean updateDynamicTable(String name,ArrayList<InformationItem> dynamicList)
	{
		createDynamicTable(name);
		SQLiteDatabase db = null;
		try
		{
			db = this.getWritableDatabase();
			db.beginTransaction();
			String str = "drop table dynamic_"+name;//ɾ����̬��
			db.execSQL(str);
			createDynamicTable(name);
			for(InformationItem item:dynamicList)
			{
				String sql = "insert into dynamic_"+name+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
				db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}
		catch(Exception e)
		{
			db.endTransaction();
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * ���û�����ȡ��Ϣ
	 * */
	public ArrayList<InformationItem> readRecordInfo(String username)
	{
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ArrayList<InformationItem> arrayList = new ArrayList<InformationItem>();
		try
		{
			db = this.getReadableDatabase();
			String sql = "select * from information_"+username;
			cursor = db.rawQuery(sql, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				InformationItem item = new InformationItem();
				item.picturePath = cursor.getString(1);
				item.username = cursor.getString(2);
				item.publishTime = cursor.getString(3);
				item.fromDevice = cursor.getString(4);
				item.text = cursor.getString(5);
				item.permission = Integer.parseInt(cursor.getString(6));
				arrayList.add(item);
				cursor.moveToNext();
			}
			if(cursor!=null) cursor.close();
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(cursor!=null) cursor.close();
			return null;
		}
	}
	/**
	 * ������¼ÿ���˺ŷ�������Ϣ�ı�
	 * */
	public void createRecordTable(String username)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				Log.i(tag,"create information table");
				db = this.getReadableDatabase();
				db.beginTransaction();
				String sql = "create table if not exists information_"+username+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//����������˻����򴴽�
				sql = "create table if not exists information_"+username+"_personally"+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//����������˻���(���Լ��ɼ�)�򴴽�
				sql = "create table if not exists information_"+username+"_friendly"+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//����������˻���(���ѿɼ�)�򴴽�
				sql = "create table if not exists information_"+username+"_common"+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//����������˻���(�����˿ɼ�)�򴴽�
				db.setTransactionSuccessful();
				db.endTransaction();
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
			}
		}
	}
	/**
	 * ��һ����¼д��ָ���˺ŵ���Ϣ����
	 * */
	public boolean writeOneRecordToDB(String username,InformationItem item)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				//����������ȴ���
				createRecordTable(username);
				db.beginTransaction();
				String sql = "insert into information_"+username+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
				db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
				if(item.permission == RecordActivity.PERMISSION_COMMON)
				{
					sql = "insert into information_"+username+"_common"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
					db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
				}
				else if(item.permission == RecordActivity.PERMISSION_FRIENDLLY)
				{
					sql = "insert into information_"+username+"_friendly"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
					db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
				}
				else
				{
					sql = "insert into information_"+username+"_personally"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
					db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * �����м�¼д��ָ���˺ŵ���Ϣ����(����ձ�)
	 * */
	public boolean writeAllRecordToDB(String username,ArrayList<InformationItem> informationList)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				//����������ȴ���
				//createRecordTable(username);
				db = this.getWritableDatabase();
				db.beginTransaction();
				for(InformationItem item:informationList)
				{
					String sql = "insert into information_"+username+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
					db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					if(item.permission == RecordActivity.PERMISSION_COMMON)
					{
						sql = "insert into information_"+username+"_common"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
						db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					}
					else if(item.permission == RecordActivity.PERMISSION_FRIENDLLY)
					{
						sql = "insert into information_"+username+"_friendly"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
						db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					}
					else
					{
						sql = "insert into information_"+username+"_personally"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
						db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					}
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * �����м�¼д��ָ���˺ŵ���Ϣ����(��ձ�����Ȩ��д�벻ͬ����)
	 * */
	public boolean reWriteAllRecordToDB(String username,ArrayList<InformationItem> informationList)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				//����������ȴ���
				//createRecordTable(username);
				db = this.getWritableDatabase();
				db.beginTransaction();
				String str = "drop table information_"+username;//ɾ����Ϣ��
				db.execSQL(str);
				str = "drop table information_"+username+"_personally";//ɾ�����˿ɼ���Ϣ��
				db.execSQL(str);
				str = "drop table information_"+username+"_friendly";//ɾ�����ѿɼ���Ϣ��
				db.execSQL(str);
				str = "drop table information_"+username+"_common";//ɾ�������˿ɼ���Ϣ��
				db.execSQL(str);
				createRecordTable(username);//�ؽ�������Ϣ��
				for(InformationItem item:informationList)
				{
					String sql = "insert into information_"+username+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
					db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					if(item.permission == RecordActivity.PERMISSION_COMMON)
					{
						sql = "insert into information_"+username+"_common"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
						db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					}
					else if(item.permission == RecordActivity.PERMISSION_FRIENDLLY)
					{
						sql = "insert into information_"+username+"_friendly"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
						db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					}
					else
					{
						sql = "insert into information_"+username+"_personally"+" (picturepath,username,publishtime,fromdevice,text,permission) values (?,?,?,?,?,?)";
						db.execSQL(sql,new String[]{item.picturePath,item.username,item.publishTime,item.fromDevice,item.text,item.permission+""});
					}
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * �����б���д��ָ���˺ŵı�������(��ձ���д��)
	 * */
	public boolean reWriteAllMemorandumToDB(String username,ArrayList<MemorandumItem> memorandumList)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				//����������ȴ���
				//createRecordTable(username);
				db = this.getWritableDatabase();
				db.beginTransaction();
				String str = "drop table memorandum_"+username;//ɾ��������
				db.execSQL(str);
				createMemorandumTable(username);//�ؽ�������
				for(MemorandumItem item:memorandumList)
				{
					String sql = "insert into memorandum_"+username+" (year,month,day,hour,minute,second,timestamp,text) values (?,?,?,?,?,?,?,?)";
					db.execSQL(sql,new String[]{item.date.year+"",item.date.month+"",item.date.day+"",item.date.hour+"",item.date.minute+"",item.date.second+"",item.date.timestamp+"",item.text});
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * accounts���޸��˺���Ϣ(�޸�ͼƬ·��)
	 * */
	public boolean updateAccountInfo(Account account)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				String sql = "update accounts set picturepath = ? where username = ?";
				db.execSQL(sql, new String[]{account.picturePath,account.username});
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * information���޸��˺���Ϣ(�޸�ͼƬ·��)
	 * */
	public boolean updateAccountInfoToInfoTable(Account account)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				db.beginTransaction();
				String sql = "update information_"+account.username+" set picturepath = ? where username = ?";
				db.execSQL(sql, new String[]{account.picturePath,account.username});
				sql = "update information_"+account.username+"_personally"+" set picturepath = ? where username = ?";
				db.execSQL(sql, new String[]{account.picturePath,account.username});
				sql = "update information_"+account.username+"_friendly"+" set picturepath = ? where username = ?";
				db.execSQL(sql, new String[]{account.picturePath,account.username});
				sql = "update information_"+account.username+"_common"+" set picturepath = ? where username = ?";
				db.execSQL(sql, new String[]{account.picturePath,account.username});
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * ���˺ű��ĳһ�˺ŵ���Ϣ
	 * */
	public Account queryAccount(String name)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			Account account = new Account();
			try
			{
				System.out.println(name);
				db = this.getReadableDatabase();
				String sql = "select * from accounts where username = ?";
				Cursor cursor = db.rawQuery(sql, new String[]{name});
				cursor.moveToFirst();
				if(cursor.getCount()>0)
				{
					account.username = cursor.getString(1);
					account.password = cursor.getString(2);
					account.picturePath = cursor.getString(3);
					if(cursor!=null) cursor.close();
					return account;
				}
				else
				{
					if(db!=null) db.close();
					return null;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	/**
	 * ����Ϣ��ɾ��һ��ָ����¼()
	 * */
	public boolean deleteOnRecordByName(String name,String publishTime)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getReadableDatabase();
				db.beginTransaction();
				String sql = "delete from information_"+name+" where publishtime = ?";
				db.execSQL(sql, new String[]{publishTime});
				sql = "delete from information_"+name+"_personally"+" where publishtime = ?";
				db.execSQL(sql, new String[]{publishTime});
				sql = "delete from information_"+name+"_friendly"+" where publishtime = ?";
				db.execSQL(sql, new String[]{publishTime});
				sql = "delete from information_"+name+"_common"+" where publishtime = ?";
				db.execSQL(sql, new String[]{publishTime});
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			}
			catch(Exception e)
			{
				db.endTransaction();
				e.printStackTrace();
				return false;
			}
		}
	}
	
	/**
	 * ������¼ÿ���˺ŵı���¼��
	 * */
	public void createMemorandumTable(String username)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists memorandum_"+username+" (id integer primary key autoincrement,year varchar(50),month varchar(50),day varchar(50),hour varchar(50),minute varchar(50),second varchar(50),timestamp varchar(50),text varchar(1000))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//����������˻����򴴽�
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * ������¼ÿ���˺ŵĶ�̬��
	 * */
	public void createDynamicTable(String username)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists dynamic_"+username+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//��������ڶ�̬���򴴽�
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * ������¼ÿ���˺Ź�ע�ҵ��˱���ҹ�ע���˱�
	 * */
	public void createFouseTable(String username)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists fouseme_"+username+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//��������ڹ�ע�ҵ��˱��򴴽�
			sql = "create table if not exists myfouse_"+username+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//����������ҹ�ע���˱��򴴽�
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * ���һ����������������
	 * */
	public boolean addOneMemorandum(String name,MemorandumItem item,MyDate date)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				String sql = "insert into memorandum_"+name+" (year,month,day,hour,minute,second,timestamp,text) values (?,?,?,?,?,?,?,?)";
				db.execSQL(sql, new String[]{date.year+"",date.month+"",date.day+"",item.date.hour+"",item.date.minute+"",item.date.second+"",item.date.timestamp+"",item.text});
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * ɾ��һ������
	 * */
	public boolean deleteOnMemorandum(String name)
	{
		return false;
	}
	/**
	 * ɾ��ĳһ�����б���
	 * */
	public boolean deleteAllMemorandumOneDay(String name,MyDate date)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				String sql = "delete from memorandum_"+name+" where year=? and month=? and day=?";
				db.execSQL(sql, new String[]{date.year+"",date.month+"",date.day+""});
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * �޸�һ������
	 * */
	public boolean updateOneMemorandum(String name,MemorandumItem item)
	{
		return false;
	}
	/**
	 * �޸�ĳһ������б���
	 * */
	public boolean updateAllMemorandumOneDay(String name,ArrayList<MemorandumItem> memorandumList,MyDate date)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				//ɾ����һ�����б���
				boolean result = deleteAllMemorandumOneDay(name,date);
				if(result==false) return false;
				//����д��
				for(MemorandumItem item:memorandumList)
				{
					addOneMemorandum(name,item,date);
				}
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}
	/**
	 * ��ѯĳһ������б�����¼
	 * */
	public ArrayList<MemorandumItem> queryMemorandumOfDay(String name,MyDate date)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			ArrayList<MemorandumItem> memorandumList = new ArrayList<MemorandumItem>();
			try
			{
				db = this.getReadableDatabase();
				String sql = "select * from memorandum_" + name + " where year=? and month=? and day=?";
				Cursor cursor = db.rawQuery(sql,new String[]{date.year+"",date.month+"",date.day+""});
				if(cursor==null) return null;
				cursor.moveToFirst();
				while(!cursor.isAfterLast())
				{
					MemorandumItem item = new MemorandumItem();
					item.date.year = Integer.parseInt(cursor.getString(1));
					item.date.month = Integer.parseInt(cursor.getString(2));
					item.date.day = Integer.parseInt(cursor.getString(3));
					item.date.hour = Integer.parseInt(cursor.getString(4));
					item.date.minute = Integer.parseInt(cursor.getString(5));
					item.date.second = Integer.parseInt(cursor.getString(6));
					item.date.timestamp = Long.parseLong(cursor.getString(7));
					item.text = cursor.getString(8);
					memorandumList.add(item);
					cursor.moveToNext();
				}
				if(cursor!=null) cursor.close();
				return memorandumList;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	/**
	 * �����ҹ�ע���˱�
	 * */
	public void createMyfouseTable(String name)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists myfouse_"+name+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//��������ڶ�̬���򴴽�
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * ��ȡ�ҹ�ע���˱�myfouse_�û���
	 * */
	public ArrayList<Fouse> readMyfouseTable(String name)
	{
		createMyfouseTable(name);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ArrayList<Fouse> arrayList = new ArrayList<Fouse>();
		try
		{
			db = this.getReadableDatabase();
			String sql = "select * from myfouse_"+name;
			cursor = db.rawQuery(sql, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				Fouse item = new Fouse();
				item.username = cursor.getString(1);
				item.timeStamp= cursor.getString(2);
				arrayList.add(item);
				cursor.moveToNext();
			}
			if(cursor!=null) cursor.close();
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(cursor!=null) cursor.close();
			return null;
		}
	}
	/**
	 * ��һ����ע��¼���뵽���ݿ����
	 * */
	public boolean insertMyFouse(String name,Fouse item)
	{
		SQLiteDatabase db = null;
		try
		{
			createMyfouseTable(name);
			db = this.getWritableDatabase();
			db.beginTransaction();
			String sql = "insert into myfouse_"+name+" (username,timestamp) values (?,?)";
			db.execSQL(sql, new String[]{item.othername,item.timeStamp});
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}
		catch(Exception e)
		{
			db.endTransaction();
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * ����myFouse_�û��� ��
	 * */
	public boolean updateMyFouse(String name,ArrayList<Fouse> myFouseList)
	{
		SQLiteDatabase db = null;
		try
		{
			createMyfouseTable(name);
			db = this.getWritableDatabase();
			db.beginTransaction();
			String str = "drop table myfouse_"+name;//ɾ����
			db.execSQL(str);
			createMyfouseTable(name);
			for(Fouse item:myFouseList)
			{
				String sql = "insert into myfouse_"+name+" (username,timestamp) values (?,?)";
				db.execSQL(sql, new String[]{item.username,item.timeStamp});
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}
		catch(Exception e)
		{
			db.endTransaction();
			e.printStackTrace();
			return false;
		}	
	}
	/**
	 * ������ע�ҵ��˱�
	 * */
	public void createFousemeTable(String name)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists fouseme_"+name+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//��������ڶ�̬���򴴽�
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * ��ȡ��ע�ҵ��˱�fouseme_�û���
	 * */
	public ArrayList<Fouse> readFousemeTable(String name)
	{
		createMyfouseTable(name);
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ArrayList<Fouse> arrayList = new ArrayList<Fouse>();
		try
		{
			db = this.getReadableDatabase();
			String sql = "select * from fouseme_"+name;
			cursor = db.rawQuery(sql, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast())
			{
				Fouse item = new Fouse();
				item.username = cursor.getString(1);
				item.timeStamp= cursor.getString(2);
				arrayList.add(item);
				cursor.moveToNext();
			}
			if(cursor!=null) cursor.close();
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(cursor!=null) cursor.close();
			return null;
		}
	}
	/**
	 * ����Fouseme_�û��� ��
	 * */
	public boolean updateFouseme(String name,ArrayList<Fouse> myFouseList)
	{
		SQLiteDatabase db = null;
		try
		{
			createFousemeTable(name);
			db = this.getWritableDatabase();
			db.beginTransaction();
			String str = "drop table fouseme_"+name;//ɾ����
			db.execSQL(str);
			createFousemeTable(name);
			for(Fouse item:myFouseList)
			{
				String sql = "insert into fouseme_"+name+" (username,timestamp) values (?,?)";
				db.execSQL(sql, new String[]{item.username,item.timeStamp});
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			return true;
		}
		catch(Exception e)
		{
			db.endTransaction();
			e.printStackTrace();
			return false;
		}	
	}
}
