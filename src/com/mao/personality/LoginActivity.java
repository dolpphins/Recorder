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
 * �˺ŵ�¼����
 * */
public class LoginActivity extends Activity{
	private final static String tag = "LoginActivity";
	private final static String databaseName = "recorder.db";//���ݿ���
	private final static int NETWORK_NO_ACCESS = 0;//���粻����
	private final static int LOGIN_SUCCESS = 1;//��¼�ɹ�
	private final static int LOGIN_FAIL = 2;//��¼ʧ��
	
	private EditText username_edit;//�û��������
	private EditText password_edit;//���������
	private TextView login_button;//��¼��ť
	private TextView forgetpassword_button;//�������밴ť
	private TextView login_register_button;//���Ͻ�ע�ᰴť
	
	private boolean canLogin = true;//����Ƿ��ܵ�¼
	private boolean isLogin = false;//����Ƿ����ڵ�¼
	
	private ProgressDialog progressDialog;
	
	private Handler handler;//���ڴ������̴߳�������һЩ��Ϣ(����UI,��ӡ��˾��)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�ޱ�����
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
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
					if(username==null||"".equals(username)) Toast.makeText(LoginActivity.this, "�������û���", Toast.LENGTH_SHORT).show();
					else if(password==null||"".equals(password)) Toast.makeText(LoginActivity.this, "����������", Toast.LENGTH_SHORT).show();
					else
					{
						if(!NetWorkUtil.isAvailable(LoginActivity.this))
						{
							//���粻����
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						//�����߳��ж��Ƿ��¼�ɹ�
						judgeLoginSuccess(username,password);
						//���Ϊ���ڵ�¼
						isLogin = true;
						progressDialog = ProgressDialog.show(LoginActivity.this, "���ڵ�¼", null, true, true, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								//�û��Լ�ȡ����¼
								Log.i(tag,"ȡ����¼");
								//ȡ�����
								isLogin = false;
							}
						});
					}
				}
			});
			//���Ͻ�ע�ᰴť����¼�
			login_register_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//��ת��ע�����
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
		
	    //��ʼ��Handler����
		initHandler();
	}
	/**
	 * ��ʼ��Handler����
	 * */
	private void initHandler()
	{
		if(handler == null) handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//���粻����
				case NETWORK_NO_ACCESS:
					Toast.makeText(LoginActivity.this, "���粻����", Toast.LENGTH_SHORT).show();
					break;
				//��¼�ɹ�
				case LOGIN_SUCCESS:
					if(progressDialog!=null) progressDialog.dismiss();
					isLogin = false;
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					LoginActivity.this.setResult(2, intent);
					LoginActivity.this.finish();//���ٸ�Activity,���ݲŻᱻ��MainActivity
					break;
				//��¼ʧ��
				case LOGIN_FAIL:
					if(progressDialog!=null) progressDialog.dismiss();
					isLogin = false;
					Toast.makeText(LoginActivity.this, "���벻��ȷ", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	/**
	 * �ж��Ƿ��¼�ɹ�
	 * */
	private void judgeLoginSuccess(final String name,final String password)
	{
		try
		{
			//�������߳�ִ�к�ʱ����
			new Thread(new Runnable(){
				public void run()
				{
					DatabaseTool dbTool = new DatabaseTool(LoginActivity.this,databaseName);
					//boolean loginSuccess = dbTool.isLoginSuccess(name, password);
					boolean loginSuccess = Network.isLoginSuccess(name, password);
					//��¼�ɹ�
					if(loginSuccess)
					{
						Account account1 = dbTool.queryAccount(name);
						Account account = Network.queryAccount(name);
						//��ѯ���û���Ϣʧ�ܣ���Ϊ��¼ʧ��
						if(account==null) 
						{
							handler.sendEmptyMessage(LOGIN_FAIL);
							return;
						}
						if(account1==null) 
						{
							Log.i(tag,"local have not current account,will write it");
							//����û�и��˺ŵ���Ϣ��д�뵽����
							account1 = new Account();
							dbTool.writeAccountToDB(account.username, account.password);
						}
						//���û��������봫��handler��
						Bundle data = new Bundle();
						Message msg = new Message();
						data.putString("username", name);
						data.putString("password", password);
						data.putString("picturepath", account1.picturePath);
						msg.setData(data);
						msg.what = LOGIN_SUCCESS;
						handler.sendMessage(msg);
					}
					//��¼ʧ��
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
