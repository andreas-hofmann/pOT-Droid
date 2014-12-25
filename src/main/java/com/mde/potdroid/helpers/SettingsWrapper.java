package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import com.mde.potdroid.R;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class provides access to the preferences of the App. All settings should be
 * retrieved through this class.
 */
public class SettingsWrapper {

    // The keys to the settings

    public static final String PREF_KEY_THEME = "pref_theme";
    public static final String PREF_KEY_LOGIN = "pref_login";
    public static final String PREF_KEY_LOGOUT = "pref_logout";
    public static final String PREF_KEY_USERNAME = "user_name";
    public static final String PREF_KEY_USERID = "user_id";
    public static final String PREF_KEY_UAGENT = "unique_uagent";
    public static final String PREF_KEY_COOKIE_NAME = "cookie_name";
    public static final String PREF_KEY_COOKIE_VALUE = "cookie_value";
    public static final String PREF_KEY_COOKIE_PATH = "cookie_path";
    public static final String PREF_KEY_COOKIE_URL = "cookie_url";
    public static final String PREF_KEY_SHOW_BENDERS = "pref_bender_position";
    public static final String PREF_KEY_DEBUG = "pref_debug_mode";
    public static final String PREF_KEY_LOAD_BENDERS = "pref_load_benders";
    public static final String PREF_KEY_LOAD_IMAGES = "pref_load_images";
    public static final String PREF_KEY_LOAD_VIDEOS = "pref_load_videos";
    public static final String PREF_KEY_POLL_MESSAGES = "pref_message_polling_interval";
    public static final String PREF_KEY_NOTIFICATION_VIBRATE = "pref_notification_vibrate";
    public static final String PREF_KEY_NOTIFICATION_SOUND = "pref_notification_sound";
    public static final String PREF_KEY_POSTINFO = "pref_show_postinfo";
    public static final String PREF_KEY_DARKEN = "pref_darken_old_posts";
    public static final String PREF_KEY_HIDE_GLOBAL = "pref_hide_global";
    public static final String PREF_KEY_START_ACTIVITY = "pref_start_activity";
    public static final String PREF_KEY_START_FORUM = "pref_start_forum";
    public static final String PREF_KEY_MATA = "pref_mata";
    public static final String PREF_KEY_MATA_FORUM = "pref_mata_forum";
    public static final String PREF_KEY_SHOW_MENU = "pref_show_menu";
    public static final String PREF_KEY_MARK_NEW_POSTS = "pref_mark_new_posts";
    public static final String PREF_KEY_BBCODE_EDITOR = "pref_bbcode_editor";
    public static final String PREF_KEY_CACHE_SIZE = "pref_cache_size";
    public static final String PREF_KEY_CONNECTION_TIMEOUT = "pref_connection_timeout";
    public static final String PREF_KEY_DYNAMIC_TOOLBARS = "pref_dynamic_toolbars";
    public static final String PREF_KEY_FASTSCROLL = "pref_fastscroll";
    public static final String PREF_KEY_SHOW_PAGNIATE_TOOLBAR = "pref_show_paginate_toolbar";
    public static final String PREF_KEY_SWIPE_TO_REFRESH = "pref_swipe_to_refresh";
    public static final String PREF_KEY_SWIPE_TO_REFRESH_TOPIC = "pref_swipe_to_refresh_topic";
    public static final String PREF_KEY_SWIPE_TO_PAGINATE = "pref_swipe_to_paginate";
    public static final String PREF_KEY_FIXED_SIDEBAR = "pref_fixed_sidebar";
    public static final String PREF_KEY_FONT_SIZE = "pref_font_size";

    public static final int START_BOARDS = 0;
    public static final int START_BOOKMARKS = 1;
    public static final int START_FORUM = 2;
    public static final int START_SIDEBAR = 3;

    // some references
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public SettingsWrapper(Context cx) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(cx);
        mContext = cx;

        // if this is a pre-3 version, delete all the preferences
        if (!mSharedPreferences.getBoolean("is_v3", false)) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.putBoolean("is_v3", true);
            editor.commit();

            // and delete the old benders
            File ext_root = Environment.getExternalStorageDirectory();
            File dir = new File(ext_root, "Android/data/" + mContext.getPackageName() + "/files/avatare");

            if (dir.exists())
                dir.delete();
        }
    }

    /**
     * Do we show benders at all?
     *
     * @return true, if benders should be shown
     */
    public Boolean showBenders() {
        return !mSharedPreferences.getString(PREF_KEY_SHOW_BENDERS, "0").equals("0");
    }

    /**
     * Position of benders.
     *
     * @return 0 -> never, 1 -> always posthead, 2 -> always postbody, 3 -> orientation dependent
     */
    public Integer benderPosition() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_SHOW_BENDERS, "0"));
    }

    /**
     * Show the post menu.
     *
     * @return 1 -> always shown, 2 -> always icon, 3 -> orientation dependent
     */
    public Integer showMenu() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_SHOW_MENU, "3"));
    }

    /**
     * The interval for the PM polling
     *
     * @return interval in seconds
     */
    public Integer pollMessagesInterval() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_POLL_MESSAGES, "0"));
    }

    /**
     * Never, always or only in Wifi benders
     *
     * @return 0 -> never, 1 -> only wifi, 2 -> always
     */
    public String loadBenders() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
    }

    /**
     * Never, always or only in Wifi images
     *
     * @return 0 -> never, 1 -> only wifi, 2 -> always
     */
    public String loadImages() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
    }

    /**
     * Never, always or only in Wifi images
     *
     * @return 0 -> never, 1 -> only wifi, 2 -> always
     */
    public String loadVideos() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_VIDEOS, "0");
    }

    /**
     * Check if, given the current network state, Benders should be loaded.
     *
     * @return true if loaded
     */
    public Boolean downloadBenders() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    public int getTheme() {
        String theme = mSharedPreferences.getString(PREF_KEY_THEME, "PotDroidDark");
        if(theme.equals("PotDroidDark"))
            return R.style.PotDroidDark;
        if(theme.equals("PotDroidLight"))
            return R.style.PotDroidLight;
        if(theme.equals("PotDroidDarkCompact"))
            return R.style.PotDroidDarkCompact;
        if(theme.equals("PotDroidLightCompact"))
            return R.style.PotDroidLightCompact;
        return -1;
    }

    /**
     * Check if, given the current network state, Images should be loaded.
     *
     * @return true if loaded
     */
    public Boolean downloadImages() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    /**
     * Check if, given the current network state, Images should be loaded.
     *
     * @return true if loaded
     */
    public Boolean downloadVideos() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_VIDEOS, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    /**
     * Check if post information should be shown
     *
     * @return true if so
     */
    public Boolean showPostInfo() {
        return mSharedPreferences.getBoolean(PREF_KEY_POSTINFO, true);
    }

    public int getCacheSize() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_CACHE_SIZE, "50")) * 1024 * 1024;
    }

    public int getDefaultFontSize() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_FONT_SIZE, "16"));
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_CONNECTION_TIMEOUT, "60"));
    }

    /**
     * Check if post information should be shown
     *
     * @return true if so
     */
    public Boolean isBBCodeEditor() {
        return mSharedPreferences.getBoolean(PREF_KEY_BBCODE_EDITOR, false);
    }

    /**
     * Check if global threads should be hidden
     *
     * @return true if so
     */
    public Boolean hideGlobalTopics() {
        return mSharedPreferences.getBoolean(PREF_KEY_HIDE_GLOBAL, false);
    }

    /**
     * Check if old posts should appear darkened
     *
     * @return true if so
     */
    public Boolean darkenOldPosts() {
        return mSharedPreferences.getBoolean(PREF_KEY_DARKEN, false);
    }

    /**
     * Check if new posts should be marked
     *
     * @return true if so
     */
    public Boolean markNewPosts() {
        return mSharedPreferences.getBoolean(PREF_KEY_MARK_NEW_POSTS, false);
    }

    /**
     * Check if dynamic toolbars in topic activity are enabled
     *
     * @return true if so
     */
    public Boolean dynamicToolbars() {
        return mSharedPreferences.getBoolean(PREF_KEY_DYNAMIC_TOOLBARS, true);
    }

    public Boolean isSwipeToRefresh() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWIPE_TO_REFRESH, true);
    }

    public Boolean isSwipeToRefreshTopic() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWIPE_TO_REFRESH_TOPIC, true);
    }

    public Boolean isSwipeToPaginate() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWIPE_TO_PAGINATE, true);
    }

    public Boolean isShowPaginateToolbar() {
        return mSharedPreferences.getBoolean(PREF_KEY_SHOW_PAGNIATE_TOOLBAR, true);
    }

    /**
     * Check if fastscroll in topic activity are enabled
     *
     * @return true if so
     */
    public Boolean fastscroll() {
        return mSharedPreferences.getBoolean(PREF_KEY_FASTSCROLL, true);
    }

    public Boolean isFixedSidebar() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        float w = displayMetrics.widthPixels / displayMetrics.density;
        return mSharedPreferences.getBoolean(PREF_KEY_FIXED_SIDEBAR, w > 768);
    }

    /**
     * Set the settings username
     */
    public void setUsername(String username) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_USERNAME, username);
        editor.commit();
    }

    public int getStartActivity() {
        return Integer.parseInt(
                mSharedPreferences.getString(PREF_KEY_START_ACTIVITY, Integer.toString(START_BOARDS)));
    }

    public int getStartForum() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_START_FORUM, "14"));
    }

    public int getMataAction() {
        return Integer.parseInt(
                mSharedPreferences.getString(PREF_KEY_MATA, Integer.toString(START_SIDEBAR)));
    }

    public int getMataForum() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_MATA_FORUM, "14"));
    }

    /**
     * Check if a username is stored in the settings
     *
     * @return true if set
     */
    public Boolean hasUsername() {
        return mSharedPreferences.contains(PREF_KEY_USERNAME);
    }

    /**
     * Check if debug mode is switched on
     *
     * @return true if switched on
     */
    public Boolean isDebug() {
        return mSharedPreferences.getBoolean(PREF_KEY_DEBUG, false);
    }

    /**
     * Check if notifications should vibrate
     *
     * @return true if switched on
     */
    public Boolean isNotificationVibrate() {
        return mSharedPreferences.getBoolean(PREF_KEY_NOTIFICATION_VIBRATE, false);
    }

    /**
     * Get the URI to the notification sound
     *
     * @return String uri of the sound
     */
    public String getNotificationSoundURI() {
        return mSharedPreferences.getString(PREF_KEY_NOTIFICATION_SOUND,
                Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    /**
     * Get the username as stored in the settings
     *
     * @return the username
     */
    public String getUsername() {
        return mSharedPreferences.getString(PREF_KEY_USERNAME, "");
    }

    /**
     * Clear the username from the settings.
     */
    public void clearUsername() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERNAME);
        editor.commit();
    }

    /**
     * Set the User ID in the sharedpreferences
     *
     * @param id the User ID
     */
    public void setUserId(int id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PREF_KEY_USERID, id);
        editor.commit();
    }

    /**
     * Clear the user ID from the settings
     */
    public void clearUserId() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERID);
        editor.commit();
    }

    /**
     * Get the user id from the settings
     *
     * @return user ID
     */
    public int getUserId() {
        return mSharedPreferences.getInt(PREF_KEY_USERID, 0);
    }

    /**
     * Check, if a login cookie is stored in the settings
     *
     * @return true if one is present
     */
    public Boolean hasLoginCookie() {
        return mSharedPreferences.contains(PREF_KEY_COOKIE_NAME);
    }

    /**
     * Get a Login Cookie from the values stored in the settings.
     *
     * @return the cookie
     */
    public BasicClientCookie getLoginCookie() {
        BasicClientCookie cookie = new BasicClientCookie(
                mSharedPreferences.getString(PREF_KEY_COOKIE_NAME, null),
                mSharedPreferences.getString(PREF_KEY_COOKIE_VALUE, null)
        );
        cookie.setPath(mSharedPreferences.getString(PREF_KEY_COOKIE_PATH, null));
        cookie.setDomain(mSharedPreferences.getString(PREF_KEY_COOKIE_URL, null));
        return cookie;
    }

    /**
     * Clear the login cookie from the settings
     */
    public void clearCookie() {
        PersistentCookieStore s = new PersistentCookieStore(mContext);
        s.removeAll();
    }

    /**
     * Get the user agent of the current user.
     *
     * @return User agent
     */
    public String getUserAgent() {
        return String.format(Network.UAGENT_TPL, mSharedPreferences.getString(PREF_KEY_UAGENT, ""));
    }

    /**
     * generate a random string for the user agent and store it in the settings.
     */
    public void generateUniqueUserAgent() {
        SecureRandom random = new SecureRandom();
        String uAgent = new BigInteger(50, random).toString(32);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_UAGENT, uAgent);
        editor.commit();
    }

    /**
     * Return true, if there was an app update
     *
     * @param cx Context
     * @return update status
     */
    public boolean isVersionUpdate(Context cx) {
        try {
            int versionCode = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 0).versionCode;
            return mSharedPreferences.getInt("installed_version", 0) < versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    /**
     * Save the currently installed app version to the settings
     *
     * @param cx Context
     */
    public void registerVersion(Context cx) {
        try {
            int versionCode = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 0).versionCode;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("installed_version", versionCode);
            editor.commit();
        } catch (PackageManager.NameNotFoundException e) {
            // shouldn't happen
        }
    }
}
