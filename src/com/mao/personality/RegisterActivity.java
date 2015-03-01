package com.mao.personality;

import com.mao.recorder.R;
import com.mao.util.DatabaseTool;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity{
	private final static String tag = "RegisterActivity";
	private final static String databaseName = "recorder.db";//数据库名
	
	private final static int NETWORK_NO_ACCESS = 0;//网络不可用
	public final int REGISTER_SUCCESS = 1;//注册成功
	public final int REGISTER_SAMENAME = 2;//出现同名
	public final int REGISTER_UNKNOWERROR = 3;//未知错误
	
	
	private EditText register_username_edit;//注册用户名
	private EditText register_password_edit;//注册密码
	private EditText register_password_again_edit;//再输一遍
	private TextView register_button;//注册按钮
	
	private boolean isRegister = false;//标记是否正在注册
	
	private ProgressDialog progressDialog;
	
	private Handler handler;//用于处理子线程传过来的一些消息(更新UI,打印吐司等)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//无标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		setContentView(R.layout.activity_register_layout);
		
		register_username_edit = (EditText) this.findViewById(R.id.register_username_edit);
		register_password_edit = (EditText) this.findViewById(R.id.register_password_edit);
		register_password_again_edit = (EditText) this.findViewById(R.id.register_password_again_edit);
		register_button = (TextView) this.findViewById(R.id.register_button);
		try
		{
			register_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String username = register_username_edit.getText().toString().trim();
					String password = register_password_edit.getText().toString().trim();
					String again_password = register_password_again_edit.getText().toString().trim();
					if(username==null||"".equals(username)) Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
					else if(password==null||"".equals(password)) Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
					else if(!password.equals(again_password))
					{
						Toast.makeText(RegisterActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
						register_password_edit.setText("");
						register_password_again_edit.setText("");
					}
					else
					{
						//网络不可用
						if(!NetWorkUtil.isAvailable(RegisterActivity.this)) 
						{
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						//在子线程判断是否注册成功
						judgeRegisterSuccess(username,password);
						//标记为正在注册
						isRegister = true;
						progressDialog = ProgressDialog.show(RegisterActivity.this, "正在注册", null, true, true, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								//用户自己取消注册
								Log.i(tag,"取消注册");
								//取消标记
								isRegister = false;
							}
						});
					}
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//初始化Handler对象
		initHandler();
	}
	/**
	 * 初始化Handler对象
	 * */
	private void initHandler()
	{
		if(handler == null) handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
			    //网络不可用
				case NETWORK_NO_ACCESS:
					Toast.makeText(RegisterActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
					break;
				//注册成功
				case REGISTER_SUCCESS:
					if(progressDialog!=null) progressDialog.dismiss();
					isRegister = false;
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					RegisterActivity.this.setResult(3, intent);
					RegisterActivity.this.finish();//销毁该Activity,数据才会被传MainActivity
					break;
				//出现同名(注册失败)
				case REGISTER_SAMENAME:
					if(progressDialog!=null) progressDialog.dismiss();
					isRegister = false;
					Toast.makeText(RegisterActivity.this, "用户名已存在", Toast.LENGTH_SHORT).show();
					break;
				//注册失败
				case REGISTER_UNKNOWERROR:
					if(progressDialog!=null) progressDialog.dismiss();
					isRegister = false;
					Toast.makeText(RegisterActivity.this, "发生未知错误", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	/**
	 * 判断是否注册成功
	 * */
	private void judgeRegisterSuccess(final String name,final String password)
	{
		try
		{
			new Thread(new Runnable(){
				public void run()
				{
					int result = Network.isRegisterSuccess(name, password);
					//用户名已存在
					if(result==REGISTER_SAMENAME) handler.sendEmptyMessage(REGISTER_SAMENAME);
					//发生未知错误
					else if(result==REGISTER_UNKNOWERROR) handler.sendEmptyMessage(REGISTER_UNKNOWERROR);
					//注册成功
					else if(result==REGISTER_SUCCESS)
					{
						//写入到本地数据库
						DatabaseTool dbTool = new DatabaseTool(RegisterActivity.this,databaseName);
						dbTool.writeAccountToDB(name,password);
						dbTool.createDynamicTable(name);
						System.out.println("username:"+name);
						System.out.println("password:"+password);
						Bundle bundle = new Bundle();
						bundle.putString("username", name);
						bundle.putString("upassword", password);
						bundle.putString("picturepath", "#");
						Message msg = new Message();
						msg.setData(bundle);
						msg.what = REGISTER_SUCCESS;
						handler.sendMessage(msg);
					}
				}
			}).start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
