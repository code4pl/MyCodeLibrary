package com.pl.app.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.LauncherActivity;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Telephony;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.pl.app.BuildConfig;
import com.pl.app.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author wlq
 */
public class CommonUtil {
    public static final String TAG = "daemon";
    public static final String ASSERTS_uninstallDaemon = "daemon";
    // public static final String ASSERTS_uninstallDaemon= "supervisor";
    private static String wallpaperPath = "/PhwLauncher/Phw_Wallpaper";
    private static String shareTempPath = "/PhwLauncher/Phw_Share_Temp";

    //预制配置文件的路径
    private static final String HW_LAUNCHER_DEFAULT_WORKSPACE_FILE_PATH_local = "/hw_launcher_default_workspace.xml";

    public static boolean IS_APP_UPDATE = false;

    /**
     * 是否是新升级的版本
     * @return
     */
    public static boolean isNewUpdate(Context context){
        int lastVersion = SharedPreferenceUtil.getInt(context, SharedPreferenceUtil.VERSION_CODE, 0);
        return BuildConfig.VERSION_CODE > lastVersion && lastVersion != 0;
    }

    /**
     * 判断当前App是否已更新,第一次安装不算版本更新
     *
     * @param context   context
     * @param packageName    the package name
     * @return true if the app updated from old version
     */
    public static boolean hasAppUpdated(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    packageName, 0);
            int currentVersion = info.versionCode;
            int lastVersion = SharedPreferenceUtil.getInt(context,
                    SharedPreferenceUtil.VERSION_CODE, 0);
            if (lastVersion != 0 && currentVersion > lastVersion) {
                return true;
            }
            LogUtil.e(TAG, "lastVersion = " + lastVersion);
        } catch (NameNotFoundException e) {
            LogUtil.e(TAG, e.toString());
        }
        return false;
    }

    /**
     * 判断当前App是否已升级，包括第一次安装的情况
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppUpdated(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    packageName, 0);
            int currentVersion = info.versionCode;
            int lastVersion = SharedPreferenceUtil.getInt(context,
                    SharedPreferenceUtil.VERSION_CODE, 0);
            if (currentVersion > lastVersion) {
                // 1.2(VersionCode=4以及之前的版本文件夹没有多屏，所以升级时要将文件夹里面的
                // 元素重新排列，1.3及以后的版本覆盖升级则不需要重新排列了。
                if (lastVersion != 0 && lastVersion <= 4 && currentVersion >= 5) {
                    IS_APP_UPDATE = true;
                }
                return true;
            }
            LogUtil.d("uninstallDaemon", "currentVersion = " + currentVersion);
            LogUtil.d("uninstallDaemon", "lastVersion = " + lastVersion);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 更新应用的当前版本号到SharedPreference中，这个方法应该只有一个地方使用
     *
     * @param context
     * @param packageName
     */
    public static void updateVersionCodeInSP(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    packageName, 0);
            int currentVersion = info.versionCode;
            SharedPreferenceUtil.putInt(context,
                    SharedPreferenceUtil.VERSION_CODE, currentVersion);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ComponentName getGlobalSearchActivity(Context context) {
        List<ResolveInfo> newGlobalSearchActivities = findGlobalSearchActivities(context);
        return findGlobalSearchActivity(context, newGlobalSearchActivities);
    }

    /**
     * Returns a sorted list of installed search providers as per the following
     * heuristics:
     * <p/>
     * (a) System apps are given priority over non system apps. (b) Among system
     * apps and non system apps, the relative ordering is defined by their
     * declared priority.
     */
    private static List<ResolveInfo> findGlobalSearchActivities(Context context) {
        // Step 1 : Query the package manager for a list
        // of activities that can handle the GLOBAL_SEARCH intent.
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        List<ResolveInfo> activities = context.getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (activities != null && !activities.isEmpty()) {
            // Step 2: Rank matching activities according to our heuristics.
            Collections.sort(activities, GLOBAL_SEARCH_RANKER);
        }

        return activities;
    }

    private static final Comparator<ResolveInfo> GLOBAL_SEARCH_RANKER = new Comparator<ResolveInfo>() {
        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            if (lhs == rhs) {
                return 0;
            }
            boolean lhsSystem = isSystemApp(lhs);
            boolean rhsSystem = isSystemApp(rhs);

            if (lhsSystem && !rhsSystem) {
                return -1;
            } else if (rhsSystem && !lhsSystem) {
                return 1;
            } else {
                // Either both system engines, or both non system
                // engines.
                //
                // Note, this isn't a typo. Higher priority numbers imply
                // higher priority, but are "lower" in the sort order.
                return rhs.priority - lhs.priority;
            }
        }
    };

    /**
     * @return true iff. the resolve info corresponds to a system application.
     */
    private static final boolean isSystemApp(ResolveInfo res) {
        return (res.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * 通过Intent判断是否为系统应用
     *
     * @param intent
     * @return
     */
    public static final boolean isSystemApp(Context context, Intent intent) {
        boolean result = false;
        try {
            ActivityInfo info;
            int queryFlag = PackageManager.MATCH_DEFAULT_ONLY;
            List<ResolveInfo> list = context.getPackageManager()
                    .queryIntentActivities(intent, queryFlag);
            for (ResolveInfo resInfo : list) {
                info = resInfo.activityInfo;
                if (info != null) {
                    ApplicationInfo appInfo = info.applicationInfo;
                    if ((ApplicationInfo.FLAG_SYSTEM & appInfo.flags) != 0) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean isSystemApp(Context context, String pkgName) {
        Intent it = context.getPackageManager().getLaunchIntentForPackage(
                pkgName);
        boolean result = false;
        try {
            ActivityInfo info;
            int queryFlag = PackageManager.MATCH_DEFAULT_ONLY;
            List<ResolveInfo> list = context.getPackageManager()
                    .queryIntentActivities(it, queryFlag);
            for (ResolveInfo resInfo : list) {
                info = resInfo.activityInfo;
                if (info != null) {
                    ApplicationInfo appInfo = info.applicationInfo;
                    if ((ApplicationInfo.FLAG_SYSTEM & appInfo.flags) != 0) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(e);
        }
        return result;
    }

    /**
     * Finds the global search activity.
     */
    private static ComponentName findGlobalSearchActivity(Context context,
                                                          List<ResolveInfo> installed) {
        // Fetch the global search provider from the system settings,
        // and if it's still installed, return it.
        final String searchProviderSetting = getGlobalSearchProviderSetting(context);
        if (!TextUtils.isEmpty(searchProviderSetting)) {
            final ComponentName globalSearchComponent = ComponentName
                    .unflattenFromString(searchProviderSetting);
            if (globalSearchComponent != null
                    && isInstalled(context, globalSearchComponent)) {
                return globalSearchComponent;
            }
        }

        return getDefaultGlobalSearchProvider(installed);
    }

    private static ComponentName getDefaultGlobalSearchProvider(
            List<ResolveInfo> providerList) {
        if (providerList != null && !providerList.isEmpty()) {
            ActivityInfo ai = providerList.get(0).activityInfo;
            return new ComponentName(ai.packageName, ai.name);
        }

        LogUtil.w("Launcher", "No global search activity found");
        return null;
    }

    private static String getGlobalSearchProviderSetting(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                "search_global_search_activity");
    }

    /**
     * Checks whether the global search provider with a given component name is
     * installed on the system or not. This deals with cases such as the removal
     * of an installed provider.
     */
    private static boolean isInstalled(Context context,
                                       ComponentName globalSearch) {
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.setComponent(globalSearch);

        List<ResolveInfo> activities = context.getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (activities != null && !activities.isEmpty()) {
            return true;
        }

        return false;
    }

    public static boolean isAppInstalled(Context context, ComponentName cpn) {
        Intent intent = new Intent();
        intent.setComponent(cpn);

        List<ResolveInfo> activities = context.getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (activities != null && !activities.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * 判断某个应用在当前系统是否已经安装
     *
     * @param cnt
     * @param pkgName
     * @return
     */
    public static boolean isPackageAlreadyInstalled(Context cnt, String pkgName) {
        boolean exist = false;
        try {
            PackageInfo pi = cnt.getPackageManager().getPackageInfo(pkgName, 0);
            if (pi.applicationInfo.enabled) {
                exist = true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return exist;
    }

    /**
     * 判断某个应用在当前系统是否已经安装,不同于isPackageAlreadyInstalled（）方法，此方法只代表系统内有此内容存在，
     * 不一定应用处于可用状态
     *
     * @param cnt
     * @param pkgName
     * @return
     */
    public static boolean isApplicationExist(Context cnt, String pkgName) {
        // boolean exist = false;
        try {
            cnt.getPackageManager().getPackageInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<PackageInfo> safeGetInstallPackages(Context context) {
        List<PackageInfo> paklist = new ArrayList<PackageInfo>();
        try {
            paklist = context.getPackageManager().getInstalledPackages(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paklist;
    }

    /**
     * 通过系统邮件应用发送反馈邮件
     *
     * @param context context
     * @param address e-mail address
     * @throws NameNotFoundException
     */
    private static void feedbackByEmail(Context context, String address)
            throws Exception {
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
        // intent.putExtra(Intent.EXTRA_EMAIL, email)，结果将匹配不到任何应用
        Uri uri = Uri.parse("mailto:" + address);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        // intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);

//        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(
//                R.string.feedback_subject, packageInfo.versionName,
//                Build.VERSION.RELEASE, Build.MODEL));
//        intent.putExtra(Intent.EXTRA_TEXT,
//                context.getString(R.string.feedback_content)); // 正文
//        context.startActivity(Intent.createChooser(intent,
//                context.getString(R.string.feedback_email_chooser_title)));
    }

    public static void goToGp(Context context, String packageName) {
        // 解决老版的gp AssetBrowserActivity找不到的ActivityNotFoundException异常
        PackageManager pm = context.getPackageManager();
        try {
            String uri;
            uri = "market://details?id=" + packageName;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.android.vending",
                    "com.android.vending.AssetBrowserActivity"));
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            try {
                String uri;
                uri = "market://details?id=" + packageName;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent);
                }
            } catch (Exception e1) {
            }
        }
    }

    /**
     * 默认短信检测
     *
     * @param context       应用对象
     * @param myPackageName 应用包名
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isDefaultSms(Context context, String myPackageName) {
        boolean isDefault = false;
        try {
            int currentapiVersion = Build.VERSION.SDK_INT;
            if (currentapiVersion >= Build.VERSION_CODES.KITKAT) {
                String defaultSmsApplication = Telephony.Sms
                        .getDefaultSmsPackage(context);
                if (defaultSmsApplication != null
                        && defaultSmsApplication.equals(myPackageName)) {
                    isDefault = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isDefault;
    }

    /**
     * 获取当前系统版本号
     *
     * @return
     */
    public static int getSystemVersion() {
        int version = Build.VERSION.SDK_INT;
        return version;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    /**
     * 保存方法
     */
    public static void saveBitmap(Bitmap bm, String name) {
        LogUtil.e("", "保存图片");
        File f = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/phw_t", name);
        if (f.exists()) {
            f.delete();
        } else {
            f.getParentFile().mkdirs();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            LogUtil.i("", "已经保存");
        } catch (IOException e) {
            LogUtil.e(e);
        }

    }

    public static String getLocalLanguage() {
        Locale locale = Locale.getDefault();
        LogUtil.d("uninstallDaemon",
                "getLocalLanguage = " + locale.getLanguage());
        return locale.getLanguage();
    }

    public static boolean isFlyme() {
        try {
            // Invoke Build.hasSmartBar()
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean isLGg4Sys() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "lge".equalsIgnoreCase(brand) && "LG-H818".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiP9() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "HUAWEI".equalsIgnoreCase(brand) && "EVA-AL10".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiChangxiang5S() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "HUAWEI".equalsIgnoreCase(brand) && "HUAWEI TAG-TL00".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiMate7() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "HUAWEI".equalsIgnoreCase(brand) && "HUAWEI MT7-TL00".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiMate2() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "HUAWEI".equalsIgnoreCase(brand) && "HUAWEI MT2-L02".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiMate8() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "HUAWEI".equalsIgnoreCase(brand) && "HUAWEI NXT-AL10".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiChangwan4X() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "Honor".equalsIgnoreCase(brand) && "CHE-TL00".equalsIgnoreCase(model);

    }

    public static boolean isHuaweiG9() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        return "HUAWEI".equalsIgnoreCase(brand) && "HUAWEI VNS-AL00".equalsIgnoreCase(model);

    }

    /**
     * 判断当前版本是否是4.4以及以后版本。
     *
     * @return
     */
    public static boolean isNewer4dot4() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static DisplayMetrics getDisplayMetrics(Display display) {
        DisplayMetrics dm = new DisplayMetrics();
        try {
            Class<?> c = Class.forName("android.view.Display");
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
        } catch (Exception e) {
            display.getMetrics(dm);
            e.printStackTrace();
        }

        return dm;
    }

    /**
     * 显示设备的虚拟菜单键
     */
    public static void showVirtualMenu(Activity activity) {
        if (Build.VERSION.SDK_INT >= 22) {
            Field needsMenuKey;
            try {
                needsMenuKey = WindowManager.LayoutParams.class
                        .getField("needsMenuKey");
                int value = WindowManager.LayoutParams.class.getField(
                        "NEEDS_MENU_SET_TRUE").getInt(null);
                needsMenuKey
                        .setInt(activity.getWindow().getAttributes(), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                activity.getWindow().addFlags(
                        WindowManager.LayoutParams.class.getField(
                                "FLAG_NEEDS_MENU_KEY").getInt(null));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                // Ignore since this field won't exist in most versions of
                // Android
            } catch (IllegalAccessException e) {
                Log.w("showMenu",
                        "Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()",
                        e);
            }
        }
    }

    public static String hashKeyForDisk(String url) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] toByteArray(InputStream input) {
        ByteArrayOutputStream output = null;
        byte[] bytes = null;
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
            bytes = output.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                    output = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static float hypo(float a, float b) {
        return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    public static String getWallpaperPath(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath()
                    + wallpaperPath;
        }
        return context.getFilesDir().getAbsolutePath() + wallpaperPath;
    }

    public static String getShareTempPath(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath()
                    + shareTempPath;
        }
        return context.getFilesDir().getAbsolutePath() + shareTempPath;
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        return baos.toByteArray();
    }

    public static void getFileFromInputStream(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 由于4.4禁止发ACTION_MEDIA_MOUNTED广播，使用另一种方式
    public static void refreshGallery(final Context context, File file) {
        // 4.4禁止发ACTION_MEDIA_MOUNTED广播
        if (Build.VERSION.SDK_INT < 19) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
                    .parse("file://"
                            + Environment.getExternalStorageDirectory())));
        } else {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file));
            // Uri uri = Uri.parse("file://" + filePath);
            // LogUtil.d("scan file:" + filePath);
            // intent.setData(uri);
            context.sendBroadcast(intent);
        }
    }

    /**
     * 获取ANDROID_ID
     *
     * @param context 上下文
     * @return ANDROID_ID 16位的随机字符串
     */
    public static String getAndroidId(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取设备mac地址
     *
     * @return mac地址
     */
    public static String getMacAddress() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    /**
     * 获取sd卡下的文件
     *
     * @param context
     * @return
     */
    public static String getSDcardFilePath(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath()
                    + HW_LAUNCHER_DEFAULT_WORKSPACE_FILE_PATH_local;
        }
        return null;
    }

    /**
     * 根据传入的uniqueName获取硬盘缓存的路径地址。
     */
    public static String getApkPath(Context context, String apkName) {
        String cachePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) && context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getPath();
            return cachePath + File.separator + apkName;
        }
        return null;

    }

    /**
     * bitmap to drawable
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                                    : Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

	/**
	 * 回收ImageView占用的图像内存;
	 * @param view 将被回收的view
	 */
	public static void recycleImageView(View view){
		if(view == null) return;
		if(view instanceof ImageView){
			Drawable drawable=((ImageView) view).getDrawable();
			if(drawable instanceof BitmapDrawable){
				Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
				if (bmp != null && !bmp.isRecycled()){
					((ImageView) view).setImageBitmap(null);
					bmp.recycle();
					bmp = null;
				}
			}
		}
	}

    public static Bitmap tintBitmap(Bitmap inBitmap , int tintColor) {
        if (inBitmap == null||inBitmap.isRecycled()) {
            return null;
        }
        Bitmap outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter( new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)) ;
        canvas.drawBitmap(inBitmap , 0, 0, paint) ;
        return outBitmap ;
    }
}
