package com.util;

import java.io.IOException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class HttpUtil {

	public static final String setInitPos = "http://120.25.230.138:8080/set_init_pos?";
	public static final String setEndPos = "http://120.25.230.138:8080/set_end_pos?";
	public static final String setCurrentPos = "http://120.25.230.138:8080/set_current_pos?";

	public static final String getInitPos = "http://120.25.230.138:8080/get_init_pos";
	public static final String getEndPos = "http://120.25.230.138:8080/get_end_pos";
	public static final String getCurrentPos = "http://120.25.230.138:8080/get_current_pos";

	public static OkHttpClient client = new OkHttpClient();

	public static String request_get(String url) {
		// Request request = new Request.Builder().url(url).build();
		// try {
		// Response response = client.newCall(request).execute();
		// return response.body().string();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		 return "";
	}

	public static String X(float x) {
		return "x=" + x + "&";
	}

	public static String Y(float y) {
		return "y=" + y;
	}

	public static Float[] getPosition(String str) {
		// String[] split = str.split(" ");
		// float x = Float.parseFloat(split[0]);
		// float y = Float.parseFloat(split[1]);
		// return new Float[] { x, y };
		return new Float[] { 1.0f, 1.0f };
	}
}
