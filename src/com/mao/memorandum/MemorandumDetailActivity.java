package com.mao.memorandum;

import java.util.ArrayList;

import com.mao.myclass.MemorandumItem;
import com.mao.myclass.MyDate;
import com.mao.recorder.DetailActivity;
import com.mao.recorder.R;
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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemorandumDetailActivity extends Activity{
	private final static String tag = "MemorandumDetailActivity";
	final private static int DELETE_SUCCESS = 1;//ɾ���ɹ�
	final private static int DELETE_FAIL = 2;//ɾ��ʧ��
	final private static int SHOW_DIALOG = 3;//��ʾ�Ի���
	final private static int CANCEL_DIALOG = 4;//ȡ���Ի���
	final private static int NETWORK_NO_ACCESS = 5;//���粻����
	
	private ImageView check_memorandum_back;//����
	private TextView check_memorandum_finish;//��ӱ���
	
	private ArrayList<MemorandumItem> memorandumList;
	private boolean hasRevise = false;//����Ƿ��޸Ĺ�
	private ListView listView;//������ʾ���б���
	
	private LinearLayout check_memorandum;
	private String username;//�û���
	private MyDate myDate;//��ǰѡ������
	
	private AlertDialog.Builder builder;
	private Handler handler;//�������̴߳���������Ϣ
    private ProgressDialog pd = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		this.setContentView(R.layout.check_memorandum_layout);
		check_memorandum = (LinearLayout) this.findViewById(R.id.check_memorandum);
		check_memorandum_back = (ImageView) this.findViewById(R.id.check_memorandum_back);
		check_memorandum_finish = (TextView) this.findViewById(R.id.check_memorandum_finish);
		check_memorandum_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("hasRevise", hasRevise);
				data.putExtra("memorandumlist", memorandumList);
				setResult(11, data);
				//�ֶ����÷���
				onBackPressed();
				MemorandumDetailActivity.this.finish();
			}
		});
		//��ӱ�������¼�(��ת����ӱ�������)
		check_memorandum_finish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MemorandumDetailActivity.this,AddMemorandumActivity.class);
				intent.putExtra("username", username);
				intent.putExtra("myDate", myDate);
				MemorandumDetailActivity.this.startActivityForResult(intent, 105);
			}
		});
		memorandumList = (ArrayList<MemorandumItem>) getIntent().getSerializableExtra("memorandumlist");
		if(memorandumList!=null) initListView();//��ʼ��ListView
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
				//ɾ���ɹ�
				case DELETE_SUCCESS:
					initListView();
					break;
				//ɾ��ʧ��
				case DELETE_FAIL:
					Toast.makeText(MemorandumDetailActivity.this, "ɾ��ʧ��", Toast.LENGTH_SHORT).show();
					break;
				//��ʾ���ȶԻ���
				case SHOW_DIALOG:
					pd = ProgressDialog.show(MemorandumDetailActivity.this, "��ʾ", "����ɾ��");
					break;
				//ȡ�����ȶԻ���
				case CANCEL_DIALOG:
					if(pd!=null) pd.cancel();
					break;
			    //���粻����
				case NETWORK_NO_ACCESS:
					Toast.makeText(MemorandumDetailActivity.this, "���粻����", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}
	@Override
	protected void onResume() {
		username = this.getIntent().getStringExtra("username");
		myDate = (MyDate) getIntent().getSerializableExtra("myDate");
		super.onResume();
	}
	//���񷵻ؼ�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent data = new Intent();
			data.putExtra("hasRevise", hasRevise);
			data.putExtra("memorandumlist", memorandumList);
			setResult(11, data);
			MemorandumDetailActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * ��ʼ��ListView
	 * */
	private void initListView()
	{
		if(memorandumList==null)
		{
			Log.i(tag,"memorandumList is null");
			return;
		}
		if(check_memorandum==null)
		{
			Log.i(tag,"content is null");
			return;
		}
		listView = new ListView(this);
		listView.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.memorandum_item_layout, null);
				TextView detail_memorandum_time = (TextView) linearLayout.findViewById(R.id.detail_memorandum_time);
				TextView detail_memorandum_text = (TextView) linearLayout.findViewById(R.id.detail_memorandum_text);
				detail_memorandum_time.setText(memorandumList.get(position).date.toString());
				detail_memorandum_text.setText(memorandumList.get(position).text);
				return linearLayout;
			}
			@Override
			public long getItemId(int position) {
				
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				
				return null;
			}
			
			@Override
			public int getCount() {
				return memorandumList.size();
			}
		});
		//ʵ�ֳ���ɾ��
		setLongClickEvent();
		//�Ƴ���ԭ�ȵ�View
		int count = check_memorandum.getChildCount();
		if(count>1) check_memorandum.removeViews(1, count-1);
		check_memorandum.addView(listView);
	}
	/**
	 * ΪListView�����¼���Ӧ
	 * */
	private void setLongClickEvent()
	{
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//���ñ���
				final View v = view;
				v.setBackground(getResources().getDrawable(R.drawable.memorandum_item_bg_sel));
				final int t = position;
				builder = new AlertDialog.Builder(MemorandumDetailActivity.this);
				builder.setMessage("���Ҫɾ����?");
				builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						v.setBackground(getResources().getDrawable(R.drawable.memorandum_item_bg_nor));
					}
				});
				builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(tag,"position:"+t);
						if(!NetWorkUtil.isAvailable(MemorandumDetailActivity.this))
						{
							handler.sendEmptyMessage(NETWORK_NO_ACCESS);
							return;
						}
						new Thread(new Runnable(){
							@Override
							public void run()
							{
								String timestamp = ""+memorandumList.get(t).date.timestamp;
								handler.sendEmptyMessage(SHOW_DIALOG);
								boolean isSuccess = Network.deleteOneMemorandum(username, timestamp);
								handler.sendEmptyMessage(CANCEL_DIALOG);
								if(isSuccess)
								{
									memorandumList.remove(t);
									hasRevise = true;//��ʾ���޸�
									handler.sendEmptyMessage(DELETE_SUCCESS);
								}
								else handler.sendEmptyMessage(DELETE_FAIL);
							}
						}).start();
					}
				});
				builder.show();
				return true;
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//����ӱ�������
		if(requestCode==105 && resultCode == 8 && data!=null)
		{
			hasRevise = true;//��ʾ���޸�
			if(memorandumList == null) memorandumList = new ArrayList<MemorandumItem>();
			memorandumList.add((MemorandumItem)data.getSerializableExtra("memorandumitem"));
			initListView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
