package com.pl.app.utils;

import android.text.TextUtils;
import android.util.Log;

import com.pl.app.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import static java.lang.String.format;

/**
 * class to get Logs
 */
public class LogUtil {
	public static final String TAG = "Li.log";
	private static final String log2Name = "AVLog2.dat";
	private static String log2Dir = null;
	private static final String CONJUNCTOR = "_";
	private static final String SPERATOR = "\t";


	private static String generateTag() {
		StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
		String tag = "%s.%s(%s.java:%d):";
		String callerClazzName = caller.getClassName();
		callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
		tag = format(tag, callerClazzName, caller.getMethodName(), callerClazzName,caller.getLineNumber());
		return tag;
	}

	public static void d(String tag, String msg) {
		if (BuildConfig.DEBUG)
			// Log.d(TextUtils.isEmpty(tag) ? TAG : tag,
			// TextUtils.isEmpty(msg) ? "" : msg);
			Log.d(TAG, (TextUtils.isEmpty(tag) ? "-->" : "-->" + tag + "-->")
					+ msg);
	}
	public static void d(String msg) {
		if (BuildConfig.DEBUG)
			// Log.d(TextUtils.isEmpty(tag) ? TAG : tag,
			// TextUtils.isEmpty(msg) ? "" : msg);
			Log.d(TAG, generateTag()+ msg);
	}
	public static void e(String msg) {
		if (BuildConfig.DEBUG)
			Log.e(TAG,  generateTag()+ msg);
	}

	public static void e(String tag, String msg) {
		if (BuildConfig.DEBUG)
			Log.e(TAG, (TextUtils.isEmpty(tag) ? "" : "-->" + tag + "-->")
					+ msg);
	}

	public static void e(String tag, String msg, Throwable e) {
		if (BuildConfig.DEBUG)
			Log.e(TAG, (TextUtils.isEmpty(tag) ? "" : "-->" + tag + "-->")
					+ msg + "-->" + e);
	}

	public static void i(String msg) {
		if (BuildConfig.DEBUG)
			Log.i(TAG, generateTag()+ msg);
	}

	public static void i(String tag, String msg) {
		if (BuildConfig.DEBUG)
			Log.i(TAG, (TextUtils.isEmpty(tag) ? "" : "-->" + tag + "-->")
					+ msg);
	}

	public static void w(String msg) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, generateTag() + msg);
	}

	public static void w(String tag, String msg) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, (TextUtils.isEmpty(tag) ? "" : "-->" + tag + "-->")
					+ msg);
	}

	public static void e(Exception e) {
		if (BuildConfig.DEBUG) {
			e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}

	public static void d(Throwable e) {
		if (BuildConfig.DEBUG) {
			d(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void writeLog(String aArg1) {
		String[] stringArray = new String[] { aArg1, getCurrentTimeString() };
		writeLog(stringArray);
	}

	public static void writeLog(String aArg1, int aArg2) {
		writeLog(aArg1, String.valueOf(aArg2));
	}

	public static void writeLog(String aArg1, String[] aArg2) {
		final String[] stringArray = new String[aArg2.length + 2];
		stringArray[0] = aArg1;
		stringArray[1] = getCurrentTimeString();
		for (int i = 0; i < aArg2.length; i++) {
			stringArray[i + 2] = aArg2[i];
		}
		new Thread() {
			@Override
			public void run() {
				writeLog(stringArray);
			}
		}.start();

	}

	private static void writeLog(String aArg1, String aArg2) {
		try {
			String[] stringArray = new String[] { aArg1,
					getCurrentTimeString(), aArg2 };
			writeLog(stringArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeLog(String aArg1, int aArg2, int aArg3) {
		writeLog(aArg1, String.valueOf(aArg2), String.valueOf(aArg3));
	}

	private static void writeLog(String aArg1, String aArg2, String aArg3) {
		String[] stringArray = new String[] { aArg1, getCurrentTimeString(),
				aArg2, aArg3 };
		writeLog(stringArray);
	}

	public static void writeLog(String aArg1, String aArg2, String aArg3,
								String aArg4) {
		String[] stringArray = new String[] { aArg1, getCurrentTimeString(),
				aArg2, aArg3, aArg4 };
		writeLog(stringArray);
	}

	public static void writeLog(String aArg1, String aArg2, String aArg3,
								String aArg4, String aArg5) {
		String[] stringArray = new String[] { aArg1, getCurrentTimeString(),
				aArg2, aArg3, aArg4, aArg5 };
		writeLog(stringArray);
	}

	public static void writeLog(String aArg1, String aArg2, String aArg3,
								String aArg4, String aArg5, String aArg6) {
		String[] stringArray = new String[] { aArg1, getCurrentTimeString(),
				aArg2, aArg3, aArg4, aArg5, aArg6 };
		writeLog(stringArray);
	}

	public static void writeLog(String aArg1, String aArg2, String aArg3,
								String aArg4, String aArg5, String aArg6, String aArg7) {
		String[] stringArray = new String[] { aArg1, getCurrentTimeString(),
				aArg2, aArg3, aArg4, aArg5, aArg6, aArg7 };
		writeLog(stringArray);
	}

	private static void writeLog(String[] stringArray) {
		if (!BuildConfig.DEBUG) {
			return;
		}
		try {
			File fileDir = new File(log2Dir);
			int action = -1; // -1表示不是第一行的log，0新用户创建文件, 1创建了新的文件 2 删除旧文件创建新文件
			boolean bLogDirExist = true;
			if (!fileDir.exists()) {
				fileDir.mkdirs();
				bLogDirExist = false;
			}

			File file = new File(log2Dir + "/" + log2Name);
			if (!file.exists()) {
				file.createNewFile();
				if (bLogDirExist) {
					action = 1;
				} else {
					action = 0;
				}
			} else if (file.length() >= 32 * 1024 || file.length() < 0) {
				// file.delete();
				// file.createNewFile();
				action = 2;
			}

			FileOutputStream fout = new FileOutputStream(file, true);
			if (action != -1) {
				String item = String.valueOf(action) + CONJUNCTOR
						+ getCurrentTimeString() +  "\t";
				fout.write(item.getBytes());
			}
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < stringArray.length; i++) {
				builder.append(stringArray[i]);
				if (i != stringArray.length - 1) {
					builder.append(CONJUNCTOR);
				} else {
					builder.append("\t");
				}
			}
			byte[] bytes = builder.toString().getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getCurrentTimeString() {

		Calendar calendar = Calendar.getInstance();

		// Month
		int m = calendar.get(Calendar.MONTH) + 1;
		String month = "";
		if (m >= 10)
			month = "" + m;
		else
			month = "0" + m;

		// Day
		int d = calendar.get(Calendar.DAY_OF_MONTH);
		String day = "";
		if (d >= 10)
			day += d;
		else
			day = "0" + d;

		// Hour
		String hour = "";
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		if (h < 10) {
			hour = "0" + h;
		} else {
			hour = "" + h;
		}

		// Minute
		String minite = "";
		int mi = calendar.get(Calendar.MINUTE);
		if (mi < 10) {
			minite = "0" + mi;
		} else {
			minite = "" + mi;
		}

		// Second
		String second = "";
		int sec = calendar.get(Calendar.SECOND);
		if (sec < 10) {
			second = "0" + sec;
		} else {
			second = "" + sec;
		}

		String s = calendar.get(Calendar.YEAR) + month + day + hour + minite
				+ second;
		if (s.length() >= 10) {
			s = s.substring(2);
		}
		return s;
	}

}
