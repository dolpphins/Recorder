package com.mao.slidingmenu;

import com.mao.recorder.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class HelpActivity extends Activity {
	private final static String tag = "HelpActivity";
	private ImageView slidingmenu_help_back;//���˰�ť
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		this.setContentView(R.layout.slidingmenu_help_layout);
		slidingmenu_help_back = (ImageView) this.findViewById(R.id.slidingmenu_help_back);
		slidingmenu_help_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
				onBackPressed();
			}
		});
	}

}
