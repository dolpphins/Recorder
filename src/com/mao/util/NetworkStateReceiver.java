package com.mao.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


/**
 * ��������״̬
 * */
public class NetworkStateReceiver extends BroadcastReceiver{
	private final static String tag = "NetworkStateReceiver";
	
	public static int networkType;
	/**
	 * ��ʼ������״̬
	 * */
	public static void initNetworkState(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm==null) networkType = NetWorkUtil.NETWORK_NO_ACCESS;
		else
		{
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if(ni != null && ni.isAvailable())
			{
				int type = ni.getType();
				if(type == ConnectivityManager.TYPE_MOBILE) networkType = NetWorkUtil.MOBILE_NETWORK;
				else if(type == ConnectivityManager.TYPE_WIFI) networkType = NetWorkUtil.WIFI_NETWORK;
				else networkType = NetWorkUtil.OTHER_NETWORK;
			}
			else networkType = NetWorkUtil.NETWORK_NO_ACCESS;
		}
		Log.i(tag,"current network state:"+networkType);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(tag,"receive broadcast");
		//����״̬�����ı�
		if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
		{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(cm != null)
			{
				NetworkInfo ni = cm.getActiveNetworkInfo();
				if(ni != null && ni.isAvailable())
				{
					int type = ni.getType();
					switch(type)
					{
					//�ƶ�����
					case ConnectivityManager.TYPE_MOBILE:
						networkType = NetWorkUtil.MOBILE_NETWORK;
						break;
					//wifi����
					case ConnectivityManager.TYPE_WIFI:
						networkType = NetWorkUtil.WIFI_NETWORK;
						break;
					default:
						networkType = NetWorkUtil.OTHER_NETWORK;
					}
				}
				else networkType = NetWorkUtil.NETWORK_NO_ACCESS;
			}
			else networkType = NetWorkUtil.NETWORK_NO_ACCESS;
			Log.i(tag,"current network state:"+networkType);
		}
	}

}
