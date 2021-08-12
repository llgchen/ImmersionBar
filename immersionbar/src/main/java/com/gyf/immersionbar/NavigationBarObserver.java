package com.gyf.immersionbar;

import android.app.Application;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import java.util.ArrayList;

import static com.gyf.immersionbar.Constants.IMMERSION_NAVIGATION_BAR_MODE_DEFAULT;
import static com.gyf.immersionbar.Constants.IMMERSION_NAVIGATION_BAR_MODE_EMUI;
import static com.gyf.immersionbar.Constants.IMMERSION_NAVIGATION_BAR_MODE_MIUI;
import static com.gyf.immersionbar.Constants.IMMERSION_NAVIGATION_BAR_MODE_OPPO;
import static com.gyf.immersionbar.Constants.IMMERSION_NAVIGATION_BAR_MODE_SAMSUNG;
import static com.gyf.immersionbar.Constants.IMMERSION_NAVIGATION_BAR_MODE_VIVO;

/**
 * 导航栏显示隐藏处理，目前支持华为、小米、VOVO、Android 10带有导航栏的手机
 *
 * @author geyifeng
 * @date 2019/4/10 6:02 PM
 */
final class NavigationBarObserver extends ContentObserver {

    private ArrayList<OnNavigationBarListener> mListeners;
    private Application mApplication;
    private boolean mIsRegister = false;

    static NavigationBarObserver getInstance() {
        return NavigationBarObserverInstance.INSTANCE;
    }

    private NavigationBarObserver() {
        super(new Handler(Looper.getMainLooper()));
    }

    void register(Application application) {
        this.mApplication = application;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mApplication != null
                && mApplication.getContentResolver() != null && !mIsRegister) {
            Uri uri;
            if (OSUtils.isHuaWei() || OSUtils.isEMUI()) {
                if (OSUtils.isEMUI3_x() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    uri = Settings.System.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_EMUI);
                } else {
                    uri = Settings.Global.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_EMUI);
                }
            } else if (OSUtils.isXiaoMi() || OSUtils.isMIUI()) {
                uri = Settings.Global.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_MIUI);
            } else if (OSUtils.isVivo()) {
                uri = Settings.Secure.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_VIVO);
            } else if (OSUtils.isOppo()) {
                uri = Settings.Secure.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_OPPO);
            } else if (OSUtils.isSamsung()) {
                uri = Settings.Global.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_SAMSUNG);
            } else {
                uri = Settings.Secure.getUriFor(IMMERSION_NAVIGATION_BAR_MODE_DEFAULT);
            }
            if (uri != null) {
                mApplication.getContentResolver().registerContentObserver(uri, true, this);
                mIsRegister = true;
            }
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mApplication != null && mApplication.getContentResolver() != null
                && mListeners != null && !mListeners.isEmpty()) {
            NavigationBarType navigationBarType = NavigationBarType.UNKNOWN;
            int type;
            boolean show = true;
            if (OSUtils.isHuaWei() || OSUtils.isEMUI()) {
                if (OSUtils.isEMUI3_x() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    type = Settings.System.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_EMUI, 0);
                } else {
                    type = Settings.Global.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_EMUI, 0);
                }
                if (type == 0) {
                    navigationBarType = NavigationBarType.CLASSIC;
                    show = true;
                } else if (type == 1) {
                    navigationBarType = NavigationBarType.GESTURES;
                    show = false;
                }
            } else if (OSUtils.isXiaoMi() || OSUtils.isMIUI()) {
                type = Settings.Global.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_MIUI, 0);
                if (type == 0) {
                    navigationBarType = NavigationBarType.CLASSIC;
                    show = true;
                } else if (type == 1) {
                    navigationBarType = NavigationBarType.GESTURES;
                    show = false;
                }
            } else if (OSUtils.isVivo()) {
                type = Settings.Secure.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_VIVO, 0);
                if (type == 0) {
                    navigationBarType = NavigationBarType.CLASSIC;
                    show = true;
                } else if (type == 1) {
                    navigationBarType = NavigationBarType.SMALL;
                    show = false;
                } else if (type == 2) {
                    navigationBarType = NavigationBarType.GESTURES;
                    show = false;
                }
            } else if (OSUtils.isOppo()) {
                type = Settings.Secure.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_OPPO, 0);
                if (type == 0) {
                    navigationBarType = NavigationBarType.CLASSIC;
                    show = true;
                } else if (type == 1 || type == 2 || type == 3) {
                    navigationBarType = NavigationBarType.GESTURES;
                    show = false;
                }
            } else if (OSUtils.isSamsung()) {
                type = Settings.Global.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_SAMSUNG, 0);
                if (type == 0) {
                    navigationBarType = NavigationBarType.CLASSIC;
                    show = true;
                } else if (type == 1) {
                    navigationBarType = NavigationBarType.GESTURES;
                    show = false;
                }
            } else {
                type = Settings.Secure.getInt(mApplication.getContentResolver(), IMMERSION_NAVIGATION_BAR_MODE_DEFAULT, 0);
                if (type == 0) {
                    navigationBarType = NavigationBarType.CLASSIC;
                    show = true;
                } else if (type == 1) {
                    navigationBarType = NavigationBarType.DOUBLE;
                    show = true;
                } else if (type == 2) {
                    navigationBarType = NavigationBarType.GESTURES;
                    show = false;
                }
            }
            for (OnNavigationBarListener onNavigationBarListener : mListeners) {
                onNavigationBarListener.onNavigationBarChange(show, navigationBarType);
            }
        }
    }

    void addOnNavigationBarListener(OnNavigationBarListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    void removeOnNavigationBarListener(OnNavigationBarListener listener) {
        if (listener == null || mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    private static class NavigationBarObserverInstance {
        private static final NavigationBarObserver INSTANCE = new NavigationBarObserver();
    }

}
