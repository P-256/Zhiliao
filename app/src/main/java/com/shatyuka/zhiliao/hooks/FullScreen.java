package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class FullScreen implements IHook {
    static Class<?> UnifyAnswerView;
    static Class<?> UnifyPinView;

    @Override
    public String getName() {
        return "禁止进入全屏模式";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            UnifyAnswerView = classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.toolbar.clearscreen.UnifyClearScreenToolBarView");
        } catch (ClassNotFoundException ignored) {}

        try {
            UnifyPinView = classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.toolbar.clearscreen.UnifyPinTopicClearScreenToolbarView");
        } catch (ClassNotFoundException ignored) {}
    }

    @Override
    public void hook() throws Throwable {
        XC_MethodHook callback = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_fullscreen", false)) {
                    param.setResult(null);
                }
            }
        };

        if (UnifyAnswerView != null) {
            XposedHelpers.findAndHookMethod(UnifyAnswerView, "enterClearScreen", callback);
        }

        if (UnifyPinView != null) {
            XposedHelpers.findAndHookMethod(UnifyPinView, "enterClearScreen", callback);
        }
    }
}
