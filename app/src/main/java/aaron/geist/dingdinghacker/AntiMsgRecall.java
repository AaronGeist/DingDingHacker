package aaron.geist.dingdinghacker;

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

    /**
     * message instance
     */
    private static final String CLASS_NAME_MESSAGE_IMPL = "com.alibaba.wukong.im.message.MessageImpl";

    /**
     * MessageDs, database
     */
    private static final String CLASS_NAME_MESSAGE_DB = "cuw";

    /**
     * MessageCache, changes will be applied on both local map cache and DB
     */
    private static final String CLASS_NAME_MESSAGE_CACHE = "cuu";

    private static final String CLASS_NAME_CONVERSATION = "com.alibaba.wukong.im.conversation.ConversationImpl";
    private static final String RECALLED_MSG = "Msg has been recalled.";
    private static final String NOTICE_SUFFIX = " [已撤回]";
    private static final int NUM_MESSAGE_TEXT_TYPE = 1;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            Utils.log(">>> Find package " + DINGDING_PACKAGE_NAME);

            // always return "not recalled" status
            XposedHelpers.findAndHookMethod(CLASS_NAME_MESSAGE_IMPL, lpparam.classLoader, "recallStatus", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    if (!PREFS.isEnableAntiRecall()) {
                        return;
                    }

                    // if msg is already recalled and RECALLED msg is stored in local DB,
                    // then let it shown as usual
                    if (RECALLED_MSG.equalsIgnoreCase(getMsgText(param.thisObject))) {
                        return;
                    }

                    Utils.log(">>> find recalled msg and return unrecalled status");
                    // set recallStatus as 0 (1=recalled)
                    param.setResult(0);
                }
            });

            /**
             * When message is recalled, it'll be replaced with default string "Msg has been recalled." in both DB and cache.
             * Instead of replacing the original message content with default string, we load the original message text and
             * append notifying text as suffix, so that user could know which message was to be recalled.
             */
            XposedHelpers.findAndHookMethod(CLASS_NAME_MESSAGE_CACHE, lpparam.classLoader, "a", String.class, Collection.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!PREFS.isEnableAntiRecall()) {
                        return;
                    }

                    boolean replace = (boolean) param.args[2];
                    if (!replace) {
                        // if not message replacement, then safe
                        return;
                    }

                    final Class<?> CLASS_MESSAGE_DB = XposedHelpers.findClass(CLASS_NAME_MESSAGE_DB, lpparam.classLoader);

                    String cid = (String) param.args[0];

                    for (Object msg : (Collection) param.args[1]) {
                        if (RECALLED_MSG.equalsIgnoreCase(getMsgText(msg))) {
                            long mid = XposedHelpers.getLongField(msg, "mMid");

                            Object conversation = XposedHelpers.getObjectField(msg, "mConversation");

                            // load original messageImpl from DB
                            Object msgInDB = XposedHelpers.callStaticMethod(CLASS_MESSAGE_DB, "a", cid, mid, conversation);

                            String originMsgText = getMsgText(msgInDB);
                            Utils.log(">>> origin msg=" + originMsgText);

                            // append info suffix
                            setMsgText(msg, originMsgText + NOTICE_SUFFIX);
                        }
                    }
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
    private String getMsgText(Object msg) {
        try {
            // msg.class = com.alibaba.wukong.im.MessageImpl
            Object innerContent = XposedHelpers.getObjectField(msg, "mMessageContent");
            // indicate which type of content
            int type = (int) XposedHelpers.callMethod(innerContent, "type");

            if (type == NUM_MESSAGE_TEXT_TYPE) {
                // innerContent.class = com.alibaba.wukong.im.message.MessageContentImpl$TextContentImpl
                return (String) XposedHelpers.callMethod(innerContent, "text");
            }
        } catch (Throwable t) {
        }

        return null;
    }

    /**
     * Set text into messageImpl instance.
     *
     * @param msg     message
     * @param newText text to be set
     */
    private void setMsgText(Object msg, String newText) {
        try {
            // msg.class = com.alibaba.wukong.im.MessageImpl
            Object innerContent = XposedHelpers.getObjectField(msg, "mMessageContent");
            // indicate which type of content
            int type = (int) XposedHelpers.callMethod(innerContent, "type");

            if (type == NUM_MESSAGE_TEXT_TYPE) {
                // innerContent.class = com.alibaba.wukong.im.message.MessageContentImpl$TextContentImpl
                XposedHelpers.callMethod(innerContent, "setText", newText);
            } else {
                Utils.log(">>> not correct type");
            }
        } catch (Throwable t) {
        }
    }
}

