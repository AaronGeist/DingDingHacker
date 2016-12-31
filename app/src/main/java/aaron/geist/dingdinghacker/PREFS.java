package aaron.geist.dingdinghacker;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Read-only preference for mod.
 * <p>
 * Created by Aaron on 2016/12/29.
 */

public enum PREFS {

    // singleton instance
    INSTANCE;

    private static XSharedPreferences preferences = null;

    public static final String KEY_ENABLE_KEEP_UNREAD = "enable_keep_unread";

    public static final String KEY_ENABLE_ANTI_RECALL = "enable_anti_recall";


    private static final boolean VALUE_DEFAULT_BOOLEAN = false;



    /**
     * Init sharedPref.
     */
    public void init() {
        preferences = new XSharedPreferences(Constants.MOD_PACKAGE_NAME, Constants.MOD_PREFS);
        preferences.makeWorldReadable();
    }

    public static boolean isEnableKeepUnread() {
        return isBoolean(KEY_ENABLE_KEEP_UNREAD);
    }

    public static boolean isEnableAntiRecall() {
        return isBoolean(KEY_ENABLE_ANTI_RECALL);
    }

    private static boolean isBoolean(String key) {
        if (null == preferences) {
            return VALUE_DEFAULT_BOOLEAN;
        }

        if (preferences.hasFileChanged()) {
            preferences.reload();
        }
        return preferences.getBoolean(key, VALUE_DEFAULT_BOOLEAN);
    }
}
