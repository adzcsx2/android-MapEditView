package com.intelrobotdemo;

public class Utils {
	//两点间距离
	public static double getDistance(float x1,float y1 ,float x2,float y2){
		double dis_x = Math.pow(x1-x2, 2);
		double dis_y = Math.pow(y1-y2, 2);
		return Math.sqrt(dis_x+dis_y);
	}
}
