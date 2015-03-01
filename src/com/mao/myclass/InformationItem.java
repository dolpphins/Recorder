package com.mao.myclass;

import java.io.Serializable;


/**
 * 信息项类，代表每条记录
 * */
@SuppressWarnings("serial")
public class InformationItem implements Serializable{
	public String picturePath="#";//头像图片路径，#表示使用默认图片
	public String username;//用户名
	public String publishTime;//发布时间
	public String fromDevice;//发布设备
	public String text;//文本
	public int permission;//权限
}
