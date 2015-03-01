package com.mao.myclass;

import java.io.Serializable;

public class MemorandumItem implements Serializable{
	public MyDate date = new MyDate();
	public String text;
}
