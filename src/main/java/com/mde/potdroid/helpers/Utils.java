package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that provides some static helper methods.
 */
public class Utils
{

    // the logcat tag
    public static final String LOG_TAG = "pOT Droid";
    // some static reference to any context for settings retrieval
    protected static Context mContext;

    /**
     * Log something to logcat
     *
     * @param msg the message to log
     */
    public static void log(String msg) {
        Log.v(Utils.LOG_TAG, msg);
    }

    /**
     * Get a drawable asset file
     *
     * @param cx the context
     * @param strName the filename
     * @return Drawable asset
     * @throws IOException
     */
    public static Drawable getDrawableFromAsset(Context cx, String strName) throws IOException {
        AssetManager assetManager = cx.getAssets();
        InputStream istr = assetManager.open(strName);
        return Drawable.createFromStream(istr, null);
    }

    /**
     * Get a drawable Icon from the assets folder
     *
     * @param cx the context
     * @param icon_id the icon id
     * @return Drawable of the icon
     * @throws IOException
     */
    public static Drawable getIcon(Context cx, Integer icon_id) throws IOException {
        return getDrawableFromAsset(cx, "thread-icons/icon" + icon_id + ".png");
    }

    /**
     * Show a long toast
     *
     * @param cx the context
     * @param content the message to show in the toast
     */
    public static void toast(Context cx, String content) {
        Toast.makeText(cx, content, Toast.LENGTH_LONG).show();
    }

    /**
     * Check if the current device version is Gingerbread (2.3.x)
     *
     * @return true if GB
     */
    public static boolean isGingerbread() {
        return !(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD ||
                android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1);
    }

    /**
     * Check if the current device version is Kitkat (4.4.x)
     *
     * @return true if Kitkat
     */
    public static boolean isKitkat() {
        return android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.KITKAT;
    }

    /**
     * Set the login state of the user to be not logged in
     */
    public static void setNotLoggedIn() {
        if (mContext == null)
            return;

        SettingsWrapper settings = new SettingsWrapper(mContext);
        settings.clearUserId();
        settings.clearCookie();
        settings.clearUsername();
    }

    public static boolean isLoggedIn() {
        if (mContext == null)
            return false;

        SettingsWrapper settings = new SettingsWrapper(mContext);
        return settings.hasUsername();
    }

    /**
     * Set the static context reference needed for some methods
     *
     * @param cx the context
     */
    public static void setApplicationContext(Context cx) {
        mContext = cx;
    }

    public static class NotLoggedInException extends Exception
    {

    }
}