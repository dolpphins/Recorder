package com.mao.dynamic;

import com.mao.myclass.InformationItem;
import com.mao.recorder.DetailActivity;
import com.mao.recorder.R;
import com.mao.util.FunctionUtil;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DynamicDetailActivity extends Activity{
	private final static String tag = "DynamicDetailActivity";
	private final static int FOUSE_SUCCESS = 1;//关注成功
	private final static int FOUSE_FAIL = 2;//关注失败
	
	private ImageView dynamic_detail_item_picture;//头像
	private TextView dynamic_detail_item_username;//用户名
	private TextView dynamic_detail_item_publishtime;//发布时间
	private TextView dynamic_detail_item_fromdevice;//来自设备
	private TextView dynamic_detail_item_text;//正文
	private TextView dynamic_detail_item_fouse;//关注
	private ImageView dynamic_detail_item_back;//返回
	
	private InformationItem informationItem;
	private boolean hasFouse = false;
	private String timestamp = "";
	private String username;
	private boolean isOwn = false;
	
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		setContentView(R.layout.dynamic_detail_layout);
		
		dynamic_detail_item_picture = (ImageView) this.findViewById(R.id.dynamic_detail_item_picture);
		dynamic_detail_item_username = (TextView) this.findViewById(R.id.dynamic_detail_item_username);
		dynamic_detail_item_publishtime = (TextView) this.findViewById(R.id.dynamic_detail_item_publishtime);
		dynamic_detail_item_fromdevice = (TextView) this.findViewById(R.id.dynamic_detail_item_fromdevice);
		dynamic_detail_item_text = (TextView) this.findViewById(R.id.dynamic_detail_item_text);
		dynamic_detail_item_fouse = (TextView) this.findViewById(R.id.dynamic_detail_item_fouse);
		dynamic_detail_item_back = (ImageView) this.findViewById(R.id.dynamic_detail_item_back);
		//返回图标点击事件
		dynamic_detail_item_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//手动调用返回
				Intent intent = new Intent();
				intent.putExtra("hasFouse", hasFouse);
				intent.putExtra("othername", informationItem.username);
				intent.putExtra("timestamp", timestamp);
				onBackPressed();
				DynamicDetailActivity.this.setResult(12,intent);
				DynamicDetailActivity.this.finish();
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
			switch(msg.what)
			{
			//关注成功
			case FOUSE_SUCCESS:
				hasFouse = true;
				timestamp = System.currentTimeMillis()+"";
				dynamic_detail_item_fouse.setText(getResources().getString(R.string.alreadyfouse));
				dynamic_detail_item_fouse.setEnabled(false);
				Toast.makeText(DynamicDetailActivity.this, "已关注", Toast.LENGTH_SHORT).show();
				break;
			//关注失败
			case FOUSE_FAIL:
				Toast.makeText(DynamicDetailActivity.this, "关注失败", Toast.LENGTH_SHORT).show();
				break;
			}
			}
		};
	}
	@Override
	protected void onResume() {
		informationItem = (InformationItem) getIntent().getSerializableExtra("dynamicinformationitem");
		hasFouse = getIntent().getBooleanExtra("hasFouse", false);
		username = getIntent().getStringExtra("myname");
		isOwn = getIntent().getBooleanExtra("isOwn", false);
		//设置头像
		if(!informationItem.picturePath.equals("#")) {dynamic_detail_item_picture.setImageBitmap(FunctionUtil.getBitmapByPath(informationItem.picturePath));}
		//设置用户名
		dynamic_detail_item_username.setText(informationItem.username);
		//设置发布时间
		dynamic_detail_item_publishtime.setText(FunctionUtil.convertTimestampToString(Long.parseLong(informationItem.publishTime)));
		//设置来自哪个设备
		dynamic_detail_item_fromdevice.setText("来自"+informationItem.fromDevice);
		//设置正文
		dynamic_detail_item_text.setText(informationItem.text);
		if(isOwn)
		{
			dynamic_detail_item_fouse.setVisibility(View.GONE);
		}
		//设置是否已关注
		else if(hasFouse)
		{
			dynamic_detail_item_fouse.setText(getResources().getString(R.string.alreadyfouse));
			dynamic_detail_item_fouse.setEnabled(false);
		}
		else
		{
			//关注按钮设置点击事件
			dynamic_detail_item_fouse.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch(event.getAction())
					{
					case MotionEvent.ACTION_DOWN:
						dynamic_detail_item_fouse.setBackground(getResources().getDrawable(R.drawable.fouse_button_bg_sel));
						break;
					case MotionEvent.ACTION_UP:
						fouseOtherPeople(informationItem.username);
						break;
					}
					return true;
				}
			});
		}
		super.onResume();
	}
	/**
	 * 点击关注时开启子线程写入服务器数据库
	 * */
	private void fouseOtherPeople(final String othername)
	{
		if(!NetWorkUtil.isAvailable(DynamicDetailActivity.this))
		{
			Toast.makeText(DynamicDetailActivity.this, "网络可不用", Toast.LENGTH_SHORT).show();
		}
		else
		{
			new Thread(new Runnable(){
				@Override
				public void run()
				{
					boolean isSuccess = Network.fouseOther(username,othername, System.currentTimeMillis()+"");
					if(isSuccess) 
					{
						handler.sendEmptyMessage(FOUSE_SUCCESS);
					}
					else handler.sendEmptyMessage(FOUSE_FAIL);
				}
			}).start();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent intent = new Intent();
			intent.putExtra("hasFouse", hasFouse);
			intent.putExtra("othername", informationItem.username);
			intent.putExtra("timestamp", timestamp);
			DynamicDetailActivity.this.setResult(12,intent);
			DynamicDetailActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
