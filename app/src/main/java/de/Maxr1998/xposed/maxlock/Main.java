package de.Maxr1998.xposed.maxlock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    public static final String MY_PACKAGE_NAME = Main.class.getPackage().getName();
    private static XSharedPreferences PREFS_PACKAGES;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        PREFS_PACKAGES = new XSharedPreferences(MY_PACKAGE_NAME, Common.PREFS_PACKAGES);
        makeReadable();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        makeReadable();

        final String packageName = lpparam.packageName;
        if (!PREFS_PACKAGES.getBoolean(packageName, false)) {
            return;
        }
        if (!PREFS_PACKAGES.getBoolean(Common.MASTER_SWITCH_ON, true)) {
            return;
        }
        Long timestamp = System.currentTimeMillis();
        Long permitTimestamp = PREFS_PACKAGES.getLong(packageName + "_tmp", 0);
        if (permitTimestamp != 0 && timestamp - permitTimestamp <= 4000) {
            return;
        }

        Class<?> activity = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
        XposedBridge.hookAllMethods(activity, "onResume", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        final Activity app = (Activity) param.thisObject;
                        if (app.getClass().getName().equals("android.app.Activity")) {
                            return;
                        }
                        launchLockView(app, packageName, PREFS_PACKAGES.getBoolean(packageName + "_fake", false) ? ".ui.FakeDieDialog" : ".ui.LockActivity");

                        app.moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
        );
    }

    private void launchLockView(final Activity app, String packageName, String launch) {
        Intent it = new Intent();
        it.setComponent(new ComponentName(MY_PACKAGE_NAME, MY_PACKAGE_NAME + launch));
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        it.putExtra(Common.INTENT_EXTRAS_INTENT, app.getIntent());
        it.putExtra(Common.INTENT_EXTRAS_PKG_NAME, packageName);
        app.startActivity(it);
    }

    private void makeReadable() {
        PREFS_PACKAGES.makeWorldReadable();
        PREFS_PACKAGES.reload();
    }
}
