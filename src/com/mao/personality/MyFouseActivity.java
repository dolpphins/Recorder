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
	private final static String databaseName = "recorder.db";//���ݿ���
	private final static int GET_SUCCESS = 1;//��ȡ���ݳɹ�
	private final static int GET_FAIL = 2;//��ȡ����ʧ��
	final private static int SHOW_DIALOG = 3;//��ʾ�Ի���
	final private static int CANCEL_DIALOG = 4;//ȡ���Ի���
	final private static int UPDATE_SUCCESS = 5;//���³ɹ�
	final private static int UPDATE_FAIL = 6;//����ʧ��
	
	private LinearLayout myfouse;
	private ImageView myfouse_back;//����ͼ��
	private PullToRefreshListView myfouse_lv;
	
	private Handler handler;
	private ArrayList<Fouse> myFouseList = null;
	private String username;
	private ProgressDialog pd=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
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
	 * ��ʼ��Handler����
	 * */
	private void initHandler()
	{
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//��ȡ���ݳɹ�
				case GET_SUCCESS:
					if(myfouse.getChildCount()>1) myfouse.removeViewAt(1);
					initListView();
					myfouse.addView(myfouse_lv);
					break;
				//��ȡ����ʧ��
				case GET_FAIL:
					Toast.makeText(MyFouseActivity.this, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
					break;
				//��ʾ���ȶԻ���
				case SHOW_DIALOG:
					pd = ProgressDialog.show(MyFouseActivity.this, "��ʾ", "���ڻ�ȡ����");
					break;
				//ȡ�����ȶԻ���
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					pd=null;
					break;
				//���³ɹ�
				case UPDATE_SUCCESS:
					break;
				//����ʧ��
				case UPDATE_FAIL:
					break;
				}
			}
		};
	}
	/**
	 * �������̻߳�ȡ������Ϣ
	 * */
	private void getInfoFromNetwork(final String name)
	{
		if(!NetWorkUtil.isAvailable(MyFouseActivity.this))
		{
			Toast.makeText(MyFouseActivity.this, "���粻����", Toast.LENGTH_SHORT).show();
		}
		else
		{
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					handler.sendEmptyMessage(SHOW_DIALOG);//��ʾ���ȶԻ���
					myFouseList = Network.readMyFouseInfo(name);
					handler.sendEmptyMessage(CANCEL_DIALOG);//ȡ�����ȶԻ���
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
	 * ��ʼ��listview
	 * */
	private void initListView()
	{
		myfouse_lv = new PullToRefreshListView(MyFouseActivity.this,Mode.PULL_FROM_START,AnimationStyle.FLIP);
		myfouse_lv.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		myfouse_lv.getLoadingLayoutProxy(true,false).setPullLabel("����ˢ��");
		myfouse_lv.getLoadingLayoutProxy(true,false).setReleaseLabel("�ͷ�����ˢ��");
		myfouse_lv.getLoadingLayoutProxy(true,false).setRefreshingLabel("����ˢ��...");
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
	 * ��ʼ��listviewһЩ�¼�
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
	 * ��ͨ�������ȡ��Ϣ����±������ݿ����ҹ�ע�����б���Ϣ
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
