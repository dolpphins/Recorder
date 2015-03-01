package com.mao.myclass;

import java.io.Serializable;

public class Fouse implements Serializable{
	public String username;
	public String othername;
	public String timeStamp;
	public Fouse(){}
	public Fouse(String username,String othername,String timestamp)
	{
		this.username = username;
		this.othername = othername;
		this.timeStamp = timestamp;
	}
}
