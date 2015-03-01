package com.mao.dynamic;

import java.util.ArrayList;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.mao.myclass.Fouse;
import com.mao.myclass.InformationItem;
import com.mao.recorder.DetailActivity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Dynamic {
	private final static String tag = "Dynamic";
	private final static String databaseName = "recorder.db";//数据库名
	private final static int REQUEST_SUCCESS = 1;//获取本地数据库动态信息成功
	private final static int REQUEST_FAIl = 2;//获取本地数据库动态信息失败
	private final static int GET_SUCCESS = 3;//通过网络获取动态信息成功
	private final static int GET_FAIL = 4;//通过网络获取动态信息失败
	private final static int GET_NO_MORE = 5;//没有更多动态信息
	
	private final static int PULL_DOWN = 6;//下来刷新
	private final static int PULL_UP = 7;//上拉加载更多
	private final static int SET_LAST_SELECT = 8;//加载更多后设置上次选择的位置
	
	private final static int FOUSE_SUCCESS = 9;//关注成功
	private final static int FOUSE_FAIL = 10;//关注失败
	
	private Activity activity;
	private String username;
	
	private ArrayList<InformationItem> dynamicInformationList = new ArrayList<InformationItem>();//保存每个信息项
	private ArrayList<Fouse> myFouseList = new ArrayList<Fouse>();
	private PullToRefreshListView listView;
	private boolean isGetting = false;//标记是否正在获取数据
	private LinearLayout linearLayout;
	private Handler handler;//用于处理子线程传过来的消息
	
	private LinearLayout no_dynamic_tip_layout;
	private int currentPosition = 0;
	private LinearLayout currentViewItem = null;
	
	private TextView tempV;
	private TextView tempV2;
	public Dynamic(Activity activity,String username)
	{
		this.activity = activity;
		this.username = username;
		//初始化Handler对象
		initHandler();
	}
	/**
	 * 显示listview界面(可以下拉刷新和加载更多)
	 * */
	public void show(View parentView)
	{
		if(parentView instanceof LinearLayout)
		{
			linearLayout = (LinearLayout)parentView;
			//获取数据
			getDbFromDB();
			//初始化ListView
		}
	}
	/**
	 * 初始化ListView
	 * */
	@SuppressWarnings("unchecked")
	public void initListView()
	{
		//支持上拉和下拉，动画为flip
		listView = new PullToRefreshListView(activity,Mode.BOTH,AnimationStyle.FLIP);
		//设置一些属性
		listView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		listView.getLoadingLayoutProxy(true,false).setPullLabel("下拉刷新");
		listView.getLoadingLayoutProxy(true,false).setReleaseLabel("释放立即刷新");
		listView.getLoadingLayoutProxy(true,false).setRefreshingLabel("正在刷新...");
		listView.getLoadingLayoutProxy(false,true).setPullLabel("上拉加载更多");
		listView.getLoadingLayoutProxy(false,true).setReleaseLabel("释放立即加载");
		listView.getLoadingLayoutProxy(false,true).setRefreshingLabel("正在加载...");
		listView.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Log.i(tag,"getView");
				LinearLayout itemView = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.dynamic_information_item_layout, null);
				ImageView dynamic_item_picture = (ImageView) itemView.findViewById(R.id.dynamic_item_picture);
				TextView dynamic_item_username = (TextView) itemView.findViewById(R.id.dynamic_item_username);
				TextView dynamic_item_publishtime = (TextView) itemView.findViewById(R.id.dynamic_item_publishtime);
				TextView dynamic_item_fromdevice = (TextView) itemView.findViewById(R.id.dynamic_item_fromdevice);
				TextView dynamic_item_text = (TextView) itemView.findViewById(R.id.dynamic_item_text);
				TextView dynamic_item_permission = (TextView) itemView.findViewById(R.id.dynamic_item_permission);
				final TextView dynamic_item_fouse = (TextView) itemView.findViewById(R.id.dynamic_item_fouse);
				if(!dynamicInformationList.get(position).picturePath.equals("#")) {dynamic_item_picture.setImageBitmap(FunctionUtil.getBitmapByPath(dynamicInformationList.get(position).picturePath));}
				dynamic_item_username.setText(dynamicInformationList.get(position).username);
				dynamic_item_publishtime.setText(FunctionUtil.convertTimestampToString(Long.parseLong(dynamicInformationList.get(position).publishTime)));
				dynamic_item_fromdevice.setText("来自"+dynamicInformationList.get(position).fromDevice);
				dynamic_item_text.setText(dynamicInformationList.get(position).text);
				int permission = dynamicInformationList.get(position).permission;
				if(permission==RecordActivity.PERMISSION_PERSONALLY) dynamic_item_permission.setText(activity.getResources().getString(R.string.personally));
				else if(permission==RecordActivity.PERMISSION_FRIENDLLY) dynamic_item_permission.setText(activity.getResources().getString(R.string.friendlly));
				else if(permission==RecordActivity.PERMISSION_COMMON) dynamic_item_permission.setText(activity.getResources().getString(R.string.common));
				if(username.equals(dynamicInformationList.get(position).username))
				{
					dynamic_item_fouse.setVisibility(View.GONE);
				}
				if(hasFouse(dynamicInformationList.get(position).username))
				{
					dynamic_item_fouse.setText(activity.getResources().getString(R.string.alreadyfouse));
					dynamic_item_fouse.setEnabled(false);
				}
				else
				{
					//关注按钮设置点击事件
					final int t = position;
					dynamic_item_fouse.setOnTouchListener(new OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							TextView tv = (TextView)v;
							String text = tv.getText().toString();
							//说明已关注，不响应点击事件
							if(activity.getResources().getString(R.string.alreadyfouse).equals(text))
							{
								return true;
							}
							switch(event.getAction())
							{
							case MotionEvent.ACTION_DOWN:
								dynamic_item_fouse.setBackground(activity.getResources().getDrawable(R.drawable.fouse_button_bg_sel));
								break;
							case MotionEvent.ACTION_UP:
								fouseOtherPeople(dynamicInformationList.get(t).username,tv,dynamic_item_fouse);
								break;
							}
							return true;
						}
					});
				}
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
				return dynamicInformationList.size();
			}
		});
		listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2() {
			//下拉刷新
			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				Log.i(tag,"pull down to refresh");
				getDynamicFromNetwork(PULL_DOWN);
			}
			//上拉刷新
			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				Log.i(tag,"pull up to refresh");
				getDynamicFromNetwork(PULL_UP);
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 Log.i(tag,""+position);
				 Intent intent = new Intent(activity,DynamicDetailActivity.class);
				 intent.putExtra("dynamicinformationitem", dynamicInformationList.get(position-1));
				 currentViewItem = (LinearLayout)view;
				 boolean hasFouse = false;
				 TextView dynamic_item_fouse = (TextView) view.findViewById(R.id.dynamic_item_fouse);
				 if("已关注".equals(dynamic_item_fouse.getText().toString())) hasFouse = true;
				 intent.putExtra("hasFouse", hasFouse);
				 intent.putExtra("myname", username);
				 boolean isOwn = false;
				 if(username.equals(dynamicInformationList.get(position-1).username)) isOwn = true;
				 intent.putExtra("isOwn", isOwn);
				 //activity.startActivityForResult(intent, 102);
				 activity.startActivityForResult(intent, 200);
			}
		});
	}
	/**
	 * 开启子线程从数据库获取动态数据
	 * */
	private void getDbFromDB()
	{
		if(!isGetting)
		{
			isGetting = true;
			new Thread(new Runnable(){
				@Override
				public void run() {
					DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
					dynamicInformationList = dbTool.readDynamicInfo(username);
					myFouseList = dbTool.readMyfouseTable(username);
					isGetting = false;
					if(dynamicInformationList==null) handler.sendEmptyMessage(REQUEST_FAIl);
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
						Log.i(tag,dynamicInformationList.size()+"");
						initListView();
						Log.i(tag,"add view to linearlayout");
						//将listView添加到指定的父容器中
						if(dynamicInformationList.size()>0) linearLayout.addView(listView);
						else
						{
							no_dynamic_tip_layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.no_dynamic_tip_layout, null);
							no_dynamic_tip_layout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							setOnClickEvent();
							linearLayout.addView(no_dynamic_tip_layout);
						}
						break;
					//获取数据失败
					case REQUEST_FAIl:
						Toast.makeText(activity, "获取数据失败", Toast.LENGTH_SHORT).show();
						break;
					//通过网络获取动态信息成功
					case GET_SUCCESS:
						Log.i(tag,"flush success");
						//listView.onRefreshComplete();//刷新完成
						linearLayout.removeViewAt(1);
						break;
					//通过网络获取动态信息失败
					case GET_FAIL:
						listView.onRefreshComplete();//刷新完成
						Toast.makeText(activity, "获取数据失败", Toast.LENGTH_SHORT).show();
						break;
					//没有更多动态信息
					case GET_NO_MORE:
						listView.onRefreshComplete();//刷新完成
						Toast.makeText(activity, "没有更多内容了", Toast.LENGTH_SHORT).show();
						break;
					//加载更多后设置上次选择的位置
					case SET_LAST_SELECT:
						listView.getRefreshableView().setSelection(currentPosition);
						break;
					//关注成功
					case FOUSE_SUCCESS:
						if(tempV!=null&&tempV2!=null)
						{
							tempV.setText(activity.getResources().getString(R.string.alreadyfouse));
							tempV2.setEnabled(false);
							Toast.makeText(activity, "已关注", Toast.LENGTH_SHORT).show();
						}
						break;
					//关注失败
					case FOUSE_FAIL:
						Toast.makeText(activity, "关注失败", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
		}
	}
	/**
	 * 设置点击事件
	 * */
	private void setOnClickEvent()
	{
		if(no_dynamic_tip_layout!=null)
		{
			no_dynamic_tip_layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!NetWorkUtil.isAvailable(activity))
					{
						Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
					}
					else
					{
						Toast.makeText(activity, "正在获取信息", Toast.LENGTH_SHORT).show();
						//通过网络获取动态信息
						getDynamicFromNetwork(PULL_DOWN);
					}
				}
			});
		}
	}
	/**
	 * 开启子线程通过网络获取动态信息(20条)
	 * @param type 类型
	 * */
	private void getDynamicFromNetwork(final int type)
	{
		if(!NetWorkUtil.isAvailable(activity))
		{
			Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
			handler.sendEmptyMessage(GET_FAIL);
		}
		else
		{
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					
					ArrayList<InformationItem> temp;
					String timestamp;
					if(type==PULL_DOWN) timestamp = System.currentTimeMillis()+"";
					else timestamp = dynamicInformationList.get(dynamicInformationList.size()-1).publishTime;
					if(type == PULL_DOWN) temp = Network.readDynamicInfo(username, timestamp);
					else temp = Network.readDynamicInfo(username, timestamp);
					myFouseList = Network.readMyFouseInfo(username);
					if(temp==null) handler.sendEmptyMessage(GET_FAIL);
					else if(temp.size()<=0) handler.sendEmptyMessage(GET_NO_MORE);
					else
					{
						//是下拉刷新
						if(type == PULL_DOWN)
						{
							dynamicInformationList = temp;
							//写入数据库
							DatabaseTool dbTool = new DatabaseTool(activity, databaseName);
							dbTool.updateDynamicTable(username, dynamicInformationList);
							dbTool.updateMyFouse(username, myFouseList);
							handler.sendEmptyMessage(GET_SUCCESS);
							handler.sendEmptyMessage(REQUEST_SUCCESS);
						}
						//是上拉加载更多
						else
						{
							currentPosition = listView.getRefreshableView().getFirstVisiblePosition();
							for(InformationItem item:temp) dynamicInformationList.add(item);
							handler.sendEmptyMessage(GET_SUCCESS);
							handler.sendEmptyMessage(REQUEST_SUCCESS);
							handler.sendEmptyMessage(SET_LAST_SELECT);
						}
					}
				}
			}).start();
		}
	}
	/**
	 * 点击关注时开启子线程写入服务器数据库
	 * */
	private void fouseOtherPeople(final String othername,final TextView v,final TextView tv2)
	{
		if(!NetWorkUtil.isAvailable(activity))
		{
			Toast.makeText(activity, "网络可不用", Toast.LENGTH_SHORT).show();
		}
		else
		{
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					String timestamp = System.currentTimeMillis()+"";
					boolean isSuccess = Network.fouseOther(username, othername, timestamp);
					if(isSuccess) 
					{
						tempV = v;
						tempV2 = tv2;
						DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
						boolean isSuccess2 = dbTool.insertMyFouse(username,new Fouse(username,othername,timestamp));
						if(isSuccess2) handler.sendEmptyMessage(FOUSE_SUCCESS);
						else handler.sendEmptyMessage(FOUSE_FAIL);
					}
					else handler.sendEmptyMessage(FOUSE_FAIL);
				}
			}).start();
		}
	}
	/**
	 * 判断是否已关注
	 * */
	private boolean hasFouse(String name)
	{
		int i=0;
		for(;i<myFouseList.size();i++)
		{
			if(name.equals(myFouseList.get(i).username)) break;
		}
		if(i<myFouseList.size()) return true;
		else return false;
	}
	/**
	 * 修改listview指定位置项的布局
	 * */
	public void reviseViewByPosition()
	{
		TextView dynamic_item_fouse = (TextView) currentViewItem.findViewById(R.id.dynamic_item_fouse);
		dynamic_item_fouse.setText(activity.getResources().getString(R.string.alreadyfouse));
		dynamic_item_fouse.setEnabled(false);
	}
	/**
	 * 添加新关注的账号
	 * */
	public void addNewFouse(String othername,String timestamp)
	{
		int i;
		for(i=0;i<myFouseList.size();i++)
		{
			if(othername.equals(myFouseList.get(i).username)) break;
		}
		if(i==myFouseList.size())
		{
			Fouse fouse = new Fouse();
			fouse.username = othername;
			fouse.timeStamp = timestamp;
			myFouseList.add(fouse);
		}
	}
}
