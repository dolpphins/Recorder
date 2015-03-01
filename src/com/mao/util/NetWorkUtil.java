package com.mao.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ���繤����
 * */
public class NetWorkUtil {
	private final static String tag = "NetWorkUtil";
	
	public final static int NETWORK_NO_ACCESS = 1;//���粻����
	public final static int MOBILE_NETWORK = 2;//�ƶ�����
	public final static int WIFI_NETWORK = 3;//wifi����
	public final static int OTHER_NETWORK = 4;//��������
	/**
	 * �ж������Ƿ����
	 * */
	public static boolean isAvailable(Context context)
	{
		try
		{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(cm != null)
			{
				NetworkInfo ni = cm.getActiveNetworkInfo();
				if(ni != null)
				{
					return ni.isAvailable();
				}
			}
			return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * �õ���ǰ��������
	 * */
	public static int getNetworkType(Context context)
	{
		int result = -1;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm != null)
		{
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(ni != null && ni.isAvailable())
			{
				int type = ni.getType();
				if(type == ConnectivityManager.TYPE_MOBILE) result = MOBILE_NETWORK;
				else if(type == ConnectivityManager.TYPE_WIFI) result = WIFI_NETWORK;
				else type = OTHER_NETWORK;
			}
			else result = NETWORK_NO_ACCESS;
		}
		else result = NETWORK_NO_ACCESS;
		return result;
	}
}
