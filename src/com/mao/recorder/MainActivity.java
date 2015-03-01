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
	private final static String databaseName = "recorder.db";//数据库名
	final private String sharedPreferencesName = "account.txt";
	
	private TabHost tabHost;//选项卡
	String[] tabLabels;//选项卡文字
	Drawable[] tabNormalDrawales;//选项卡未被选中时图标
	Drawable[] tabSelDrawales;//选项卡被选中时图标
	int[] tabLayoutIds;//选项卡对应的内容区域的view的id
	int[] tabLayoutLayouts;//选项卡每个选项的布局
	LinearLayout[] tabViews;//选项卡每个选项的view（自定义）
	int[] tabImageIds;//选项卡每个选项的图标id
	ImageView[] imageViews;//记录每个图标引用
	TextView[] textViews;//记录每个选项文字引用
	int[] tabTextIds;//选项卡每个选项的文字id
	int currentSelTab = 0;//记录当前选中哪个tab
	
	private static int screenWidth;//保存屏幕宽度
	private static int screenHeight;//保存屏幕高度
	
	private SlidingMenu slidingMenu;//侧滑菜单(右边)
	private ImageView show_slidingmenu;//点击后显示右侧滑菜单图标
	private RelativeLayout menu_settings;//设置
	private RelativeLayout menu_update;//版本更新
	private RelativeLayout menu_share;//软件分享
	private RelativeLayout menu_help;//使用帮助
	private RelativeLayout menu_about;//关于我们
	
	private LinearLayout login_register_layout_1;//第一个选项内容区域登录注册布局
	private LinearLayout login_register_layout_2;//第二个选项内容区域登录注册布局
	private LinearLayout login_register_layout_3;//第三个选项内容区域登录注册布局
	private LinearLayout login_register_layout_4;//第四个选项内容区域登录注册布局
	private LinearLayout personality_content_layout;//第五个内容区域布局
	private LinearLayout head_picture_layout;//上传头像布局
	private LinearLayout tab1_content;//第一个选项布局
	private LinearLayout tab2_content;//第二个选项布局
	private LinearLayout tab3_content;//第三个选项布局
	private LinearLayout tab4_content;//第四个选项布局
	private LinearLayout tab5_content;//第五个选项布局
	private TextView login_1;//登录按钮
	private TextView register_1;//注册按钮
	private TextView login_2;//登录按钮
	private TextView register_2;//注册按钮
	private TextView login_3;//登录按钮
	private TextView register_3;//注册按钮
	private TextView login_4;//登录按钮
	private TextView register_4;//注册按钮
	
	private ImageView personality_head_picture;//头像
	private boolean willUploadPicture = false;//标记是否显示上传头像按钮
	private TextView takephoto_button;//拍照上传头像按钮
	private TextView localpicture_button;//本地图片上传头像按钮
	private TextView account_username;//个人中心用户名
	private TextView account_exit;//个人中心退出 
	private LinearLayout myfocus_button;//我关注的人
	private LinearLayout focusme_button;//关注我的人
	private LinearLayout message_button;//消息中心
	
	private Account currentAccount;//保存当前登录账号信息
	private boolean isLogin = false;//记录是否已登录
	private SharedPreferences sharedPreferences;//保存一些信息
	public static ArrayList<InformationItem> informationList = new ArrayList<InformationItem>();//保存当前账号记录的信息
	private ArrayList<Fouse> myFouseList = null;//在登录后保存我关注的人信息
	private ArrayList<Fouse> fousemeList = null;//在登录后保存关注我的人信息
	
	private Dynamic dynamic;//发现选项布局
	private RecordInformation record;//今日记选项布局
	private Memorandum memorandum;//备忘录选项布局
	
	private long firstTime = 0;//用于判断是否连续两次按返回键退出程序
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//设置布局
		setContentView(R.layout.activity_main);
		//获取屏幕大小
		getScreenSize();
		//初始化一些变量
		initConstant();
		//初始化选项卡布局
		initTab();
		//初始化布局(侧滑菜单等)
		initLayout();
		//得到账号是否登录信息
		getAccountInfo();
		
		drawLayoutNoLogin();//绘制没有登录状态下特定布局
		if(isLogin) updateUIAfterLogin();//如果登录了修改布局
		NetworkStateReceiver.initNetworkState(this);//初始化网络状态
	
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
     * 获取屏幕大小
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
	 * 初始化一些变量
	 * */
	public void initConstant()
	{
		//选项卡字符串
		tabLabels=new String[5];
		tabLabels[0]=getResources().getString(R.string.today);
		tabLabels[1]=getResources().getString(R.string.memorandum);
		tabLabels[2]=getResources().getString(R.string.write);
		tabLabels[3]=getResources().getString(R.string.dynamic);
		tabLabels[4]=getResources().getString(R.string.personality);
		//选项卡未被选中时图标
		tabNormalDrawales = new Drawable[5];
		tabNormalDrawales[0] = getResources().getDrawable(R.drawable.icon_today_nor);
		tabNormalDrawales[1] = getResources().getDrawable(R.drawable.icon_memorandum_nor);
		tabNormalDrawales[2] = getResources().getDrawable(R.drawable.icon_edit);
		tabNormalDrawales[3] = getResources().getDrawable(R.drawable.icon_dynamic_nor);
		tabNormalDrawales[4] = getResources().getDrawable(R.drawable.icon_personality_nor);
		//选项卡被选中时图标
		tabSelDrawales = new Drawable[5];
		tabSelDrawales[0] = getResources().getDrawable(R.drawable.icon_today_sel);
		tabSelDrawales[1] = getResources().getDrawable(R.drawable.icon_memorandum_sel);
		tabSelDrawales[2] = getResources().getDrawable(R.drawable.icon_edit);
		tabSelDrawales[3] = getResources().getDrawable(R.drawable.icon_dynamic_sel);
		tabSelDrawales[4] = getResources().getDrawable(R.drawable.icon_personality_sel);
		//选项卡id
		tabLayoutIds = new int[5];
		tabLayoutIds[0]=R.id.tab1;
		tabLayoutIds[1]=R.id.tab2;
		tabLayoutIds[2]=R.id.tab3;
		tabLayoutIds[3]=R.id.tab4;
		tabLayoutIds[4]=R.id.tab5;
		//选项卡每个选项的布局
		tabLayoutLayouts = new int[5];
		tabLayoutLayouts[0] = R.layout.today_layout;
		tabLayoutLayouts[1] = R.layout.memorandum_layout;
		tabLayoutLayouts[2] = R.layout.write_layout;
		tabLayoutLayouts[3] = R.layout.dynamic_layout;
		tabLayoutLayouts[4] = R.layout.personality_layout;
		//选项卡每个选项的图标id
		tabImageIds = new int[5];
		tabImageIds[0] = R.id.icon_today;
		tabImageIds[1] = R.id.icon_memorandum;
		tabImageIds[2] = R.id.icon_edit;
		tabImageIds[3] = R.id.icon_dynamic;
		tabImageIds[4] = R.id.icon_personality;
		//选项卡每个选项的文字id
		tabTextIds = new int[5];
		tabTextIds[0] = R.id.text_today;
		tabTextIds[1] = R.id.text_memorandum;
		tabTextIds[2] = R.id.text_write;
		tabTextIds[3] = R.id.text_dynamic;
		tabTextIds[4] = R.id.text_personality;
		//选项卡view
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
			//实例化一个ImageView对象
			ImageView image = new ImageView(this);
			//设置布局参数
			image.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			//实例化一个TextView对象
			TextView text = new TextView(this);
			//设置布局参数
			text.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			
			image.setBackground(tabDrawales[i]);
			text.setText(tabLabels[i]);
			linearLayout.addView(image,0);
			linearLayout.addView(text,1);
			
			//将线性布局加到相对布局中，居中
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			tabViews[i].addView(linearLayout);*/
		}
	}
	/**
	 * 初始化选项卡布局
	 * */
	public void initTab()
	{
		if(tabLabels==null||tabLayoutIds==null||tabNormalDrawales==null)
		{
			Log.e(tag, "tableLabels , tabLayoutIds or tabDrawales is null,can't init TabHost");
			return;
		}
		tabHost = (TabHost) this.findViewById(R.id.tabhost);
		tabHost.setup();//实例化tabWidget和tabContent
		//添加选项
		for(int i=0;i<tabLabels.length;i++)
		{
			tabHost.addTab(tabHost.newTabSpec(tabLabels[i]).setIndicator(tabViews[i]).setContent(tabLayoutIds[i]));
		}
		//默认第一个选项选中
		imageViews[currentSelTab].setBackground(tabSelDrawales[currentSelTab]);
		tabHost.setCurrentTab(currentSelTab);
		//注册点击事件
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
				//点击事件
				if(tabId.equals("写一写"))
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
	 * 初始化布局(侧滑菜单、点击事件等)
	 * */
	public void initLayout()
	{
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setMode(SlidingMenu.RIGHT);
		//slidingMenu.setShadowWidth(500);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setBehindWidth(7*screenWidth/10);//设置SlidingMenu菜单的宽度
		slidingMenu.setFadeDegree(0.2f);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//必须要调用
		slidingMenu.setMenu(R.layout.slidingmenu_layout);//侧滑菜单布局
		show_slidingmenu = (ImageView) this.findViewById(R.id.show_slidingmenu);
		if(show_slidingmenu==null)
		{
			Log.e(tag,"can't init slidingmenu");
			return;
		}
		//设置点击事件
		show_slidingmenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				slidingMenu.showMenu();//显示侧滑菜单
			}
		});
		// 初始化侧滑菜单点击事件
		LinearLayout slidingMenuLayout = (LinearLayout) slidingMenu.getMenu();
		menu_settings = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_settings);
		menu_update = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_update);
		menu_share = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_share);
		menu_help = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_help);
		menu_about = (RelativeLayout) slidingMenuLayout.findViewById(R.id.menu_about);
		//设置
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
		//版本更新
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
		//软件分享
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
			        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");   
			        intent.putExtra(Intent.EXTRA_TEXT, "嘿!我正在使用一款很好用的软件【随身记】,快去下载吧!");    
			        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
			        startActivity(Intent.createChooser(intent, getTitle()));
					break;
				}
				return true;
			}
		});
		//使用帮助
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
		//关于我们
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
	 * 得到账号是否登录信息
	 * */
	public void getAccountInfo()
	{
		if(currentAccount==null) currentAccount = new Account();
		sharedPreferences = this.getSharedPreferences(sharedPreferencesName,Context.MODE_PRIVATE);
		isLogin = sharedPreferences.getBoolean("isLogin",false);
		//如果已登录
		if(isLogin)
		{
			//读取用户名、密码、头像路径等信息
			currentAccount.username = sharedPreferences.getString("username", null);
			currentAccount.password = sharedPreferences.getString("password", null);
			currentAccount.picturePath = sharedPreferences.getString("picturepath", "#");
		}
	}
	/**
	 * 绘制那些在没有登录状态下的布局
	 * */
	public void drawLayoutNoLogin()
	{
		//第一个选项内容区域
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
		//登录按钮点击事件
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
		//注册按钮点击事件
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
		//第二个选项内容区域
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
		//登录按钮点击事件
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
		//注册按钮点击事件
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
		//第三个选项内容(写一写)
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
		//登录按钮点击事件
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
		//注册按钮点击事件
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
		//第四个选项内容(发现)
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
		//登录按钮点击事件
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
		//注册按钮点击事件
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
		//第五个选项内容(个人中心)
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
		//登录状态下点击上传头像
		personality_head_picture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isLogin && !willUploadPicture)
				{
					//显示拍照/本地图片按钮(动画)
					willUploadPicture = true;
					Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.upload_picture_show_anim);
					head_picture_layout.setVisibility(View.VISIBLE);
					head_picture_layout.startAnimation(anim);
				}
				else if(!isLogin)
				{
					Toast.makeText(MainActivity.this, "请先登录再上传头像", Toast.LENGTH_SHORT).show();
				}
			}
		});
		//拍照上传头像按钮点击事件
		takephoto_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//调用系统相机拍照
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				MainActivity.this.startActivityForResult(intent, 100);
			}
		});
		//本地图片上传头像点击事件
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
					//调用相册程序(可能是第三方的)
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
		//退出账户按钮
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
		//我关注的人
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
						Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
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
		//关注我的人
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
						Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
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
		//消息中心
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
						Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
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
		//从登录或注册界面返回
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
		//从写一写界面返回但没保存
		else if(resultCode==4)
		{
			tabHost.setCurrentTab(0);
		}
		//从写一写界面返回而且保存
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
			informationList.add(item);//添加到记录已保存的信息中
			tab1_content.removeViewAt(1);
			record.setInformationList(informationList);
			record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//显示
			insertToTable(item);//将添加的信息保存到数据库中
			
			tabHost.setCurrentTab(0);
		}
		//相机拍照成功返回
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
				record.updateAccountInfo(currentAccount);//在数据库更新当前账号信息
				Toast.makeText(this, "上传头像成功", Toast.LENGTH_SHORT).show();
				record.setHeadPicture(currentAccount.username,currentAccount.picturePath);
				if(informationList.size()>0)
				{
					tab1_content.removeViewAt(1);
					record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//显示
				}
			}
			else
			{
				Toast.makeText(this, "上传头像失败", Toast.LENGTH_SHORT).show();
			}
		}
		//调用相册程序成功返回
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
				record.updateAccountInfo(currentAccount);//在数据库更新当前账号信息
				Toast.makeText(this, "上传头像成功", Toast.LENGTH_SHORT).show();
				record.setHeadPicture(currentAccount.username,currentAccount.picturePath);
				if(informationList.size()>0)
				{
					tab1_content.removeViewAt(1);
					record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//显示
				}
			}
			else
			{
				Toast.makeText(this, "上传头像失败", Toast.LENGTH_SHORT).show();
			}
		}
		//从正文返回并且删除
		else if(requestCode == 102 && resultCode == 6 && null != data)
		{
			int position = data.getIntExtra("position", -1);
			if(position==-1) return;
			Log.i(tag,position+"");
			System.out.println(informationList.size());
			String username = informationList.get(position).username;
			String publishTime = informationList.get(position).publishTime;
			informationList.remove(position);//将指定位置的记录删除
			tab1_content.removeViewAt(1);
			record.setInformationList(informationList);
			record.show(tab1_content,RecordInformation.NOT_FIRST_LOGIN);//显示
			setLayoutNoRecord(tab1_content);
			deleteFromTable(username,publishTime);//删除指定位置的一条记录
		}
		//从正文返回但没删除
		else if(requestCode == 102 && resultCode == 7)
		{
			//更新发布时间
			if(record!=null) record.updateAllPublishTime();
		}
		//从添加备忘返回(添加成功)
		else if(requestCode == 103 && resultCode == 8 && data != null)
		{
			Toast.makeText(this, "添加备忘成功", Toast.LENGTH_SHORT).show();
			memorandum.addOneMemorandum(currentAccount.username,(MemorandumItem)data.getSerializableExtra("memorandumitem"));
		}
		//从备忘详情返回
		else if(requestCode == 104 && resultCode == 11 && data != null)
		{
			boolean hasRevise = data.getBooleanExtra("hasRevise", false);
			//备忘发生修改
			if(hasRevise)
			{
				memorandum.handleRevise((ArrayList<MemorandumItem>)data.getSerializableExtra("memorandumlist"));
			}
		}
		//从动态详情页返回
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
	 * 在登录后更新布局
	 * */
	private void updateUIAfterLogin()
	{
		Log.i(tag,"login success");
		//第一个选项
		tab1_content.removeViewAt(1);
		record = new RecordInformation(this, currentAccount.username);
		record.show(tab1_content,RecordInformation.FIRST_LOGIN);//显示
		record.getInformationListFromNetwork(currentAccount.picturePath);//从服务器获取信息并同步到本地数据库
		//第二个选项
		tab2_content.removeViewAt(1);
		memorandum = new Memorandum(this,currentAccount.username);
		memorandum.show(tab2_content);
		memorandum.getInformationListFromNetwork();//从服务器获取备份信息并同步到本地数据库
		//第三个选项
		tab3_content.removeViewAt(1);
		//第四个选项
		tab4_content.removeViewAt(1);
		dynamic = new Dynamic(this,currentAccount.username);
		dynamic.show(tab4_content);
		//第五个选项
		setHeadPicture();//设置头像
		account_username.setText(currentAccount.username);//设置用户名
		account_exit.setText("退出");
		getMyFouseListInfo();//从本地数据库获取我关注的人列表信息
		getFousemeListInfo();//从本地数据库获取关注我的人列表信息
	}
	/**
	 * 在退出登录后更新布局
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
		account_username.setText("请登录");
		tab4_content.removeViewAt(1);
		tab4_content.addView(login_register_layout_4);
		account_exit.setText("");
		personality_head_picture.setImageResource(R.drawable.head_default_picture);
		myFouseList = null;
		fousemeList = null;
	}
	/**
	 * 插入一条记录到当前账号表中
	 * */
	private void insertToTable(InformationItem item)
	{
		if(record==null) record = new RecordInformation(this,currentAccount.username);
		record.insert(item);
	}
	/**
	 * 删除指定位置的一条记录
	 * */
	private void deleteFromTable(String name,String publishTime)
	{
		if(record==null) record = new RecordInformation(this,currentAccount.username);
		record.delete(name,publishTime);
	}
	/**
	 * 捕获返回键
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(willUploadPicture && keyCode == KeyEvent.KEYCODE_BACK)
		{
			willUploadPicture = false;
			Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.upload_picture_hide_anim);
			head_picture_layout.startAnimation(anim);
			head_picture_layout.setVisibility(View.INVISIBLE);
			return true;//表示已经处理完了
		}
		//如果侧滑菜单被显示,注意与isShow()的区别
		if(slidingMenu.isMenuShowing())
		{
			slidingMenu.showContent();
			return true;//表示已经处理完了
		}
		//实现连续两次按返回键退出程序
		long lastTime = System.currentTimeMillis();
		if(lastTime-firstTime>2000)
		{
			System.out.println("firstTime-lastTime:"+(lastTime-firstTime));
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
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
	 * 设置头像
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
	 * 没有记录时设置布局
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
	 * 在登录后从本地数据库获取我关注的人信息
	 * */
	private void getMyFouseListInfo()
	{
		DatabaseTool dbTool = new DatabaseTool(this,databaseName);
		myFouseList = dbTool.readMyfouseTable(currentAccount.username);
	}
	/**
	 * 在登录后从本地数据库获取关注我的人信息
	 * */
	private void getFousemeListInfo()
	{
		DatabaseTool dbTool = new DatabaseTool(this,databaseName);
		fousemeList = dbTool.readFousemeTable(currentAccount.username);
	}
}
