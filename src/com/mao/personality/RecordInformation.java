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
	private final static String databaseName = "recorder.db";//���ݿ���
	private final static int REQUEST_SUCCESS = 1;//��ȡ���ݿ�ɹ�
	private final static int REQUEST_FAIl = 2;//��ȡ���ݿ�ʧ��
	public final static int FIRST_LOGIN = 3;//����˺ŵ�¼
	public final static int NOT_FIRST_LOGIN = 4;//����˺���Ӽ�¼
	public final static int UPDATE_ACCOUNT_SUCCESS = 5;//���µ�ǰ�˺���Ϣ�ɹ�
	public final static int UPDATE_ACCOUNT_FAIL = 6;//���µ�ǰ�˺���Ϣʧ��
	public final static int DELETE_SUCCESS = 7;//ɾ���ɹ�
	public final static int DELETE_FAIL = 8;//ɾ��ʧ��
	public final static int GET_SUCCESS = 9;//��ȡ�������ݳɹ�
	public final static int GET_FAIL = 10;//��ȡ��������ʧ��
	
	private ArrayList<InformationItem> informationList = new ArrayList<InformationItem>();//����ÿ����Ϣ��
	private boolean isGetting = false;//����Ƿ����ڻ�ȡ����
	private boolean hasFinish = false;//��Ǵӱ������ݿ��ȡ��Ϣ�Ƿ���ɲ���ʾ
	
	private LinearLayout linearLayout;
	private ListView listView;
	
	private Activity activity;
	private String username;
	private Handler handler;//�������̴߳���������Ϣ
	public RecordInformation(Activity activity,String username)
	{
		this.activity = activity;
		this.username = username;
		//��ʼ��Handler����
	    initHandler();
	}
	/**
	 * ��ʾ����
	 * */
	public void show(View parentView,int flags)
	{
		if(parentView instanceof LinearLayout)
		{
			linearLayout = (LinearLayout)parentView;
			//��¼ʱȥ���ݿ��ȡ��Ϣ����ʾ
			if(flags==FIRST_LOGIN)
			{
				//��ȡ����
				getDbFromDB();
			}
			//���һ����¼ʱ
			else if(flags==NOT_FIRST_LOGIN)
			{
				initListView();
				//��listView��ӵ�ָ���ĸ�������
				linearLayout.addView(listView);
			}
		}
	}
	/**
	 * �������̴߳����ݿ��ȡ����
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
	 * ��ʼ��Handler����
	 * */
	private void initHandler()
	{
		if(handler!=null) Log.i(tag,"handler is not null��can't init handler");
		else
		{
			handler = new Handler(){
				@Override
				public void handleMessage(Message msg)
				{
					switch(msg.what)
					{
					//��ȡ���ݳɹ�
					case REQUEST_SUCCESS:
						copyInformationList();
						initListView();
						Log.i(tag,"add view to linearlayout");
						//��listView��ӵ�ָ���ĸ�������
						if(informationList.size()>0) linearLayout.addView(listView);
						else
						{
							LinearLayout temp = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.no_record_tip_layout, null);
							temp.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
							linearLayout.addView(temp);
						}
						hasFinish = true;//��ʾ�ӱ������ݿ��ȡ��Ϣ��ɲ���ʾ
						break;
					//��ȡ����ʧ��
					case REQUEST_FAIl:
						Toast.makeText(activity, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
						break;
					//���µ�ǰ�˺ųɹ�
					case UPDATE_ACCOUNT_SUCCESS:
						break;
					//���µ�ǰ�˺�ʧ��
					case UPDATE_ACCOUNT_FAIL:
						break;
					//ɾ���ɹ�
					case DELETE_SUCCESS:
						Log.i(tag,"delete success");
						break;
				    //ɾ��ʧ��
					case DELETE_FAIL:
						Log.i(tag,"delete fail");
						break;
					//��ȡ�������ݳɹ�
					case GET_SUCCESS:
						linearLayout.removeViewAt(1);
						copyInformationList();
						initListView();
						Log.i(tag,"add view to linearlayout");
						//��listView��ӵ�ָ���ĸ�������
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
					//��ȡ��������ʧ��
					case GET_FAIL:
						Toast.makeText(activity, "��ȡ��������ʧ��", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
		}
	}
	/**
	 * �������̲߳���һ����¼������
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
	 * �������̲߳����¼��������
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
	 * �������߳�ɾ������ָ��λ�õļ�¼
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
	 * �������ݼ�
	 * */
	public void setInformationList(ArrayList<InformationItem> informationList)
	{
		this.informationList = informationList;
	}
	/**
	 * ��ȡ��¼��
	 * */
	public ArrayList<InformationItem> getInformationList()
	{
		if(informationList==null) System.out.println("informationList is null");
		else System.out.println("informationList is not null");
		return informationList;
	}
	/**
	 * ��ʼ��ListView
	 * */
	/**
	 * �������ݼ�
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
				item_fromdevice.setText("����"+informationList.get(count-position-1).fromDevice);
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
	 * �������̸߳��µ�ǰ�˺���Ϣ(д���ݿ�)
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
	 * ����ͷ��
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
	 * ����ListView�����з���ʱ��
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
	 * �������̻߳�ȡ��������(�ڸ��˺ŵ�¼�ɹ������)
	 * */
	public void getInformationListFromNetwork(final String picturePath)
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
					ArrayList<InformationItem> tempList = Network.readRecordInfo(username);
					if(tempList==null) handler.sendEmptyMessage(GET_FAIL);
					else
					{
						//һֱ�ȴ�ֱ����ȡ����������ɲ���ʾ
						while(!hasFinish) {}
						//�޸�����ͷ��·��
						informationList = tempList;
						for(InformationItem item:informationList)
						{
							item.picturePath = picturePath;
						}
						//������ͬ�����������ݿ�
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
