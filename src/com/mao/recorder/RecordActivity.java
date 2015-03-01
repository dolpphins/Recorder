package com.mao.recorder;

import com.mao.memorandum.AddMemorandumActivity;
import com.mao.myclass.Account;
import com.mao.myclass.InformationItem;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity{
	private final static String tag = "RecordActivity";
	public final static int PERMISSION_PERSONALLY = 1;//仅自己可见
	public final static int PERMISSION_FRIENDLLY = 2;//好友可见
	public final static int PERMISSION_COMMON = 3;//所有人可见
	final private String sharedPreferencesName = "account.txt";
	final private static int UPLOAD_SUCCESS = 4;//上传成功
	final private static int UPLOAD_FAIL = 5;//上传失败
	final private static int SHOW_DIALOG = 6;//显示对话框
	final private static int CANCEL_DIALOG = 7;//取消对话框
	
	private ImageView write_one_write_back;
	private EditText text_edit;
	private TextView text_save;
	
	private LinearLayout permission_bg;//权限设置父节点
	private LinearLayout permission_setting;//权限设置布局
	private TextView permission_personally;//仅自己可见
	private TextView permission_friendly;//好友可见
	private TextView permission_common;//所有人可见
	
	private Animation showAnim;//显示权限设置布局动画
	private Animation hideAnim;//隐藏权限设置布局动画
	private boolean isShow = false;//标记权限设置布局是否显示
	
	private Animation showBgAnim;//显示背景动画(阴暗效果)
	private Animation hideBgAnim;//隐藏背景动画
	
	private String text;//保存编辑框的文本
	
	private Handler handler;//处理子线程传过来的消息
	private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		this.setContentView(R.layout.write_one_write_layout);
	
		write_one_write_back = (ImageView) this.findViewById(R.id.write_one_write_back);
		text_edit = (EditText) this.findViewById(R.id.text_edit);
		text_save = (TextView) this.findViewById(R.id.text_save);
		permission_bg = (LinearLayout) this.findViewById(R.id.permission_bg);
		permission_setting = (LinearLayout) this.findViewById(R.id.permission_setting);
		permission_personally = (TextView) this.findViewById(R.id.permission_personally);
		permission_friendly = (TextView) this.findViewById(R.id.permission_friendly);
		permission_common = (TextView) this.findViewById(R.id.permission_common);
		permission_setting.setVisibility(View.INVISIBLE);
		isShow = false;//表示权限设置布局隐藏
		permission_bg.setVisibility(View.INVISIBLE);
		write_one_write_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//手动调用返回
				onBackPressed();
				RecordActivity.this.finish();
			}
		});
		text_save.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_UP:
					text = text_edit.getText().toString().trim();
					if("".equals(text)) Toast.makeText(RecordActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
					else
					{
						if(!isShow)
						{
							Log.i(tag,"start show animation");
							//隐藏软键盘
							//text_edit.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
							//设置编辑框不可用(此时软键盘会自动隐藏)
							text_edit.setEnabled(false);
							//InputMethodManager imm = (InputMethodManager) RecordActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
							//imm.hideSoftInputFromInputMethod(text_edit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
							//初始化显示权限布局动画
							showAnim = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.permission_setting_show_anim);
							showAnim.setFillAfter(true);
							//初始化显示背景动画(阴暗效果)
							showBgAnim = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.permission_bg_show_anim);
							showBgAnim.setFillAfter(true);
							
							permission_setting.startAnimation(showAnim);
							permission_bg.startAnimation(showBgAnim);
							
							isShow = true;//表示权限设置布局显示
						}
					}
					break;
				}
				return true;
			}
		});
		//仅自己可见
		permission_personally.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				Drawable drawable = null;
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					drawable = permission_personally.getBackground();
					permission_personally.setBackgroundColor(Color.parseColor("#efe4e4"));
					break;
				case MotionEvent.ACTION_UP:
					Log.i(tag,"permission:personally");
					//permission_personally.setBackgroundColor(Color.parseColor("#ffffff"));
					permission_personally.setBackground(drawable);
					Intent intent = new Intent();
					intent.putExtra("permission", PERMISSION_PERSONALLY);
					returnMainActivity(intent);
					break;
				}
				return true;
			}
		});
		//好友可见
		permission_friendly.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				Drawable drawable = null;
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					drawable = permission_friendly.getBackground();
					permission_friendly.setBackgroundColor(Color.parseColor("#efe4e4"));
					break;
				case MotionEvent.ACTION_UP:
					Log.i(tag,"permission:friendly");
					//permission_friendly.setBackgroundColor(Color.parseColor("#ffffff"));
					permission_friendly.setBackground(drawable);
					Intent intent = new Intent();
					intent.putExtra("permission", PERMISSION_FRIENDLLY);
					returnMainActivity(intent);
					break;
				}
				return true;
			}
		});
		//所有人可见
		permission_common.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				Drawable drawable = null;
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					drawable = permission_common.getBackground();
					permission_common.setBackgroundColor(Color.parseColor("#efe4e4"));
					break;
				case MotionEvent.ACTION_UP:
					Log.i(tag,"permission:common");
					//permission_common.setBackgroundColor(Color.parseColor("#ffffff"));
					permission_common.setBackground(drawable);
					Intent intent = new Intent();
					intent.putExtra("permission", PERMISSION_COMMON);
					returnMainActivity(intent);
					break;
				}
				return true;
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//上传成功
				case UPLOAD_SUCCESS:
					Bundle bundle = msg.getData();
				    Intent intent = new Intent();
				    intent.putExtras(bundle);
					RecordActivity.this.setResult(5, intent);
					RecordActivity.this.finish();
					break;
				//上传失败
				case UPLOAD_FAIL:
					Toast.makeText(RecordActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
					break;
				//显示对话框
				case SHOW_DIALOG:
					pd = ProgressDialog.show(RecordActivity.this, "提示", "正在上传");
					break;
				//取消对话框
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
				}
			}
		};
		
	}
	//捕获返回键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK && isShow)
		{
			hidePermissionLayout();
			isShow = false;
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * 返回到MainActivity
	 * */
	private void returnMainActivity(final Intent intent)
	{
		intent.putExtra("text", text);//文本
		intent.putExtra("publishTime", System.currentTimeMillis()+"");//发布时间
		intent.putExtra("fromDevice", new Build().MODEL);//设备型号
		//隐藏布局
		hidePermissionLayout();
		if(!NetWorkUtil.isAvailable(this)) Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
		else
		{
			//开启子线程将数据同步到服务器上
			new Thread(new Runnable(){
				@Override
				public void run() {
					InformationItem item = new InformationItem();
					SharedPreferences sp = RecordActivity.this.getSharedPreferences(sharedPreferencesName,Context.MODE_PRIVATE);
					item.picturePath = sp.getString("picturepath", "");
					item.username = sp.getString("username", "");
					item.publishTime = intent.getStringExtra("publishTime");
					item.fromDevice = intent.getStringExtra("fromDevice");
					item.text = intent.getStringExtra("text");
					item.permission = intent.getIntExtra("permission", -1);
					handler.sendEmptyMessage(SHOW_DIALOG);//显示进度对话框
					boolean isSuccess = Network.uploadOneRecord(item);
					handler.sendEmptyMessage(CANCEL_DIALOG);//取消进度对话框
					if(isSuccess) 
					{
						Message msg = new Message();
						msg.what = UPLOAD_SUCCESS;
						msg.setData(intent.getExtras());
						handler.sendMessage(msg);
					}
					else handler.sendEmptyMessage(UPLOAD_FAIL);
				}
			}).start();
		}
	}
	/**
	 * 隐藏权限设置布局
	 * */
	private void hidePermissionLayout()
	{
		//如果用户权限设置布局显示，那么隐藏它
		if(isShow)
		{
			Log.i(tag,"start hide animation");
			isShow = false;
			//设置编辑框可用(不会自动弹出软键盘)
			text_edit.setEnabled(true);
			hideAnim = AnimationUtils.loadAnimation(this, R.anim.permission_setting_hide_anim);
			hideAnim.setFillAfter(true);
			hideBgAnim = AnimationUtils.loadAnimation(this, R.anim.permission_bg_hide_anim);
			hideBgAnim.setFillAfter(true);
			permission_setting.startAnimation(hideAnim);
			permission_bg.startAnimation(hideBgAnim);
		}
	}
}
