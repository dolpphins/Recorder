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
	private final static int GET_SUCCESS = 1;//获取成功
	private final static int GET_FAIL = 2;//获取失败
	private ImageView slidingmenu_update_back;//后退按钮
	private TextView update_version_tv;
	private String version;//当前版本号
	private String latestVersion;//最新版本号
	private String updateTip = "点击更新";
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		this.setContentView(R.layout.slidingmenu_update_layout);
		slidingmenu_update_back = (ImageView) this.findViewById(R.id.slidingmenu_update_back);
		slidingmenu_update_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//手动调用返回
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
				//获取数据成功
				case GET_SUCCESS:
					System.out.println("currentVersion:"+version);
					System.out.println("latestVersion:"+latestVersion);
					if(latestVersion.equals(version)) Toast.makeText(UpdateActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
					else
					{
						Toast.makeText(UpdateActivity.this, "检查到新版本", Toast.LENGTH_SHORT).show();
						update_version_tv.setText(updateTip);//提示点击更新
					}
					break;
				//获取数据失败
				case GET_FAIL:
					Toast.makeText(UpdateActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
		update_version_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = update_version_tv.getText().toString();
				//进行版本更新
				if(updateTip.equals(text))
				{
					
				}
			}
		});
		if(!NetWorkUtil.isAvailable(this)) Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
		else checkUpdate();
	}
	/**
	 * 检查更新
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
