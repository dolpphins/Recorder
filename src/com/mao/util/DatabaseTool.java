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
	private static int version = 1;//数据库版本
	
	public final int REGISTER_SUCCESS = 1;//注册成功
	public final int REGISTER_SAMENAME = 2;//出现同名
	public final int REGISTER_UNKNOWERROR = 3;//未知错误
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
		db.execSQL(sql);//如果不存在账户表则创建
		sql = "create table if not exists dynamic_information (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000))";
		db.execSQL(sql);//如果动态信息表不存在则创建
		sql = "create table if not exists information_common (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000))";
		db.execSQL(sql);//如果公共信息表不存在则创建
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	/**
	 * 判断是否登录成功
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
	 * 判断注册是否成功(是否存在同名)
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
	 * 注册成功后将账户信息写入数据库
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
	 * 从dynamic_用户名 表读取信息
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
	 * 更新dynamic_用户名 表
	 * */
	public boolean updateDynamicTable(String name,ArrayList<InformationItem> dynamicList)
	{
		createDynamicTable(name);
		SQLiteDatabase db = null;
		try
		{
			db = this.getWritableDatabase();
			db.beginTransaction();
			String str = "drop table dynamic_"+name;//删除动态表
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
	 * 从用户表表读取信息
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
	 * 创建记录每个账号发布的信息的表
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
				db.execSQL(sql);//如果不存在账户表则创建
				sql = "create table if not exists information_"+username+"_personally"+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//如果不存在账户表(仅自己可见)则创建
				sql = "create table if not exists information_"+username+"_friendly"+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//如果不存在账户表(好友可见)则创建
				sql = "create table if not exists information_"+username+"_common"+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
				db.execSQL(sql);//如果不存在账户表(所有人可见)则创建
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
	 * 将一条记录写到指定账号的信息表中
	 * */
	public boolean writeOneRecordToDB(String username,InformationItem item)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				//如果表不存在先创建
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
	 * 将所有记录写到指定账号的信息表中(不清空表)
	 * */
	public boolean writeAllRecordToDB(String username,ArrayList<InformationItem> informationList)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				//如果表不存在先创建
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
	 * 将所有记录写到指定账号的信息表中(清空表并根据权限写入不同表中)
	 * */
	public boolean reWriteAllRecordToDB(String username,ArrayList<InformationItem> informationList)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				//如果表不存在先创建
				//createRecordTable(username);
				db = this.getWritableDatabase();
				db.beginTransaction();
				String str = "drop table information_"+username;//删除信息表
				db.execSQL(str);
				str = "drop table information_"+username+"_personally";//删除个人可见信息表
				db.execSQL(str);
				str = "drop table information_"+username+"_friendly";//删除好友可见信息表
				db.execSQL(str);
				str = "drop table information_"+username+"_common";//删除所有人可见信息表
				db.execSQL(str);
				createRecordTable(username);//重建所有信息表
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
	 * 将所有备忘写到指定账号的备忘表中(清空表再写入)
	 * */
	public boolean reWriteAllMemorandumToDB(String username,ArrayList<MemorandumItem> memorandumList)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				//如果表不存在先创建
				//createRecordTable(username);
				db = this.getWritableDatabase();
				db.beginTransaction();
				String str = "drop table memorandum_"+username;//删除备忘表
				db.execSQL(str);
				createMemorandumTable(username);//重建备忘表
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
	 * accounts表修改账号信息(修改图片路径)
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
	 * information表修改账号信息(修改图片路径)
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
	 * 从账号表查某一账号的信息
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
	 * 在信息表删除一条指定记录()
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
	 * 创建记录每个账号的备忘录表
	 * */
	public void createMemorandumTable(String username)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists memorandum_"+username+" (id integer primary key autoincrement,year varchar(50),month varchar(50),day varchar(50),hour varchar(50),minute varchar(50),second varchar(50),timestamp varchar(50),text varchar(1000))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//如果不存在账户表则创建
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 创建记录每个账号的动态表
	 * */
	public void createDynamicTable(String username)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists dynamic_"+username+" (id integer primary key autoincrement,picturepath varchar(50),username varchar(50),publishtime varchar(50),fromdevice varchar(50),text varchar(1000),permission varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//如果不存在动态表则创建
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 创建记录每个账号关注我的人表和我关注的人表
	 * */
	public void createFouseTable(String username)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists fouseme_"+username+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//如果不存在关注我的人表则创建
			sql = "create table if not exists myfouse_"+username+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//如果不存在我关注的人表则创建
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 添加一条备忘到备忘表中
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
	 * 删除一条备忘
	 * */
	public boolean deleteOnMemorandum(String name)
	{
		return false;
	}
	/**
	 * 删除某一天所有备忘
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
	 * 修改一条备忘
	 * */
	public boolean updateOneMemorandum(String name,MemorandumItem item)
	{
		return false;
	}
	/**
	 * 修改某一天的所有备忘
	 * */
	public boolean updateAllMemorandumOneDay(String name,ArrayList<MemorandumItem> memorandumList,MyDate date)
	{
		synchronized (this)
		{
			SQLiteDatabase db = null;
			try
			{
				db = this.getWritableDatabase();
				//删除掉一条所有备忘
				boolean result = deleteAllMemorandumOneDay(name,date);
				if(result==false) return false;
				//重新写入
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
	 * 查询某一天的所有备忘记录
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
	 * 创建我关注的人表
	 * */
	public void createMyfouseTable(String name)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists myfouse_"+name+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//如果不存在动态表则创建
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 读取我关注的人表myfouse_用户名
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
	 * 将一条关注记录插入到数据库表中
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
	 * 更新myFouse_用户名 表
	 * */
	public boolean updateMyFouse(String name,ArrayList<Fouse> myFouseList)
	{
		SQLiteDatabase db = null;
		try
		{
			createMyfouseTable(name);
			db = this.getWritableDatabase();
			db.beginTransaction();
			String str = "drop table myfouse_"+name;//删除表
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
	 * 创建关注我的人表
	 * */
	public void createFousemeTable(String name)
	{
		SQLiteDatabase db = null;
		try
		{
			String sql = "create table if not exists fouseme_"+name+" (id integer primary key autoincrement,username varchar(50),timestamp varchar(50))";
			db = this.getReadableDatabase();
			db.execSQL(sql);//如果不存在动态表则创建
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 读取关注我的人表fouseme_用户名
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
	 * 更新Fouseme_用户名 表
	 * */
	public boolean updateFouseme(String name,ArrayList<Fouse> myFouseList)
	{
		SQLiteDatabase db = null;
		try
		{
			createFousemeTable(name);
			db = this.getWritableDatabase();
			db.beginTransaction();
			String str = "drop table fouseme_"+name;//删除表
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
