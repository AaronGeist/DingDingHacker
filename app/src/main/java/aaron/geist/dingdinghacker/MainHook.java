package aaron.geist.dingdinghacker;

import de.robv.android.xposed.IXposedHookZygoteInit;

/**
 * Main entrance for the entire hooks.
 * <p>
 * Created by Aaron on 2016/12/30.
 */

public class MainHook implements IXposedHookZygoteInit {

    private static String MODULE_PATH = null;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;

        // init preference
        PREFS.INSTANCE.init();
    }
}
