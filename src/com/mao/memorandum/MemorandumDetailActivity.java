package com.mao.memorandum;

import java.util.ArrayList;

import com.mao.myclass.MemorandumItem;
import com.mao.myclass.MyDate;
import com.mao.recorder.DetailActivity;
import com.mao.recorder.R;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemorandumDetailActivity extends Activity{
	private final static String tag = "MemorandumDetailActivity";
	final private static int DELETE_SUCCESS = 1;//删除成功
	final private static int DELETE_FAIL = 2;//删除失败
	final private static int SHOW_DIALOG = 3;//显示对话框
	final private static int CANCEL_DIALOG = 4;//取消对话框
	final private static int NETWORK_NO_ACCESS = 5;//网络不可用
	
	private ImageView check_memorandum_back;//返回
	private TextView check_memorandum_finish;//添加备忘
	
	private ArrayList<MemorandumItem> memorandumList;
	private boolean hasRevise = false;//标记是否修改过
	private ListView listView;//用于显示所有备忘
	
	private LinearLayout check_memorandum;
	private String username;//用户名
	private MyDate myDate;//当前选中日期
	
	private AlertDialog.Builder builder;
	private Handler handler;//处理子线程传过来的消息
    private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		this.setContentView(R.layout.check_memorandum_layout);
		check_memorandum = (LinearLayout) this.findViewById(R.id.check_memorandum);
		check_memorandum_back = (ImageView) this.findViewById(R.id.check_memorandum_back);
		check_memorandum_finish = (TextView) this.findViewById(R.id.check_memorandum_finish);
		check_memorandum_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("hasRevise", hasRevise);
				data.putExtra("memorandumlist", memorandumList);
				setResult(11, data);
				//手动调用返回
				onBackPressed();
				MemorandumDetailActivity.this.finish();
			}
		});
		//添加备忘点击事件(跳转到添加备忘界面)
		check_memorandum_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MemorandumDetailActivity.this,AddMemorandumActivity.class);
				intent.putExtra("username", username);
				intent.putExtra("myDate", myDate);
				MemorandumDetailActivity.this.startActivityForResult(intent, 105);
			}
		});
		memorandumList = (ArrayList<MemorandumItem>) getIntent().getSerializableExtra("memorandumlist");
		if(memorandumList!=null) initListView();//初始化ListView
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//删除成功
				case DELETE_SUCCESS:
					initListView();
					break;
				//删除失败
				case DELETE_FAIL:
					Toast.makeText(MemorandumDetailActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
					break;
				//显示进度对话框
				case SHOW_DIALOG:
					pd = ProgressDialog.show(MemorandumDetailActivity.this, "提示", "正在删除");
					break;
				//取消进度对话框
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
			    //网络不可用
				case NETWORK_NO_ACCESS:
					Toast.makeText(MemorandumDetailActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	@Override
	protected void onResume() {
		username = this.getIntent().getStringExtra("username");
		myDate = (MyDate) getIntent().getSerializableExtra("myDate");
		super.onResume();
	}
	//捕获返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent data = new Intent();
			data.putExtra("hasRevise", hasRevise);
			data.putExtra("memorandumlist", memorandumList);
			setResult(11, data);
			MemorandumDetailActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * 初始化ListView
	 * */
	private void initListView()
	{
		if(memorandumList==null)
		{
			Log.i(tag,"memorandumList is null");
			return;
		}
		if(check_memorandum==null)
		{
			Log.i(tag,"content is null");
			return;
		}
		listView = new ListView(this);
		listView.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.memorandum_item_layout, null);
				TextView detail_memorandum_time = (TextView) linearLayout.findViewById(R.id.detail_memorandum_time);
				TextView detail_memorandum_text = (TextView) linearLayout.findViewById(R.id.detail_memorandum_text);
				detail_memorandum_time.setText(memorandumList.get(position).date.toString());
				detail_memorandum_text.setText(memorandumList.get(position).text);
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
				return memorandumList.size();
			}
		});
		//实现长按删除
		setLongClickEvent();
		//移除掉原先的View
		int count = check_memorandum.getChildCount();
		if(count>1) check_memorandum.removeViews(1, count-1);
		check_memorandum.addView(listView);
	}
	/**
	 * 为ListView设置事件响应
	 * */
	private void setLongClickEvent()
	{
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//设置背景
				final View v = view;
				v.setBackground(getResources().getDrawable(R.drawable.memorandum_item_bg_sel));
				final int t = position;
				builder = new AlertDialog.Builder(MemorandumDetailActivity.this);
				builder.setMessage("真的要删除吗?");
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						v.setBackground(getResources().getDrawable(R.drawable.memorandum_item_bg_nor));
					}
				});
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(tag,"position:"+t);
						if(!NetWorkUtil.isAvailable(MemorandumDetailActivity.this))
						{
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						new Thread(new Runnable(){
							@Override
							public void run()
							{
								String timestamp = ""+memorandumList.get(t).date.timestamp;
								handler.sendEmptyMessage(SHOW_DIALOG);
								boolean isSuccess = Network.deleteOneMemorandum(username, timestamp);
								handler.sendEmptyMessage(CANCEL_DIALOG);
								if(isSuccess)
								{
									memorandumList.remove(t);
									hasRevise = true;//表示已修改
									handler.sendEmptyMessage(DELETE_SUCCESS);
								}
								else handler.sendEmptyMessage(DELETE_FAIL);
							}
						}).start();
					}
				});
				builder.show();
				return true;
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//从添加备忘返回
		if(requestCode==105 && resultCode == 8 && data!=null)
		{
			hasRevise = true;//表示已修改
			if(memorandumList == null) memorandumList = new ArrayList<MemorandumItem>();
			memorandumList.add((MemorandumItem)data.getSerializableExtra("memorandumitem"));
			initListView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
