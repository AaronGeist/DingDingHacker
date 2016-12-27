package aaron.geist.dingdinghacker;

import aaron.geist.util.Utils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static aaron.geist.dingdinghacker.Constants.DINGDING_PACKAGE_NAME;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Keep message unread, even if you have read it.
 * <p>
 * Created by maojun on 2016/12/26.
 */

public class KeepMsgUnread implements IXposedHookLoadPackage {

    private static final String CLASS_NAME_MESSAGE_READ_TASK = "cuz";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            // When message is read, rpc will be sent to server, notifying that it's read.
            // So such rpc sending function is replaced here.
            findAndHookMethod(CLASS_NAME_MESSAGE_READ_TASK, lpparam.classLoader, "b", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    Utils.log(">>> replacing rpc sending read status");
                    return null;
                }
            });

        }
    }
}
