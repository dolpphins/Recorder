package com.mao.memorandum;

import com.mao.myclass.MemorandumItem;
import com.mao.myclass.MyDate;
import com.mao.recorder.R;
import com.mao.recorder.RecordActivity;
import com.mao.util.FunctionUtil;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddMemorandumActivity extends Activity{
	private final static String tag = "AddMemorandumActivity";
	final private static int UPLOAD_SUCCESS = 1;//上传成功
	final private static int UPLOAD_FAIL = 2;//上传失败
	final private static int SHOW_DIALOG = 3;//显示对话框
	final private static int CANCEL_DIALOG = 4;//取消对话框
	final private static int NETWORK_NO_ACCESS = 5;//网络不可用
	
	private ImageView add_memorandum_back;//后退按钮
	private TextView add_memorandum_finish;//完成按钮
	private EditText memorandum_edit;//输入框
	private LinearLayout memorandum_bg;//背景
	private LinearLayout memorandum_timepicker_layout;//选择时间布局
	private TimePicker memorandum_timepicker;//TimePicker控件
	private TextView timepicker_no;//取消
	private TextView timepicker_yes;//确定
	
	private Animation showAnim;//显示时间选择布局动画
	private Animation hideAnim;//隐藏时间选择布局动画
	private boolean isShow = false;//标记时间选择布局是否显示
	
	private Animation showBgAnim;//显示背景动画(阴暗效果)
	private Animation hideBgAnim;//隐藏背景动画
	
	private String username;//用户名
	private MyDate myDate;//当前选中日期
	String text;
	
	private Handler handler;//处理子线程传过来的消息
	private ProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//无标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		this.setContentView(R.layout.add_memorandum_layout);
		
		add_memorandum_back = (ImageView) this.findViewById(R.id.add_memorandum_back);
		add_memorandum_finish = (TextView) this.findViewById(R.id.add_memorandum_finish);
		memorandum_edit = (EditText) this.findViewById(R.id.memorandum_edit);
		memorandum_bg = (LinearLayout) this.findViewById(R.id.memorandum_bg);
		memorandum_timepicker_layout = (LinearLayout) this.findViewById(R.id.memorandum_timepicker_layout);
		memorandum_timepicker = (TimePicker) this.findViewById(R.id.memorandum_timepicker);
		timepicker_no = (TextView) this.findViewById(R.id.timepicker_no);
		timepicker_yes = (TextView) this.findViewById(R.id.timepicker_yes);
		memorandum_timepicker_layout.setVisibility(View.INVISIBLE);
		isShow = false;//表示时间选择布局隐藏
		memorandum_bg.setVisibility(View.INVISIBLE);
		//返回点击事件
		add_memorandum_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//手动调用返回
				onBackPressed();
				AddMemorandumActivity.this.finish();
			}
		});
		//完成点击事件
		add_memorandum_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    text = memorandum_edit.getText().toString().trim();
				if("".equals(text))
				{
					Toast.makeText(AddMemorandumActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//设置编辑框不可用(此时软键盘会自动隐藏)
					memorandum_edit.setEnabled(false);
					showAnim = AnimationUtils.loadAnimation(AddMemorandumActivity.this, R.anim.permission_setting_show_anim);
					showAnim.setFillAfter(true);
					//初始化显示背景动画(阴暗效果)
					showBgAnim = AnimationUtils.loadAnimation(AddMemorandumActivity.this, R.anim.permission_bg_show_anim);
					showBgAnim.setFillAfter(true);
					memorandum_timepicker_layout.startAnimation(showAnim);
					memorandum_bg.startAnimation(showBgAnim);
					isShow = true;//表示时间选择布局显示
					
					/*final MemorandumItem item = new MemorandumItem();
					//MyDate date = FunctionUtil.getDate();
					item.date = myDate;
					item.text = text;
					if(!NetWorkUtil.isAvailable(AddMemorandumActivity.this))
					{
						handler.sendEmptyMessage(NETWORK_NO_ACCESS);
						return;
					}
					//开启子线程上传服务器
					new Thread(new Runnable(){
						@Override
						public void run()
						{
							System.out.println("username:"+username);
							handler.sendEmptyMessage(SHOW_DIALOG);
							Boolean isSuccess = Network.uploadOneMemorandum(username,item);
							handler.sendEmptyMessage(CANCEL_DIALOG);
							if(isSuccess)
							{
								Bundle bundle = new Bundle();
								bundle.putSerializable("memorandumitem", item);
								Message msg = new Message();
								msg.setData(bundle);
								msg.what = UPLOAD_SUCCESS;
								handler.sendMessage(msg);
							}
							else handler.sendEmptyMessage(UPLOAD_FAIL);
						}
					}).start();*/
				}
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//网络不可用
				case NETWORK_NO_ACCESS:
					Toast.makeText(AddMemorandumActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
					break;
				//上传成功
				case UPLOAD_SUCCESS:
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					setResult(8, intent);
					AddMemorandumActivity.this.finish();
					break;
				//上传失败
				case UPLOAD_FAIL:
					Toast.makeText(AddMemorandumActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
					break;
				//显示进度对话框
				case SHOW_DIALOG:
					pd = ProgressDialog.show(AddMemorandumActivity.this, "提示", "正在上传");
					break;
				//取消进度对话框
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
				}
			}
		};
		//时间选择确定点击事件
		timepicker_yes.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				Drawable drawable = null;
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					drawable = timepicker_yes.getBackground();
					timepicker_yes.setBackgroundColor(Color.parseColor("#efe4e4"));
					break;
				case MotionEvent.ACTION_UP:
					timepicker_yes.setBackground(drawable);
					//开始处理
					System.out.println("hour:"+memorandum_timepicker.getCurrentHour());
					System.out.println("minute:"+memorandum_timepicker.getCurrentMinute());
					final MemorandumItem item = new MemorandumItem();
					item.date = myDate;
					item.date.timestamp = System.currentTimeMillis();
					item.date.hour = memorandum_timepicker.getCurrentHour();
					item.date.minute = memorandum_timepicker.getCurrentMinute();
					item.date.second = 0;
					item.text = text;
					if(!NetWorkUtil.isAvailable(AddMemorandumActivity.this))
					{
						handler.sendEmptyMessage(NETWORK_NO_ACCESS);
					}
					else
					{
						//开启子线程上传服务器
						new Thread(new Runnable(){
							@Override
							public void run()
							{
								System.out.println("username:"+username);
								handler.sendEmptyMessage(SHOW_DIALOG);
								Boolean isSuccess = Network.uploadOneMemorandum(username,item);
								handler.sendEmptyMessage(CANCEL_DIALOG);
								if(isSuccess)
								{
									Bundle bundle = new Bundle();
									bundle.putSerializable("memorandumitem", item);
									Message msg = new Message();
									msg.setData(bundle);
									msg.what = UPLOAD_SUCCESS;
									handler.sendMessage(msg);
								}
								else handler.sendEmptyMessage(UPLOAD_FAIL);
							}
						}).start();
					}
					break;
				}
				return true;
			}
		});
		//时间选择取消点击事件
		timepicker_no.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				Drawable drawable = null;
				switch(action)
				{
				case MotionEvent.ACTION_DOWN:
					drawable = timepicker_no.getBackground();
					timepicker_no.setBackgroundColor(Color.parseColor("#efe4e4"));
					break;
				case MotionEvent.ACTION_UP:
					timepicker_no.setBackground(drawable);
					hidePermissionLayout();
					break;
				}
				return true;
			}
		});
	}
	@Override
	protected void onResume() {
		username = getIntent().getStringExtra("username");
		myDate = (MyDate) getIntent().getSerializableExtra("myDate");
		super.onResume();
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
	private void hidePermissionLayout()
	{
		//如果用户权限设置布局显示，那么隐藏它
		if(isShow)
		{
			Log.i(tag,"start hide animation");
			isShow = false;
			//设置编辑框可用(不会自动弹出软键盘)
			memorandum_edit.setEnabled(true);
			hideAnim = AnimationUtils.loadAnimation(this, R.anim.permission_setting_hide_anim);
			hideAnim.setFillAfter(true);
			hideBgAnim = AnimationUtils.loadAnimation(this, R.anim.permission_bg_hide_anim);
			hideBgAnim.setFillAfter(true);
			memorandum_timepicker_layout.startAnimation(hideAnim);
			memorandum_bg.startAnimation(hideBgAnim);
		}
	}
}
