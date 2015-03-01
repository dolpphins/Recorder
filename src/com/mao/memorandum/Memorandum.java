package com.mao.memorandum;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mao.myclass.InformationItem;
import com.mao.myclass.MemorandumItem;
import com.mao.myclass.MyDate;
import com.mao.recorder.R;
import com.mao.util.DatabaseTool;
import com.mao.util.FunctionUtil;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

public class Memorandum {
	private final static String tag = "Memorandum";
	private final static String databaseName = "recorder.db";//数据库名
	private final static int REQUEST_SUCCESS = 1;//查询成功
	private final static int REQUEST_FAIL = 2;//查询失败
	private final static int INSERT_SUCCESS = 3;//插入成功
	private final static int INSERT_FAIL = 4;//插入失败
	private final static int UPDATE_SUCCESS = 5;//更新成功
	private final static int UPDATE_FAIL = 6;//更新失败
	public final static int GET_SUCCESS = 7;//获取网络数据成功
	public final static int GET_FAIL = 8;//获取网络数据失败
	
	private Activity activity;
	private String username;
	private LinearLayout calender;//日历
	
	private TextView current_year_and_month;//当前年份和月份
	private TableLayout calendar_table;//日历中表格布局
	private ImageView date_forward_button;//向前按钮
	private ImageView date_after_button;//向后按钮
	private MyDate myDate;
	private MyDate realDate;
	private TextView currentDayTextView;//当天的textview
	private TextView realDayTextView;//真实日期当天textview
	private TextView memorandum_detail;//备忘详情
	
	private ArrayList<MemorandumItem> memorandumList;//某一天的备忘集
	private ArrayList<MemorandumItem> memorandumListAllDay;//所有天的备忘集(从网络获取)
	
	private Handler handler;//处理子线程传过来的消息
	public Memorandum(Activity activity,String username)
	{
		this.activity = activity;
		this.username = username;
		//初始化Handler对象
		initHandler();
		//创建该用户表(不存在的话)
		DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
		dbTool.createMemorandumTable(username);
	}
	/**
	 * 显示布局(日历)
	 * */
	public void show(View view)
	{
		if(view == null) return;
		if(view instanceof LinearLayout)
		{
			if(calender==null) calender = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.memorandum_content_layout, null);
			current_year_and_month = (TextView) calender.findViewById(R.id.current_year_and_month);
			calendar_table = (TableLayout) calender.findViewById(R.id.calender_table);
			date_forward_button = (ImageView) calender.findViewById(R.id.date_forward_button);
			date_after_button = (ImageView) calender.findViewById(R.id.date_after_button);
			memorandum_detail = (TextView) calender.findViewById(R.id.memorandum_detail);
			myDate = FunctionUtil.getDate();
			myDate = FunctionUtil.getCurrentCalendar(myDate);
			realDate = FunctionUtil.getDate();
			initCalendar();
			setClickEvent();//为后退按钮、前进按钮、备忘详情添加点击事件
			calender.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			((LinearLayout)view).addView(calender);
			//从数据库获取数据
			getMemorandumOneDay(myDate);
		}
	}
	/**
	 * 初始化日历
	 * */
	private void initCalendar()
	{
		for(int y = 0;y<myDate.currentMonthTable.length;y++)
		{
			System.out.println("myDate.currentMonthTable:"+myDate.currentMonthTable[y]);
		}
		if(current_year_and_month!=null)
		{
			current_year_and_month.setText(myDate.year + "年" + (myDate.month+1) + "月");
		}
		if(calendar_table!=null)
		{
			int i,j,m,n,k=0;
			m = calendar_table.getChildCount();
			int start = -1,end = -1;
			for(i=0;i<myDate.currentMonthTable.length;i++)
			{
				if(start ==-1 && myDate.currentMonthTable[i]==1) start = i;
				if(myDate.currentMonthTable[i]==1) end = i;
			}
			for(i=1;i<m;i++)
			{
				TableRow tr = (TableRow)calendar_table.getChildAt(i);
				n = tr.getChildCount();
				for(j=0;j<n;j++)
				{
					final TextView tv = (TextView) tr.getChildAt(j);
					tv.setText(myDate.currentMonthTable[k]+"");
					if(k<start || k>=end) 
					{
						tv.setTextColor(Color.parseColor("#cbb9b9"));
						tv.setBackground(null);
						//移除掉点击事件
						tv.setOnClickListener(null);
					}
					else if(myDate.day == myDate.currentMonthTable[k])
					{
						if(myDate.currentMonthTable[k]==realDate.day && myDate.month==realDate.month && myDate.year==realDate.year)
						{
							tv.setTextColor(Color.BLUE);
							realDayTextView = tv;
						}
						else tv.setTextColor(Color.parseColor("#ffffff"));
						tv.setBackground(activity.getResources().getDrawable(R.drawable.current_day_bg));
						currentDayTextView = tv;
						final int t = k;
						tv.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if(currentDayTextView!=null) 
							    {
									currentDayTextView.setBackground(null);
									if(realDayTextView!=currentDayTextView || myDate.month!=realDate.month) currentDayTextView.setTextColor(Color.parseColor("#000000"));
							    }
								tv.setBackground(activity.getResources().getDrawable(R.drawable.current_day_bg));
								currentDayTextView = tv;
								if(!(myDate.currentMonthTable[t]==realDate.day && myDate.month==realDate.month && myDate.year==realDate.year)) tv.setTextColor(Color.WHITE);
								if(myDate.currentMonthTable[t]==realDate.day && myDate.month==realDate.month && myDate.year==realDate.year)
								{
									tv.setTextColor(Color.BLUE);
									realDayTextView = tv;
								}
								myDate.day = myDate.currentMonthTable[t];
								//从数据库获取数据
								getMemorandumOneDay(myDate);
							}
						});
					}
					else 
					{
						if(myDate.currentMonthTable[k]==realDate.day && myDate.month==realDate.month && myDate.year==realDate.year)
						{
							tv.setTextColor(Color.BLUE);
							realDayTextView = tv;
						}
						else tv.setTextColor(Color.parseColor("#000000"));
						tv.setBackground(null);
						final int t = k;
						tv.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if(currentDayTextView!=null) 
								{
									currentDayTextView.setBackground(null);
									if(realDayTextView!=currentDayTextView || myDate.month!=realDate.month) currentDayTextView.setTextColor(Color.parseColor("#000000"));
								}
								tv.setBackground(activity.getResources().getDrawable(R.drawable.current_day_bg));
								currentDayTextView = tv;
								if(!(myDate.day == myDate.currentMonthTable[t] && myDate.currentMonthTable[t]==realDate.day && myDate.month==realDate.month && myDate.year==realDate.year)) tv.setTextColor(Color.WHITE);
								myDate.day = myDate.currentMonthTable[t];
								//从数据库获取数据
								getMemorandumOneDay(myDate);
							}
						});
					}
					k++;
				}
			}
		}
	}
	/**
	 * 设置点击事件
	 * */
	public void setClickEvent()
	{
		//向前按钮点击事件
		date_forward_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myDate = FunctionUtil.getLastMonth(myDate);
				myDate = FunctionUtil.getCurrentCalendar(myDate);
				initCalendar();
			}
		});
		//向后按钮点击事件
		date_after_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myDate = FunctionUtil.getNextMonth(myDate);
				myDate = FunctionUtil.getCurrentCalendar(myDate);
				initCalendar();
			}
		});
		//备忘详情点击事件
		memorandum_detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//重新获取
				if(memorandumList==null) 
				{
					Toast.makeText(activity, "正在重新获取数据", Toast.LENGTH_SHORT).show();
					//从数据库获取数据
					getMemorandumOneDay(myDate);
				}
				//跳转到添加备忘界面
				else if(memorandumList.size()<=0)
				{
					Intent intent = new Intent(activity,AddMemorandumActivity.class);
					intent.putExtra("username", username);
					intent.putExtra("myDate", myDate);
					activity.startActivityForResult(intent, 103);
				}
				//跳转到查看备忘详情和添加备忘界面
				else
				{
					Intent intent = new Intent(activity,MemorandumDetailActivity.class);
					intent.putExtra("memorandumlist", memorandumList);
					intent.putExtra("username", username);
					intent.putExtra("myDate", myDate);
					activity.startActivityForResult(intent, 104);
				}
			}
		});
	}
	/**
	 * 初始化Handler对象
	 * */
	private void initHandler()
	{
		if(handler == null)
		{
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg)
				{
					switch(msg.what)
					{
					//查询备忘成功
					case REQUEST_SUCCESS:
						updateUI();
						break;
					//查询备忘失败
					case REQUEST_FAIL:
						Toast.makeText(activity, "获取数据失败", Toast.LENGTH_SHORT).show();
						if(memorandum_detail!=null) memorandum_detail.setText("点击重新获取");
						break;
					//插入成功
					case INSERT_SUCCESS:
						break;
					//插入失败
					case INSERT_FAIL:
						break;
					//更新成功
					case UPDATE_SUCCESS:
						break;
					//更新失败
					case UPDATE_FAIL:
						break;
					//获取网络备忘数据成功
					case GET_SUCCESS:
						Log.i(tag,"get memorandum from network success");
						break;
					//获取备忘网络数据失败
					case GET_FAIL:
						Toast.makeText(activity, "获取网络数据失败", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
		}
	}
	/**
	 * 开启子线程查询数据库当天的所有备忘
	 * */
	private void getMemorandumOneDay(MyDate date)
	{
		new Thread(
			new Runnable(){
				@Override
				public void run() {
					DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
					memorandumList = dbTool.queryMemorandumOfDay(username, myDate);
					if(memorandumList==null) handler.sendEmptyMessage(REQUEST_FAIL);
					else handler.sendEmptyMessage(REQUEST_SUCCESS);
				}
			}).start();
	}
	/**
	 * 将添加的备忘保存起来(内存中和数据库)
	 * */
	public void addOneMemorandum(final String name,final MemorandumItem item)
	{
		if(memorandumList==null) memorandumList = new ArrayList<MemorandumItem>();
		memorandumList.add(item);
		updateUI();//更新UI
		//开启子线程写入到数据库中
		new Thread(
				new Runnable(){
					@Override
					public void run() {
						DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
						boolean isSuccess= dbTool.addOneMemorandum(name, item,myDate);
						if(!isSuccess) handler.sendEmptyMessage(INSERT_FAIL);
						else handler.sendEmptyMessage(INSERT_SUCCESS);
					}
				}).start();
	}
	/**
	 * 查询备忘时更新UI
	 * */
	private void updateUI()
	{
		if(memorandum_detail==null) return;
		Log.i(tag,memorandumList.size()+"");
		if(memorandumList.size()>0)
		{
			memorandum_detail.setText("当天有"+memorandumList.size()+"条备忘点击查看");
		}
		else
		{
			Log.i(tag,"当天没有备忘");
			memorandum_detail.setText(activity.getResources().getString(R.string.memorandum_tip));
		}
	}
	/**
	 * 备忘发生修改时调用
	 * */
	public void handleRevise(ArrayList<MemorandumItem> memorandumList)
	{
		this.memorandumList = memorandumList;
		//更新UI
		updateUI();
		//开启子线程更新数据库
		new Thread(new Runnable(){
			@Override
			public void run() {
				DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
				boolean isSuccess = dbTool.updateAllMemorandumOneDay(username, Memorandum.this.memorandumList, myDate);
				if(isSuccess) handler.sendEmptyMessage(UPDATE_SUCCESS);
				else handler.sendEmptyMessage(UPDATE_FAIL);
			}
		}).start();
	}
	/**
	 * 开启子线程获取网络备忘数据(在该账号登录成功后调用)
	 * */
	public void getInformationListFromNetwork()
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
					ArrayList<MemorandumItem> tempList = Network.readMemorandumInfo(username);
					if(tempList==null) handler.sendEmptyMessage(GET_FAIL);
					else
					{
						//将数据同步到本地数据库
						memorandumListAllDay = tempList;
						Boolean isSuccess = false;
						DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
						isSuccess = dbTool.reWriteAllMemorandumToDB(username, tempList);
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
