package com.mao.myclass;

import java.io.Serializable;

public class MyDate implements Serializable{
	public int[] currentMonthTable = new int[42];
	public int year;
	public int month;
	public int day;
	public int week;//���������ڼ�
	public int weekOfFirstDay;//1�������ڼ�
	public int hour;
	public int minute;
	public int second;
	public long timestamp;
	@Override
	public String toString()
	{
		String hour,minute,second;
		if(this.hour<10) hour = "0"+this.hour;
		else hour = this.hour+"";
		if(this.minute<10) minute = "0"+this.minute;
		else minute = this.minute+"";
		if(this.second<10) second = "0"+this.second;
		else second = this.second+"";
		String result = year+"��"+(month+1)+"��"+day+"��"+" "+hour+":"+minute+":"+second;
		return result;
	}
}
