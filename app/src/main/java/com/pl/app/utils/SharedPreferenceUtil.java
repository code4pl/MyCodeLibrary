package com.pl.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Set;

public class SharedPreferenceUtil {
	private static final String SHARED_PREFERENCE_NAME = "ali_sp";
	public static final String VERSION_CODE = "app_version_code";

	public static void putString(Context context, String key, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.putString(key, value);
		editor.apply();// 提交修改
	}

	public static void putInt(Context context, String key, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.putInt(key, value);
		editor.apply();// 提交修改
	}

	public static void putBoolean(Context context, String key, boolean value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.putBoolean(key, value);
		editor.apply();// 提交修改
	}
	
	public static void putLong(Context context, String key, long value){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.putLong(key, value);
		editor.apply();// 提交修改
	}

	public static String getString(Context context, String key,
								   String defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, defaultValue);
	}

	public static int getInt(Context context, String key, int defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(key, defaultValue);
	}

	public static boolean getBoolean(Context context, String key,
									 boolean defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(key, defaultValue);
	}
	
	public static Long getLong(Context context, String key,
							   long defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getLong(key, defaultValue);
	}

	public static Set<String> getStringSet(Context context, String key){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
			SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getStringSet(key, null);
	}

	public static void putStringSet(Context context, String key, Set<String> value){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
			SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.putStringSet(key, value);
		editor.commit();// 提交修改
	}
	
}
