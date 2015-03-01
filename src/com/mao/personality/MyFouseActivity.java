package com.mao.personality;

import java.util.ArrayList;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.AnimationStyle;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.mao.myclass.Fouse;
import com.mao.recorder.R;
import com.mao.util.DatabaseTool;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyFouseActivity extends Activity {
	private final static String tag = "MyFouseActivity";
	private final static String databaseName = "recorder.db";//数据库名
	private final static int GET_SUCCESS = 1;//获取数据成功
	private final static int GET_FAIL = 2;//获取数据失败
	final private static int SHOW_DIALOG = 3;//显示对话框
	final private static int CANCEL_DIALOG = 4;//取消对话框
	final private static int UPDATE_SUCCESS = 5;//更新成功
	final private static int UPDATE_FAIL = 6;//更新失败
	
	private LinearLayout myfouse;
	private ImageView myfouse_back;//后退图标
	private PullToRefreshListView myfouse_lv;
	
	private Handler handler;
	private ArrayList<Fouse> myFouseList = null;
	private String username;
	private ProgressDialog pd=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		this.setContentView(R.layout.myfouse_layout);
		initHandler();
		myfouse = (LinearLayout) this.findViewById(R.id.myfouse);
		myfouse_back = (ImageView) this.findViewById(R.id.myfouse_back);
		myfouse_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		username = getIntent().getStringExtra("username");
		//myFouseList = (ArrayList<Fouse>) getIntent().getSerializableExtra("myFouseList");
		//if(myFouseList==null)
		//{
			getInfoFromNetwork(username);
		//}
		//else
		//{
		//	initListView();
		//	myfouse.addView(myfouse_lv);
		//}
	}
	/**
	 * 初始化Handler对象
	 * */
	private void initHandler()
	{
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//获取数据成功
				case GET_SUCCESS:
					if(myfouse.getChildCount()>1) myfouse.removeViewAt(1);
					initListView();
					myfouse.addView(myfouse_lv);
					break;
				//获取数据失败
				case GET_FAIL:
					Toast.makeText(MyFouseActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
					break;
				//显示进度对话框
				case SHOW_DIALOG:
					pd = ProgressDialog.show(MyFouseActivity.this, "提示", "正在获取数据");
					break;
				//取消进度对话框
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					pd=null;
					break;
				//更新成功
				case UPDATE_SUCCESS:
					break;
				//更新失败
				case UPDATE_FAIL:
					break;
				}
			}
		};
	}
	/**
	 * 开启子线程获取网络信息
	 * */
	private void getInfoFromNetwork(final String name)
	{
		if(!NetWorkUtil.isAvailable(MyFouseActivity.this))
		{
			Toast.makeText(MyFouseActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
		}
		else
		{
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					handler.sendEmptyMessage(SHOW_DIALOG);//显示进度对话框
					myFouseList = Network.readMyFouseInfo(name);
					handler.sendEmptyMessage(CANCEL_DIALOG);//取消进度对话框
					if(myFouseList!=null)
					{
						handler.sendEmptyMessage(GET_SUCCESS);
						updateMyFouse();
					}
					else handler.sendEmptyMessage(GET_FAIL);
				}
			}).start();
		}
	}
	/**
	 * 初始化listview
	 * */
	private void initListView()
	{
		myfouse_lv = new PullToRefreshListView(MyFouseActivity.this,Mode.PULL_FROM_START,AnimationStyle.FLIP);
		myfouse_lv.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		myfouse_lv.getLoadingLayoutProxy(true,false).setPullLabel("下拉刷新");
		myfouse_lv.getLoadingLayoutProxy(true,false).setReleaseLabel("释放立即刷新");
		myfouse_lv.getLoadingLayoutProxy(true,false).setRefreshingLabel("正在刷新...");
		myfouse_lv.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final LinearLayout linearLayout = (LinearLayout) MyFouseActivity.this.getLayoutInflater().inflate(R.layout.myfouse_item_layout, null);
				TextView tv = (TextView) linearLayout.findViewById(R.id.myfouse_item_tv);
				tv.setText(myFouseList.get(position).username);
				linearLayout.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						int type = event.getAction();
						switch(type)
						{
						case MotionEvent.ACTION_DOWN:
							linearLayout.setBackground(getResources().getDrawable(R.drawable.fouse_item_bg_sel));
							break;
						case MotionEvent.ACTION_UP:
							linearLayout.setBackground(getResources().getDrawable(R.drawable.fouse_item_bg_nor));
							break;
						}
						return true;
					}
				});
				return linearLayout;
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
				return myFouseList.size();
			}
		});
	}
	/**
	 * 初始化listview一些事件
	 * */
	private void initEvent()
	{
		myfouse_lv.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.i(tag,"refresh my fouse");
				updateMyFouse();
			}
		});
	}
	/**
	 * 在通过网络获取信息后更新本地数据库中我关注的人列表信息
	 * */
	private void updateMyFouse()
	{
		if(username!=null&&myFouseList!=null)
		{
			new Thread(new Runnable(){
				@Override
				public void run() {
					DatabaseTool dbTool = new DatabaseTool(MyFouseActivity.this,databaseName);
					boolean isSuccess = dbTool.updateMyFouse(username, myFouseList);
					if(isSuccess) handler.sendEmptyMessage(UPDATE_SUCCESS);
					else handler.sendEmptyMessage(UPDATE_FAIL);
				}
			}).start();
		}
		else handler.sendEmptyMessage(UPDATE_FAIL);
	}
}
