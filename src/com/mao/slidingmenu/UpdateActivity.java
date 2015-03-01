package com.mao.slidingmenu;

import com.mao.recorder.R;
import com.mao.util.FunctionUtil;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateActivity extends Activity {
	private final static String tag = "UpdateActivity";
	private final static int GET_SUCCESS = 1;//��ȡ�ɹ�
	private final static int GET_FAIL = 2;//��ȡʧ��
	private ImageView slidingmenu_update_back;//���˰�ť
	private TextView update_version_tv;
	private String version;//��ǰ�汾��
	private String latestVersion;//���°汾��
	private String updateTip = "�������";
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		this.setContentView(R.layout.slidingmenu_update_layout);
		slidingmenu_update_back = (ImageView) this.findViewById(R.id.slidingmenu_update_back);
		slidingmenu_update_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
				onBackPressed();
			}
		});
		update_version_tv = (TextView) this.findViewById(R.id.update_version_tv);
		version = FunctionUtil.getVersion(this);
		update_version_tv.setText(version);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//��ȡ���ݳɹ�
				case GET_SUCCESS:
					System.out.println("currentVersion:"+version);
					System.out.println("latestVersion:"+latestVersion);
					if(latestVersion.equals(version)) Toast.makeText(UpdateActivity.this, "�Ѿ������°汾", Toast.LENGTH_SHORT).show();
					else
					{
						Toast.makeText(UpdateActivity.this, "��鵽�°汾", Toast.LENGTH_SHORT).show();
						update_version_tv.setText(updateTip);//��ʾ�������
					}
					break;
				//��ȡ����ʧ��
				case GET_FAIL:
					Toast.makeText(UpdateActivity.this, "��ȡ����ʧ��", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
		update_version_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = update_version_tv.getText().toString();
				//���а汾����
				if(updateTip.equals(text))
				{
					
				}
			}
		});
		if(!NetWorkUtil.isAvailable(this)) Toast.makeText(this, "���粻����", Toast.LENGTH_SHORT).show();
		else checkUpdate();
	}
	/**
	 * ������
	 * */
	private void checkUpdate()
	{
		new Thread(new Runnable() {	
			@Override
			public void run() {
				System.out.println("checkUpdate");
				latestVersion = Network.checkUpdate();
				if(latestVersion==null) handler.sendEmptyMessage(GET_FAIL);
				else handler.sendEmptyMessage(GET_SUCCESS);
			}
		}).start();
	}
}
