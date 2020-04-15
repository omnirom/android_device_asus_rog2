package org.omnirom.device.gripsensor;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragment;
import org.omnirom.device.R;
import org.omnirom.device.Utils;
import org.omnirom.omnilib.utils.OmniVibe;

public class TapStrengthAdjust extends PreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        }

        private int[] mAdjustMaxStrength = {0, 0};
        private int[] mAdjustMinStrength = {100, 100};
        private AirTriggerUtils mAirTriggerUtils;
        private BroadcastReceiver mBroadcastReceiver;
        private Button mCancelButton;
        private CompoundButton.OnCheckedChangeListener mCheckListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                TapStrengthAdjust.this.showIncreasedToast(z);
                TapStrengthAdjust.this.saveCheckState(z);
            }
        };
        private CheckBox mCkbSensitivity;
        private int mCurrentIndexLeft;
        private int mCurrentIndexRight;
        public int mCurrentState;
        private BroadcastReceiver mDockEventReceiver;
        private int mDockState = 0;
        private Button mDoneButton;
        private Drawable mDrawableIntroNew;
        private Drawable mDrawableIntroOld;
        private String[] mEntries;
        private boolean mHasTouchSenseFeature;
        private ImageView mImgPic;
        private int mInitialIndexLeft;
        private int mInitialIndexRight;
        private OnGripSeekBarChangeListener mListener;
        private LinearLayout mLlDesc;
        private LinearLayout mLlIntro;
        private final View.OnApplyWindowInsetsListener mOnApplyWindowInsetsListener = new View.OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                if (!AirTriggerUtils.isTwinViewModeEnabled(TapStrengthAdjust.this.getContext()) || TapStrengthAdjust.this.mDockState == 7 || windowInsets.getStableInsetBottom() != 0) {
                    TapStrengthAdjust.this.updateLayoutHeight(false);
                } else {
                    TapStrengthAdjust.this.updateLayoutHeight(true);
                }
                return windowInsets;
            }
        };
        private SharedPreferences mPreferences = null;
        private Resources mRes;
        private SqueezeLevelSeekBar mSeekBar;
        private ObjectAnimator mSeekBarAnimator;
        private Drawable mSeekBarDefaultLeftThumbBg;
        private ObjectAnimator mSeekBarLeftAnimator;
        private Drawable mSeekBarThumbBg;
        private TextView mTitle;
        private TextView mTxtGripDesc;
        private TextView mTxtIntroNew;
        private TextView mTxtIntroOld;
        private int[] mValues;
        private Vibrator mVibrator;

        public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
            Log.d("TapStrengthAdjust", "onCreateView");
            getActivity().getActionBar().setTitle("");
            View inflate = layoutInflater.inflate(R.layout.tap_strength_adjust_page, viewGroup, false);
            TextView textView = (TextView) inflate.findViewById(R.id.txt_high);
            TextView textView2 = (TextView) inflate.findViewById(R.id.txt_low);
            this.mTxtIntroOld = (TextView) inflate.findViewById(R.id.txt_intro_old);
            this.mTxtIntroNew = (TextView) inflate.findViewById(R.id.txt_intro_new);
            this.mDrawableIntroOld = getResources().getDrawable(R.drawable.grip_seekbar_default_thumb);
            this.mDrawableIntroNew = getResources().getDrawable(R.drawable.grip_seekbar_thumb);
            this.mCkbSensitivity = (CheckBox) inflate.findViewById(R.id.ckb_level_up);
            this.mTitle = (TextView) inflate.findViewById(R.id.title);
            this.mImgPic = (ImageView) inflate.findViewById(R.id.img_left_right_indicator);
            this.mRes = getContext().getResources();
            this.mLlDesc = (LinearLayout) inflate.findViewById(R.id.ll_desc);
            this.mListener = new OnGripSeekBarChangeListener();
            updateCheckState();
            updateInitIndex();
            this.mValues = this.mAirTriggerUtils.getTapArray();
            this.mEntries = new String[this.mValues.length];
            int i = 0;
            while (i < this.mValues.length) {
                int i2 = i + 1;
                this.mEntries[i] = String.valueOf(i2);
                i = i2;
            }
            this.mSeekBar = (SqueezeLevelSeekBar) inflate.findViewById(R.id.seek_bar);
            this.mSeekBarThumbBg = ((LayerDrawable) this.mSeekBar.getThumb()).getDrawable(0);
            this.mSeekBarThumbBg.setAlpha(0);
            this.mSeekBarDefaultLeftThumbBg = ((LayerDrawable) this.mSeekBar.getDefaultLeftThumb()).getDrawable(0);
            this.mSeekBarDefaultLeftThumbBg.setAlpha(0);
            this.mSeekBar.showLeftDefThumb(false);
            this.mSeekBar.setLabels(this.mEntries);
            this.mSeekBar.setMax(Math.max(1, this.mEntries.length - 1));
            textView.setText(String.valueOf(this.mEntries.length));
            textView2.setText("1");
            this.mTxtGripDesc = (TextView) inflate.findViewById(R.id.txt_desc);
            this.mLlIntro = (LinearLayout) inflate.findViewById(R.id.ll_intro);
            this.mDrawableIntroOld.setBounds(0, 0, 40, 40);
            this.mDrawableIntroNew.setBounds(0, 0, 40, 40);
            this.mTxtIntroOld.setCompoundDrawables(this.mDrawableIntroOld, null, null, null);
            this.mTxtIntroNew.setCompoundDrawables(this.mDrawableIntroNew, null, null, null);
            this.mCancelButton = (Button) inflate.findViewById(R.id.cancel_button);
            this.mCancelButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (TapStrengthAdjust.this.mCurrentState != 0) {
                        int i = 1;
                        if (TapStrengthAdjust.this.mCurrentState != 1) {
                            if (TapStrengthAdjust.this.mCurrentState == 2 || TapStrengthAdjust.this.mCurrentState == 3) {
                                TapStrengthAdjust TapStrengthAdjust = TapStrengthAdjust.this;
                                if (TapStrengthAdjust.mInitialIndexLeft == TapStrengthAdjust.this.mCurrentIndexLeft) {
                                    i = 0;
                                }
                                int unused = TapStrengthAdjust.mCurrentState = i;
                                TapStrengthAdjust.this.setTapEnable();
                                TapStrengthAdjust.this.refreshUiByState();
                                return;
                            }
                            int unused2 = TapStrengthAdjust.this.mCurrentState = 0;
                            TapStrengthAdjust.this.setTapEnable();
                            TapStrengthAdjust.this.updateInitIndex();
                            TapStrengthAdjust.this.initTapThresholdForAnim();
                            TapStrengthAdjust.this.refreshUiByState();
                            return;
                        }
                    }
                    TapStrengthAdjust.this.getActivity().finish();
                }
            });
            this.mCancelButton.setVisibility(0);
            this.mDoneButton = (Button) inflate.findViewById(R.id.done_button);
            this.mDoneButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    int i = 2;
                    if (TapStrengthAdjust.this.mCurrentState == 0 || TapStrengthAdjust.this.mCurrentState == 1) {
                        TapStrengthAdjust TapStrengthAdjust = TapStrengthAdjust.this;
                        if (TapStrengthAdjust.mInitialIndexRight != TapStrengthAdjust.this.mCurrentIndexRight) {
                            i = 3;
                        }
                        int unused = TapStrengthAdjust.mCurrentState = i;
                        TapStrengthAdjust.this.setTapEnable();
                        TapStrengthAdjust.this.refreshUiByState();
                    } else if (TapStrengthAdjust.this.mCurrentState == 2 || TapStrengthAdjust.this.mCurrentState == 3) {
                        TapStrengthAdjust.this.commit();
                        int unused2 = TapStrengthAdjust.this.mCurrentState = 4;
                        TapStrengthAdjust.this.setTapEnable();
                        TapStrengthAdjust.this.refreshUiByState();
                    } else {
                        if (TapStrengthAdjust.this.mPreferences.getBoolean("first_setting_tap_strength", true)) {
                            TapStrengthAdjust.this.mPreferences.edit().putBoolean("first_setting_tap_strength", false).commit();
                        }
                        TapStrengthAdjust.this.getActivity().finish();
                    }
                }
            });
            initThumbAnim();
            return inflate;
        }

        private void showIncreasedToast(boolean z) {
            if (z) {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.ckb_increase_sensitivity_toast), 0).show();
            }
        }

        private void saveCheckState(boolean z) {
            this.mAirTriggerUtils.setIncreaseSensitivity(z);
        }

        private void updateCheckState() {
            this.mCkbSensitivity.setChecked(this.mAirTriggerUtils.isIncreaseSensitivity());
            this.mCkbSensitivity.setOnCheckedChangeListener(this.mCheckListener);
        }

        @Override
        public void onCreate(Bundle bundle) {
            Log.d("TapStrengthAdjust", "onCreate");
            super.onCreate(bundle);
            this.mAirTriggerUtils = AirTriggerUtils.getInstance(getContext());
            this.mDockEventReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        int unused = TapStrengthAdjust.this.mDockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                    }
                }
            };
            Intent registerReceiver = getContext().registerReceiver(this.mDockEventReceiver, new IntentFilter("android.intent.action.DOCK_EVENT"));
            if (registerReceiver != null) {
                this.mDockState = registerReceiver.getIntExtra("android.intent.extra.DOCK_STATE", 0);
            }
            IntentFilter intentFilter = new IntentFilter("org.omnirom.device.NOTIFY_TAP_UI_UPDATE");
            intentFilter.addAction("org.omnirom.device.NOTIFY_TAP_ANIMATE");
            intentFilter.addAction("org.omnirom.device.NOTIFY_BACK_KEY");
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.d("TapStrengthAdjust", "on receive tap state ");
                    if (action.equals("org.omnirom.device.NOTIFY_TAP_UI_UPDATE")) {
                        TapStrengthAdjust.this.refreshSeekBar(intent.getIntExtra("tap_press", 0), intent.getIntExtra("tap_side", 0));
                    } else if (action.equals("org.omnirom.device.NOTIFY_TAP_ANIMATE")) {
                        TapStrengthAdjust.this.startThumbAnim(intent.getIntExtra("tap_side", 0), false);
                    } else if (!action.equals("org.omnirom.device.NOTIFY_BACK_KEY")) {
                    } else {
                        if (TapStrengthAdjust.this.mCurrentState == 4) {
                            TapStrengthAdjust.this.getActivity().finish();
                        } else {
                            TapStrengthAdjust.this.mCancelButton.performClick();
                        }
                    }
                }
            };
            getContext().registerReceiver(this.mBroadcastReceiver, intentFilter);
            restoreSavedInstance(bundle);
            this.mPreferences = getContext().getSharedPreferences("air_trigger_first_setting_strength", 0);
            if (this.mPreferences.getBoolean("first_setting_tap_strength", true)) {
                this.mCurrentState = 0;
            } else {
                this.mCurrentState = 4;
            }
            this.mHasTouchSenseFeature = getContext().getPackageManager().hasSystemFeature("asus.hardware.touchsense");
            this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        }

        private void updateLayoutHeight(boolean z) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.tap_adjust_desc_height);
            if (z) {
                dimensionPixelSize -= AirTriggerUtils.getDtDockNavigationBarHeight(getContext());
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mLlDesc.getLayoutParams();
            layoutParams.height = dimensionPixelSize;
            this.mLlDesc.setLayoutParams(layoutParams);
        }

        private void updateInitIndex() {
            this.mInitialIndexLeft = this.mAirTriggerUtils.getTapThresholdLevel(1) - 1;
            this.mCurrentIndexLeft = this.mInitialIndexLeft;
            this.mInitialIndexRight = this.mAirTriggerUtils.getTapThresholdLevel(2) - 1;
            this.mCurrentIndexRight = this.mInitialIndexRight;
        }

        private void initTapThresholdForAnim() {
            this.mAirTriggerUtils.setTapThreshold(1, 1);
            this.mAirTriggerUtils.setTapThreshold(1, 2);
        }

        private void initThumbAnim() {
            this.mSeekBarAnimator = ObjectAnimator.ofInt(this.mSeekBarThumbBg, "alpha", 127, 0);
            this.mSeekBarAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mSeekBarAnimator.setDuration(1800L);
            this.mSeekBarLeftAnimator = ObjectAnimator.ofInt(this.mSeekBarDefaultLeftThumbBg, "alpha", 127, 0);
            this.mSeekBarLeftAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mSeekBarLeftAnimator.setDuration(1800L);
            this.mSeekBarLeftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    TapStrengthAdjust.this.mSeekBar.invalidate();
                }
            });
        }

        private void startThumbAnim(int i, boolean z) {
            int i2 = -1;
            if (i != 1 || this.mCurrentIndexLeft == this.mCurrentIndexRight) {
                ObjectAnimator objectAnimator = this.mSeekBarAnimator;
                if (!z) {
                    i2 = 0;
                }
                objectAnimator.setRepeatCount(i2);
                this.mSeekBarAnimator.start();
                return;
            }
            ObjectAnimator objectAnimator2 = this.mSeekBarLeftAnimator;
            if (!z) {
                i2 = 0;
            }
            objectAnimator2.setRepeatCount(i2);
            this.mSeekBarLeftAnimator.start();
        }

        private void stopThumbAnim(int i) {
            if (i == 1) {
                this.mSeekBarDefaultLeftThumbBg.setAlpha(0);
                if (this.mSeekBarLeftAnimator.isStarted()) {
                    this.mSeekBarLeftAnimator.end();
                    return;
                }
                return;
            }
            this.mSeekBarThumbBg.setAlpha(0);
            if (this.mSeekBarAnimator.isStarted()) {
                this.mSeekBarAnimator.end();
            }
        }

        private boolean isThumbOverlap() {
            if (this.mCurrentState == 1 && this.mCurrentIndexLeft == this.mInitialIndexLeft) {
                return true;
            }
            if (this.mCurrentState == 3 && this.mCurrentIndexRight == this.mInitialIndexRight) {
                return true;
            }
            return false;
        }

        private void notifyTapVibrate(boolean z) {
            Log.d("TapStrengthAdjust", "notify GripSensorService to vibrate or not");
            Intent intent = new Intent();
            intent.setAction("adjust_vibration");
            intent.setPackage("org.omnirom.device");
            intent.putExtra("can_vibrate", z);
            getContext().sendBroadcast(intent);
        }

        private void refreshSeekBar(int i, int i2) {
            int i3;
            Log.d("TapStrengthAdjust", "refreshSeekBar pressure=" + i + " side=" + i2);
            int i4 = this.mCurrentState;
            if (i4 == 4) {
                return;
            }
            if (i2 != 1 || (i4 != 2 && i4 != 3)) {
                if (i2 != 2 || ((i3 = this.mCurrentState) != 0 && i3 != 1)) {
                    int i5 = 0;
                    while (true) {
                        int[] iArr = this.mValues;
                        if (i5 < iArr.length) {
                            if (i == iArr[i5]) {
                                this.mSeekBar.setProgress(i5);
                            } else if (i < iArr[i5]) {
                                this.mSeekBar.setProgress(i5 - 1);
                                return;
                            } else if (i >= iArr[iArr.length - 1]) {
                                this.mSeekBar.setProgress(iArr.length - 1);
                            }
                            i5++;
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle bundle) {
            Log.d("TapStrengthAdjust", "onSaveInstanceState");
            super.onSaveInstanceState(bundle);
            bundle.putInt("current_state", this.mCurrentState);
        }

        private void restoreSavedInstance(Bundle bundle) {
            Log.d("TapStrengthAdjust", "restoreSavedInstance");
            if (bundle != null) {
                this.mCurrentState = bundle.getInt("current_state", 0);
            } else {
                this.mCurrentState = 0;
            }
        }

        @Override
        public void onResume() {
            Log.d("TapStrengthAdjust", "onResume");
            super.onResume();
            setTapEnable();
            if (this.mCurrentState != 4) {
                initTapThresholdForAnim();
            }
            refreshUiByState();
        }

        private void setTapEnable() {
            int i = this.mCurrentState;
            if (i == 0 || i == 1) {
                this.mAirTriggerUtils.setTapEnable(1, true);
                this.mAirTriggerUtils.setTapEnable(2, false);
            } else if (i == 2 || i == 3) {
                this.mAirTriggerUtils.setTapEnable(1, false);
                this.mAirTriggerUtils.setTapEnable(2, true);
            } else {
                this.mAirTriggerUtils.setTapEnable(1, true);
                this.mAirTriggerUtils.setTapEnable(2, true);
            }
            this.mAirTriggerUtils.setRawDataEnable(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (this.mCurrentState != 4) {
                resetToPreValue();
            }
        }

        @Override
        public void onDestroy() {
            Log.d("TapStrengthAdjust", "onDestroy");
            super.onDestroy();
            getContext().unregisterReceiver(this.mBroadcastReceiver);
            getContext().unregisterReceiver(this.mDockEventReceiver);
            this.mAirTriggerUtils = null;
        }

        private void refreshUiByState() {
            updateTapSideToService();
            updateTitle();
            updateImg();
            updateSeekBarTint();
            updateDefaultThumb();
            updateSeekBarState();
            updateSeekBarProgress();
            updateHintText();
            updateButton();
        }

        private void tintSeekbar(int i) {
            Utils.tintSeekbar(getContext(), this.mSeekBar, i);
            mSeekBar.getThumb().clearColorFilter();
            Utils.tintDrawable(((LayerDrawable) this.mSeekBar.getThumb()).getDrawable(2), i);
            Utils.tintDrawable(((LayerDrawable) this.mSeekBar.getThumb()).getDrawable(1), i);
            Utils.tintDrawable(((LayerDrawable) this.mSeekBar.getThumb()).getDrawable(0), i);
            Utils.tintDrawable(this.mDrawableIntroOld, i);
            Utils.tintDrawable(((LayerDrawable) this.mDrawableIntroNew).getDrawable(1), i);
            Utils.tintDrawable(((LayerDrawable) this.mDrawableIntroNew).getDrawable(0), i);
            Utils.tintDrawable(this.mSeekBar.getDefaultThumb(), i);
        }

        private void updateSeekBarTint() {
            this.mSeekBar.setIsOverlap(isThumbOverlap());
            int i = this.mCurrentState;
            if (i == 0 || i == 1) {
                tintSeekbar(this.mRes.getColor(R.color.grip_sensor_tap_left_color, null));
            } else if (i == 2 || i == 3) {
                tintSeekbar(this.mRes.getColor(R.color.grip_sensor_tap_right_color, null));
            } else {
                tintSeekbar(this.mRes.getColor(R.color.grip_sensor_tap_right_color, null));
            }
        }

        private void updateDefaultThumb() {
            int i = this.mCurrentState;
            if (i == 0 || i == 2 || i == 4) {
                this.mSeekBar.getDefaultThumb().setAlpha(255);
            } else {
                this.mSeekBar.getDefaultThumb().setAlpha(204);
            }
        }

        private void updateImg() {
            int i = this.mCurrentState;
            if (i == 0 || i == 1) {
                this.mImgPic.setImageResource(R.drawable.asus_pic_settings_air_trigger_hotkey_left);
            } else if (i == 2 || i == 3) {
                this.mImgPic.setImageResource(R.drawable.asus_pic_settings_air_trigger_hotkey_right);
            } else {
                this.mImgPic.setImageResource(R.drawable.asus_pic_settings_air_trigger_hotkey);
            }
        }

        private void updateTitle() {
            int i = this.mCurrentState;
            if (i == 0 || i == 1) {
                this.mTitle.setText(R.string.title_tap_left);
            } else if (i == 2 || i == 3) {
                this.mTitle.setText(R.string.title_tap_right);
            } else {
                this.mTitle.setText(R.string.title_tap_try_force);
            }
        }

        private void updateSeekBarProgress() {
            int i = this.mCurrentState;
            if (i == 0 || i == 2) {
                stopThumbAnim(2);
                this.mSeekBar.setOnSeekBarChangeListener(null);
                this.mSeekBar.setLevel(this.mCurrentState == 0 ? this.mInitialIndexLeft : this.mInitialIndexRight);
                this.mSeekBar.setProgress(this.mCurrentState == 0 ? this.mInitialIndexLeft : this.mInitialIndexRight);
                this.mSeekBar.setOnSeekBarChangeListener(this.mListener);
            } else if (i == 1 || i == 3) {
                this.mSeekBar.setLevel(this.mCurrentState == 1 ? this.mInitialIndexLeft : this.mInitialIndexRight);
                this.mSeekBar.setProgress(this.mCurrentState == 1 ? this.mCurrentIndexLeft : this.mCurrentIndexRight);
            } else {
                stopThumbAnim(2);
                int i2 = this.mCurrentIndexRight;
                int i3 = this.mCurrentIndexLeft;
                if (i2 != i3) {
                    this.mSeekBar.setLeftDefLevel(i3);
                    this.mSeekBar.showLeftDefThumb(true);
                } else {
                    this.mSeekBar.showLeftDefThumb(false);
                }
                this.mSeekBar.setLevel(this.mCurrentIndexRight);
                this.mSeekBar.setProgress(this.mCurrentIndexRight);
            }
        }

        private void updateSeekBarState() {
            if (this.mCurrentState == 4) {
                this.mSeekBar.setEnabled(false);
                notifyTapVibrate(true);
                return;
            }
            this.mSeekBar.showLeftDefThumb(false);
            this.mSeekBar.setEnabled(true);
            notifyTapVibrate(false);
        }

        private void updateHintText() {
            int i = this.mCurrentState;
            if (i == 0 || i == 2) {
                this.mTxtGripDesc.setText(R.string.gripsensor_tap_strength_adjust_desc);
                this.mCkbSensitivity.setVisibility(0);
                this.mLlIntro.setVisibility(0);
                this.mTxtIntroOld.setVisibility(8);
                this.mTxtIntroNew.setVisibility(8);
            } else if (i == 1 || i == 3) {
                this.mTxtGripDesc.setText(R.string.gripsensor_tap_strength_adjust_desc);
                this.mCkbSensitivity.setVisibility(0);
                this.mLlIntro.setVisibility(0);
                this.mTxtIntroOld.setVisibility(0);
                this.mTxtIntroNew.setVisibility(0);
            } else {
                this.mTxtGripDesc.setText(R.string.grip_notifi_success);
                this.mCkbSensitivity.setVisibility(8);
                this.mLlIntro.setVisibility(8);
            }
        }

        private void updateButton() {
            int i = this.mCurrentState;
            if (i == 0 || i == 1) {
                this.mCancelButton.setEnabled(true);
                this.mCancelButton.setText(R.string.cancel);
                this.mDoneButton.setEnabled(true);
                this.mDoneButton.setText(R.string.btn_next);
            } else if (i == 2 || i == 3) {
                this.mCancelButton.setEnabled(true);
                this.mCancelButton.setText(R.string.btn_back);
                this.mDoneButton.setEnabled(true);
                this.mDoneButton.setText(R.string.btn_apply);
            } else {
                this.mCancelButton.setEnabled(true);
                this.mCancelButton.setText(R.string.btn_reset);
                this.mDoneButton.setEnabled(true);
                this.mDoneButton.setText(R.string.okay);
            }
        }

        private void commit() {
            this.mAirTriggerUtils.setTapThreshold(this.mCurrentIndexLeft + 1, 1);
            this.mAirTriggerUtils.setTapThreshold(this.mCurrentIndexRight + 1, 2);
        }

        private void resetToPreValue() {
            this.mAirTriggerUtils.setTapThreshold(this.mInitialIndexLeft + 1, 1);
            this.mAirTriggerUtils.setTapThreshold(this.mInitialIndexRight + 1, 2);
        }

        private class OnGripSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
            private boolean mSeekByTouch;

            private OnGripSeekBarChangeListener() {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                Log.d("TapStrengthAdjust", "onProgressChanged");
                if (TapStrengthAdjust.this.mCurrentState == 0 || TapStrengthAdjust.this.mCurrentState == 1) {
                    int unused = TapStrengthAdjust.this.mCurrentIndexLeft = i;
                    int unused2 = TapStrengthAdjust.this.mCurrentState = 1;
                    statisticAdjusstStrength(0, i);
                } else if (TapStrengthAdjust.this.mCurrentState == 2 || TapStrengthAdjust.this.mCurrentState == 3) {
                    int unused3 = TapStrengthAdjust.this.mCurrentIndexRight = i;
                    int unused4 = TapStrengthAdjust.this.mCurrentState = 3;
                    statisticAdjusstStrength(1, i);
                }
                if (TapStrengthAdjust.this.mCurrentState != 4) {
                    TapStrengthAdjust.this.startThumbAnim(2, true);
                }
                OmniVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, getContext());
                TapStrengthAdjust.this.refreshUiByState();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("TapStrengthAdjust", "onStartTrackingTouch");
                this.mSeekByTouch = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("TapStrengthAdjust", "onStopTrackingTouch");
                this.mSeekByTouch = false;
            }

            private void statisticAdjusstStrength(int i, int i2) {
                int tapValue = TapStrengthAdjust.this.mAirTriggerUtils.getTapValue(i2 + 1);
                if (tapValue < TapStrengthAdjust.this.mAdjustMinStrength[i]) {
                    TapStrengthAdjust.this.mAdjustMinStrength[i] = tapValue;
                }
                if (tapValue > TapStrengthAdjust.this.mAdjustMaxStrength[i]) {
                    TapStrengthAdjust.this.mAdjustMaxStrength[i] = tapValue;
                }
            }
        }

        private void updateTapSideToService() {
            int i = this.mCurrentState;
            if (i == 0 || i == 1) {
                notifyGripServiceTapSide(1);
            } else if (i == 2 || i == 3) {
                notifyGripServiceTapSide(2);
            } else {
                notifyGripServiceTapSide(3);
            }
        }

        private void notifyGripServiceTapSide(int i) {
            Log.d("TapStrengthAdjust", "notify GripService to enable which tap side or both, side =" + i);
            Intent intent = new Intent();
            intent.setAction("org.omnirom.device.NOTIFY_TAP_SIDE");
            intent.setPackage("org.omnirom.device");
            intent.putExtra("tap_side", i);
            getActivity().sendBroadcast(intent);
        }
}
