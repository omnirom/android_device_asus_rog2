package org.omnirom.device.gripsensor;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import org.omnirom.device.R;
import org.omnirom.device.Utils;
import org.omnirom.device.gripsensor.SqueezeFragment;
//import org.omnirom.device.gripsensor.TwinAppsUtil;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;
import java.util.ArrayList;
import java.util.List;

public class ChooseAppFragment extends PreferenceFragment implements ApplicationsState.Callbacks {
    private static SqueezeFragment.AppData mCheckData;
    private static SaveHandler mSaveHandler;
    private ApplicationsState mApplicationsState;
    private Handler mBgHandler;
    private Handler mFgHandler;
    private boolean mHasReceivedLoadEntries;
    private IntentFilter mIntentFilter;

    Preference.OnPreferenceClickListener mListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            RadioPreference radioPreference = (RadioPreference) preference;
            String key = radioPreference.getKey();
            ChooseAppFragment.mCheckData.mKey = key;
            ChooseAppFragment.this.updateList();
            ChooseAppFragment.this.updateCheckedData(key, radioPreference.getPath());
            return false;
        }
    };
    private PackageManager mPm;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = true;
            if (!action.equals("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON") && action.equals("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF")) {
                z = false;
            }
            ChooseAppFragment.this.handleStateChanged(z);
        }
    };
    public ContentResolver mResolver;
    private ApplicationsState.Session mSession;
    private SharedPreferences mSharedPreferences;

    public int getType() {
        return 1;
    }

    @Override
    public void onAllSizesComputed() {
    }

    @Override
    public void onPackageIconChanged() {
    }

    @Override
    public void onPackageListChanged() {
    }

    @Override
    public void onPackageSizeChanged(String str) {
    }

    @Override
    public void onRunningStateChanged(boolean z) {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.grip_choose_app, rootKey);
        this.mPm = getContext().getPackageManager();
        this.mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        this.mBgHandler = new Handler(this.mApplicationsState.getBackgroundLooper());
        this.mFgHandler = new Handler();
        this.mSession = this.mApplicationsState.newSession(this);
        this.mResolver = getContext().getContentResolver();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        this.mIntentFilter = new IntentFilter("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON");
        this.mIntentFilter.addAction("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF");
        //.setTitle(getContext().getResources().getString(R.string.google_assist_edit_app));
        initHandler();
        loadData();
        rebuild();
    }

    private void initHandler() {
        if (mSaveHandler == null) {
            HandlerThread handlerThread = new HandlerThread("ChooseAppFragment_SaveHandler");
            handlerThread.start();
            mSaveHandler = new SaveHandler(handlerThread.getLooper());
            mSaveHandler.setContext(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mHasReceivedLoadEntries = false;
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    public void rebuild() {
        if (this.mHasReceivedLoadEntries) {
            ApplicationsState.AppFilter appFilter = ApplicationsState.FILTER_ALL_ENABLED;
            this.mBgHandler.post(() -> {
                ArrayList<ApplicationsState.AppEntry> rebuild = this.mSession.rebuild(appFilter, ApplicationsState.ALPHA_COMPARATOR, false);
                    if (rebuild != null) {
                        this.mFgHandler.post(() -> {
                        ChooseAppFragment.this.onRebuildComplete(rebuild);
                    });
                }
            });
        }
    }

    @Override
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> arrayList) {
        loadAppList(arrayList);
        updateList();
    }

    @Override
    public void onLauncherInfoChanged() {
        this.mHasReceivedLoadEntries = true;
        rebuild();
    }

    @Override
    public void onLoadEntriesCompleted() {
        this.mHasReceivedLoadEntries = true;
        rebuild();
    }

    public void loadData() {
        mCheckData = SqueezeFragment.getProviderVal(getContext(), getType(), 2);
    }

    private void loadAppList(ArrayList<ApplicationsState.AppEntry> arrayList) {
        String str;
        String str2;
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            int twinAppUid = getTwinAppUid();
            List<ResolveInfo> queryIntentActivities = this.mPm.queryIntentActivities(new Intent("android.intent.action.MAIN", (Uri) null).addCategory("android.intent.category.LAUNCHER"), 0);
            List<ResolveInfo> queryIntentActivities2 = this.mPm.queryIntentActivities(new Intent("android.intent.action.MAIN", (Uri) null).addCategory("android.intent.category.INFO"), 0);
            preferenceScreen.removeAll();
            preferenceScreen.setOrderingAsAdded(false);
            if (!Utils.isCNSKU()) {
                preferenceScreen.addPreference(getLayoutPref());
            }
            int i = 0;
            int i2 = 0;
            while (true) {
                str = "/";
                if (i2 >= queryIntentActivities2.size()) {
                    break;
                }
                String charSequence = queryIntentActivities2.get(i2).activityInfo.loadLabel(this.mPm).toString();
                Drawable loadIcon = queryIntentActivities2.get(i2).activityInfo.loadIcon(this.mPm);
                String str3 = queryIntentActivities2.get(i2).activityInfo.packageName;
                String str4 = "launch/" + str3 + str + queryIntentActivities2.get(i2).activityInfo.name + str + "launch_app";
                if (!isTwinApp(twinAppUid, queryIntentActivities2.get(i2).activityInfo.applicationInfo.uid) && (str3.equals("com.asus.maxxaudio.audiowizard") || str3.equals("com.asus.fmradio"))) {
                    int i3 = i + 1;
                    preferenceScreen.addPreference(getPref(charSequence, i3, loadIcon, str3, str4));
                    i = i3;
                }
                i2++;
            }
            int i4 = 0;
            while (i4 < arrayList.size()) {
                String str5 = arrayList.get(i4).label;
                this.mApplicationsState.ensureIcon(arrayList.get(i4));
                Drawable drawable = arrayList.get(i4).icon;
                String str6 = arrayList.get(i4).info.packageName;
                if (!isTwinApp(twinAppUid, arrayList.get(i4).info.uid) && !str6.equals("com.asus.camera") && ((!str6.equals("com.asus.gamecenter") || !AirTriggerUtils.isSupportHidl(1)) && (Utils.isCNSKU() || (!Utils.isCNSKU() && isAllowed(str6))))) {
                    int i5 = 0;
                    while (true) {
                        if (i5 >= queryIntentActivities.size()) {
                            break;
                        } else if (str6.equals(queryIntentActivities.get(i5).activityInfo.packageName)) {
                            int i6 = i + 1;
                            str2 = str;
                            preferenceScreen.addPreference(getPref(str5, i6, drawable, str6, "launch/" + str6 + str + queryIntentActivities.get(i5).activityInfo.name + str + "launch_app"));
                            i = i6;
                            break;
                        } else {
                            i5++;
                        }
                    }
                }
                str2 = str;
                i4++;
                str = str2;
            }
            handleStateChanged(!shouldDisable());
        }
    }

    private boolean isAllowed(String str) {
        String category = getCategory(str);
        return category.equals("GAME") || category.equals("COMMUNICATION") || category.equals("SOCIAL") || isPreload(str);
    }

    private LayoutPreference getLayoutPref() {
        LayoutPreference layoutPreference = new LayoutPreference(getActivity(), (int) R.layout.asus_grip_app_warning);
        layoutPreference.setKey("support_warning");
        layoutPreference.setOrder(-1);
        layoutPreference.setSelectable(false);
        return layoutPreference;
    }

    private boolean isPreload(String str) {
        try {
            if ((this.mPm.getApplicationInfo(str, 128).flags & 1) == 1) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getCategory(String str) {
        Bundle call = getContext().getContentResolver().call(Uri.parse("content://com.asus.launcher.categoryprovider"), "getCategoryId", str, (Bundle) null);
        if (call != null) {
            return call.getString("category", "");
        }
        return "";
    }

    private void handleStateChanged(boolean z) {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
                preferenceScreen.getPreference(i).setEnabled(z);
            }
        }
    }

    private boolean shouldDisable() {
        return !AirTriggerUtils.isMainSwitchEnable(getContext());
    }

    private boolean isTwinApp(int i, int i2) {
        return i != -1 && UserHandle.getUserId(i2) == i;
    }

    private int getTwinAppUid() {
        //~ if (TwinAppsUtil.isTwinAppsSupport(getContext())) {
            //~ return ((UserManager) getContext().getSystemService("user")).getTwinAppsId();
        //~ }
        return 1;
    }

    private RadioPreference getPref(String str, int i, Drawable drawable, String str2, String str3) {
        RadioPreference radioPreference = new RadioPreference(getContext());
        radioPreference.setKey(str2);
        radioPreference.setTitle(str);
        radioPreference.setOrder(i);
        radioPreference.setPath(str3);
        radioPreference.setOnPreferenceClickListener(this.mListener);
        if (drawable != null) {
            radioPreference.setIcon(drawable);
        }
        return radioPreference;
    }

    private void updateList() {
        updateCheckState(getPreferenceScreen());
    }

    private void updateCheckState(PreferenceGroup preferenceGroup) {
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            boolean equals = preference.getKey().equals(mCheckData.mKey);
            if (preference instanceof RadioPreference) {
                ((RadioPreference) preference).setChecked(equals);
            }
        }
    }

    protected void updateCheckedData(String str, String str2) {
        SqueezeFragment.AppData appData = mCheckData;
        appData.mKey = str;
        appData.mPath = str2;
        Log.d("ChooseAppFragment", "updateCheckedData path=" + str2);
        saveToDb(mCheckData);
        checkNeedSaveRoot();
    }

    private void checkNeedSaveRoot() {
        if (this.mSharedPreferences.getBoolean(SqueezeFragment.PREF_CHOOSE_APP_LIST[getType() - 3], false)) {
            SqueezeFragment.AppData appData = mCheckData;
            saveToDb(new SqueezeFragment.AppData("", appData.mPath, appData.mUri, 2, "Global_grip", "", "", 0, "", true, false));
        }
    }

    private void saveToDb(SqueezeFragment.AppData appData) {
        startSaveHandler(appData);
    }

    @Override
    public void onStart() {
        this.mSession.onResume();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        initHandler();
        loadData();
        rebuild();
        getContext().registerReceiver(this.mReceiver, this.mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(this.mReceiver);
        this.mSession.onDestroy();
        SaveHandler saveHandler = mSaveHandler;
        if (saveHandler != null) {
            saveHandler.removeMessages(6000);
            mSaveHandler.getLooper().quit();
            mSaveHandler = null;
        }
    }

    private static void startSaveHandler(SqueezeFragment.AppData appData) {
        mSaveHandler.obtainMessage(6000, appData).sendToTarget();
    }
}