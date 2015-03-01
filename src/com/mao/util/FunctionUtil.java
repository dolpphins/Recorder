package com.mao.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.mao.myclass.MyDate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.provider.MediaStore;


/**
 * 工具函数类
 * */
public class FunctionUtil {
	/**
	 * 将时间戳转化为日期格式字符串
	 * */
	public static String convertTimestampToString(long timestamp)
	{
		long currentTime = System.currentTimeMillis();
		long difference = currentTime - timestamp;
		String result;
		//少于1分钟
		if(difference<60*1000)
		{
			result = "刚刚";
		}
		//少于1小时
		else if(difference<60*60*1000)
		{
			result = difference/(60*1000)+"分钟前";
		}
		//少于1天
		else if(difference<24*60*60*1000)
		{
			result = difference/(60*60*1000)+"小时前";
		}
		//5天内
		else if(difference<5*24*60*60*1000)
		{
			result = difference/(24*60*60*1000)+"天前";
		}
		//大于5天
		else
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
			result = sdf.format(new Date(Long.valueOf(timestamp)));
		}
		return result;
	}
	/**
	 * 将一张位图转换后保存到sd卡指定目录里
	 * */
	public static boolean savePictureToSd(Bitmap bitmap,String name)
	{
		FileOutputStream fos = null;
		try
		{
			Matrix matrix = new Matrix();
			matrix.postScale(((float)128.0)/bitmap.getWidth(), ((float)128.0)/ bitmap.getHeight());
			Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			File file = new File("/sdcard/Recorder");
			if(!file.exists()) file.mkdir();
			file = new File("/sdcard/Recorder/HeadPictureFile");
			if(!file.exists()) file.mkdir();
			fos = new FileOutputStream("/sdcard/Recorder/HeadPictureFile/head_picture_"+name+".jpg");
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 将指定路径的图片转换后保存到sd卡指定目录里
	 * */
	public static boolean savePictureToSd(Context context,Intent intent,String name)
	{
		FileOutputStream fos = null;
		try
		{
			Cursor cursor = context.getContentResolver().query(intent.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null);
			cursor.moveToFirst();
			String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			cursor.close();
			Bitmap bitmap = getBitmapByPath(path);
			savePictureToSd(bitmap,name);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 从指定路径获取一张位图
	 * */
	public static Bitmap getBitmapByPath(String path)
	{
		try
		{
			Bitmap bmp = BitmapFactory.decodeFile(path);
			return bmp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 获取当前月份表
	 * */
	public static MyDate getCurrentCalendar(MyDate myDate)
	{
		Calendar calendar = Calendar.getInstance();
		int year = myDate.year;
		int month = myDate.month;
		int day = 1;
		int currentMonthCount = 0,lastMonthCount = 0;
		calendar.set(year, month, day);
		int week = calendar.get(Calendar.DAY_OF_WEEK)-1;//从周日算起
		//得到当前月的天数
		currentMonthCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		//得到上一个月的天数
		if(month == 0)
		{
			calendar.set(Calendar.YEAR, year-1);
			calendar.set(Calendar.MONTH, 11);
		}
		else
		{
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
		}
		lastMonthCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int i = week-1;
		for(;i>=0;i--)
		{
			myDate.currentMonthTable[i] = lastMonthCount;
			lastMonthCount--;
		}
		int n = 1;
		for(i=week;i<42;i++)
		{
			if(n>currentMonthCount) n = 1;
			myDate.currentMonthTable[i] = n;
			n++;
		}
		return myDate;
	}
	/**
	 * 得到当天年月日
	 * */
	public static MyDate getDate()
	{
		MyDate myDate = new MyDate();
		Calendar calendar = Calendar.getInstance();
		myDate.year = calendar.get(Calendar.YEAR);
		myDate.month = calendar.get(Calendar.MONTH);
		myDate.day = calendar.get(Calendar.DAY_OF_MONTH);
		myDate.week = calendar.get(Calendar.DAY_OF_WEEK)-1;
		calendar.set(myDate.year, myDate.month, 1);
		myDate.weekOfFirstDay = calendar.get(Calendar.DAY_OF_WEEK)-1;
		myDate.hour = calendar.get(Calendar.HOUR_OF_DAY);
		myDate.minute = calendar.get(Calendar.MINUTE);
		myDate.second = calendar.get(Calendar.SECOND);
		myDate.timestamp = System.currentTimeMillis();
		return myDate;
	}
	/**
	 * 得到上一个月
	 * */
	public static MyDate getLastMonth(MyDate myDate)
	{
		MyDate d = myDate;
		if(d.month==0) 
		{
			d.month = 11;
			d.year--;
		}
		else d.month--;
		Calendar calendar = Calendar.getInstance();
		calendar.set(myDate.year, myDate.month, 1);
		myDate.weekOfFirstDay = calendar.get(Calendar.DAY_OF_WEEK)-1;
		return d;
	}
	/**
	 * 得到下一个月
	 * */
	public static MyDate getNextMonth(MyDate myDate)
	{
		MyDate d = myDate;
		if(d.month==11) 
		{
			d.month = 0;
			d.year++;
		}
		else d.month++;
		Calendar calendar = Calendar.getInstance();
		calendar.set(myDate.year, myDate.month, 1);
		myDate.weekOfFirstDay = calendar.get(Calendar.DAY_OF_WEEK)-1;
		return d;
	}
	/**
	 * 获取当前版本号
	 * */
	public static String getVersion(Context context)
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			return "V "+pi.versionName;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "V "+"未知";
		}
	}
}

















