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
	final private static int UPLOAD_SUCCESS = 1;//�ϴ��ɹ�
	final private static int UPLOAD_FAIL = 2;//�ϴ�ʧ��
	final private static int SHOW_DIALOG = 3;//��ʾ�Ի���
	final private static int CANCEL_DIALOG = 4;//ȡ���Ի���
	final private static int NETWORK_NO_ACCESS = 5;//���粻����
	
	private ImageView add_memorandum_back;//���˰�ť
	private TextView add_memorandum_finish;//��ɰ�ť
	private EditText memorandum_edit;//�����
	private LinearLayout memorandum_bg;//����
	private LinearLayout memorandum_timepicker_layout;//ѡ��ʱ�䲼��
	private TimePicker memorandum_timepicker;//TimePicker�ؼ�
	private TextView timepicker_no;//ȡ��
	private TextView timepicker_yes;//ȷ��
	
	private Animation showAnim;//��ʾʱ��ѡ�񲼾ֶ���
	private Animation hideAnim;//����ʱ��ѡ�񲼾ֶ���
	private boolean isShow = false;//���ʱ��ѡ�񲼾��Ƿ���ʾ
	
	private Animation showBgAnim;//��ʾ��������(����Ч��)
	private Animation hideBgAnim;//���ر�������
	
	private String username;//�û���
	private MyDate myDate;//��ǰѡ������
	String text;
	
	private Handler handler;//�������̴߳���������Ϣ
	private ProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//�ޱ�����
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
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
		isShow = false;//��ʾʱ��ѡ�񲼾�����
		memorandum_bg.setVisibility(View.INVISIBLE);
		//���ص���¼�
		add_memorandum_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
				onBackPressed();
				AddMemorandumActivity.this.finish();
			}
		});
		//��ɵ���¼�
		add_memorandum_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    text = memorandum_edit.getText().toString().trim();
				if("".equals(text))
				{
					Toast.makeText(AddMemorandumActivity.this, "����������", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//���ñ༭�򲻿���(��ʱ����̻��Զ�����)
					memorandum_edit.setEnabled(false);
					showAnim = AnimationUtils.loadAnimation(AddMemorandumActivity.this, R.anim.permission_setting_show_anim);
					showAnim.setFillAfter(true);
					//��ʼ����ʾ��������(����Ч��)
					showBgAnim = AnimationUtils.loadAnimation(AddMemorandumActivity.this, R.anim.permission_bg_show_anim);
					showBgAnim.setFillAfter(true);
					memorandum_timepicker_layout.startAnimation(showAnim);
					memorandum_bg.startAnimation(showBgAnim);
					isShow = true;//��ʾʱ��ѡ�񲼾���ʾ
					
					/*final MemorandumItem item = new MemorandumItem();
					//MyDate date = FunctionUtil.getDate();
					item.date = myDate;
					item.text = text;
					if(!NetWorkUtil.isAvailable(AddMemorandumActivity.this))
					{
						handler.sendEmptyMessage(NETWORK_NO_ACCESS);
						return;
					}
					//�������߳��ϴ�������
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
				//���粻����
				case NETWORK_NO_ACCESS:
					Toast.makeText(AddMemorandumActivity.this, "���粻����", Toast.LENGTH_SHORT).show();
					break;
				//�ϴ��ɹ�
				case UPLOAD_SUCCESS:
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					setResult(8, intent);
					AddMemorandumActivity.this.finish();
					break;
				//�ϴ�ʧ��
				case UPLOAD_FAIL:
					Toast.makeText(AddMemorandumActivity.this, "�ϴ�ʧ��", Toast.LENGTH_SHORT).show();
					break;
				//��ʾ���ȶԻ���
				case SHOW_DIALOG:
					pd = ProgressDialog.show(AddMemorandumActivity.this, "��ʾ", "�����ϴ�");
					break;
				//ȡ�����ȶԻ���
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
				}
			}
		};
		//ʱ��ѡ��ȷ������¼�
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
					//��ʼ����
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
						//�������߳��ϴ�������
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
		//ʱ��ѡ��ȡ������¼�
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
	private void hidePermissionLayout()
	{
		//����û�Ȩ�����ò�����ʾ����ô������
		if(isShow)
		{
			Log.i(tag,"start hide animation");
			isShow = false;
			//���ñ༭�����(�����Զ����������)
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
