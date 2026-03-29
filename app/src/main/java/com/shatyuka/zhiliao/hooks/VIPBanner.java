package com.shatyuka.zhiliao.hooks;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.R;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class VIPBanner implements IHook {
    static Class<?> VipEntranceView;
    static Class<?> MoreVipData;
    static Class<?> NewMoreFragment;

    static Method initView;
    static Method initView_new;

    @Override
    public String getName() {
        return "隐藏会员卡片";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            VipEntranceView = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.widget.VipEntranceView");
            initView = VipEntranceView.getDeclaredMethod("a", Context.class);
        } catch (ClassNotFoundException ignored) {
            VipEntranceView = classLoader.loadClass("com.zhihu.android.premium.view.VipEntranceView");
            try {
                initView_new = VipEntranceView.getDeclaredMethod("initView", Context.class);
            } catch (NoSuchMethodException e) {
            }
        }

        try {
            MoreVipData = classLoader.loadClass("com.zhihu.android.api.MoreVipData");
            NewMoreFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.more.more.NewMoreFragment");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_vipbanner", false)) {
            if (VipEntranceView == null) return;

            final String targetClassName = VipEntranceView.getName();
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.thisObject.getClass().getName().equals(targetClassName)) {
                        param.args[0] = View.GONE;
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
            XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View v = (View) param.thisObject;
                    int id = v.getId();
                    if (id == View.NO_ID) return;
                    try {
                        String entryName = v.getResources().getResourceEntryName(id);
                        if ("profile_kvip_bar2".equals(entryName) || entryName.contains("vip_entrance")) {
                            
                            View current = v;
                            for (int i = 0; i < 2; i++) {
                                Object parent = current.getParent();
                                if (parent instanceof ViewGroup) {
                                    View pView = (View) parent;
                                    pView.setVisibility(View.GONE);
                                    ViewGroup.LayoutParams lp = pView.getLayoutParams();
                                    if (lp != null) {
                                        lp.width = 0;
                                        lp.height = 0;
                                        pView.setLayoutParams(lp);
                                    }
                                    current = pView;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
            if (initView != null) {
                XposedBridge.hookMethod(initView, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        XmlResourceParser layout = Helper.modRes.getLayout(R.layout.layout_vipentranceview);
                        LayoutInflater.from((Context) param.args[0]).inflate(layout, (ViewGroup) param.thisObject);
                        return null;
                    }
                });
            }

            if (initView_new != null) {
                XposedBridge.hookMethod(initView_new, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        XmlResourceParser layout = Helper.modRes.getLayout(R.layout.layout_vipentranceview_new);
                        LayoutInflater.from((Context) param.args[0]).inflate(layout, (ViewGroup) param.thisObject);
                        return null;
                    }
                });
            }
            for (Method method : VipEntranceView.getDeclaredMethods()) {
                if (method.getName().equals("setData")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            ((View) param.thisObject).setVisibility(View.GONE);
                            param.setResult(null); 
                        }
                    });
                }
            }
            XposedHelpers.findAndHookMethod(VipEntranceView, "onClick", View.class, XC_MethodReplacement.returnConstant(null));
            XposedBridge.hookAllMethods(VipEntranceView, "resetStyle", XC_MethodReplacement.returnConstant(null));
            
            if (MoreVipData != null) {
                if (NewMoreFragment != null) {
                    XposedHelpers.findAndHookMethod(NewMoreFragment, "a", MoreVipData, XC_MethodReplacement.returnConstant(null));
                }
                XposedBridge.hookAllMethods(MoreVipData, "isLegal", XC_MethodReplacement.returnConstant(Boolean.FALSE));
            }
        }
    }
}
