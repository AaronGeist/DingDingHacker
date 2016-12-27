package aaron.geist.util;

import aaron.geist.dingdinghacker.Constants;
import de.robv.android.xposed.XposedBridge;

/**
 * Created by maojun on 2016/12/26.
 */

public class Utils {

    public static void log(String msg) {
        if (Constants.ENABLE_LOG) {
            XposedBridge.log(msg);
        }
    }

}
