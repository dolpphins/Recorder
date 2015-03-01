package com.mao.slidingmenu;

import com.mao.recorder.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class SettingActivity extends Activity {
	private final static String tag = "SettingActivity";
	private ImageView slidingmenu_setting_back;//���˰�ť
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		this.setContentView(R.layout.slidingmenu_setting_layout);
		slidingmenu_setting_back = (ImageView) this.findViewById(R.id.slidingmenu_setting_back);
		slidingmenu_setting_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
				onBackPressed();
			}
		});
	}
}
