package com.mao.recorder;

import com.mao.myclass.InformationItem;
import com.mao.util.FunctionUtil;
import com.mao.util.NetWorkUtil;
import com.mao.util.Network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity{
	private final static String tag = "DetailActivity";
	final private static int DELETE_SUCCESS = 1;//ɾ���ɹ�
	final private static int DELETE_FAIL = 2;//ɾ��ʧ��
	final private static int SHOW_DIALOG = 3;//��ʾ�Ի���
	final private static int CANCEL_DIALOG = 4;//ȡ���Ի���
	final private static int NETWORK_NO_ACCESS = 5;//���粻����
	
	private ImageView detail_item_picture;//ͷ��
	private TextView detail_item_username;//�û���
	private TextView detail_item_publishtime;//����ʱ��
	private TextView detail_item_fromdevice;//�����豸
	private TextView detail_item_text;//����
	private TextView detail_item_delete;//ɾ��
	private ImageView detail_item_back;//����
	
	private InformationItem informationItem;
	private int position = -1;
	
	AlertDialog.Builder builder;
    private Handler handler;//�������̴߳���������Ϣ
    private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		setContentView(R.layout.detail_layout);
		
		detail_item_picture = (ImageView) this.findViewById(R.id.detail_item_picture);
		detail_item_username = (TextView) this.findViewById(R.id.detail_item_username);
		detail_item_publishtime = (TextView) this.findViewById(R.id.detail_item_publishtime);
		detail_item_fromdevice = (TextView) this.findViewById(R.id.detail_item_fromdevice);
		detail_item_text = (TextView) this.findViewById(R.id.detail_item_text);
		detail_item_delete = (TextView) this.findViewById(R.id.detail_item_delete);
		detail_item_back = (ImageView) this.findViewById(R.id.detail_item_back);
		//ɾ������¼�
		detail_item_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				builder = new AlertDialog.Builder(DetailActivity.this);
				builder.setMessage("���Ҫɾ����?");
				builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(tag,"cancel delete");
					}
				});
				builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(tag,"delete");
						if(!NetWorkUtil.isAvailable(DetailActivity.this))
						{
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						handler.sendEmptyMessage(SHOW_DIALOG);
						final String name = informationItem.username;
						final String timestamp = informationItem.publishTime;
						final String permission = informationItem.permission+"";
						Log.i(tag,"name:"+name);
						Log.i(tag,"timestamp:"+timestamp);
						Log.i(tag,"permission:"+permission);
						//�������̴߳ӷ�����ɾ���ü�¼
						new Thread(new Runnable(){
							@Override
							public void run()
							{
								Boolean isSuccess = Network.deleteOneRecord(name, timestamp,permission);
								handler.sendEmptyMessage(CANCEL_DIALOG);
								if(isSuccess)
								{
									Bundle data = new Bundle();
									data.putInt("position", position);
									Message msg = new Message();
									msg.setData(data);
									msg.what = DELETE_SUCCESS;
									handler.sendMessage(msg);
								}
								else handler.sendEmptyMessage(DELETE_FAIL);
							}
						}).start();
					}
				});
				builder.show();
			}
		});
		//����ͼ�����¼�
		detail_item_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ֶ����÷���
				onBackPressed();
				DetailActivity.this.setResult(7);
				DetailActivity.this.finish();
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//ɾ���ɹ�
				case DELETE_SUCCESS:
					Bundle data = msg.getData();
					Intent intent = new Intent();
					intent.putExtras(data);
					DetailActivity.this.setResult(6, intent);
					DetailActivity.this.finish();
					break;
				//ɾ��ʧ��
				case DELETE_FAIL:
					Toast.makeText(DetailActivity.this, "ɾ��ʧ��", Toast.LENGTH_SHORT).show();
					break;
				//��ʾ���ȶԻ���
				case SHOW_DIALOG:
					pd = ProgressDialog.show(DetailActivity.this, "��ʾ", "����ɾ��");
					break;
				//ȡ�����ȶԻ���
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
				//���粻����
				case NETWORK_NO_ACCESS:
					Toast.makeText(DetailActivity.this, "���粻����", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	@Override
	protected void onResume() {
		informationItem = (InformationItem) getIntent().getSerializableExtra("informationitem");
		position = getIntent().getIntExtra("position", -1);
		//����ͷ��
		if(!informationItem.picturePath.equals("#")) {detail_item_picture.setImageBitmap(FunctionUtil.getBitmapByPath(informationItem.picturePath));}
		//�����û���
		detail_item_username.setText(informationItem.username);
		//���÷���ʱ��
		detail_item_publishtime.setText(FunctionUtil.convertTimestampToString(Long.parseLong(informationItem.publishTime)));
		//���������ĸ��豸
		detail_item_fromdevice.setText("����"+informationItem.fromDevice);
		//��������
		detail_item_text.setText(informationItem.text);
		Log.i(tag,position+"");
		super.onResume();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//���񷵻ؼ�
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			DetailActivity.this.setResult(7);
			DetailActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
