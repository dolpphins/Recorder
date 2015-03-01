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
	private final static int FOUSE_SUCCESS = 1;//��ע�ɹ�
	private final static int FOUSE_FAIL = 2;//��עʧ��
	
	private ImageView dynamic_detail_item_picture;//ͷ��
	private TextView dynamic_detail_item_username;//�û���
	private TextView dynamic_detail_item_publishtime;//����ʱ��
	private TextView dynamic_detail_item_fromdevice;//�����豸
	private TextView dynamic_detail_item_text;//����
	private TextView dynamic_detail_item_fouse;//��ע
	private ImageView dynamic_detail_item_back;//����
	
	private InformationItem informationItem;
	private boolean hasFouse = false;
	private String timestamp = "";
	private String username;
	private boolean isOwn = false;
	
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		setContentView(R.layout.dynamic_detail_layout);
		
		dynamic_detail_item_picture = (ImageView) this.findViewById(R.id.dynamic_detail_item_picture);
		dynamic_detail_item_username = (TextView) this.findViewById(R.id.dynamic_detail_item_username);
		dynamic_detail_item_publishtime = (TextView) this.findViewById(R.id.dynamic_detail_item_publishtime);
		dynamic_detail_item_fromdevice = (TextView) this.findViewById(R.id.dynamic_detail_item_fromdevice);
		dynamic_detail_item_text = (TextView) this.findViewById(R.id.dynamic_detail_item_text);
		dynamic_detail_item_fouse = (TextView) this.findViewById(R.id.dynamic_detail_item_fouse);
		dynamic_detail_item_back = (ImageView) this.findViewById(R.id.dynamic_detail_item_back);
		//����ͼ�����¼�
		dynamic_detail_item_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
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
			//��ע�ɹ�
			case FOUSE_SUCCESS:
				hasFouse = true;
				timestamp = System.currentTimeMillis()+"";
				dynamic_detail_item_fouse.setText(getResources().getString(R.string.alreadyfouse));
				dynamic_detail_item_fouse.setEnabled(false);
				Toast.makeText(DynamicDetailActivity.this, "�ѹ�ע", Toast.LENGTH_SHORT).show();
				break;
			//��עʧ��
			case FOUSE_FAIL:
				Toast.makeText(DynamicDetailActivity.this, "��עʧ��", Toast.LENGTH_SHORT).show();
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
		//����ͷ��
		if(!informationItem.picturePath.equals("#")) {dynamic_detail_item_picture.setImageBitmap(FunctionUtil.getBitmapByPath(informationItem.picturePath));}
		//�����û���
		dynamic_detail_item_username.setText(informationItem.username);
		//���÷���ʱ��
		dynamic_detail_item_publishtime.setText(FunctionUtil.convertTimestampToString(Long.parseLong(informationItem.publishTime)));
		//���������ĸ��豸
		dynamic_detail_item_fromdevice.setText("����"+informationItem.fromDevice);
		//��������
		dynamic_detail_item_text.setText(informationItem.text);
		if(isOwn)
		{
			dynamic_detail_item_fouse.setVisibility(View.GONE);
		}
		//�����Ƿ��ѹ�ע
		else if(hasFouse)
		{
			dynamic_detail_item_fouse.setText(getResources().getString(R.string.alreadyfouse));
			dynamic_detail_item_fouse.setEnabled(false);
		}
		else
		{
			//��ע��ť���õ���¼�
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
	 * �����עʱ�������߳�д����������ݿ�
	 * */
	private void fouseOtherPeople(final String othername)
	{
		if(!NetWorkUtil.isAvailable(DynamicDetailActivity.this))
		{
			Toast.makeText(DynamicDetailActivity.this, "����ɲ���", Toast.LENGTH_SHORT).show();
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
