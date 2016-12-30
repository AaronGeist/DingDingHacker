package aaron.geist.dingdinghacker;

import java.util.ArrayList;
import java.util.Collection;

import aaron.geist.util.Utils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static aaron.geist.dingdinghacker.Constants.DINGDING_PACKAGE_NAME;

/**
 * An Xposed sub-module to avoid recalling message.
 * <p>
 * Created by Aaron on 2016/12/24.
 */

public class AntiMsgRecall implements IXposedHookLoadPackage {

    private static final String CLASS_NAME_MESSAGE_IMPL = "com.alibaba.wukong.im.message.MessageImpl";
    private static final String CLASS_NAME_MESSAGE_DB = "cuw";
    private static final String RECALLED_MSG = "Msg has been recalled.";
    private static final int NUM_MESSAGE_TEXT_TYPE = 1;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            Utils.log(">>> Find package " + DINGDING_PACKAGE_NAME);

            // always return "not recalled" status
            XposedHelpers.findAndHookMethod(CLASS_NAME_MESSAGE_IMPL, lpparam.classLoader, "recallStatus", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    // if msg is recalled and RECALLED msg is stored in local DB, then let it shown as recalled as usual
                    if (RECALLED_MSG.equalsIgnoreCase(loadMsgText(param.thisObject))) {
                        return;
                    }

                    Utils.log(">>> find recalled msg and return unrecalled status");
                    // set recallStatus as 0 (1=recalled)
                    param.setResult(0);
                }
            });

            // stop replacing message content with default recalled string in database
            XposedHelpers.findAndHookMethod(CLASS_NAME_MESSAGE_DB, lpparam.classLoader, "b", String.class, Collection.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Collection msgs = new ArrayList();
                    for (Object o : (Collection) param.args[1]) {
                        String msg = loadMsgText(o);
                        if (!RECALLED_MSG.equalsIgnoreCase(msg)) {
                            msgs.add(o);
                        }
                    }
                    param.args[1] = msgs;

                    Utils.log(">>> removed all messages to be recalled");
                }
            });

            // always show "recall" option
            XposedHelpers.findAndHookMethod(CLASS_NAME_MESSAGE_IMPL, lpparam.classLoader, "canRecall", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        }
    }

    /**
     * Load text from messageImpl instance.
     *
     * @param msg message
     * @return message string, null if not text type
     */
    private String loadMsgText(Object msg) {
        try {
            // msg.class = com.alibaba.wukong.im.MessageContent
            Object innerContent = XposedHelpers.getObjectField(msg, "mMessageContent");
            // indicate which type of content
            int type = (int) XposedHelpers.callMethod(innerContent, "type");

            if (type == NUM_MESSAGE_TEXT_TYPE) {
                // ctx.class = com.alibaba.wukong.im.message.MessageContentImpl$TextContentImpl
                return (String) XposedHelpers.callMethod(innerContent, "text");
            }
        } catch (Throwable t) {
        }

        return null;
    }
}

