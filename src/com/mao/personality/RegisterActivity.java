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
	private final static String databaseName = "recorder.db";//���ݿ���
	
	private final static int NETWORK_NO_ACCESS = 0;//���粻����
	public final int REGISTER_SUCCESS = 1;//ע��ɹ�
	public final int REGISTER_SAMENAME = 2;//����ͬ��
	public final int REGISTER_UNKNOWERROR = 3;//δ֪����
	
	
	private EditText register_username_edit;//ע���û���
	private EditText register_password_edit;//ע������
	private EditText register_password_again_edit;//����һ��
	private TextView register_button;//ע�ᰴť
	
	private boolean isRegister = false;//����Ƿ�����ע��
	
	private ProgressDialog progressDialog;
	
	private Handler handler;//���ڴ������̴߳�������һЩ��Ϣ(����UI,��ӡ��˾��)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�ޱ�����
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
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
					if(username==null||"".equals(username)) Toast.makeText(RegisterActivity.this, "�������û���", Toast.LENGTH_SHORT).show();
					else if(password==null||"".equals(password)) Toast.makeText(RegisterActivity.this, "����������", Toast.LENGTH_SHORT).show();
					else if(!password.equals(again_password))
					{
						Toast.makeText(RegisterActivity.this, "���벻һ��", Toast.LENGTH_SHORT).show();
						register_password_edit.setText("");
						register_password_again_edit.setText("");
					}
					else
					{
						//���粻����
						if(!NetWorkUtil.isAvailable(RegisterActivity.this)) 
						{
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						//�����߳��ж��Ƿ�ע��ɹ�
						judgeRegisterSuccess(username,password);
						//���Ϊ����ע��
						isRegister = true;
						progressDialog = ProgressDialog.show(RegisterActivity.this, "����ע��", null, true, true, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								//�û��Լ�ȡ��ע��
								Log.i(tag,"ȡ��ע��");
								//ȡ�����
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
					Toast.makeText(RegisterActivity.this, "���粻����", Toast.LENGTH_SHORT).show();
					break;
				//ע��ɹ�
				case REGISTER_SUCCESS:
					if(progressDialog!=null) progressDialog.dismiss();
					isRegister = false;
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					RegisterActivity.this.setResult(3, intent);
					RegisterActivity.this.finish();//���ٸ�Activity,���ݲŻᱻ��MainActivity
					break;
				//����ͬ��(ע��ʧ��)
				case REGISTER_SAMENAME:
					if(progressDialog!=null) progressDialog.dismiss();
					isRegister = false;
					Toast.makeText(RegisterActivity.this, "�û����Ѵ���", Toast.LENGTH_SHORT).show();
					break;
				//ע��ʧ��
				case REGISTER_UNKNOWERROR:
					if(progressDialog!=null) progressDialog.dismiss();
					isRegister = false;
					Toast.makeText(RegisterActivity.this, "����δ֪����", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	/**
	 * �ж��Ƿ�ע��ɹ�
	 * */
	private void judgeRegisterSuccess(final String name,final String password)
	{
		try
		{
			new Thread(new Runnable(){
				public void run()
				{
					int result = Network.isRegisterSuccess(name, password);
					//�û����Ѵ���
					if(result==REGISTER_SAMENAME) handler.sendEmptyMessage(REGISTER_SAMENAME);
					//����δ֪����
					else if(result==REGISTER_UNKNOWERROR) handler.sendEmptyMessage(REGISTER_UNKNOWERROR);
					//ע��ɹ�
					else if(result==REGISTER_SUCCESS)
					{
						//д�뵽�������ݿ�
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
