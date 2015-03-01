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
	private final static String databaseName = "recorder.db";//���ݿ���
	private final static int REQUEST_SUCCESS = 1;//��ѯ�ɹ�
	private final static int REQUEST_FAIL = 2;//��ѯʧ��
	private final static int INSERT_SUCCESS = 3;//����ɹ�
	private final static int INSERT_FAIL = 4;//����ʧ��
	private final static int UPDATE_SUCCESS = 5;//���³ɹ�
	private final static int UPDATE_FAIL = 6;//����ʧ��
	public final static int GET_SUCCESS = 7;//��ȡ�������ݳɹ�
	public final static int GET_FAIL = 8;//��ȡ��������ʧ��
	
	private Activity activity;
	private String username;
	private LinearLayout calender;//����
	
	private TextView current_year_and_month;//��ǰ��ݺ��·�
	private TableLayout calendar_table;//�����б�񲼾�
	private ImageView date_forward_button;//��ǰ��ť
	private ImageView date_after_button;//���ť
	private MyDate myDate;
	private MyDate realDate;
	private TextView currentDayTextView;//�����textview
	private TextView realDayTextView;//��ʵ���ڵ���textview
	private TextView memorandum_detail;//��������
	
	private ArrayList<MemorandumItem> memorandumList;//ĳһ��ı�����
	private ArrayList<MemorandumItem> memorandumListAllDay;//������ı�����(�������ȡ)
	
	private Handler handler;//�������̴߳���������Ϣ
	public Memorandum(Activity activity,String username)
	{
		this.activity = activity;
		this.username = username;
		//��ʼ��Handler����
		initHandler();
		//�������û���(�����ڵĻ�)
		DatabaseTool dbTool = new DatabaseTool(activity,databaseName);
		dbTool.createMemorandumTable(username);
	}
	/**
	 * ��ʾ����(����)
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
			setClickEvent();//Ϊ���˰�ť��ǰ����ť������������ӵ���¼�
			calender.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			((LinearLayout)view).addView(calender);
			//�����ݿ��ȡ����
			getMemorandumOneDay(myDate);
		}
	}
	/**
	 * ��ʼ������
	 * */
	private void initCalendar()
	{
		for(int y = 0;y<myDate.currentMonthTable.length;y++)
		{
			System.out.println("myDate.currentMonthTable:"+myDate.currentMonthTable[y]);
		}
		if(current_year_and_month!=null)
		{
			current_year_and_month.setText(myDate.year + "��" + (myDate.month+1) + "��");
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
						//�Ƴ�������¼�
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
								//�����ݿ��ȡ����
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
								//�����ݿ��ȡ����
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
	 * ���õ���¼�
	 * */
	public void setClickEvent()
	{
		//��ǰ��ť����¼�
		date_forward_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myDate = FunctionUtil.getLastMonth(myDate);
				myDate = FunctionUtil.getCurrentCalendar(myDate);
				initCalendar();
			}
		});
		//���ť����¼�
		date_after_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				myDate = FunctionUtil.getNextMonth(myDate);
				myDate = FunctionUtil.getCurrentCalendar(myDate);
				initCalendar();
			}
		});
		//�����������¼�
		memorandum_detail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//���»�ȡ
				if(memorandumList==null) 
				{
					Toast.makeText(activity, "�������»�ȡ����", Toast.LENGTH_SHORT).show();
					//�����ݿ��ȡ����
					getMemorandumOneDay(myDate);
				}
				//��ת����ӱ�������
				else if(memorandumList.size()<=0)
				{
					Intent intent = new Intent(activity,AddMemorandumActivity.class);
					intent.putExtra("username", username);
					intent.putExtra("myDate", myDate);
					activity.startActivityForResult(intent, 103);
				}
				//��ת���鿴�����������ӱ�������
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
	 * ��ʼ��Handler����
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
					//��ѯ�����ɹ�
					case REQUEST_SUCCESS:
						updateUI();
						break;
					//��ѯ����ʧ��
					case REQUEST_FAIL:
						Toast.makeText(activity, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
						if(memorandum_detail!=null) memorandum_detail.setText("������»�ȡ");
						break;
					//����ɹ�
					case INSERT_SUCCESS:
						break;
					//����ʧ��
					case INSERT_FAIL:
						break;
					//���³ɹ�
					case UPDATE_SUCCESS:
						break;
					//����ʧ��
					case UPDATE_FAIL:
						break;
					//��ȡ���籸�����ݳɹ�
					case GET_SUCCESS:
						Log.i(tag,"get memorandum from network success");
						break;
					//��ȡ������������ʧ��
					case GET_FAIL:
						Toast.makeText(activity, "��ȡ��������ʧ��", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
		}
	}
	/**
	 * �������̲߳�ѯ���ݿ⵱������б���
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
	 * ����ӵı�����������(�ڴ��к����ݿ�)
	 * */
	public void addOneMemorandum(final String name,final MemorandumItem item)
	{
		if(memorandumList==null) memorandumList = new ArrayList<MemorandumItem>();
		memorandumList.add(item);
		updateUI();//����UI
		//�������߳�д�뵽���ݿ���
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
	 * ��ѯ����ʱ����UI
	 * */
	private void updateUI()
	{
		if(memorandum_detail==null) return;
		Log.i(tag,memorandumList.size()+"");
		if(memorandumList.size()>0)
		{
			memorandum_detail.setText("������"+memorandumList.size()+"����������鿴");
		}
		else
		{
			Log.i(tag,"����û�б���");
			memorandum_detail.setText(activity.getResources().getString(R.string.memorandum_tip));
		}
	}
	/**
	 * ���������޸�ʱ����
	 * */
	public void handleRevise(ArrayList<MemorandumItem> memorandumList)
	{
		this.memorandumList = memorandumList;
		//����UI
		updateUI();
		//�������̸߳������ݿ�
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
	 * �������̻߳�ȡ���籸������(�ڸ��˺ŵ�¼�ɹ������)
	 * */
	public void getInformationListFromNetwork()
	{
		if(!NetWorkUtil.isAvailable(activity))
		{
			Toast.makeText(activity, "���粻����,�޷���ȡ��������", Toast.LENGTH_SHORT).show();
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
						//������ͬ�����������ݿ�
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
