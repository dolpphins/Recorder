package com.mao.recorder;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mao.dynamic.Dynamic;
import com.mao.memorandum.Memorandum;
import com.mao.myclass.Account;
import com.mao.myclass.Fouse;
import com.mao.myclass.InformationItem;
import com.mao.myclass.MemorandumItem;
import com.mao.personality.FousemeActivity;
import com.mao.personality.LoginActivity;
import com.mao.personality.MessageActivity;
import com.mao.personality.MyFouseActivity;
import com.mao.personality.RecordInformation;
import com.mao.personality.RegisterActivity;
import com.mao.slidingmenu.AboutActivity;
import com.mao.slidingmenu.HelpActivity;
import com.mao.slidingmenu.SettingActivity;
import com.mao.slidingmenu.ShareActivity;
import com.mao.slidingmenu.UpdateActivity;
import com.mao.util.DatabaseTool;
import com.mao.util.FunctionUtil;
import com.mao.util.Network;
import com.mao.util.NetworkStateReceiver;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Color;

public class MainActivity extends Activity {
	final private String tag = "MainActivity";
	private final static String databaseName = "recorder.db";//���ݿ���
	final private String sharedPreferencesName = "account.txt";
	
	private TabHost tabHost;//ѡ�
	String[] tabLabels;//ѡ�����
	Drawable[] tabNormalDrawales;//ѡ�δ��ѡ��ʱͼ��
	Drawable[] tabSelDrawales;//ѡ���ѡ��ʱͼ��
	int[] tabLayoutIds;//ѡ���Ӧ�����������view��id
	int[] tabLayoutLayouts;//ѡ�ÿ��ѡ��Ĳ���
	LinearLayout[] tabViews;//ѡ�ÿ��ѡ���view���Զ��壩
	int[] tabImageIds;//ѡ�ÿ��ѡ���ͼ��id
	ImageView[] imageViews;//��¼ÿ��ͼ������
	TextView[] textViews;//��¼ÿ��ѡ����������
	int[] tabTextIds;//ѡ�ÿ��ѡ�������id
	int currentSelTab = 0;//��¼��ǰѡ���ĸ�tab
	
	private static int screenWidth;//������Ļ���
	private static int screenHeight;//������Ļ�߶�
	
	private SlidingMenu slidingMenu;//�໬�˵�(�ұ�)
	private ImageView show_slidingmenu;//�������ʾ�Ҳ໬�˵�ͼ��
	private RelativeLayout menu_settings;//����
	private RelativeLayout menu_update;//�汾����
	private RelativeLayout menu_share;//�������
	private RelativeLayout menu_help;//ʹ�ð���
	private RelativeLayout menu_about;//��������
	
	private LinearLayout login_register_layout_1;//��һ��ѡ�����������¼ע�᲼��
	private LinearLayout login_register_layout_2;//�ڶ���ѡ�����������¼ע�᲼��
	private LinearLayout login_register_layout_3;//������ѡ�����������¼ע�᲼��
	private LinearLayout login_register_layout_4;//���ĸ�ѡ�����������¼ע�᲼��
	private LinearLayout personality_content_layout;//������������򲼾�
	private LinearLayout head_picture_layout;//�ϴ�ͷ�񲼾�
	private LinearLayout tab1_content;//��һ��ѡ���
	private LinearLayout tab2_content;//�ڶ���ѡ���
	private LinearLayout tab3_content;//������ѡ���
	private LinearLayout tab4_content;//���ĸ�ѡ���
	private LinearLayout tab5_content;//�����ѡ���
	private TextView login_1;//��¼��ť
	private TextView register_1;//ע�ᰴť
	private TextView login_2;//��¼��ť
	private TextView register_2;//ע�ᰴť
	private TextView login_3;//��¼��ť
	private TextView register_3;//ע�ᰴť
	private TextView login_4;//��¼��ť
	private TextView register_4;//ע�ᰴť
	
	private ImageView personality_head_picture;//ͷ��
	private boolean willUploadPicture = false;//����Ƿ���ʾ�ϴ�ͷ��ť
	private TextView takephoto_button;//�����ϴ�ͷ��ť
	private TextView localpicture_button;//����ͼƬ�ϴ�ͷ��ť
	private TextView account_username;//���������û���
	private TextView account_exit;//���������˳� 
	private LinearLayout myfocus_button;//�ҹ�ע����
	private LinearLayout focusme_button;//��ע�ҵ���
	private LinearLayout message_button;//��Ϣ����
	
	private Account currentAccount;//���浱ǰ��¼�˺���Ϣ
	private boolean isLogin = false;//��¼�Ƿ��ѵ�¼
	private SharedPreferences sharedPreferences;//����һЩ��Ϣ
	public static ArrayList<InformationItem> informationList = new ArrayList<InformationItem>();//���浱ǰ�˺ż�¼����Ϣ
	private ArrayList<Fouse> myFouseList = null;//�ڵ�¼�󱣴��ҹ�ע������Ϣ
	private ArrayList<Fouse> fousemeList = null;//�ڵ�¼�󱣴��ע�ҵ�����Ϣ
	
	private Dynamic dynamic;//����ѡ���
	private RecordInformation record;//���ռ�ѡ���
	private Memorandum memorandum;//����¼ѡ���
	
	private long firstTime = 0;//�����ж��Ƿ��������ΰ����ؼ��˳�����
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�ޱ���
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//���ò���
		setContentView(R.layout.activity_main);
		//��ȡ��Ļ��С
		getScreenSize();
		//��ʼ��һЩ����
		initConstant();
		//��ʼ��ѡ�����
		initTab();
		//��ʼ������(�໬�˵���)
		initLayout();
		//�õ��˺��Ƿ��¼��Ϣ
		getAccountInfo();
		
		drawLayoutNoLogin();//����û�е�¼״̬���ض�����
		if(isLogin) updateUIAfterLogin();//�����¼���޸Ĳ���
		NetworkStateReceiver.initNetworkState(this);//��ʼ������״̬
	
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(currentSelTab==2 && tabHost!=null)
		{
			tabHost.setCurrentTab(0);
		}
		if(willUploadPicture)
		{
			willUploadPicture = false;
			head_picture_layout.setVisibility(View.INVISIBLE);
		}
	}
    /**
     * ��ȡ��Ļ��С
     * */
	public void getScreenSize()
	{
		WindowManager windowManager = this.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		screenWidth = outMetrics.widthPixels;
		screenHeight = outMetrics.heightPixels;
	}
	/**
	 * ��ʼ��һЩ����
	 * */
	public void initConstant()
	{
		//ѡ��ַ���
		tabLabels=new String[5];
		tabLabels[0]=getResources().getString(R.string.today);
		tabLabels[1]=getResources().getString(R.string.memorandum);
		tabLabels[2]=getResources().getString(R.string.write);
		tabLabels[3]=getResources().getString(R.string.dynamic);
		tabLabels[4]=getResources().getString(R.string.personality);
		//ѡ�δ��ѡ��ʱͼ��
		tabNormalDrawales = new Drawable[5];
		tabNormalDrawales[0] = getResources().getDrawable(R.drawable.icon_today_nor);
		tabNormalDrawales[1] = getResources().getDrawable(R.drawable.icon_memorandum_nor);
		tabNormalDrawales[2] = getResources().getDrawable(R.drawable.icon_edit);
		tabNormalDrawales[3] = getResources().getDrawable(R.drawable.icon_dynamic_nor);
		tabNormalDrawales[4] = getResources().getDrawable(R.drawable.icon_personality_nor);
		//ѡ���ѡ��ʱͼ��
		tabSelDrawales = new Drawable[5];
		tabSelDrawales[0] = getResources().getDrawable(R.drawable.icon_today_sel);
		tabSelDrawales[1] = getResources().getDrawable(R.drawable.icon_memorandum_sel);
		tabSelDrawales[2] = getResources().getDrawable(R.drawable.icon_edit);
		tabSelDrawales[3] = getResources().getDrawable(R.drawable.icon_dynamic_sel);
		tabSelDrawales[4] = getResources().getDrawable(R.drawable.icon_personality_sel);
		//ѡ�id
		tabLayoutIds = new int[5];
		tabLayoutIds[0]=R.id.tab1;
		tabLayoutIds[1]=R.id.tab2;
		tabLayoutIds[2]=R.id.tab3;
		tabLayoutIds[3]=R.id.tab4;
		tabLayoutIds[4]=R.id.tab5;
		//ѡ�ÿ��ѡ��Ĳ���
		tabLayoutLayouts = new int[5];
		tabLayoutLayouts[0] = R.layout.today_layout;
		tabLayoutLayouts[1] = R.layout.memorandum_layout;
		tabLayoutLayouts[2] = R.layout.write_layout;
		tabLayoutLayouts[3] = R.layout.dynamic_layout;
		tabLayoutLayouts[4] = R.layout.personality_layout;
		//ѡ�ÿ��ѡ���ͼ��id
		tabImageIds = new int[5];
		tabImageIds[0] = R.id.icon_today;
		tabImageIds[1] = R.id.icon_memorandum;
		tabImageIds[2] = R.id.icon_edit;
		tabImageIds[3] = R.id.icon_dynamic;
		tabImageIds[4] = R.id.icon_personality;
		//ѡ�ÿ��ѡ�������id
		tabTextIds = new int[5];
		tabTextIds[0] = R.id.text_today;
		tabTextIds[1] = R.id.text_memorandum;
		tabTextIds[2] = R.id.text_write;
		tabTextIds[3] = R.id.text_dynamic;
		tabTextIds[4] = R.id.text_personality;
		//ѡ�view
		tabViews = new LinearLayout[5];
		imageViews = new ImageView[5];
		textViews = new TextView[5];
		for(int i=0;i<tabViews.length;i++)
		{
			LayoutInflater layoutInflater = this.getLayoutInflater();
			tabViews[i] = (LinearLayout)layoutInflater.inflate(tabLayoutLayouts[i], null);
			imageViews[i] = new ImageView(this);
			imageViews[i] = (ImageView)tabViews[i].findViewById(tabImageIds[i]);
			textViews[i] = (TextView) tabViews[i].findViewById(tabTextIds[i]);
			/*LinearLayout linearLayout = new LinearLayout(this);
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			//ʵ����һ��ImageView����
			ImageView image = new ImageView(this);
			//���ò��ֲ���
			image.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			//ʵ����һ��TextView����
			TextView text = new TextView(this);
			//���ò��ֲ���
			text.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			
			image.setBackground(tabDrawales[i]);
			text.setText(tabLabels[i]);
			linearLayout.addView(image,0);
			linearLayout.addView(text,1);
			
			//�����Բ��ּӵ���Բ����У�����
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			tabViews[i].addView(linearLayout);*/
		}
	}
	/**
	 * ��ʼ��ѡ�����
	 * */
	public void initTab()
	{
		if(tabLabels==null||tabLayoutIds==null||tabNormalDrawales==null)
		{
			Log.e(tag, "tableLabels , tabLayoutIds or tabDrawales is null,can't init TabHost");
			return;
		}
		tabHost = (TabHost) this.findViewById(R.id.tabhost);
		tabHost.setup();//ʵ����tabWidget��tabContent
		//���ѡ��
		for(int i=0;i<tabLabels.length;i++)
		{
			tabHost.addTab(tabHost.newTabSpec(tabLabels[i]).setIndicator(tabViews[i]).setContent(tabLayoutIds[i]));
		}
		//Ĭ�ϵ�һ��ѡ��ѡ��
		imageViews[currentSelTab].setBackground(tabSelDrawales[currentSelTab]);
		tabHost.setCurrentTab(currentSelTab);
		//ע�����¼�
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				willUploadPicture = false;
				head_picture_layout.setVisibility(View.INVISIBLE);
				for(int i=0;i<tabSelDrawales.length;i++)
				{
					if(tabId.equals(tabLabels[i]))
					{
						Log.i(tag,tabId);
						try
						{
							imageViews[currentSelTab].setBackground(tabNormalDrawales[currentSelTab]);
							textViews[currentSelTab].setTextColor(Color.BLACK);
							currentSelTab = i;
							imageViews[currentSelTab].setBackground(tabSelDrawales[currentSelTab]);
							textViews[currentSelTab].setTextColor(Color.BLUE);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						break;
					}
				}
				//����¼�
				if(tabId.equals("дһд"))
				{
					if(isLogin)
					{
						Intent intent = new Intent(MainActivity.this,RecordActivity.class);
						intent.putExtra("username", currentAccount.username);
						MainActivity.this.startActivityForResult(intent,1);
						if(Integer.valueOf(android.os.Build.VERSION.SDK)>=5)
						{
							MainActivity.this.overridePendingTransition(R.anim.write_activity_start_anim, R.anim.write_activity_end_anim);
						}
					}
				}
			}
		});
	}
	/**
	 * ��ʼ������(�໬�˵�������¼���)
	 * */
	public void initLayout()
	{
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.RIGHT);
		//slidingMenu.setShadowWidth(500);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setBehindWidth(7*screenWidth/10);//����SlidingMenu�˵��Ŀ��
		slidingMenu.setFadeDegree(0.2f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//����Ҫ����
		slidingMenu.setMenu(R.layout.slidingmenu_layout);//�໬�˵�����
		show_slidingmenu = (ImageView) this.findViewById(R.id.show_slidingmenu);
		if(show_slidingmenu==null)
		{
			Log.e(tag,"can't init slidingmenu");
			return;
		}
		//���õ���¼�
		show_slidingmenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				slidingMenu.showMenu();//��ʾ�໬�˵�
			}
		});
		// ��ʼ���໬�˵�����¼�
		LinearLayout slidingMenuLayout = (LinearLayout) slidingMenu.getMenu();
		menu_settings = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_settings);
		menu_update = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_update);
		menu_share = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_share);
		menu_help = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_help);
		menu_about = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_about);
		//����
		menu_settings.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				RelativeLayout relativeLayout = (RelativeLayout)v;
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					relativeLayout.setBackgroundColor(Color.parseColor("#cccccc"));
					break;
				case MotionEvent.ACTION_UP:
					relativeLayout.setBackgroundColor(Color.parseColor("#191f24"));
					Intent intent = new Intent(MainActivity.this,SettingActivity.class);
					startActivity(intent);
					break;
				}
				return true;
			}
		});
		//�汾����
		menu_update.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				RelativeLayout relativeLayout = (RelativeLayout)v;
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					relativeLayout.setBackgroundColor(Color.parseColor("#cccccc"));
					break;
				case MotionEvent.ACTION_UP:
					relativeLayout.setBackgroundColor(Color.parseColor("#191f24"));
					Intent intent = new Intent(MainActivity.this,UpdateActivity.class);
					startActivity(intent);
					break;
				}
				return true;
			}
		});
		//�������
		menu_share.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				RelativeLayout relativeLayout = (RelativeLayout)v;
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					relativeLayout.setBackgroundColor(Color.parseColor("#cccccc"));
					break;
				case MotionEvent.ACTION_UP:
					relativeLayout.setBackgroundColor(Color.parseColor("#191f24"));
					//Intent intent = new Intent(MainActivity.this,ShareActivity.class);
					//startActivity(intent);
					Intent intent=new Intent(Intent.ACTION_SEND);   
			        //intent.setType("image/*");
					intent.setType("text/plain");
			        intent.putExtra(Intent.EXTRA_SUBJECT, "����");   
			        intent.putExtra(Intent.EXTRA_TEXT, "��!������ʹ��һ��ܺ��õ����������ǡ�,��ȥ���ذ�!");    
			        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
			        startActivity(Intent.createChooser(intent, getTitle()));
					break;
				}
				return true;
			}
		});
		//ʹ�ð���
		menu_help.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				RelativeLayout relativeLayout = (RelativeLayout)v;
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					relativeLayout.setBackgroundColor(Color.parseColor("#cccccc"));
					break;
				case MotionEvent.ACTION_UP:
					relativeLayout.setBackgroundColor(Color.parseColor("#191f24"));
					Intent intent = new Intent(MainActivity.this,HelpActivity.class);
					startActivity(intent);
					break;
				}
				return true;
			}
		});
		//��������
		menu_about.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				RelativeLayout relativeLayout = (RelativeLayout)v;
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					relativeLayout.setBackgroundColor(Color.parseColor("#cccccc"));
					break;
				case MotionEvent.ACTION_UP:
					relativeLayout.setBackgroundColor(Color.parseColor("#191f24"));
					Intent intent = new Intent(MainActivity.this,AboutActivity.class);
					startActivity(intent);
					break;
				}
				return true;
			}
		});
				
	}
	/**
	 * �õ��˺��Ƿ��¼��Ϣ
	 * */
	public void getAccountInfo()
	{
		if(currentAccount==null) currentAccount = new Account();
		sharedPreferences = this.getSharedPreferences(sharedPreferencesName,Context.MODE_PRIVATE);
		isLogin = sharedPreferences.getBoolean("isLogin",false);
		//����ѵ�¼
		if(isLogin)
		{
			//��ȡ�û��������롢ͷ��·������Ϣ
			currentAccount.username = sharedPreferences.getString("username", null);
			currentAccount.password = sharedPreferences.getString("password", null);
			currentAccount.picturePath = sharedPreferences.getString("picturepath", "#");
		}
	}
	/**
	 * ������Щ��û�е�¼״̬�µĲ���
	 * */
	public void drawLayoutNoLogin()
	{
		//��һ��ѡ����������
		login_register_layout_1 = (LinearLayout) getLayoutInflater().inflate(R.layout.login_register_layout, null);
		if(login_register_layout_1==null)
		{
			Log.e(tag,"login_register_layout_1 is null");
			return;
		}
		tab1_content = (LinearLayout) this.findViewById(R.id.tab1);
		if(tab1_content==null)
		{
			Log.e(tag,"tab1_content is null");
			return;
		}
		login_register_layout_1.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		tab1_content.addView(login_register_layout_1);
		login_1 = (TextView) login_register_layout_1.findViewById(R.id.login);
		register_1 = (TextView) login_register_layout_1.findViewById(R.id.register);
		if(login_1==null || register_1==null) return;
		//��¼��ť����¼�
		login_1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					login_1.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					login_1.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,LoginActivity.class);
					MainActivity.this.startActivityForResult(intent, 1);
					break;
				}
				return true;
			}
		});
		//ע�ᰴť����¼�
		register_1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					register_1.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					register_1.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
					MainActivity.this.startActivityForResult(intent, 2);
					break;
				}
				return true;
			}
		});
		//�ڶ���ѡ����������
		login_register_layout_2 = (LinearLayout) getLayoutInflater().inflate(R.layout.login_register_layout, null);
		if(login_register_layout_2==null)
		{
			Log.e(tag,"login_register_layout_2 is null");
			return;
		}
		tab2_content = (LinearLayout) this.findViewById(R.id.tab2);
		if(tab2_content==null)
		{
			Log.e(tag,"tab2_content is null");
			return;
		}
		login_register_layout_2.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		tab2_content.addView(login_register_layout_2);
		login_2 = (TextView) login_register_layout_2.findViewById(R.id.login);
		register_2 = (TextView) login_register_layout_2.findViewById(R.id.register);
		if(login_2==null || register_2==null) 
		{
			Log.e(tag,"login or register is null");
			return;
		}
		System.out.println("abc");
		//��¼��ť����¼�
		login_2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					login_2.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					login_2.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,LoginActivity.class);
					MainActivity.this.startActivityForResult(intent, 1);
					break;
				}
				return true;
			}
		});
		//ע�ᰴť����¼�
		register_2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					register_2.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					register_2.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
					MainActivity.this.startActivityForResult(intent, 2);
					break;
				}
				return true;
			}
		});
		//������ѡ������(дһд)
		login_register_layout_3 = (LinearLayout) getLayoutInflater().inflate(R.layout.login_register_layout, null);
		if(login_register_layout_3==null)
		{
			Log.e(tag,"login_register_layout_3 is null");
			return;
		}
		tab3_content = (LinearLayout) this.findViewById(R.id.tab3);
		if(tab3_content==null)
		{
			Log.e(tag,"tab3_content is null");
			return;
		}
		login_register_layout_3.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		tab3_content.addView(login_register_layout_3);
		login_3 = (TextView) login_register_layout_3.findViewById(R.id.login);
		register_3 = (TextView) login_register_layout_3.findViewById(R.id.register);
		if(login_3==null || register_3==null) 
		{
			Log.e(tag,"login or register is null");
			return;
		}
		System.out.println("abc");
		//��¼��ť����¼�
		login_3.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					login_3.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					login_3.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,LoginActivity.class);
					MainActivity.this.startActivityForResult(intent, 1);
					break;
				}
				return true;
			}
		});
		//ע�ᰴť����¼�
		register_3.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					register_3.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					register_3.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
					MainActivity.this.startActivityForResult(intent, 2);
					break;
				}
				return true;
			}
		});
		//���ĸ�ѡ������(����)
		login_register_layout_4 = (LinearLayout) getLayoutInflater().inflate(R.layout.login_register_layout, null);
		if(login_register_layout_4==null)
		{
			Log.e(tag,"login_register_layout_4 is null");
			return;
		}
		tab4_content = (LinearLayout) this.findViewById(R.id.tab4);
		if(tab4_content==null)
		{
			Log.e(tag,"tab4_content is null");
			return;
		}
		login_register_layout_4.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		tab4_content.addView(login_register_layout_4);
		login_4 = (TextView) login_register_layout_4.findViewById(R.id.login);
		register_4 = (TextView) login_register_layout_4.findViewById(R.id.register);
		if(login_4==null || register_4==null) 
		{
			Log.e(tag,"login or register is null");
			return;
		}
		System.out.println("abc");
		//��¼��ť����¼�
		login_4.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					login_4.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					login_4.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,LoginActivity.class);
					MainActivity.this.startActivityForResult(intent, 1);
					break;
				}
				return true;
			}
		});
		//ע�ᰴť����¼�
		register_4.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					register_4.setBackground(getResources().getDrawable(R.drawable.login_register_border_sel));
					break;
				case MotionEvent.ACTION_UP:
					register_4.setBackground(getResources().getDrawable(R.drawable.login_register_border_nor));
					Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
					MainActivity.this.startActivityForResult(intent, 2);
					break;
				}
				return true;
			}
		});
		//�����ѡ������(��������)
		personality_content_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.personality_content_layout, null);
		if(personality_content_layout==null)
		{
			Log.e(tag,"personality_content_layout is null");
			return;
		}
		tab5_content = (LinearLayout) this.findViewById(R.id.tab5);
		if(tab5_content==null)
		{
			Log.e(tag,"tab1_content is null");
			return;
		}
		//head_picture_layout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.upload_head_picture_layout, null);
		head_picture_layout = (LinearLayout) this.findViewById(R.id.head_picture_layout);
		if(head_picture_layout==null)
		{
			Log.e(tag,"head_picture_layout is null");
			return;
		}
		takephoto_button = (TextView) this.findViewById(R.id.takephoto_button);
		localpicture_button = (TextView) this.findViewById(R.id.localpicture_button);
		personality_head_picture = (ImageView) personality_content_layout.findViewById(R.id.personality_head_picture);
		account_username = (TextView) personality_content_layout.findViewById(R.id.account_username);
		account_exit = (TextView) personality_content_layout.findViewById(R.id.account_exit);
		myfocus_button = (LinearLayout) personality_content_layout.findViewById(R.id.myfocus_button);
		focusme_button = (LinearLayout) personality_content_layout.findViewById(R.id.focusme_button);
		message_button = (LinearLayout) personality_content_layout.findViewById(R.id.message_button);
		//��¼״̬�µ���ϴ�ͷ��
		personality_head_picture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isLogin && !willUploadPicture)
				{
					//��ʾ����/����ͼƬ��ť(����)
					willUploadPicture = true;
					Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.upload_picture_show_anim);
					head_picture_layout.setVisibility(View.VISIBLE);
					head_picture_layout.startAnimation(anim);
				}
				else if(!isLogin)
				{
					Toast.makeText(MainActivity.this, "���ȵ�¼���ϴ�ͷ��", Toast.LENGTH_SHORT).show();
				}
			}
		});
		//�����ϴ�ͷ��ť����¼�
		takephoto_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//����ϵͳ�������
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				MainActivity.this.startActivityForResult(intent, 100);
			}
		});
		//����ͼƬ�ϴ�ͷ�����¼�
		localpicture_button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					localpicture_button.setBackground(getResources().getDrawable(R.drawable.upload_picture_bg_sel));
					break;
				case MotionEvent.ACTION_UP:
					localpicture_button.setBackground(getResources().getDrawable(R.drawable.upload_picture_bg_nor));
					//����������(�����ǵ�������)
					Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					MainActivity.this.startActivityForResult(intent,101);
					break;
				}
				return true;
			}
		});
		account_username.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isLogin)
				{
					Intent intent = new Intent(MainActivity.this,LoginActivity.class);
					MainActivity.this.startActivityForResult(intent, 100);
				}
			}
		});
		//�˳��˻���ť
		account_exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(willUploadPicture) return;
				isLogin = false;
				informationList = new ArrayList<InformationItem>();
				sharedPreferences = MainActivity.this.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.putBoolean("isLogin", isLogin);
				editor.commit();
				updateUIAfterExit();
			}
		});
		//�ҹ�ע����
		myfocus_button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(willUploadPicture) return true;
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					myfocus_button.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.item_bg_sel));
					break;
				case MotionEvent.ACTION_UP:
					myfocus_button.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.item_bg_nor));
					if(!isLogin)
					{
						Toast.makeText(MainActivity.this, "���ȵ�¼", Toast.LENGTH_SHORT).show();
					}
					else
					{
						Intent intent = new Intent(MainActivity.this,MyFouseActivity.class);
						intent.putExtra("username", currentAccount.username);
						intent.putExtra("myFouseList", myFouseList);
						MainActivity.this.startActivity(intent);
					}
					break;
				}
				return true;
			}
		});
		//��ע�ҵ���
		focusme_button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(willUploadPicture) return true;
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					focusme_button.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.item_bg_sel));
					break;
				case MotionEvent.ACTION_UP:
					focusme_button.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.item_bg_nor));
					if(!isLogin)
					{
						Toast.makeText(MainActivity.this, "���ȵ�¼", Toast.LENGTH_SHORT).show();
					}
					else
					{
						Intent intent = new Intent(MainActivity.this,FousemeActivity.class);
						intent.putExtra("username", currentAccount.username);
						intent.putExtra("fousemeList", fousemeList);
						MainActivity.this.startActivity(intent);
					}
					break;
				}
				return true;
			}
		});
		//��Ϣ����
		message_button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(willUploadPicture) return true;
				int type = event.getAction();
				switch(type)
				{
				case MotionEvent.ACTION_DOWN:
					message_button.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.item_bg_sel));
					break;
				case MotionEvent.ACTION_UP:
					message_button.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.item_bg_nor));
					if(!isLogin)
					{
						Toast.makeText(MainActivity.this, "���ȵ�¼", Toast.LENGTH_SHORT).show();
					}
					else
					{
						Intent intent = new Intent(MainActivity.this,MessageActivity.class);
						intent.putExtra("username", currentAccount.username);
						MainActivity.this.startActivity(intent);
					}
					break;
				}
				return true;
			}
		});
		personality_content_layout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		tab5_content.addView(personality_content_layout);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("onActivityResult");
		System.out.println("requestCode:"+requestCode);
		System.out.println("resultCode:"+resultCode);
		//�ӵ�¼��ע����淵��
		if((resultCode==2||resultCode==3) && data != null)
		{
			Account account = new Account();
			account.username = data.getStringExtra("username");
			account.password = data.getStringExtra("password");
			account.picturePath = data.getStringExtra("picturepath");
			currentAccount = account;
			isLogin = true;
			sharedPreferences = this.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putBoolean("isLogin", isLogin);
			editor.putString("username", currentAccount.username);
			editor.putString("password", currentAccount.password);
			editor.putString("picturepath", currentAccount.picturePath);
			editor.commit();
			updateUIAfterLogin();
		}
		//��дһд���淵�ص�û����
		else if(resultCode==4)
		{
			tabHost.setCurrentTab(0);
		}
		//��дһд���淵�ض��ұ���
		else if(resultCode==5 && data != null)
		{
			InformationItem item = new InformationItem();
			item.username = currentAccount.username;
			item.picturePath = currentAccount.picturePath;
			item.publishTime = data.getStringExtra("publishTime");
			item.text = data.getStringExtra("text");
			item.fromDevice = data.getStringExtra("fromDevice");
			item.permission = data.getIntExtra("permission", RecordActivity.PERMISSION_PERSONALLY);
			System.out.println("username:"+item.username);
			System.out.println("publishTime:"+item.publishTime);
			System.out.println("fromDevice:"+item.fromDevice);
			System.out.println("text:"+item.text);
			System.out.println("permission:"+item.permission);
			informationList.add(item);//��ӵ���¼�ѱ������Ϣ��
			tab1_content.removeViewAt(1);
			record.setInformationList(informationList);
			record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//��ʾ
			insertToTable(item);//����ӵ���Ϣ���浽���ݿ���
			
			tabHost.setCurrentTab(0);
		}
		//������ճɹ�����
		else if(requestCode == 100 && resultCode == Activity.RESULT_OK && data !=null)
		{
			System.out.println("take photo success");
			boolean isSaveSuccess = FunctionUtil.savePictureToSd((Bitmap)(data.getExtras().get("data")), currentAccount.username);
			if(isSaveSuccess)
			{
				currentAccount.picturePath = "/sdcard/Recorder/HeadPictureFile/head_picture_"+currentAccount.username+".jpg";
				personality_head_picture.setImageBitmap(FunctionUtil.getBitmapByPath(currentAccount.picturePath));
				sharedPreferences = this.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.putString("picturepath", currentAccount.picturePath);
				editor.commit();
				record.updateAccountInfo(currentAccount);//�����ݿ���µ�ǰ�˺���Ϣ
				Toast.makeText(this, "�ϴ�ͷ��ɹ�", Toast.LENGTH_SHORT).show();
				record.setHeadPicture(currentAccount.username,currentAccount.picturePath);
				if(informationList.size()>0)
				{
					tab1_content.removeViewAt(1);
					record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//��ʾ
				}
			}
			else
			{
				Toast.makeText(this, "�ϴ�ͷ��ʧ��", Toast.LENGTH_SHORT).show();
			}
		}
		//����������ɹ�����
		else if(requestCode == 101 && resultCode == Activity.RESULT_OK && data !=null)
		{
			System.out.println("get local photo success");
			boolean isSaveSuccess = FunctionUtil.savePictureToSd(this, data, currentAccount.username);
			if(isSaveSuccess)
			{
				currentAccount.picturePath = "/sdcard/Recorder/HeadPictureFile/head_picture_"+currentAccount.username+".jpg";
				personality_head_picture.setImageBitmap(FunctionUtil.getBitmapByPath(currentAccount.picturePath));
				Editor editor = sharedPreferences.edit();
				editor.putString("picturepath", currentAccount.picturePath);
				editor.commit();
				record.updateAccountInfo(currentAccount);//�����ݿ���µ�ǰ�˺���Ϣ
				Toast.makeText(this, "�ϴ�ͷ��ɹ�", Toast.LENGTH_SHORT).show();
				record.setHeadPicture(currentAccount.username,currentAccount.picturePath);
				if(informationList.size()>0)
				{
					tab1_content.removeViewAt(1);
					record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//��ʾ
				}
			}
			else
			{
				Toast.makeText(this, "�ϴ�ͷ��ʧ��", Toast.LENGTH_SHORT).show();
			}
		}
		//�����ķ��ز���ɾ��
		else if(requestCode == 102 && resultCode == 6 && null != data)
		{
			int position = data.getIntExtra("position", -1);
			if(position==-1) return;
			Log.i(tag,position+"");
			System.out.println(informationList.size());
			String username = informationList.get(position).username;
			String publishTime = informationList.get(position).publishTime;
			informationList.remove(position);//��ָ��λ�õļ�¼ɾ��
			tab1_content.removeViewAt(1);
			record.setInformationList(informationList);
			record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//��ʾ
			setLayoutNoRecord(tab1_content);
			deleteFromTable(username,publishTime);//ɾ��ָ��λ�õ�һ����¼
		}
		//�����ķ��ص�ûɾ��
		else if(requestCode == 102 && resultCode == 7)
		{
			//���·���ʱ��
			if(record!=null) record.updateAllPublishTime();
		}
		//����ӱ�������(��ӳɹ�)
		else if(requestCode == 103 && resultCode == 8 && data != null)
		{
			Toast.makeText(this, "��ӱ����ɹ�", Toast.LENGTH_SHORT).show();
			memorandum.addOneMemorandum(currentAccount.username,(MemorandumItem)data.getSerializableExtra("memorandumitem"));
		}
		//�ӱ������鷵��
		else if(requestCode == 104 && resultCode == 11 && data != null)
		{
			boolean hasRevise = data.getBooleanExtra("hasRevise", false);
			//���������޸�
			if(hasRevise)
			{
				memorandum.handleRevise((ArrayList<MemorandumItem>)data.getSerializableExtra("memorandumlist"));
			}
		}
		//�Ӷ�̬����ҳ����
		else if(requestCode == 200 && resultCode == 12 && data != null)
		{
			boolean hasFouse = data.getBooleanExtra("hasFouse", false);
			String othername = data.getStringExtra("othername");
			String timestamp = data.getStringExtra("timestamp");
			if(hasFouse&&!"".equals(timestamp))
			{
				dynamic.reviseViewByPosition();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/**
	 * �ڵ�¼����²���
	 * */
	private void updateUIAfterLogin()
	{
		Log.i(tag,"login success");
		//��һ��ѡ��
		tab1_content.removeViewAt(1);
		record = new RecordInformation(this, currentAccount.username);
		record.show(tab1_content,RecordInformation.FIRST_LOGIN);//��ʾ
		record.getInformationListFromNetwork(currentAccount.picturePath);//�ӷ�������ȡ��Ϣ��ͬ�����������ݿ�
		//�ڶ���ѡ��
		tab2_content.removeViewAt(1);
		memorandum = new Memorandum(this,currentAccount.username);
		memorandum.show(tab2_content);
		memorandum.getInformationListFromNetwork();//�ӷ�������ȡ������Ϣ��ͬ�����������ݿ�
		//������ѡ��
		tab3_content.removeViewAt(1);
		//���ĸ�ѡ��
		tab4_content.removeViewAt(1);
		dynamic = new Dynamic(this,currentAccount.username);
		dynamic.show(tab4_content);
		//�����ѡ��
		setHeadPicture();//����ͷ��
		account_username.setText(currentAccount.username);//�����û���
		account_exit.setText("�˳�");
		getMyFouseListInfo();//�ӱ������ݿ��ȡ�ҹ�ע�����б���Ϣ
		getFousemeListInfo();//�ӱ������ݿ��ȡ��ע�ҵ����б���Ϣ
	}
	/**
	 * ���˳���¼����²���
	 * */
	private void updateUIAfterExit()
	{
		Log.i(tag,"exit success");
		tab1_content.removeViewAt(1);
		tab1_content.addView(login_register_layout_1);
		tab2_content.removeViewAt(1);
		tab2_content.addView(login_register_layout_2);
		//Log.i(tag,tab3_content.getChildCount()+"");
		//tab3_content.removeViewAt(1);
		tab3_content.addView(login_register_layout_3);
		account_username.setText("���¼");
		tab4_content.removeViewAt(1);
		tab4_content.addView(login_register_layout_4);
		account_exit.setText("");
		personality_head_picture.setImageResource(R.drawable.head_default_picture);
		myFouseList = null;
		fousemeList = null;
	}
	/**
	 * ����һ����¼����ǰ�˺ű���
	 * */
	private void insertToTable(InformationItem item)
	{
		if(record==null) record = new RecordInformation(this,currentAccount.username);
		record.insert(item);
	}
	/**
	 * ɾ��ָ��λ�õ�һ����¼
	 * */
	private void deleteFromTable(String name,String publishTime)
	{
		if(record==null) record = new RecordInformation(this,currentAccount.username);
		record.delete(name,publishTime);
	}
	/**
	 * ���񷵻ؼ�
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(willUploadPicture && keyCode == KeyEvent.KEYCODE_BACK)
		{
			willUploadPicture = false;
			Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.upload_picture_hide_anim);
			head_picture_layout.startAnimation(anim);
			head_picture_layout.setVisibility(View.INVISIBLE);
			return true;//��ʾ�Ѿ���������
		}
		//����໬�˵�����ʾ,ע����isShow()������
		if(slidingMenu.isMenuShowing())
		{
			slidingMenu.showContent();
			return true;//��ʾ�Ѿ���������
		}
		//ʵ���������ΰ����ؼ��˳�����
		long lastTime = System.currentTimeMillis();
		if(lastTime-firstTime>2000)
		{
			System.out.println("firstTime-lastTime:"+(lastTime-firstTime));
			Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
			firstTime = lastTime;
			return true;
		}
		else
		{
			System.out.println("firstTime-lastTime:"+(lastTime-firstTime));
			return super.onKeyDown(keyCode, event);
		}
	}
	/**
	 * ����ͷ��
	 * */
	private void setHeadPicture()
	{
		System.out.println(1);
		if(currentAccount==null) return;
		System.out.println(2);
		System.out.println("picturePath:"+currentAccount.picturePath);
		if("#".equals(currentAccount.picturePath)) return;
		System.out.println(3);
		
		personality_head_picture.setImageBitmap(FunctionUtil.getBitmapByPath(currentAccount.picturePath));
	}
	/**
	 * û�м�¼ʱ���ò���
	 * */
	private void setLayoutNoRecord(LinearLayout tab)
	{
		if(informationList == null) return;
		if(informationList.size()<=0)
		{
			if(tab.getChildCount()>=1)
			{
				LinearLayout linearLayout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.no_record_tip_layout, null);
				linearLayout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
				if(tab.getChildCount()>=2) tab.removeViewAt(1);
				tab.addView(linearLayout);
			}
		}
	}
	/**
	 * �ڵ�¼��ӱ������ݿ��ȡ�ҹ�ע������Ϣ
	 * */
	private void getMyFouseListInfo()
	{
		DatabaseTool dbTool = new DatabaseTool(this,databaseName);
		myFouseList = dbTool.readMyfouseTable(currentAccount.username);
	}
	/**
	 * �ڵ�¼��ӱ������ݿ��ȡ��ע�ҵ�����Ϣ
	 * */
	private void getFousemeListInfo()
	{
		DatabaseTool dbTool = new DatabaseTool(this,databaseName);
		fousemeList = dbTool.readFousemeTable(currentAccount.username);
	}
}
