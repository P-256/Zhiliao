package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class FullScreen implements IHook {
    static Class<?> ClearScreenToolBarView;

    @Override
    public String getName() {
        return "禁止进入全屏模式";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            ClearScreenToolBarView = classLoader.loadClass("com.zhihu.android.feature.short_container_feature.ui.widget.toolbar.clearscreen.ClearScreenToolBarView");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (ClearScreenToolBarView != null) {
            XposedHelpers.findAndHookMethod(ClearScreenToolBarView, "enterClearScreen", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_fullscreen", false)) {
                        param.setResult(null);
                    }
                }
            });
        }
    }
}
