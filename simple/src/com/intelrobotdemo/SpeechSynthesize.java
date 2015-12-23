package com.intelrobotdemo;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class SpeechSynthesize {
	/** 英语 */
	public static final String EN_MAN_Tom = "aisTom";
	public static final String EN_MAN_henry = "henry";
	public static final String EN_WOMAN_Catherine = "aiscatherine";

	/** 男声 */
	public static final String CN_MAN_XIAOYU = "aisxiaoyu";
	public static final String CN_MAN_XUDUO = "aisduoxu";
	public static final String CN_MAN_XUJIU = "aisjiuxu";
	public static final String HENAN_MAN_XIAOKUN = "aisxrong";
	public static final String HUNAN_MAN_XIAOQIANG = "aisxqiang";
	public static final String GUANGDONG_MAN_DALONG = "aisdalong";
	public static final String GUANGDONG_MAN_NIYANG = "niyang";
	/** 女声 */
	public static final String CN_WOMAN_XYAN = "aisxyan";
	public static final String CN_WOMAN_XIAOYAN = "xiaoyan";
	public static final String CN_WOMAN_XIAOQIAN = "aisjinger";
	public static final String CN_WOMAN_XIAOMENG = "aisxmeng";
	public static final String CN_WOMAN_XIAOPING = "aisxping";
	public static final String CN_WOMAN_XIAOAI = "aisxa";
	public static final String CN_WOMAN_XIAOQI = "xiaoqi";
	public static final String CN_WOMAN_ZIQI = "ziqi";
	public static final String CN_WOMAN_YEFANG = "yefang";
	public static final String CN_WOMAN_MENGCHUN = "aismengchun";
	public static final String TAIWAN_WOMAN_XIAOLING = "aisxlin";
	public static final String DONGBEI_WOMAN_XIAOQIAN = "aisxqian";
	public static final String SICHUAN_WOMAN_XIAOLING = "aisxrong";
	public static final String GUANGDONG_WOMAN_XIAOMEI = "aisxmei";
	/** 童声 */
	public static final String CN_YOUNG_XIAOBAO = "aisBABYXu";
	public static final String CN_YOUNG_NANNAN = "aisnn";

	/** 卡通 */
	public static final String CN_CARTOON_TANGLAOYA = "aisDuck";
	public static final String CN_CARTOON_XIAOXIN = "aisxxin";

	/** 发音语速 */
	public static final String VOICERSPEED_SLOWEST = "25";
	public static final String VOICERSPEED_SLOW = "35";
	public static final String VOICERSPEED_NORMAL = "45";
	public static final String VOICERSPEED_FAST = "55";
	public static final String VOICERSPEED_FASTEST = "65";

	/** 发音语调 */
	public static final String VOICEPITCH_SLOWEST = "45";
	public static final String VOICEPITCH_SLOW = "55";
	public static final String VOICEPITCH_NORMAL = "65";
	public static final String VOICEPITCH_FAST = "75";
	public static final String VOICEPITCH_FASTEST = "85";

	protected static final String TAG = "SpeechSynthesize";
	private Context context;
	// 语音合成对象
	private SpeechSynthesizer mTts;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// 默认云端发音人
	public static String voicerCloud = CN_YOUNG_NANNAN;
	// 发音语速
	public static String voicerSpeed = VOICERSPEED_NORMAL;
	// 发音语调
	public static String voicerPitch = VOICEPITCH_NORMAL;
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	public OnSpeechFinish callback;

	private Handler imageHandler;

	private static SpeechSynthesize speechSynthesize;

	private boolean isChangeface = true;

	private SpeechSynthesize(Context context) {
		super();
		this.context = context;
		mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
		mSharedPreferences = context.getSharedPreferences(
				TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		setParam();
	}

	public static SpeechSynthesize getInstance(Context context) {
		if (speechSynthesize == null) {
			synchronized (SpeechSynthesize.class) {
				if (speechSynthesize == null) {
					speechSynthesize = new SpeechSynthesize(context);
				}
			}
		}
		return speechSynthesize;
	}

	/**
	 * 用于获取SpeechSynthesize实例，但并不创建实例
	 * 
	 * @return
	 */
	public static SpeechSynthesize getInstance() {
		return speechSynthesize;
	}

	public void setHandler(Handler handler) {
		imageHandler = handler;
	}

	/**
	 * 
	 * @param content
	 *            合成内容
	 * @param callback
	 *            合成完毕回调(无error返回"",有error返回errorcode)
	 * @return
	 */
	public int speak(String content, OnSpeechFinish callback) {
		this.callback = callback;
		if (mTts.isSpeaking()) {
			mTts.stopSpeaking();
		}
		isChangeface = true;
		int code = mTts.startSpeaking(content, mTtsListener);
		return code;
	}

	/***
	 * 不改变脸的speak方法
	 * 
	 * @param content
	 * @param callback
	 * @param isChangeface
	 * @return
	 */
	public int speak(String content, OnSpeechFinish callback,
			boolean isChangeface) {
		this.callback = callback;
		this.isChangeface = isChangeface;
		if (mTts.isSpeaking()) {
			mTts.stopSpeaking();
		}
		int code = mTts.startSpeaking(content, mTtsListener);
		return code;
	}

	public void stopSpeaking() {
		mTts.stopSpeaking();
	}

	public boolean isSpeaking() {
		return mTts.isSpeaking();
	}

	public void pauseSpeaking() {
		mTts.pauseSpeaking();
	}

	public void restartSpeaking() {
		mTts.resumeSpeaking();
	}

	/**
	 * 
	 * @param voicerName
	 *            发音人，详见本类静态参数
	 */
	public void setVoiceCloud(String voicerName) {
		this.voicerCloud = voicerName;
		if (mTts != null)
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
	}

	/**
	 * 
	 * @param voicerSpeed
	 *            发音语速，详见本类静态参数
	 */
	public void setVoicerSpeed(String voicerSpeed) {
		this.voicerSpeed = voicerSpeed;
		if (mTts != null)
			mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences
					.getString("speed_preference", voicerSpeed));
	}

	/**
	 * 
	 * @param voicerPitch
	 *            发音音调高低，详见本类静态参数
	 */
	public void setVoicerPitch(String voicerPitch) {
		this.voicerPitch = voicerPitch;
		if (mTts != null)
			mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences
					.getString("pitch_preference", voicerPitch));
	}

	/**
	 * 默认的参数设置
	 * 
	 * @return
	 */
	private void setParam() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 设置使用云端引擎
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED,
				mSharedPreferences.getString("speed_preference", voicerSpeed));

		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH,
				mSharedPreferences.getString("pitch_preference", voicerPitch));

		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME,
				mSharedPreferences.getString("volume_preference", "100"));

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE,
				mSharedPreferences.getString("stream_preference", "3"));
		// 播放的时候降低背景音
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "1");
		 //合成路径
		 mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
		 Environment.getExternalStorageDirectory().getAbsolutePath()+"/aaa.wav");

	}

	/** 设置参数的扩展 */
	public void setParams(String parameter, String value) {
		mTts.setParameter(parameter, value);
	}

	/** 获取参数 */
	public String getParams(String parameter){
		return mTts.getParameter(parameter);
	}

	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				Log.e(TAG, "初始化失败,错误码：" + code);
			}
		}
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			if (isChangeface) {
				isChangeface = true;
			}
		}

		@Override
		public void onSpeakPaused() {
		}

		@Override
		public void onSpeakResumed() {
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {

		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
		}

		@Override
		public void onCompleted(SpeechError error) {


			if (callback == null) {
				return;
			}
			if (error == null) {
				callback.onSuccssFinish();
			} else if (error != null) {
				callback.onErrorFinish(error.getErrorCode() + "");
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub

		}
	};

	public void updateFace(int state) {
		if (imageHandler == null) {
			return;
		}
		imageHandler.sendEmptyMessage(state);
	}

	public interface OnSpeechFinish {
		void onSuccssFinish();

		void onErrorFinish(String error);

		void onBegin();
	}

	public void destroy() {
		if (mTts != null) {
			mTts.stopSpeaking();
			mTts.destroy();
		}
	}
}
