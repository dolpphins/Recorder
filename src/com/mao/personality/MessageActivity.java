package com.mao.personality;

import java.util.ArrayList;

import com.mao.myclass.Fouse;
import com.mao.recorder.R;
import com.mao.util.FunctionUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MessageActivity extends Activity {
	private final static String tag = "MessageActivity";
	
	private final int GET_SUCCESS = 1;//��ȡ�ɹ�
	private final int GET_FAIL = 2;//��ȡʧ��
	private final int SHOW_PD = 3;//��ʾ���ȶԻ���
	private final int HIDE_PD = 4;//���ؽ��ȶԻ���
	private String username;//��ǰ�û���
	private ArrayList<Fouse> messageList = null;//��Ϣ�б�
	private ListView message_lv;
	private Handler handler;
	private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		this.setContentView(R.layout.message_layout);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//��ȡ��Ϣ�б�ɹ�
				case GET_SUCCESS:
					initListView();
					break;
				//��ȡ��Ϣ�б�ʧ��
				case GET_FAIL:
					Toast.makeText(MessageActivity.this, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
					break;
				//��ʾ���ȶԻ���
				case SHOW_PD:
					pd = ProgressDialog.show(MessageActivity.this, "��ʾ", "���ڻ�ȡ����");
					break;
				//���ؽ��ȶԻ���
				case HIDE_PD:
					if(pd!=null) 
					{
						pd.cancel();
						pd = null;
					}
					break;
				}
			}
		};
		username = getIntent().getStringExtra("username");
		getMessage(username);
	}
	/**
	 * �������̻߳�ȡ��Ϣ
	 * */
	private void getMessage(final String name)
	{
		new Thread(new Runnable(){
			@Override
			public void run() {
				handler.sendEmptyMessage(SHOW_PD);
				messageList = Network.readMessageInfo(name);
				if(messageList==null) handler.sendEmptyMessage(GET_FAIL);
				else handler.sendEmptyMessage(GET_SUCCESS);
				handler.sendEmptyMessage(HIDE_PD);
			}
		}).start();
	}
	/**
	 * ��ʼ��listview
	 * */
	private void initListView()
	{
		message_lv = (ListView) this.findViewById(R.id.message_lv);
		message_lv.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View v, ViewGroup vg) {
				RelativeLayout relativeLayout = (RelativeLayout) MessageActivity.this.getLayoutInflater().inflate(R.layout.message_item_layout, null);
				TextView message_item_tv = (TextView) relativeLayout.findViewById(R.id.message_item_tv);
				TextView message_item_time = (TextView) relativeLayout.findViewById(R.id.message_item_time);
				String text1 = messageList.get(position).username+"��ע��"+messageList.get(position).othername;
				String text2 = FunctionUtil.convertTimestampToString(Long.parseLong(messageList.get(position).timeStamp));
				message_item_tv.setText(text1);
				message_item_time.setText(text2);
				return relativeLayout;
			}
			
			@Override
			public long getItemId(int arg0) {
				// TODO �Զ����ɵķ������
				return 0;
			}
			
			@Override
			public Object getItem(int arg0) {
				return null;
			}
			
			@Override
			public int getCount() {
				if(messageList != null) return messageList.size();
				else return 0;
			}
		});
	}
}
