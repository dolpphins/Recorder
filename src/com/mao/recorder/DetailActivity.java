package com.mao.recorder;

import com.mao.myclass.InformationItem;
import com.mao.util.FunctionUtil;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity{
	private final static String tag = "DetailActivity";
	final private static int DELETE_SUCCESS = 1;//删除成功
	final private static int DELETE_FAIL = 2;//删除失败
	final private static int SHOW_DIALOG = 3;//显示对话框
	final private static int CANCEL_DIALOG = 4;//取消对话框
	final private static int NETWORK_NO_ACCESS = 5;//网络不可用
	
	private ImageView detail_item_picture;//头像
	private TextView detail_item_username;//用户名
	private TextView detail_item_publishtime;//发布时间
	private TextView detail_item_fromdevice;//来自设备
	private TextView detail_item_text;//正文
	private TextView detail_item_delete;//删除
	private ImageView detail_item_back;//返回
	
	private InformationItem informationItem;
	private int position = -1;
	
	AlertDialog.Builder builder;
    private Handler handler;//处理子线程传过来的消息
    private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		setContentView(R.layout.detail_layout);
		
		detail_item_picture = (ImageView) this.findViewById(R.id.detail_item_picture);
		detail_item_username = (TextView) this.findViewById(R.id.detail_item_username);
		detail_item_publishtime = (TextView) this.findViewById(R.id.detail_item_publishtime);
		detail_item_fromdevice = (TextView) this.findViewById(R.id.detail_item_fromdevice);
		detail_item_text = (TextView) this.findViewById(R.id.detail_item_text);
		detail_item_delete = (TextView) this.findViewById(R.id.detail_item_delete);
		detail_item_back = (ImageView) this.findViewById(R.id.detail_item_back);
		//删除点击事件
		detail_item_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				builder = new AlertDialog.Builder(DetailActivity.this);
				builder.setMessage("真的要删除吗?");
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(tag,"cancel delete");
					}
				});
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(tag,"delete");
						if(!NetWorkUtil.isAvailable(DetailActivity.this))
						{
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						handler.sendEmptyMessage(SHOW_DIALOG);
						final String name = informationItem.username;
						final String timestamp = informationItem.publishTime;
						final String permission = informationItem.permission+"";
						Log.i(tag,"name:"+name);
						Log.i(tag,"timestamp:"+timestamp);
						Log.i(tag,"permission:"+permission);
						//开启子线程从服务器删除该记录
						new Thread(new Runnable(){
							@Override
							public void run()
							{
								Boolean isSuccess = Network.deleteOneRecord(name, timestamp,permission);
								handler.sendEmptyMessage(CANCEL_DIALOG);
								if(isSuccess)
								{
									Bundle data = new Bundle();
									data.putInt("position", position);
									Message msg = new Message();
									msg.setData(data);
									msg.what = DELETE_SUCCESS;
									handler.sendMessage(msg);
								}
								else handler.sendEmptyMessage(DELETE_FAIL);
							}
						}).start();
					}
				});
				builder.show();
			}
		});
		//返回图标点击事件
		detail_item_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//手动调用返回
				onBackPressed();
				DetailActivity.this.setResult(7);
				DetailActivity.this.finish();
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//删除成功
				case DELETE_SUCCESS:
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					DetailActivity.this.setResult(6, intent);
					DetailActivity.this.finish();
					break;
				//删除失败
				case DELETE_FAIL:
					Toast.makeText(DetailActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
					break;
				//显示进度对话框
				case SHOW_DIALOG:
					pd = ProgressDialog.show(DetailActivity.this, "提示", "正在删除");
					break;
				//取消进度对话框
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
				//网络不可用
				case NETWORK_NO_ACCESS:
					Toast.makeText(DetailActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	@Override
	protected void onResume() {
		informationItem = (InformationItem) getIntent().getSerializableExtra("informationitem");
		position = getIntent().getIntExtra("position", -1);
		//设置头像
		if(!informationItem.picturePath.equals("#")) {detail_item_picture.setImageBitmap(FunctionUtil.getBitmapByPath(informationItem.picturePath));}
		//设置用户名
		detail_item_username.setText(informationItem.username);
		//设置发布时间
		detail_item_publishtime.setText(FunctionUtil.convertTimestampToString(Long.parseLong(informationItem.publishTime)));
		//设置来自哪个设备
		detail_item_fromdevice.setText("来自"+informationItem.fromDevice);
		//设置正文
		detail_item_text.setText(informationItem.text);
		Log.i(tag,position+"");
		super.onResume();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//捕获返回键
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			DetailActivity.this.setResult(7);
			DetailActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
