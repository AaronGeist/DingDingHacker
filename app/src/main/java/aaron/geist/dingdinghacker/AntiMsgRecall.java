package aaron.geist.dingdinghacker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * An Xposed sub-module to avoid recalling message.
 * <p>
 * Created by Aaron on 2016/12/24.
 */

public class AntiMsgRecall implements IXposedHookLoadPackage {

    private static final String DINGDING_PACKAGE_NAME = "com.alibaba.android.rimet";
    private static final String CLASS_NAME_MESSAGE_IMPL = "com.alibaba.wukong.im.message.MessageImpl";
    private static final String CLASS_NAME_MESSAGE_CONTENT = "com.alibaba.wukong.im.MessageContent";
    private static final String CLASS_NAME_TEXT_CONTENT_IMPL = "com.alibaba.wukong.im.message.MessageContentImpl$TextContentImpl";
    private static final String CLASS_NAME_MESSAGE_DB = "cuw";
    private static final String RECALLED_MSG = "Msg has been recalled.";
    private static final int NUM_MESSAGE_TEXT_TYPE = 1;

    // log message
    private void log(String msg) {
        XposedBridge.log(msg);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            // always return "not recalled" status
            findAndHookMethod(CLASS_NAME_MESSAGE_IMPL, lpparam.classLoader, "recallStatus", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                    String msgText = loadMsgText(param.thisObject, lpparam);
//                    // if msg is recalled and RECALLED msg is stored in local DB, then let it shown as recalled as usual
//                    if (RECALLED_MSG.equalsIgnoreCase(msgText)) {
//                        return;
//                    }

                    // set recallStatus as 0 (1=recalled)
                    param.setResult(0);
                }
            });

            // stop replacing message content with default recalled string in database
            findAndHookMethod(CLASS_NAME_MESSAGE_DB, lpparam.classLoader, "b", String.class, Collection.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Collection msgs = new ArrayList();
                    for (Object o : (Collection) param.args[1]) {
                        String msg = loadMsgText(o, lpparam);
                        if (!RECALLED_MSG.equalsIgnoreCase(msg)) {
                            msgs.add(o);
                        }
                    }
                    param.args[1] = msgs;
                }
            });

            // always show "recall" option
            findAndHookMethod(CLASS_NAME_MESSAGE_IMPL, lpparam.classLoader, "canRecall", new XC_MethodHook() {
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
     * @param msg
     * @param lpparam
     * @return
     */
    private String loadMsgText(Object msg, final XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            Field ctxField = findClass(CLASS_NAME_MESSAGE_IMPL, lpparam.classLoader).getDeclaredField("mMessageContent");
            ctxField.setAccessible(true);
            Object ctx = ctxField.get(msg);

            Method getType = findClass(CLASS_NAME_MESSAGE_CONTENT, lpparam.classLoader).getDeclaredMethod("type");
            getType.setAccessible(true);
            int type = (int) getType.invoke(ctx);

            if (type == NUM_MESSAGE_TEXT_TYPE) {
                Method text = findClass(CLASS_NAME_TEXT_CONTENT_IMPL, lpparam.classLoader).getDeclaredMethod("text");
                text.setAccessible(true);
                return (String) text.invoke(ctx);
            }
        } catch (Throwable t) {
        }

        return null;
    }
}

