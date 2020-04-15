package org.omnirom.device.gripsensor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreference;
import org.omnirom.device.R;
import org.omnirom.device.Utils;
//import org.omnirom.device.widget.SwitchBarController;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.LayoutPreference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SqueezeFragment extends PreferenceFragment {
    public static final String[] FILED_LIST = {"asus_grip_short_squeeze", "asus_grip_long_squeeze"};
    static final String[] GOOGLE_ASSIST_FILTER = {"en_au", "en_us", "en_ca", "en_sg", "en_in", "en_uk", "fr_fr", "fr_ca", "de_de", "it_it", "ja_jp", "es_es", "es_mx", "es_419", "pt_br", "zh_tw", "da_dk", "id_id", "nl_nl", "no_no", "sv_se", "ru_ru", "hi_in", "th_th", "ko_kr"};
    public static final int[][] ID_LIST = {new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{11, 12}};
    private static final String KEY_DO_NOT_DISTURB = String.valueOf(6);
    private static final String KEY_FLASHLIGHT = String.valueOf(1);
    private static final String KEY_GAME_CENTER = String.valueOf(7);
    private static final String KEY_GA_WITH_SNAPSHOT = String.valueOf(5);
    private static final String KEY_GOOGLE_ASSIST = String.valueOf(219);
    private static final String KEY_KEYCODE_BACK = String.valueOf(4);
    private static final String KEY_KEYCODE_HOME = String.valueOf(3);
    private static final String KEY_KEYCODE_RECENT = String.valueOf(187);
    public static final String[][] KEY_LIST;
    public static final String[][] KEY_LIST_HIDE;
    private static final String KEY_SCREENSHOT = String.valueOf(0);
    private static final String KEY_SOUND_MODE = String.valueOf(4);
    private static final String KEY_X_MODE = String.valueOf(2);
    public static final int[][] NAME_LIST = {new int[]{R.string.app_google_assistant, R.string.app_google_voice_search, R.string.google_assist_pref_snap_shot, R.string.google_assist_pref_sound_mode, R.string.pref_flashlight, R.string.asus_zen_mode_settings_title, R.string.accelerometer_title, R.string.google_assist_pref_cam_back, R.string.asus_advanced_settings_screenshot, R.string.pref_x_mode}, new int[]{R.string.pref_start_amoury_crate, R.string.gripsensor_squeeze_trigger_launch_app_categoty}};
    public static final int[][] NAME_LIST_HIDE = {new int[]{R.string.app_google_assistant, R.string.app_google_voice_search, R.string.google_assist_pref_sound_mode, R.string.pref_flashlight, R.string.accelerometer_title, R.string.google_assist_pref_cam_back, R.string.asus_advanced_settings_screenshot, R.string.pref_x_mode}, new int[]{R.string.gripsensor_squeeze_trigger_launch_app_categoty}};
    public static final String[][] PATH_LIST = {new String[]{"keycode/219", "special/3", "special/5", "special/4", "special/1", "special/6", "setting/System/accelerometer_rotation", "launch/com.asus.camera/com.asus.camera.CameraApp", "special/0", "special/2"}, new String[]{"special/7", ""}};
    public static final String[][] PATH_LIST_HIDE = {new String[]{"keycode/219", "special/3", "special/4", "special/1", "setting/System/accelerometer_rotation", "launch/com.asus.camera/com.asus.camera.CameraApp", "special/0", "special/2"}, new String[]{""}};
    public static final String[] PREF_CHOOSE_APP_LIST = {"short_squeeze_choose_app", "long_squeeze_choose_app"};
    private static AppData mAppListData;
    private static AppData mCheckData;
    private static HashMap<String, Integer> mFilter = new HashMap<>();
    private static SaveHandler mSaveHandler;
    public PreferenceGroup mAllCanUseGroup;
    public PreferenceGroup mAppListGroup;
    private ApplicationsState mApplicationsState;
    private Handler mBgHandler;
    //private SqueezeSwitchEnabler mEnabler;
    private Handler mFgHandler;
    Preference.OnPreferenceClickListener mListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            SqueezeFragment.mCheckData.mKey = key;
            SqueezeFragment.this.updateList();
            boolean z = SqueezeFragment.this.mAllCanUseGroup.findPreference(key) != null;
            if (preference instanceof RadioPreference) {
                SqueezeFragment.this.updateCheckedData(key, ((RadioPreference) preference).getPath(), z);
            } else if (preference instanceof GripMasterRadioPreference) {
                SqueezeFragment.this.updateCheckedData(key, ((GripMasterRadioPreference) preference).getPath(), z);
            }
            return false;
        }
    };
    Preference.OnPreferenceChangeListener mMasterListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object obj) {
            SqueezeFragment.mCheckData.mKey = preference.getKey();
            SqueezeFragment.this.updateList();
            SqueezeFragment.this.updateCheckedData(preference.getKey(), ((GripMasterRadioPreference) preference).getPath(), false);
            return true;
        }
    };
    private PackageManager mPm;
    public ContentResolver mResolver;
    private SharedPreferences mSharedPreferences;
    private LayoutPreference mTutorial;
    public PreferenceGroup mUnlockGroup;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.grip_sensor_squeeze_prefs, rootKey);
    }

    public int getType() {
        return 1;
    }

    static {
        String str = KEY_GOOGLE_ASSIST;
        String str2 = KEY_SOUND_MODE;
        String str3 = KEY_FLASHLIGHT;
        String str4 = KEY_SCREENSHOT;
        String str5 = KEY_X_MODE;
        KEY_LIST = new String[][]{new String[]{str, "KEY_GOOGLE_VOICE_SEARCH", KEY_GA_WITH_SNAPSHOT, str2, str3, KEY_DO_NOT_DISTURB, "accelerometer_rotation", "com.asus.camera", str4, str5}, new String[]{KEY_GAME_CENTER, "launch_app"}};
        KEY_LIST_HIDE = new String[][]{new String[]{str, "KEY_GOOGLE_VOICE_SEARCH", str2, str3, "accelerometer_rotation", "com.asus.camera", str4, str5}, new String[]{"launch_app"}};
    }

    public static final class AppData {
        public boolean mEnable;
        public String mField;
        public String mKey;
        public String mLabel;
        public String mLaunchPkg;
        public int mMode;
        public String mPath;
        public String mPkg;
        public boolean mSaveLock;
        public int mUid;
        public Uri mUri;

        public AppData(String str, String str2, Uri uri, int i, String str3, String str4, String str5, int i2, String str6, boolean z, boolean z2) {
            this.mLabel = str;
            this.mPath = str2;
            this.mUri = uri;
            this.mUid = i;
            this.mPkg = str3;
            this.mField = str4;
            this.mKey = str5;
            this.mMode = i2;
            this.mEnable = z;
            this.mSaveLock = z2;
            this.mLaunchPkg = str6;
        }
    }

    @Override // org.omnirom.device.SettingsPreferenceFragment, androidx.preference.PreferenceFragmentCompat, org.omnirom.device.dashboard.DashboardFragment, androidx.fragment.app.Fragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mAllCanUseGroup = (PreferenceGroup) findPreference("category_all_can_use");
        this.mUnlockGroup = (PreferenceGroup) findPreference("category_unlock_use");
        this.mPm = getActivity().getPackageManager();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        this.mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        this.mBgHandler = new Handler(this.mApplicationsState.getBackgroundLooper());
        this.mFgHandler = new Handler();
        this.mResolver = getActivity().getContentResolver();
        getActivity().setTitle(getType() == 3 ? R.string.gripsensor_lightpress_trigger : R.string.gripsensor_heavypress_trigger);
        readGoogleAssistFilter();
        initHandler();
        updateTutorial();
        loadData();
        loadFuncList();
    }

    public static int getIdMap(Context context, int i) {
        HashMap hashMap = new HashMap();
        for (int i2 = 0; i2 < KEY_LIST.length; i2++) {
            int i3 = 0;
            while (true) {
                String[][] strArr = KEY_LIST;
                if (i3 >= strArr[i2].length) {
                    break;
                }
                hashMap.put(strArr[i2][i3], Integer.valueOf(ID_LIST[i2][i3]));
                i3++;
            }
        }
        AppData providerVal = getProviderVal(context, i, 1);
        if (hashMap.containsKey(providerVal.mKey)) {
            return ((Integer) hashMap.get(providerVal.mKey)).intValue();
        }
        return -1;
    }

    private void updateTutorial() {
        this.mTutorial = (LayoutPreference) findPreference("tutorial");
        ((GifView) this.mTutorial.findViewById(R.id.tutorial_gif)).setGifResource(getContext(), getType() == 3 ? R.drawable.asus_airtrigger_quickly_press : R.drawable.asus_airtrigger_press);
        ((TextView) this.mTutorial.findViewById(R.id.instructions_text)).setText(getType() == 3 ? R.string.tutorial_short_squeeze : R.string.tutorial_long_squeeze);
    }

    private void initHandler() {
        if (mSaveHandler == null) {
            HandlerThread handlerThread = new HandlerThread("SaveHandler");
            handlerThread.start();
            mSaveHandler = new SaveHandler(handlerThread.getLooper());
            mSaveHandler.setContext(getActivity());
        }
    }

    private static void readGoogleAssistFilter() {
        if (mFilter.isEmpty()) {
            int i = 0;
            while (true) {
                String[] strArr = GOOGLE_ASSIST_FILTER;
                if (i < strArr.length) {
                    mFilter.put(strArr[i], Integer.valueOf(i));
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private static boolean isGoogleAssistant() {
        readGoogleAssistFilter();
        String str = Locale.getDefault().getLanguage().toLowerCase() + "_" + Locale.getDefault().getCountry().toLowerCase();
        Log.d("SqueezeFragment", "language=" + str);
        return mFilter.containsKey(str);
    }

    private void loadFuncList() {
        if (AirTriggerUtils.isSupportHidl(1)) {
            loadFuncList(NAME_LIST, PATH_LIST, KEY_LIST);
        } else {
            loadFuncList(NAME_LIST_HIDE, PATH_LIST_HIDE, KEY_LIST_HIDE);
        }
    }

    private void loadData() {
        mCheckData = getProviderVal(getActivity(), getType(), 1);
    }

    private void loadAppData() {
        mAppListData = getProviderVal(getActivity(), getType(), 2);
    }

    public static AppData getProviderVal(Context context, int i, int i2) {
        boolean z;
        boolean z2;
        int i3;
        String str;
        String str2;
        Cursor cursor;
        String str3;
        boolean z3;
        int i4 = 0;
        String str4 = new String();
        Context context2;
        Cursor cursor2;
        int i5;
        Cursor cursor3;
        AppData appData;
        String str5;
        String str6;
        String str7;
        String str8;
        int i6;
        String str9;
        String str10;
        ContentResolver contentResolver = context.getContentResolver();
        int i7 = (i - 1) % 2;
        Uri uri = GripUtils.URI_LIST[i7];
        int i8 = i2 == 1 ? 2 : 3;
        String str11 = i2 == 1 ? "Global_grip" : "Global_grip_app";
        String str12 = FILED_LIST[i7];
        Uri withAppendedId = ContentUris.withAppendedId(uri, (long) i8);
        boolean z4 = true;
        String string = context.getString(R.string.pref_select_apps);
        AppData appData2 = null;
        Cursor query = contentResolver.query(withAppendedId, GripUtils.GRIP_ALL_COLUMN, null, null, null);
        String str13 = "";
        String str14 = "launch///launch_app";
        if (query == null || query.getCount() <= 0) {
            cursor = query;
            z2 = true;
            z = true;
            str2 = string;
            i3 = -1;
            str3 = "";
            str = str13;
        } else {
            query.moveToFirst();
            int i9 = query.getInt(2);
            if (i9 == -1) {
                i9 = Integer.parseInt(AirTriggerUtils.getSettingsProviderForGrip(contentResolver, str12, String.valueOf(-1)));
                z3 = false;
            } else {
                z3 = true;
            }
            switch (i9) {
                case 0:
                    str3 = query.getString(13);
                    String string2 = query.getString(14);
                    String string3 = query.getString(10);
                    if (string2.equals("com.google.android.apps.gsa.staticplugins.opa.OpaActivity")) {
                        if (isGoogleAssistant()) {
                            str8 = KEY_GOOGLE_ASSIST;
                            str7 = "keycode/219";
                            i4 = 4;
                            str6 = "";
                            appData = updateGALanguage(context, str7, uri, i8, str11, str12, str8, 4, str6, z3, true);
                        } else {
                            str8 = "KEY_GOOGLE_VOICE_SEARCH";
                            str7 = "special/3";
                            i4 = 6;
                            str6 = "";
                            appData = updateGALanguage(context, str7, uri, i8, str11, str12, str8, 6, str6, z3, true);
                        }
                        cursor = query;
                        str4 = str8;
                        str14 = str7;
                        str3 = str6;
                        appData2 = appData;
                        context2 = context;
                        break;
                    } else if (string3 == null || !string3.equals("launch_app")) {
                        str14 = "launch/" + str3 + "/" + string2;
                        context2 = context;
                        str4 = str3;
                        i4 = i9;
                        cursor = query;
                        break;
                    } else {
                        if (i2 == 1) {
                            str5 = "launch_app";
                        } else {
                            str5 = str3;
                        }
                        str14 = "launch/" + str3 + "/" + string2 + "/" + "launch_app";
                        str4 = str5;
                        i4 = i9;
                        z4 = false;
                        cursor = query;
                        context2 = context;
                    }
                    break;
                case 1:
                    cursor2 = query;
                    str13 = cursor2.getString(3);
                    context2 = context;
                    i5 = i9;
                    cursor3 = cursor2;
                    str3 = "";
                    break;
                case 2:
                    cursor2 = query;
                    str13 = cursor2.getString(5);
                    context2 = context;
                    i5 = i9;
                    cursor3 = cursor2;
                    str3 = "";
                    break;
                case 3:
                default:
                    cursor2 = query;
                    context2 = context;
                    i5 = i9;
                    cursor3 = cursor2;
                    str3 = "";
                    break;
                case 4:
                    cursor2 = query;
                    str13 = String.valueOf(cursor2.getInt(9));
                    if (str13.equals(KEY_GOOGLE_ASSIST) && !isGoogleAssistant()) {
                        str10 = "KEY_GOOGLE_VOICE_SEARCH";
                        str9 = "special/3";
                        i6 = 6;
                        appData = updateGALanguage(context, str9, uri, i8, str11, str12, str10, 6, "", z3, true);
                        cursor = cursor2;
                        str3 = "";
                        str4 = str10;
                        str14 = str9;
                        i4 = i6;
                        appData2 = appData;
                        context2 = context;
                        break;
                    }
                    context2 = context;
                    i5 = i9;
                    cursor3 = cursor2;
                    str3 = "";
                    break;
                case 5:
                    cursor2 = query;
                    str13 = cursor2.getString(11);
                    context2 = context;
                    i5 = i9;
                    cursor3 = cursor2;
                    str3 = "";
                    break;
                case 6:
                    str13 = query.getString(15);
                    if (str13.equals(String.valueOf(3))) {
                        if (!isGoogleAssistant()) {
                            cursor2 = query;
                            str13 = "KEY_GOOGLE_VOICE_SEARCH";
                            context2 = context;
                            i5 = i9;
                            cursor3 = cursor2;
                            str3 = "";
                            break;
                        } else {
                            str10 = KEY_GOOGLE_ASSIST;
                            str9 = "keycode/219";
                            i6 = 4;
                            cursor2 = query;
                            appData = updateGALanguage(context, str9, uri, i8, str11, str12, str10, 4, "", z3, true);
                            cursor = cursor2;
                            str3 = "";
                            str4 = str10;
                            str14 = str9;
                            i4 = i6;
                            appData2 = appData;
                            context2 = context;
                            break;
                        }
                    } else {
                        context2 = context;
                        i5 = i9;
                        cursor3 = query;
                        str3 = "";
                    }
            }
            str2 = getLabel(context2, str4, str3);
            str = str4;
            z = z4;
            z2 = z3;
            i3 = i4;
        }
        if (query != null) {
            query.close();
        }
        return appData2 == null ? new AppData(str2, str14, uri, i8, str11, str12, str, i3, str3, z2, z) : appData2;
    }

    private static AppData updateGALanguage(Context context, String str, Uri uri, int i, String str2, String str3, String str4, int i2, String str5, boolean z, boolean z2) {
        AppData appData = new AppData("", str, uri, i, str2, str3, str4, i2, str5, z, z2);
        saveToDb(context, appData);
        return appData;
    }

    private boolean shouldDisable() {
        if (!AirTriggerUtils.isMainSwitchEnable(getActivity())) {
            return true;
        }
        return !isSwitchEnabled(getActivity().getContentResolver(), getType());
    }

    private RadioPreference getPref(String str, int i, Drawable drawable, String str2, String str3) {
        RadioPreference radioPreference = new RadioPreference(getActivity());
        radioPreference.setKey(str2);
        radioPreference.setTitle(str);
        radioPreference.setOrder(i);
        if (drawable != null) {
            radioPreference.setIcon(drawable);
        }
        radioPreference.setPath(str3);
        radioPreference.setOnPreferenceClickListener(this.mListener);
        if (str2.equals("accelerometer_rotation") || str2.equals("accessibility_onehand_ctrl_enabled") || str2.equals(KEY_X_MODE) || str2.equals(KEY_FLASHLIGHT) || str2.equals(KEY_DO_NOT_DISTURB)) {
            radioPreference.setSummary(R.string.summary_turn_on_off);
        } else if (str2.equals(KEY_SOUND_MODE)) {
            radioPreference.setSummary(R.string.google_assist_pref_sound_mode_summary);
        }
        return radioPreference;
    }

    private void loadFuncList(int[][] iArr, String[][] strArr, String[][] strArr2) {
        this.mAllCanUseGroup.removeAll();
        this.mAllCanUseGroup.setOrderingAsAdded(false);
        this.mUnlockGroup.removeAll();
        this.mUnlockGroup.setOrderingAsAdded(false);
        for (int i = 0; i < iArr.length; i++) {
            for (int i2 = 0; i2 < iArr[i].length; i2++) {
                String string = getActivity().getResources().getString(iArr[i][i2]);
                String str = strArr2[i][i2];
                String str2 = strArr[i][i2];
                if (str.equals(KEY_GOOGLE_ASSIST)) {
                    if (isGoogleAssistant()) {
                        if (Utils.isCNSKU()) {
                        }
                    }
                } else if (str.equals("KEY_GOOGLE_VOICE_SEARCH")) {
                    if (!isGoogleAssistant()) {
                        if (Utils.isCNSKU()) {
                        }
                    }
                } else if (str.equals(KEY_GA_WITH_SNAPSHOT) && Utils.isCNSKU()) {
                }
                if (str.equals("launch_app")) {
                    this.mUnlockGroup.addPreference(getMasterPref(string, i2 + 1, null, str, str2));
                } else {
                    RadioPreference pref = getPref(string, i2 + 1, null, str, str2);
                    if (i == 0) {
                        this.mAllCanUseGroup.addPreference(pref);
                    } else {
                        this.mUnlockGroup.addPreference(pref);
                    }
                }
            }
        }
        if (!Utils.isCNSKU()) {
            this.mUnlockGroup.addPreference(getLayoutPref());
        }
        for (int i3 = 0; i3 < this.mAllCanUseGroup.getPreferenceCount(); i3++) {
            this.mAllCanUseGroup.getPreference(i3).setEnabled(!shouldDisable());
        }
        for (int i4 = 0; i4 < this.mUnlockGroup.getPreferenceCount(); i4++) {
            this.mUnlockGroup.getPreference(i4).setEnabled(!shouldDisable());
        }
    }

    private void enableAllowScreenOff(boolean z) {
        boolean z2 = true;
        if (AirTriggerUtils.isSupportHidl(1)) {
            SwitchPreference switchPreference = (SwitchPreference) findPreference(AllowScreenOffPreferenceController.KEY);
            boolean isSwitchEnabled = isSwitchEnabled(this.mResolver, getType());
            switchPreference.setEnabled(isSwitchEnabled && z);
            if (!isSwitchEnabled || !z || !AirTriggerUtils.isAllowScreenOff(getActivity(), getType() - 3)) {
                z2 = false;
            }
            switchPreference.setChecked(z2);
        }
    }

    private GripMasterRadioPreference getMasterPref(String str, int i, Drawable drawable, String str2, String str3) {
        GripMasterRadioPreference gripMasterRadioPreference = new GripMasterRadioPreference(getActivity());
        gripMasterRadioPreference.setKey(str2);
        gripMasterRadioPreference.setOrder(i);
        gripMasterRadioPreference.setTitle(R.string.gripsensor_squeeze_trigger_launch_app_categoty);
        gripMasterRadioPreference.setFragment(getFragmentName());
        gripMasterRadioPreference.setSummary(getActivity().getResources().getString(R.string.google_assist_pref_self_def_summary));
        gripMasterRadioPreference.setOnPreferenceChangeListener(this.mMasterListener);
        return gripMasterRadioPreference;
    }

    private String getFragmentName() {
        int type = getType();
        return (type == 3 || type != 4) ? "org.omnirom.device.gripsensor.ShortSqueezeChooseAppFragment" : "org.omnirom.device.gripsensor.LongSqueezeChooseAppFragment";
    }

    private LayoutPreference getLayoutPref() {
        LayoutPreference layoutPreference = new LayoutPreference(getActivity(), (int) R.layout.asus_google_assist_warning);
        layoutPreference.setKey("support_warning");
        layoutPreference.setOrder(30);
        layoutPreference.setSelectable(false);
        return layoutPreference;
    }

    private void updateList() {
        updateCheckState(getPreferenceScreen());
        enableAllowScreenOff(this.mAllCanUseGroup.findPreference(mCheckData.mKey) != null);
    }

    private void updateCheckState(PreferenceGroup preferenceGroup) {
        int preferenceCount = preferenceGroup.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = preferenceGroup.getPreference(i);
            boolean equals = preference.getKey().equals(mCheckData.mKey);
            if (preference instanceof GripMasterRadioPreference) {
                ((GripMasterRadioPreference) preference).setChecked(equals);
            } else if (preference instanceof RadioPreference) {
                ((RadioPreference) preference).setChecked(equals);
            } else if (preference instanceof PreferenceCategory) {
                updateCheckState((PreferenceCategory) preference);
            }
        }
    }

    protected void updateCheckedData(String str, String str2, boolean z) {
        if (str.equals("launch_app")) {
            this.mSharedPreferences.edit().putBoolean(PREF_CHOOSE_APP_LIST[getType() - 3], true).commit();
        } else {
            this.mSharedPreferences.edit().putBoolean(PREF_CHOOSE_APP_LIST[getType() - 3], false).commit();
        }
        AppData appData = mCheckData;
        appData.mKey = str;
        appData.mPath = str2;
        appData.mEnable = true;
        appData.mSaveLock = z;
        Log.d("SqueezeFragment", "updateCheckedData path=" + str2 + " saveLock=" + z);
        saveToDb(getActivity(), mCheckData);
    }

    private static void saveToDb(Context context, AppData appData) {
        startSaveHandler(context, appData);
    }

    private static String getLabel(Context context, String str, String str2) {
        String string = context.getString(R.string.pref_select_apps);
        if (str2.isEmpty()) {
            return context.getResources().getString(getFuncLabel(str));
        }
        try {
            return String.valueOf(context.getPackageManager().getApplicationInfo(str2, 0).loadLabel(context.getPackageManager()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return string;
        }
    }

    public static String getSwitchLabel(Context context, int i, int i2) {
        return getProviderVal(context, i, i2).mLabel;
    }

    public static int getFuncLabel(String str) {
        if (AirTriggerUtils.isSupportHidl(1)) {
            return getFuncLabel(str, NAME_LIST, KEY_LIST);
        }
        return getFuncLabel(str, NAME_LIST_HIDE, KEY_LIST_HIDE);
    }

    public static int getFuncLabel(String str, int[][] iArr, String[][] strArr) {
        for (int i = 0; i < iArr.length; i++) {
            for (int i2 = 0; i2 < iArr[i].length; i2++) {
                if (strArr[i][i2].equals(str)) {
                    return iArr[i][i2];
                }
            }
        }
        return R.string.pref_select_apps;
    }

    public static void setSwitchState(Context context, int i, boolean z) {
        ContentResolver contentResolver = context.getContentResolver();
        int i2 = (i - 1) % 2;
        Uri uri = GripUtils.URI_LIST[i2];
        int i3 = i > 2 ? 2 : 1;
        String str = FILED_LIST[i2];
        Log.d("SqueezeFragment", "setSwitchState field=" + str + " enable=" + z);
        Uri withAppendedId = ContentUris.withAppendedId(uri, (long) i3);
        ContentValues contentValues = new ContentValues();
        if (z) {
            contentValues.put("currentMode", Integer.valueOf(Integer.parseInt(AirTriggerUtils.getSettingsProviderForGrip(contentResolver, str, String.valueOf(-1)))));
        } else {
            if (i > 2) {
                AirTriggerUtils.setSettingsProviderForGrip(contentResolver, str, String.valueOf(getModeByUid(contentResolver, i)));
            }
            contentValues.put("currentMode", -1);
        }
        contentResolver.update(withAppendedId, contentValues, null, null);
    }

    private static int getModeByUid(ContentResolver contentResolver, int i) {
        int i2 = 2;
        Uri uri = GripUtils.URI_LIST[(i - 1) % 2];
        if (i <= 2) {
            i2 = 1;
        }
        Cursor query = contentResolver.query(ContentUris.withAppendedId(uri, (long) i2), new String[]{"currentMode"}, null, null, null);
        int i3 = -1;
        if (query != null && query.getCount() > 0) {
            query.moveToFirst();
            i3 = query.getInt(0);
        }
        if (query != null) {
            query.close();
        }
        return i3;
    }

    public static boolean isSwitchEnabled(ContentResolver contentResolver, int i) {
        return getModeByUid(contentResolver, i) >= 0;
    }

    private static void startSaveHandler(Context context, AppData appData) {
        if (mSaveHandler == null) {
            HandlerThread handlerThread = new HandlerThread("SaveHandler");
            handlerThread.start();
            mSaveHandler = new SaveHandler(handlerThread.getLooper());
            mSaveHandler.setContext(context);
        }
        mSaveHandler.obtainMessage(6000, appData).sendToTarget();
    }

    @Override // org.omnirom.device.SettingsPreferenceFragment, org.omnirom.device.dashboard.DashboardFragment, androidx.fragment.app.Fragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment
    public void onDestroy() {
        super.onDestroy();
        PreferenceGroup preferenceGroup = this.mAppListGroup;
        if (preferenceGroup != null) {
            preferenceGroup.removeAll();
            this.mAppListGroup = null;
        }
        PreferenceGroup preferenceGroup2 = this.mAllCanUseGroup;
        if (preferenceGroup2 != null) {
            preferenceGroup2.removeAll();
            this.mAllCanUseGroup = null;
        }
        SaveHandler saveHandler = mSaveHandler;
        if (saveHandler != null) {
            saveHandler.removeMessages(6000);
            mSaveHandler.getLooper().quit();
            mSaveHandler = null;
        }
    }

    @Override // org.omnirom.device.SettingsPreferenceFragment, org.omnirom.device.core.InstrumentedPreferenceFragment, org.omnirom.device.dashboard.DashboardFragment, androidx.fragment.app.Fragment, com.android.settingslib.core.lifecycle.ObservablePreferenceFragment
    public void onResume() {
        super.onResume();
        loadAppData();
        updateAppSummary();
        updateList();
    }

    private void updateAppSummary() {
        GripMasterRadioPreference gripMasterRadioPreference = (GripMasterRadioPreference) getPreferenceScreen().findPreference("launch_app");
        gripMasterRadioPreference.setPkg(mAppListData.mLaunchPkg);
        gripMasterRadioPreference.setPath(mAppListData.mPath);
        gripMasterRadioPreference.setSummary(mAppListData.mLabel);
        gripMasterRadioPreference.setIcon(getAppIcon(mAppListData.mLaunchPkg));
    }

    private Drawable getAppIcon(String str) {
        PackageManager packageManager = getActivity().getPackageManager();
        try {
            return packageManager.getApplicationInfo(str, 0).loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
