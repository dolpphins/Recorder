package com.mao.myclass;

import java.io.Serializable;


/**
 * ��Ϣ���࣬����ÿ����¼
 * */
@SuppressWarnings("serial")
public class InformationItem implements Serializable{
	public String picturePath="#";//ͷ��ͼƬ·����#��ʾʹ��Ĭ��ͼƬ
	public String username;//�û���
	public String publishTime;//����ʱ��
	public String fromDevice;//�����豸
	public String text;//�ı�
	public int permission;//Ȩ��
}
