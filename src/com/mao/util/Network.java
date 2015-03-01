package com.mao.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mao.myclass.Account;
import com.mao.myclass.Fouse;
import com.mao.myclass.InformationItem;
import com.mao.myclass.MemorandumItem;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * 提供一系列与并应用程序相关的网络操作，如判断注册是否成功等
 * */
public class Network {
	private final static String tag = "Network";
	
	/**
	 * 判断注册是否成功(成功时写入到数据库中)
	 * */
	public static int isRegisterSuccess(String name,String password)
	{
		try
		{
			//注册url
			String urlString = "http://1.maorecorder.sinaapp.com/register.php"+"?"+"username="+name+"&"+"password="+password;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,"result:"+result);
			int resultCode = Integer.parseInt(result);
			if(resultCode==1) return 1;//注册成功
			else if(resultCode==2) return 2;//出现同名
			else return 3;//未知错误
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 3;//未知错误
		}
	}
	/**
	 * 判断登录是否成功
	 * */
	public static boolean isLoginSuccess(String name,String password)
	{
		try
		{
			//登录url
			String urlString = "http://1.maorecorder.sinaapp.com/login.php"+"?"+"username="+name+"&"+"password="+password;
			Log.i(tag,urlString);
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			int resultCode = Integer.parseInt(result);
			if(resultCode==1) return false;//用户名不存在
			else if(resultCode==2) return false;//密码错误
			else if(resultCode==3) return true;
			else return false;//未知错误
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;//未知错误
		}
	}
	/**
	 * 由字节流得到字符形式内容
	 * */
	private static String getData(InputStream in)
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String result="";
			String str;
			while((str=br.readLine())!=null)
			{
				result += str;
			}
			in.close();
			return result;
		}
		catch(Exception e)
		{
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 由用户名查询用户信息
	 * */
	public static Account queryAccount(String name)
	{
		try
		{
			//查询账号信息url
			String urlString = "http://1.maorecorder.sinaapp.com/account.php"+"?"+"username="+name;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			//获取信息失败
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			else
			{
				String[] str = result.split(",");
				if(str.length<3) return null;
				Account account = new Account();
				account.username = str[0];
				account.password = str[1];
				account.picturePath = str[2];
				return account;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 从网络获取指定用户名的所有记录信息
	 * */
	public static ArrayList<InformationItem> readRecordInfo(String username)
	{
		ArrayList<InformationItem> informationList;
		try
		{
			//获取记录信息url
			String urlString = "http://1.maorecorder.sinaapp.com/information.php"+"?"+"username="+username;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			informationList = parseInformationJson(result);
			if(informationList==null) return null;
			else return informationList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;//未知错误
		}
	}
	/**
	 * 解析information json数据
	 * */
	private static ArrayList<InformationItem> parseInformationJson(String result)
	{
		System.out.println(result);
		ArrayList<InformationItem> arrayList = new ArrayList<InformationItem>();
		try
		{
			JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
			for(int i=0;i<jsonArray.length();i++)
			{
				InformationItem item = new InformationItem();
				item.picturePath = jsonArray.getJSONObject(i).getString("picturepath");
				item.username = jsonArray.getJSONObject(i).getString("username");
				item.publishTime = jsonArray.getJSONObject(i).getString("publishtime");
				item.fromDevice = jsonArray.getJSONObject(i).getString("fromdevice");
				item.text = jsonArray.getJSONObject(i).getString("text");
				item.permission = Integer.parseInt(jsonArray.getJSONObject(i).getString("permission"));
				arrayList.add(item);
			}
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 从网络获取指定用户名的所有备忘录信息
	 * */
	public static ArrayList<MemorandumItem> readMemorandumInfo(String username)
	{
		ArrayList<MemorandumItem> memorandumList;
		try
		{
			//获取记录信息url
			String urlString = "http://1.maorecorder.sinaapp.com/memorandum.php"+"?"+"username="+username;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			memorandumList = parseMemorandumJson(result);
			if(memorandumList==null) return null;
			else return memorandumList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;//未知错误
		}
	}
	/**
	 * 解析memorandum json数据
	 * */
	public static ArrayList<MemorandumItem> parseMemorandumJson(String result)
	{
		System.out.println(result);
		ArrayList<MemorandumItem> memorandumList = new ArrayList<MemorandumItem>();
		try
		{
			JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
			for(int i=0;i<jsonArray.length();i++)
			{
				MemorandumItem item = new MemorandumItem();
				item.date.year = Integer.parseInt(jsonArray.getJSONObject(i).getString("year"));
				item.date.month = Integer.parseInt(jsonArray.getJSONObject(i).getString("month"));
				item.date.day = Integer.parseInt(jsonArray.getJSONObject(i).getString("day"));
				item.date.hour = Integer.parseInt(jsonArray.getJSONObject(i).getString("hour"));
				item.date.minute = Integer.parseInt(jsonArray.getJSONObject(i).getString("minute"));
				item.date.second = Integer.parseInt(jsonArray.getJSONObject(i).getString("second"));
				item.date.timestamp = Long.parseLong(jsonArray.getJSONObject(i).getString("timestamp"));
				item.text = jsonArray.getJSONObject(i).getString("text");
				memorandumList.add(item);
			}
			return memorandumList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 从网络获取指定用户名的20条动态信息
	 * */
	public static ArrayList<InformationItem> readDynamicInfo(String username,String timestamp)
	{
		ArrayList<InformationItem> informationList;
		try
		{
			Log.i(tag,"username:"+username);
			Log.i(tag,"timestamp:"+timestamp);
			//获取记录信息url
			String urlString = "http://1.maorecorder.sinaapp.com/dynamic.php"+"?"+"username="+username+"&timestamp="+timestamp;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			informationList = parseInformationJson(result);
			if(informationList==null) return null;
			else return informationList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;//未知错误
		}
	}
	/**
	 * 解析dynamic json数据
	 * */
	private static ArrayList<InformationItem> parseDynamicJson(String result)
	{
		System.out.println(result);
		ArrayList<InformationItem> arrayList = new ArrayList<InformationItem>();
		try
		{
			JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
			for(int i=0;i<jsonArray.length();i++)
			{
				InformationItem item = new InformationItem();
				item.picturePath = jsonArray.getJSONObject(i).getString("picturepath");
				item.username = jsonArray.getJSONObject(i).getString("username");
				item.publishTime = jsonArray.getJSONObject(i).getString("publishtime");
				item.fromDevice = jsonArray.getJSONObject(i).getString("fromdevice");
				item.text = jsonArray.getJSONObject(i).getString("text");
				item.permission = Integer.parseInt(jsonArray.getJSONObject(i).getString("permission"));
				arrayList.add(item);
			}
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 将一条记录上传到服务器上
	 * */
	public static boolean uploadOneRecord(InformationItem item)
	{
		try
		{
			String urlString = "http://1.maorecorder.sinaapp.com/insertinformation.php";
			HttpPost httpPost = new HttpPost(urlString);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("picturepath",item.picturePath));
			parameters.add(new BasicNameValuePair("username",item.username));
			parameters.add(new BasicNameValuePair("publishtime",item.publishTime));
			parameters.add(new BasicNameValuePair("fromdevice",item.fromDevice));
			parameters.add(new BasicNameValuePair("text",item.text));
			parameters.add(new BasicNameValuePair("permission",""+item.permission));
			HttpEntity entity = new UrlEncodedFormEntity(parameters,HTTP.UTF_8);
			httpPost.addHeader("charset", HTTP.UTF_8);//否则会出现中文乱码
			httpPost.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			//设置超时
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			HttpResponse response = httpClient.execute(httpPost);
			if(response==null) return false;
			String result = getData(response.getEntity().getContent());
			Log.i(tag,result);
			if("1".equals(result)) return true;
			else return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 将服务器上一条记录删除
	 * */
	public static boolean deleteOneRecord(String name,String timestamp,String permission)
	{
		try
		{
			String urlString = "http://1.maorecorder.sinaapp.com/deleteinformation.php";
			HttpPost httpPost = new HttpPost(urlString);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("username",name));
			parameters.add(new BasicNameValuePair("timestamp",timestamp));
			parameters.add(new BasicNameValuePair("permission",permission));
			HttpEntity entity = new UrlEncodedFormEntity(parameters,HTTP.UTF_8);
			httpPost.addHeader("charset", HTTP.UTF_8);//否则会出现中文乱码
			httpPost.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			//设置超时
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			HttpResponse response = httpClient.execute(httpPost);
			if(response==null) return false;
			String result = getData(response.getEntity().getContent());
			Log.i(tag,result);
			if("1".equals(result)) return true;
			else return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 将一条备忘上传到服务器
	 * */
	public static boolean uploadOneMemorandum(String name,MemorandumItem item)
	{
		try
		{
			Log.i(tag,"day:"+item.date.day);
			Log.i(tag,"timestamp:"+item.date.timestamp);
			String urlString = "http://1.maorecorder.sinaapp.com/insertmemorandum.php";
			HttpPost httpPost = new HttpPost(urlString);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("username",name));
			parameters.add(new BasicNameValuePair("year",item.date.year+""));
			parameters.add(new BasicNameValuePair("month",item.date.month+""));
			parameters.add(new BasicNameValuePair("day",item.date.day+""));
			parameters.add(new BasicNameValuePair("hour",item.date.hour+""));
			parameters.add(new BasicNameValuePair("minute",item.date.minute+""));
			parameters.add(new BasicNameValuePair("second",item.date.second+""));
			parameters.add(new BasicNameValuePair("timestamp",item.date.timestamp+""));
			parameters.add(new BasicNameValuePair("text",item.text));
			HttpEntity entity = new UrlEncodedFormEntity(parameters,HTTP.UTF_8);
			httpPost.addHeader("charset", HTTP.UTF_8);//否则会出现中文乱码
			httpPost.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			//设置超时
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			HttpResponse response = httpClient.execute(httpPost);
			if(response==null) return false;
			String result = getData(response.getEntity().getContent());
			Log.i(tag,result);
			if("1".equals(result)) return true;
			else return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 将服务器上一条备忘删除
	 * */
	public static boolean deleteOneMemorandum(String name,String timestamp)
	{
		try
		{
			String urlString = "http://1.maorecorder.sinaapp.com/deletememorandum.php";
			HttpPost httpPost = new HttpPost(urlString);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("username",name));
			parameters.add(new BasicNameValuePair("timestamp",timestamp));
			HttpEntity entity = new UrlEncodedFormEntity(parameters,HTTP.UTF_8);
			httpPost.addHeader("charset", HTTP.UTF_8);//否则会出现中文乱码
			httpPost.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			//设置超时
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			HttpResponse response = httpClient.execute(httpPost);
			if(response==null) return false;
			String result = getData(response.getEntity().getContent());
			Log.i(tag,result);
			if("1".equals(result)) return true;
			else return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 关注别人
	 * */
	public static boolean fouseOther(String name,String othername,String timestamp)
	{
		try
		{
			Log.i(tag,"myname:"+name);
			Log.i(tag,"othername:"+othername);
			Log.i(tag,"timestamp:"+timestamp);
			String urlString = "http://1.maorecorder.sinaapp.com/fouse.php";
			HttpPost httpPost = new HttpPost(urlString);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("username",name));
			parameters.add(new BasicNameValuePair("timestamp",timestamp));
			parameters.add(new BasicNameValuePair("othername",othername));
			HttpEntity entity = new UrlEncodedFormEntity(parameters,HTTP.UTF_8);
			httpPost.addHeader("charset", HTTP.UTF_8);//否则会出现中文乱码
			httpPost.setEntity(entity);
			HttpClient httpClient = new DefaultHttpClient();
			//设置超时
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			HttpResponse response = httpClient.execute(httpPost);
			if(response==null) return false;
			String result = getData(response.getEntity().getContent());
			Log.i(tag,result);
			if("1".equals(result)) return true;
			else return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 读取所有我关注的用户名
	 * */
	public static ArrayList<Fouse> readMyFouseInfo(String name)
	{
		ArrayList<Fouse> myFouseList;
		try
		{
			Log.i(tag,"username:"+name);
			//获取记录信息url
			String urlString = "http://1.maorecorder.sinaapp.com/allfouse.php"+"?"+"username="+name;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			myFouseList = parseMyFouseJson(result);
			if(myFouseList==null) return null;
			else return myFouseList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;//未知错误
		}
	}
	/**
	 * 解析MyFouse json数据
	 * */
	private static ArrayList<Fouse> parseMyFouseJson(String result)
	{
		System.out.println(result);
		ArrayList<Fouse> arrayList = new ArrayList<Fouse>();
		try
		{
			JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
			for(int i=0;i<jsonArray.length();i++)
			{
				Fouse item = new Fouse();
				item.username = jsonArray.getJSONObject(i).getString("username");
				item.timeStamp = jsonArray.getJSONObject(i).getString("timestamp");
				arrayList.add(item);
			}
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 读取所有关注我的用户名
	 * */
	public static ArrayList<Fouse> readFousemeInfo(String name)
	{
		ArrayList<Fouse> myFouseList;
		try
		{
			Log.i(tag,"username:"+name);
			//获取记录信息url
			String urlString = "http://1.maorecorder.sinaapp.com/fouseme.php"+"?"+"username="+name;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			myFouseList = parseFousemeJson(result);
			if(myFouseList==null) return null;
			else return myFouseList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;//未知错误
		}
	}
	/**
	 * 解析Fouseme json数据
	 * */
	private static ArrayList<Fouse> parseFousemeJson(String result)
	{
		System.out.println(result);
		ArrayList<Fouse> arrayList = new ArrayList<Fouse>();
		try
		{
			JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
			for(int i=0;i<jsonArray.length();i++)
			{
				Fouse item = new Fouse();
				item.username = jsonArray.getJSONObject(i).getString("username");
				item.timeStamp = jsonArray.getJSONObject(i).getString("timestamp");
				arrayList.add(item);
			}
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 获取消息
	 * */
	public static ArrayList<Fouse> readMessageInfo(String name)
	{
		ArrayList<Fouse> messageList;
		try
		{
			Log.i(tag,"username:"+name);
			//获取记录信息url
			String urlString = "http://1.maorecorder.sinaapp.com/message.php"+"?"+"username="+name;
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			Log.i(tag,result);
			if("fail".equals(result)) return null;
			messageList = parseMessageJson(result);
			if(messageList==null) return null;
			else return messageList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;//未知错误
		}
	}
	/**
	 * 解析message json数据
	 * */
	private static ArrayList<Fouse> parseMessageJson(String result)
	{
		System.out.println(result);
		ArrayList<Fouse> arrayList = new ArrayList<Fouse>();
		try
		{
			JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
			for(int i=0;i<jsonArray.length();i++)
			{
				Fouse item = new Fouse();
				item.username = jsonArray.getJSONObject(i).getString("username");
				item.othername = jsonArray.getJSONObject(i).getString("othername");
				item.timeStamp = jsonArray.getJSONObject(i).getString("timestamp");
				arrayList.add(item);
			}
			return arrayList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 上传头像
	 * */
	public static boolean uploadPicture(String name,String path)
	{
		return false;
	}
	/**
	 * 检查最新版本
	 * */
	public static String checkUpdate()
	{
		try
		{
			String urlString = "http://1.maorecorder.sinaapp.com/version.php";
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(10000);//设置超时
			con.setConnectTimeout(10000);
			String result = getData(con.getInputStream());
			con.disconnect();
			System.out.println("checkUpdate");
			return result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
}
