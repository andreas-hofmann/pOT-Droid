/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.janoliver.potdroid.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.util.Log;

/**
 * Defines some static variables and handles instances of the shared preferences
 * and some other classes that are needed application wide.
 */
public class PotUtils {

    protected static WebsiteInteraction sWebsiteInteraction = null;
    protected static Activity sActivity = null;

    public static final String LOGIN_URL = "http://login.mods.de/";
    public static final String ASYNC_URL = "http://forum.mods.de/bb/async/";
    public static final String FORUM_URL = "http://forum.mods.de/bb/xml/boards.php";
    public static final String BOARD_URL_BASE = "http://forum.mods.de/bb/xml/board.php?BID=";
    public static final String THREAD_URL_BASE = "http://forum.mods.de/bb/xml/thread.php?update_bookmark=1&TID=";
    public static final String BOARD_URL_POST = "http://forum.mods.de/bb/newreply.php";
    public static final String BOARD_URL_EDITPOST = "http://forum.mods.de/bb/editreply.php";
    public static final String THREAD_URL = "http://forum.mods.de/bb/thread.php?TID=";
    public static final String BOOKMARK_URL = "http://forum.mods.de/bb/xml/bookmarks.php";
    public static final String DEFAULT_ENCODING = "ISO-8859-15";
    public static final String FORUM_HOST = "forum.mods.de";
    public static final String COOKIE_LIFETIME = "31536000";

    public static final String LOG_TAG = "pOT Droid";
    public static final int WAIT_CONNECTION_TIME = 500;

    /**
     * Returns an instance of the WebsiteInteractionClass.
     */
    public static WebsiteInteraction getWebsiteInteractionInstance(Activity act) {
        if (sWebsiteInteraction == null) {
            sWebsiteInteraction = new WebsiteInteraction(act);
        }
        return sWebsiteInteraction;
    }

    public static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }

        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static void log(String msg) {
        Log.v(PotUtils.LOG_TAG, msg);
    }

}
