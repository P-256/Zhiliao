package com.shatyuka.zhiliao.hooks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class VIPBanner implements IHook {
    static Class<?> VipEntranceView;
    static Class<?> MoreVipData;
    static Class<?> NewMoreFragment;

    static Method initView_new;

    @Override
    public String getName() {
        return "隐藏会员卡片";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            VipEntranceView = classLoader.loadClass("com.zhihu.android.premium.view.VipEntranceView");
            initView_new = VipEntranceView.getDeclaredMethod("initView", Context.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            try {
                VipEntranceView = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.widget.VipEntranceView");
            } catch (ClassNotFoundException ignored2) {}
        }

        try {
            MoreVipData = classLoader.loadClass("com.zhihu.android.api.MoreVipData");
            NewMoreFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.NewMoreFragment");
        } catch (ClassNotFoundException ignored) {}
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_vipbanner", false)) {
            if (VipEntranceView == null) return;
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.thisObject.getClass().getName().equals(VipEntranceView.getName())) {
                        param.args[0] = View.GONE; // 强制参数为 GONE
                    }
                }
            });
            XposedHelpers.findAndHookMethod(VipEntranceView, "onMeasure", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View v = (View) param.thisObject;
                    v.setVisibility(View.GONE);
                    XposedHelpers.callMethod(v, "setMeasuredDimension", 0, 0);
                    param.setResult(null);
                }
            });
            XposedHelpers.findAndHookMethod(VipEntranceView, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View child = (View) param.thisObject;
                    child.setVisibility(View.GONE);
                    Object parent = child.getParent();
                    if (parent instanceof ViewGroup) {
                        View parentView = (View) parent;
                        parentView.setVisibility(View.GONE);
                        ViewGroup.LayoutParams lp = parentView.getLayoutParams();
                        if (lp != null) {
                            lp.height = 0;
                            lp.width = 0;
                            parentView.setLayoutParams(lp);
                        }
                    }
                }
            });
            for (Method method : VipEntranceView.getDeclaredMethods()) {
                if (method.getName().equals("setData")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(null); 
                        }
                    });
                }
            }

            if (MoreVipData != null) {
                XposedBridge.hookAllMethods(MoreVipData, "isLegal", XC_MethodReplacement.returnConstant(Boolean.FALSE));
            }
        }
    }
}
