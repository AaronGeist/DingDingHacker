package aaron.geist.dingdinghacker;

import java.util.Map;

import aaron.geist.util.Utils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static aaron.geist.dingdinghacker.Constants.DINGDING_PACKAGE_NAME;

/**
 * Keep message unread, even if you have read it.
 * <p>
 * Created by Aaron on 2016/12/26.
 */

public class KeepMsgUnread implements IXposedHookLoadPackage {

    private static final String CLASS_NAME_MESSAGE_READ_TASK = "cuz";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            // When message is read, rpc will be sent to server, notifying that it's read.
            // So message to be updated is cleaned here.
            XposedHelpers.findAndHookMethod(CLASS_NAME_MESSAGE_READ_TASK, lpparam.classLoader, "b", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (PREFS.isEnableKeepUnread()) {
                        Utils.log(">>> enable keep unread");
                        // 'a' contains all messages to be set as read.
                        Map innerMap = (Map) XposedHelpers.getObjectField(param.thisObject, "a");
                        if (innerMap != null) {
                            innerMap.clear();
                        }
                    } else {
                        Utils.log(">>> disable keep unread");
                    }
                }
            });
        }
    }
}
