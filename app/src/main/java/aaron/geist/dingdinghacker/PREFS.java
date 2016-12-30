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

    private static final boolean VALUE_DEFAULT_BOOLEAN = false;

    /**
     * Init sharedPref.
     */
    public void init() {
        preferences = new XSharedPreferences(Constants.MOD_PACKAGE_NAME, Constants.MOD_PREFS);
        preferences.makeWorldReadable();
    }

    public static boolean isEnableKeepUnread() {
        if (null == preferences) {
            return VALUE_DEFAULT_BOOLEAN;
        }

        if (preferences.hasFileChanged()) {
            preferences.reload();
        }
        return preferences.getBoolean(KEY_ENABLE_KEEP_UNREAD, VALUE_DEFAULT_BOOLEAN);
    }
}
