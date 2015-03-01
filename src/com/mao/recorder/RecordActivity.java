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
	public final static int PERMISSION_PERSONALLY = 1;//���Լ��ɼ�
	public final static int PERMISSION_FRIENDLLY = 2;//���ѿɼ�
	public final static int PERMISSION_COMMON = 3;//�����˿ɼ�
	final private String sharedPreferencesName = "account.txt";
	final private static int UPLOAD_SUCCESS = 4;//�ϴ��ɹ�
	final private static int UPLOAD_FAIL = 5;//�ϴ�ʧ��
	final private static int SHOW_DIALOG = 6;//��ʾ�Ի���
	final private static int CANCEL_DIALOG = 7;//ȡ���Ի���
	
	private ImageView write_one_write_back;
	private EditText text_edit;
	private TextView text_save;
	
	private LinearLayout permission_bg;//Ȩ�����ø��ڵ�
	private LinearLayout permission_setting;//Ȩ�����ò���
	private TextView permission_personally;//���Լ��ɼ�
	private TextView permission_friendly;//���ѿɼ�
	private TextView permission_common;//�����˿ɼ�
	
	private Animation showAnim;//��ʾȨ�����ò��ֶ���
	private Animation hideAnim;//����Ȩ�����ò��ֶ���
	private boolean isShow = false;//���Ȩ�����ò����Ƿ���ʾ
	
	private Animation showBgAnim;//��ʾ��������(����Ч��)
	private Animation hideBgAnim;//���ر�������
	
	private String text;//����༭����ı�
	
	private Handler handler;//�������̴߳���������Ϣ
	private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
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
		isShow = false;//��ʾȨ�����ò�������
		permission_bg.setVisibility(View.INVISIBLE);
		write_one_write_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
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
					if("".equals(text)) Toast.makeText(RecordActivity.this, "����������", Toast.LENGTH_SHORT).show();
					else
					{
						if(!isShow)
						{
							Log.i(tag,"start show animation");
							//���������
							//text_edit.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
							//���ñ༭�򲻿���(��ʱ����̻��Զ�����)
							text_edit.setEnabled(false);
							//InputMethodManager imm = (InputMethodManager) RecordActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
							//imm.hideSoftInputFromInputMethod(text_edit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
							//��ʼ����ʾȨ�޲��ֶ���
							showAnim = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.permission_setting_show_anim);
							showAnim.setFillAfter(true);
							//��ʼ����ʾ��������(����Ч��)
							showBgAnim = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.permission_bg_show_anim);
							showBgAnim.setFillAfter(true);
							
							permission_setting.startAnimation(showAnim);
							permission_bg.startAnimation(showBgAnim);
							
							isShow = true;//��ʾȨ�����ò�����ʾ
						}
					}
					break;
				}
				return true;
			}
		});
		//���Լ��ɼ�
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
		//���ѿɼ�
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
		//�����˿ɼ�
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
				//�ϴ��ɹ�
				case UPLOAD_SUCCESS:
					Bundle bundle = msg.getData();
				    Intent intent = new Intent();
				    intent.putExtras(bundle);
					RecordActivity.this.setResult(5, intent);
					RecordActivity.this.finish();
					break;
				//�ϴ�ʧ��
				case UPLOAD_FAIL:
					Toast.makeText(RecordActivity.this, "�ϴ�ʧ��", Toast.LENGTH_SHORT).show();
					break;
				//��ʾ�Ի���
				case SHOW_DIALOG:
					pd = ProgressDialog.show(RecordActivity.this, "��ʾ", "�����ϴ�");
					break;
				//ȡ���Ի���
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
				}
			}
		};
		
	}
	//���񷵻ؼ�
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
	 * ���ص�MainActivity
	 * */
	private void returnMainActivity(final Intent intent)
	{
		intent.putExtra("text", text);//�ı�
		intent.putExtra("publishTime", System.currentTimeMillis()+"");//����ʱ��
		intent.putExtra("fromDevice", new Build().MODEL);//�豸�ͺ�
		//���ز���
		hidePermissionLayout();
		if(!NetWorkUtil.isAvailable(this)) Toast.makeText(this, "���粻����", Toast.LENGTH_SHORT).show();
		else
		{
			//�������߳̽�����ͬ������������
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
					handler.sendEmptyMessage(SHOW_DIALOG);//��ʾ���ȶԻ���
					boolean isSuccess = Network.uploadOneRecord(item);
					handler.sendEmptyMessage(CANCEL_DIALOG);//ȡ�����ȶԻ���
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
	 * ����Ȩ�����ò���
	 * */
	private void hidePermissionLayout()
	{
		//����û�Ȩ�����ò�����ʾ����ô������
		if(isShow)
		{
			Log.i(tag,"start hide animation");
			isShow = false;
			//���ñ༭�����(�����Զ����������)
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
