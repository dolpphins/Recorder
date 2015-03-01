package com.mao.personality;

import com.mao.myclass.Account;
import com.mao.recorder.R;
import com.mao.util.DatabaseTool;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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
/**
 * 账号登录界面
 * */
public class LoginActivity extends Activity{
	private final static String tag = "LoginActivity";
	private final static String databaseName = "recorder.db";//数据库名
	private final static int NETWORK_NO_ACCESS = 0;//网络不可用
	private final static int LOGIN_SUCCESS = 1;//登录成功
	private final static int LOGIN_FAIL = 2;//登录失败
	
	private EditText username_edit;//用户名输入框
	private EditText password_edit;//密码输入框
	private TextView login_button;//登录按钮
	private TextView forgetpassword_button;//忘记密码按钮
	private TextView login_register_button;//右上角注册按钮
	
	private boolean canLogin = true;//标记是否能登录
	private boolean isLogin = false;//标记是否正在登录
	
	private ProgressDialog progressDialog;
	
	private Handler handler;//用于处理子线程传过来的一些消息(更新UI,打印吐司等)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//无标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		setContentView(R.layout.activity_login_layout);
		
		username_edit = (EditText) this.findViewById(R.id.username_edit);
		password_edit = (EditText) this.findViewById(R.id.password_edit);
		login_button = (TextView) this.findViewById(R.id.login_button);
		login_register_button = (TextView) this.findViewById(R.id.login_register_button);
		//forgetpassword_button = (TextView) this.findViewById(R.id.forgetpassword_button);
		try
		{
			login_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String username = username_edit.getText().toString().trim();
					String password = password_edit.getText().toString().trim();
					if(username==null||"".equals(username)) Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
					else if(password==null||"".equals(password)) Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
					else
					{
						if(!NetWorkUtil.isAvailable(LoginActivity.this))
						{
							//网络不可用
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						//在子线程判断是否登录成功
						judgeLoginSuccess(username,password);
						//标记为正在登录
						isLogin = true;
						progressDialog = ProgressDialog.show(LoginActivity.this, "正在登录", null, true, true, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								//用户自己取消登录
								Log.i(tag,"取消登录");
								//取消标记
								isLogin = false;
							}
						});
					}
				}
			});
			//右上角注册按钮点击事件
			login_register_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//跳转到注册界面
					Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
					LoginActivity.this.startActivityForResult(intent, 100);
					//LoginActivity.this.finish();
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
					Toast.makeText(LoginActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
					break;
				//登录成功
				case LOGIN_SUCCESS:
					if(progressDialog!=null) progressDialog.dismiss();
					isLogin = false;
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					LoginActivity.this.setResult(2, intent);
					LoginActivity.this.finish();//销毁该Activity,数据才会被传MainActivity
					break;
				//登录失败
				case LOGIN_FAIL:
					if(progressDialog!=null) progressDialog.dismiss();
					isLogin = false;
					Toast.makeText(LoginActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	/**
	 * 判断是否登录成功
	 * */
	private void judgeLoginSuccess(final String name,final String password)
	{
		try
		{
			//开启子线程执行耗时操作
			new Thread(new Runnable(){
				public void run()
				{
					DatabaseTool dbTool = new DatabaseTool(LoginActivity.this,databaseName);
					//boolean loginSuccess = dbTool.isLoginSuccess(name, password);
					boolean loginSuccess = Network.isLoginSuccess(name, password);
					//登录成功
					if(loginSuccess)
					{
						Account account1 = dbTool.queryAccount(name);
						Account account = Network.queryAccount(name);
						//查询该用户信息失败，认为登录失败
						if(account==null) 
						{
							handler.sendEmptyMessage(LOGIN_FAIL);
							return;
						}
						if(account1==null) 
						{
							Log.i(tag,"local have not current account,will write it");
							//本地没有该账号的信息，写入到本地
							account1 = new Account();
							dbTool.writeAccountToDB(account.username, account.password);
						}
						//将用户名和密码传到handler中
						Bundle data = new Bundle();
						Message msg = new Message();
						data.putString("username", name);
						data.putString("password", password);
						data.putString("picturepath", account1.picturePath);
						msg.setData(data);
						msg.what = LOGIN_SUCCESS;
						handler.sendMessage(msg);
					}
					//登录失败
					else handler.sendEmptyMessage(LOGIN_FAIL);
				}
			}).start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			handler.sendEmptyMessage(LOGIN_FAIL);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(tag,"onActivityResult");
		setResult(3, data);
		LoginActivity.this.finish();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
