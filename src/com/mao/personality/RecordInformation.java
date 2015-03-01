package com.mao.personality;

import java.util.ArrayList;

import com.mao.myclass.Account;
import com.mao.myclass.InformationItem;
import com.mao.recorder.DetailActivity;
import com.mao.recorder.MainActivity;
import com.mao.recorder.R;
import com.mao.recorder.RecordActivity;
import com.mao.util.DatabaseTool;
import com.mao.util.FunctionUtil;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RecordInformation {
	private final static String tag = "RecordInformation";
	private final static String databaseName = "recorder.db";//数据库名
	private final static int REQUEST_SUCCESS = 1;//读取数据库成功
	private final static int REQUEST_FAIl = 2;//读取数据库失败
	public final static int FIRST_LOGIN = 3;//标记账号登录
	public final static int NOT_FIRST_LOGIN = 4;//标记账号添加记录
	public final static int UPDATE_ACCOUNT_SUCCESS = 5;//更新当前账号信息成功
	public final static int UPDATE_ACCOUNT_FAIL = 6;//更新当前账号信息失败
	public final static int DELETE_SUCCESS = 7;//删除成功
	public final static int DELETE_FAIL = 8;//删除失败
	public final static int GET_SUCCESS = 9;//获取网络数据成功
	public final static int GET_FAIL = 10;//获取网络数据失败
	
	private ArrayList<InformationItem> informationList = new ArrayList<InformationItem>();//保存每个信息项
	private boolean isGetting = false;//标记是否正在获取数据
	private boolean hasFinish = false;//标记从本地数据库读取信息是否完成并显示
	
	private LinearLayout linearLayout;
	private ListView listView;
	
	private Activity activity;
	private String username;
	private Handler handler;//处理子线程传过来的消息
	public RecordInformation(Activity activity,String username)
	{
		this.activity = activity;
		this.username = username;
		//初始化Handler对象
	    initHandler();
	}
	/**
	 * 显示界面
	 * */
	public void show(View parentView,int flags)
	{
		if(parentView instanceof LinearLayout)
		{
			linearLayout = (LinearLayout)parentView;
			//登录时去数据库获取信息并显示
			if(flags==FIRST_LOGIN)
			{
				//获取数据
				getDbFromDB();
			}
			//添加一条记录时
			else if(flags==NOT_FIRST_LOGIN)
			{
				initListView();
				//将listView添加到指定的父容器中
				linearLayout.addView(listView);
			}
		}
	}
	/**
	 * 开启子线程从数据库获取数据
	 * */
	private void getDbFromDB()
	{
		Log.i(tag,"start get data");
		if(!isGetting)
		{
			isGetting = true;
			new Thread(new Runnable(){
				@Override
				public void run() {
					DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
					dbTool.createRecordTable(username);
					informationList = dbTool.readRecordInfo(username);
					System.out.println("size:"+informationList.size());
					isGetting = false;
					if(informationList==null) handler.sendEmptyMessage(REQUEST_FAIl);
					else handler.sendEmptyMessage(REQUEST_SUCCESS);
				}
			}).start();
		}
	}
	/**
	 * 初始化Handler对象
	 * */
	private void initHandler()
	{
		if(handler!=null) Log.i(tag,"handler is not null，can't init handler");
		else
		{
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg)
				{
					switch(msg.what)
					{
					//获取数据成功
					case REQUEST_SUCCESS:
						copyInformationList();
						initListView();
						Log.i(tag,"add view to linearlayout");
						//将listView添加到指定的父容器中
						if(informationList.size()>0) linearLayout.addView(listView);
						else
						{
							LinearLayout temp = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.no_record_tip_layout, null);
							temp.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							linearLayout.addView(temp);
						}
						hasFinish = true;//表示从本地数据库读取信息完成并显示
						break;
					//获取数据失败
					case REQUEST_FAIl:
						Toast.makeText(activity, "获取数据失败", Toast.LENGTH_SHORT).show();
						break;
					//更新当前账号成功
					case UPDATE_ACCOUNT_SUCCESS:
						break;
					//更新当前账号失败
					case UPDATE_ACCOUNT_FAIL:
						break;
					//删除成功
					case DELETE_SUCCESS:
						Log.i(tag,"delete success");
						break;
				    //删除失败
					case DELETE_FAIL:
						Log.i(tag,"delete fail");
						break;
					//获取网络数据成功
					case GET_SUCCESS:
						linearLayout.removeViewAt(1);
						copyInformationList();
						initListView();
						Log.i(tag,"add view to linearlayout");
						//将listView添加到指定的父容器中
						if(informationList.size()>0) 
						{
							linearLayout.addView(listView);
						}
						else
						{
							LinearLayout temp = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.no_record_tip_layout, null);
							temp.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							linearLayout.addView(temp);
						}
						break;
					//获取网络数据失败
					case GET_FAIL:
						Toast.makeText(activity, "获取网络数据失败", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
		}
	}
	/**
	 * 开启子线程插入一条记录到表中
	 * */
	public void insert(final InformationItem item)
	{
		new Thread(new Runnable(){
			@Override
			public void run() {
				DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
				dbTool.writeOneRecordToDB(username, item);
			}
		}).start();
	}
	/**
	 * 开启子线程插入记录集到表中
	 * */
	public void insert(final ArrayList<InformationItem> informationList)
	{
		new Thread(new Runnable(){
			@Override
			public void run() {
				DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
				dbTool.writeAllRecordToDB(username, informationList);
			}
		}).start();
	}
	/**
	 * 开启子线程删除表中指定位置的记录
	 * */
	public void delete(final String name,final String publishTime)
	{
		new Thread(new Runnable(){
			@Override
			public void run() {
				DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
				boolean result = dbTool.deleteOnRecordByName(name,publishTime);
				
			}
		}).start();
	}
	/**
	 * 更新数据集
	 * */
	public void setInformationList(ArrayList<InformationItem> informationList)
	{
		this.informationList = informationList;
	}
	/**
	 * 获取记录集
	 * */
	public ArrayList<InformationItem> getInformationList()
	{
		if(informationList==null) System.out.println("informationList is null");
		else System.out.println("informationList is not null");
		return informationList;
	}
	/**
	 * 初始化ListView
	 * */
	/**
	 * 复制数据集
	 * */
    private void copyInformationList()
    {
    	if(informationList==null) return;
    	for(InformationItem item:informationList)
    	{
    		MainActivity.informationList.add(item);
    	}
    }
	private void initListView()
	{
		listView = new ListView(activity);
		listView.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				int count = informationList.size();
				LinearLayout itemView = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.information_item_layout, null);
				ImageView item_picture = (ImageView) itemView.findViewById(R.id.item_picture);
				TextView item_username = (TextView) itemView.findViewById(R.id.item_username);
				TextView item_publishtime = (TextView) itemView.findViewById(R.id.item_publishtime);
				TextView item_fromdevice = (TextView) itemView.findViewById(R.id.item_fromdevice);
				TextView item_text = (TextView) itemView.findViewById(R.id.item_text);
				TextView item_permission = (TextView) itemView.findViewById(R.id.item_permission);
				
				if(!informationList.get(count-position-1).picturePath.equals("#")) {item_picture.setImageBitmap(FunctionUtil.getBitmapByPath(informationList.get(count-position-1).picturePath));}
				item_username.setText(informationList.get(count-position-1).username);
				item_publishtime.setText(FunctionUtil.convertTimestampToString(Long.parseLong(informationList.get(count-position-1).publishTime)));
				item_fromdevice.setText("来自"+informationList.get(count-position-1).fromDevice);
				item_text.setText(informationList.get(count-position-1).text);
				int permission = informationList.get(count-position-1).permission;
				if(permission==RecordActivity.PERMISSION_PERSONALLY) item_permission.setText(activity.getResources().getString(R.string.personally));
				else if(permission==RecordActivity.PERMISSION_FRIENDLLY) item_permission.setText(activity.getResources().getString(R.string.friendlly));
				else if(permission==RecordActivity.PERMISSION_COMMON) item_permission.setText(activity.getResources().getString(R.string.common));
				return itemView;
			}
			
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return informationList.size();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 Log.i(tag,""+position);
				 int count = informationList.size();
				 Intent intent = new Intent(activity,DetailActivity.class);
				 intent.putExtra("informationitem", informationList.get(count-position-1));
				 intent.putExtra("position", count-position-1);
				 activity.startActivityForResult(intent, 102);
			}
		});
	}
	/**
	 * 开启子线程更新当前账号信息(写数据库)
	 * */
	public void updateAccountInfo(final Account account)
	{
		new Thread(new Runnable(){
			@Override
			public void run()
			{
				DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
				boolean isSuccess = dbTool.updateAccountInfo(account);
				if(isSuccess) handler.sendEmptyMessage(UPDATE_ACCOUNT_SUCCESS);
				else handler.sendEmptyMessage(UPDATE_ACCOUNT_FAIL);
			}
		}).start();
	}
	/**
	 * 设置头像
	 * */
	public void setHeadPicture(String name,String path)
	{
		DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
		dbTool.createRecordTable(name);
		for(InformationItem item:informationList)
    	{
    		if(name.equals(item.username))
    		{
    			item.picturePath = path;
    			Account account = new Account();
    			account.username = name;
    			account.picturePath = path;
    			dbTool.updateAccountInfoToInfoTable(account);
    		}
    	}
	}
	/**
	 * 更新ListView的所有发布时间
	 * */
	public void updateAllPublishTime()
	{
		/*int count = listView.getChildCount();
		int i;
		int n;
		if(count<informationList.size()) n = count;
		else n = informationList.size();
		for(i=0;i<n;i++)
		{
			TextView tv = (TextView) listView.getChildAt(i).findViewById(R.id.item_publishtime);
			if(tv!=null)
			{
				tv.setText(FunctionUtil.convertTimestampToString(Long.parseLong(informationList.get(n-1-i).publishTime)));
			}
		}*/
	}
	/**
	 * 开启子线程获取网络数据(在该账号登录成功后调用)
	 * */
	public void getInformationListFromNetwork(final String picturePath)
	{
		if(!NetWorkUtil.isAvailable(activity))
		{
			Toast.makeText(activity, "网络不可用,无法获取网络数据", Toast.LENGTH_SHORT).show();
		}
		else
		{
			new Thread(new Runnable(){
				@Override
				public void run() {
					ArrayList<InformationItem> tempList = Network.readRecordInfo(username);
					if(tempList==null) handler.sendEmptyMessage(GET_FAIL);
					else
					{
						//一直等待直到读取本地数据完成并显示
						while(!hasFinish) {}
						//修改所有头像路径
						informationList = tempList;
						for(InformationItem item:informationList)
						{
							item.picturePath = picturePath;
						}
						//将数据同步到本地数据库
						Boolean isSuccess = false;
						DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
						isSuccess = dbTool.reWriteAllRecordToDB(username, tempList);
						if(isSuccess)
						{
							handler.sendEmptyMessage(GET_SUCCESS);
						}
						else handler.sendEmptyMessage(GET_FAIL);
					}
				}
				
			}).start();
		}
	}
}
