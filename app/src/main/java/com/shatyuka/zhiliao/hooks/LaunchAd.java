package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class LaunchAd implements IHook {
    static Class<?> AdNetworkManager;

    static Method isShowLaunchAd;

    @Override
    public String getName() {
        return "去启动页广告";
    }

    @Override
    public void init(final ClassLoader classLoader) throws Throwable {
        AdNetworkManager = Helper.findClass(classLoader, "com.zhihu.android.sdk.launchad.",
                (Class<?> clazz) -> {
                    try {
                        if (clazz.getDeclaredField("c").getType().getName().startsWith("okhttp3.")) return true;
                    } catch (Throwable ignored) {}
                    
                    for (Method m : clazz.getDeclaredMethods()) {
                        Class<?>[] p = m.getParameterTypes();
                        if (p.length == 4 && p[0] == int.class && p[1] == long.class && p[2] == long.class && p[3] == String.class) return true;
                    }
                    return false;
                });
        if (AdNetworkManager == null) {
            throw new ClassNotFoundException("com.zhihu.android.sdk.launchad.AdNetworkManager");
        }
Helper.findClass(classLoader, "com.zhihu.android.app.util.", 1, 500,
                (Class<?> LaunchAdHelper) -> {
                    for (Class<?> iface : LaunchAdHelper.getInterfaces()) {
                        if (iface.getName().contains("LaunchAdInterface")) {
                            try {
                                isShowLaunchAd = LaunchAdHelper.getDeclaredMethod("isShowLaunchAd");
                                return true;
                            } catch (NoSuchMethodException ignored) {}
                        }
                    }
                    
                    try {
                        isShowLaunchAd = LaunchAdHelper.getDeclaredMethod("isShowLaunchAd");
                        return true;
                    } catch (NoSuchMethodException ignored) {}
                    return false;
                });
        if (isShowLaunchAd == null)
            throw new NoSuchMethodException("com.zhihu.android.app.util.LaunchAdHelper.isShowLaunchAd()");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(isShowLaunchAd, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true))  
                    param.setResult(false);
            }
        });

        try {
            XposedHelpers.findAndHookMethod(isShowLaunchAd.getDeclaringClass(), "isLaunchAdShow", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true))
                        param.setResult(false);
                }
            });
        } catch (Throwable ignored) {}

        XposedHelpers.findAndHookMethod(AdNetworkManager, "a", int.class, long.class, long.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true)) {
                    param.setResult("");
                }
            }
        });
    }
}
