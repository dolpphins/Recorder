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
	
	private final int GET_SUCCESS = 1;//获取成功
	private final int GET_FAIL = 2;//获取失败
	private final int SHOW_PD = 3;//显示进度对话框
	private final int HIDE_PD = 4;//隐藏进度对话框
	private String username;//当前用户名
	private ArrayList<Fouse> messageList = null;//消息列表
	private ListView message_lv;
	private Handler handler;
	private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		this.setContentView(R.layout.message_layout);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//获取消息列表成功
				case GET_SUCCESS:
					initListView();
					break;
				//获取消息列表失败
				case GET_FAIL:
					Toast.makeText(MessageActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
					break;
				//显示进度对话框
				case SHOW_PD:
					pd = ProgressDialog.show(MessageActivity.this, "提示", "正在获取数据");
					break;
				//隐藏进度对话框
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
	 * 开启子线程获取消息
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
	 * 初始化listview
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
				String text1 = messageList.get(position).username+"关注了"+messageList.get(position).othername;
				String text2 = FunctionUtil.convertTimestampToString(Long.parseLong(messageList.get(position).timeStamp));
				message_item_tv.setText(text1);
				message_item_time.setText(text2);
				return relativeLayout;
			}
			
			@Override
			public long getItemId(int arg0) {
				// TODO 自动生成的方法存根
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
