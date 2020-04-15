package org.omnirom.device.gripsensor;

import android.app.Activity;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import org.omnirom.device.R;
import org.omnirom.device.Utils;
import org.omnirom.omnilib.utils.OmniVibe;

public class GripStrengthAdjust extends PreferenceFragment {
        private AirTriggerUtils mAirTriggerUtils;
        private BroadcastReceiver mBroadcastReceiver;
        private LinearLayout mButtonBar;
        protected Button mCancelButton;
        private int mCurrentIndex;
        private int mCurrentState;
        private BroadcastReceiver mDockEventReceiver;
        private int mDockState = 0;
        protected Button mDoneButton;
        private GradientDrawable mDrawableLeft;
        private GradientDrawable mDrawableRight;
        private String[] mEntries;
        private GifView mGifView;
        //private boolean mHasTouchSenseFeature;
        private ImageView mImgLeft;
        private ImageView mImgRight;
        private ImageView mImgTutorial;
        private int mInitialIndex;
        private LinearLayout mLlIntro;
        private int mMaxAdjustSqueezeValue = 0;
        private float mMaxRadius;
        private int mMinAdjustSqueezeValue = 255;
        private final View.OnApplyWindowInsetsListener mOnApplyWindowInsetsListener = new View.OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                if (!AirTriggerUtils.isTwinViewModeEnabled(GripStrengthAdjust.this.getContext()) || GripStrengthAdjust.this.mDockState == 7 || windowInsets.getStableInsetBottom() != 0) {
                    GripStrengthAdjust.this.updateButtonPos(false);
                } else {
                    GripStrengthAdjust.this.updateButtonPos(true);
                }
                return windowInsets;
            }
        };
        private Resources mRes;
        private SqueezeLevelSeekBar mSeekBar;
        private ObjectAnimator mSeekBarAnimator;
        private Drawable mSeekBarThumbBg;
        private TextView mTxtGripDesc;
        private int[] mValues;
        private Vibrator mVibrator;
        private SharedPreferences preferences = null;
        private Animation scaleAnimationLeft;
        private Animation scaleAnimationRight;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {}

        @Override // org.omnirom.device.SettingsPreferenceFragment, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
            Log.d("GripStrengthAdjust", "onCreateView");
            getActivity().getActionBar().setTitle(R.string.gripsensor_grip_strength_adjust);
            this.mAirTriggerUtils = AirTriggerUtils.getInstance(getContext());
            View inflate = layoutInflater.inflate(R.layout.grip_strength_adjust_page, viewGroup, false);
            TextView textView = (TextView) inflate.findViewById(R.id.txt_high);
            TextView textView2 = (TextView) inflate.findViewById(R.id.txt_low);
            TextView textView3 = (TextView) inflate.findViewById(R.id.txt_intro_old);
            TextView textView4 = (TextView) inflate.findViewById(R.id.txt_intro_new);
            Drawable drawable = getResources().getDrawable(R.drawable.grip_seekbar_default_thumb);
            Drawable drawable2 = getResources().getDrawable(R.drawable.grip_seekbar_thumb);
            this.mImgTutorial = (ImageView) inflate.findViewById(R.id.img_tutorial);
            this.mGifView = (GifView) inflate.findViewById(R.id.tutorial_gif);
            this.mGifView.setGifResource(getContext(), R.drawable.ic_asus_pic_settings_grip_sensor_gif);
            this.mImgTutorial.setVisibility(0);
            this.mGifView.setVisibility(8);
            this.mRes = getContext().getResources();
            this.mButtonBar = (LinearLayout) inflate.findViewById(R.id.button_bar);
            updateInitIndex();
            this.mValues = this.mAirTriggerUtils.getSqueezeArray();
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
            this.mSeekBar.setLabels(this.mEntries);
            this.mSeekBar.setMax(Math.max(1, this.mEntries.length - 1));
            this.mSeekBar.setProgress(this.mInitialIndex);
            this.mSeekBar.setOnSeekBarChangeListener(new onGripSeekBarChangeListener());
            this.mSeekBar.setLevel(this.mInitialIndex);
            textView.setText(String.valueOf(this.mEntries.length));
            textView2.setText("1");
            this.mTxtGripDesc = (TextView) inflate.findViewById(R.id.txt_desc);
            this.mLlIntro = (LinearLayout) inflate.findViewById(R.id.ll_intro);
            drawable.setBounds(0, 0, 40, 40);
            drawable2.setBounds(0, 0, 40, 40);
            textView3.setCompoundDrawables(drawable, null, null, null);
            textView4.setCompoundDrawables(drawable2, null, null, null);
            this.mCancelButton = (Button) inflate.findViewById(R.id.cancel_button);
            this.mCancelButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (GripStrengthAdjust.this.mCurrentState == 2) {
                        int unused = GripStrengthAdjust.this.mCurrentState = 0;
                        GripStrengthAdjust.this.updateInitIndex();
                        GripStrengthAdjust.this.refreshUiByState();
                        return;
                    }
                    GripStrengthAdjust.this.getActivity().setResult(0, new Intent());
                    GripStrengthAdjust.this.getActivity().finish();
                }
            });
            this.mCancelButton.setVisibility(0);
            this.mDoneButton = (Button) inflate.findViewById(R.id.done_button);
            this.mDoneButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (GripStrengthAdjust.this.mCurrentState == 2) {
                        GripStrengthAdjust.this.getActivity().setResult(-1, new Intent());
                        GripStrengthAdjust.this.getActivity().finish();
                        return;
                    }
                    GripStrengthAdjust.this.commit();
                    int unused = GripStrengthAdjust.this.mCurrentState = 2;
                    GripStrengthAdjust.this.refreshUiByState();
                }
            });
            this.mImgLeft = (ImageView) inflate.findViewById(R.id.img_left);
            this.mImgRight = (ImageView) inflate.findViewById(R.id.img_right);
            ((RelativeLayout.LayoutParams) this.mImgLeft.getLayoutParams()).setMargins(0, ((int) getResources().getDimension(R.dimen.gripsensor_hint_margin_top)) + 250, 0, 0);
            ((RelativeLayout.LayoutParams) this.mImgRight.getLayoutParams()).setMargins(0, ((int) getResources().getDimension(R.dimen.gripsensor_hint_margin_top)) + 250, 0, 0);
            this.mDrawableLeft = (GradientDrawable) this.mImgLeft.getDrawable();
            this.mDrawableRight = (GradientDrawable) this.mImgRight.getDrawable();
            this.mMaxRadius = getContext().getResources().getDimension(R.dimen.gripsensor_gradient_radius);
            this.scaleAnimationLeft = AnimationUtils.loadAnimation(getContext(), R.anim.grip_position_hint_anim_left);
            this.scaleAnimationRight = AnimationUtils.loadAnimation(getContext(), R.anim.grip_position_hint_anim_right);
            initThumbAnim();
            this.mImgLeft.startAnimation(this.scaleAnimationLeft);
            this.mImgRight.startAnimation(this.scaleAnimationRight);
            return inflate;
        }

        private void updateButtonPos(boolean z) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.gripsensor_btn_margin_top);
            if (z) {
                dimensionPixelSize -= AirTriggerUtils.getDtDockNavigationBarHeight(getContext());
            }
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mButtonBar.getLayoutParams();
            layoutParams.setMargins(0, dimensionPixelSize, 0, 0);
            this.mButtonBar.setLayoutParams(layoutParams);
        }

        private void initThumbAnim() {
            this.mSeekBarAnimator = ObjectAnimator.ofInt(this.mSeekBarThumbBg, "alpha", 127, 0);
            this.mSeekBarAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mSeekBarAnimator.setDuration(1800L);
        }

        private void startThumbAnim(boolean z) {
            this.mSeekBarAnimator.setRepeatCount(z ? -1 : 0);
            this.mSeekBarAnimator.start();
        }

        private void stopThumbAnim() {
            this.mSeekBarThumbBg.setAlpha(0);
            if (this.mSeekBarAnimator.isStarted()) {
                this.mSeekBarAnimator.end();
            }
        }

        private void updateInitIndex() {
            this.mInitialIndex = this.mAirTriggerUtils.getSqueezeThresholdLevel() - 1;
            this.mCurrentIndex = this.mInitialIndex;
        }

        private void setGripPosHintColor(int i) {
            this.mDrawableLeft.setColors(new int[]{i, 0});
            this.mDrawableRight.setColors(new int[]{i, 0});
        }

        @Override // org.omnirom.device.SettingsPreferenceFragment, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment, org.omnirom.devicelib.core.lifecycle.ObservablePreferenceFragment
        public void onCreate(Bundle bundle) {
            Log.d("GripStrengthAdjust", "onCreate");
            super.onCreate(bundle);
            this.mDockEventReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        int unused = GripStrengthAdjust.this.mDockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                    }
                }
            };
            Intent registerReceiver = getContext().registerReceiver(this.mDockEventReceiver, new IntentFilter("android.intent.action.DOCK_EVENT"));
            if (registerReceiver != null) {
                this.mDockState = registerReceiver.getIntExtra("android.intent.extra.DOCK_STATE", 0);
            }
            IntentFilter intentFilter = new IntentFilter("org.omnirom.device.NOTIFY_GRIP_UI_UPDATE");
            intentFilter.addAction("org.omnirom.device.NOTIFY_GRIP_ACTIVITY_HIDE_HINT_POSITION");
            intentFilter.addAction("org.omnirom.device.NOTIFY_GRIP_ANIMATE");
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.d("GripStrengthAdjust", "on receive grip state, action =" + action);
                    if (action.equals("org.omnirom.device.NOTIFY_GRIP_UI_UPDATE")) {
                        GripStrengthAdjust.this.refreshSeekBar(intent.getIntExtra("grip_press", 0));
                    } else if (action.equals("org.omnirom.device.NOTIFY_GRIP_ACTIVITY_HIDE_HINT_POSITION")) {
                        GripStrengthAdjust.this.mImgLeft.clearAnimation();
                        GripStrengthAdjust.this.mImgLeft.setVisibility(8);
                        GripStrengthAdjust.this.mImgRight.clearAnimation();
                        GripStrengthAdjust.this.mImgRight.setVisibility(8);
                    } else if (action.equals("org.omnirom.device.NOTIFY_GRIP_ANIMATE")) {
                        GripStrengthAdjust.this.startThumbAnim(false);
                    }
                }
            };
            getContext().registerReceiver(this.mBroadcastReceiver, intentFilter);
            restoreSavedInstance(bundle);
            this.mCurrentState = 0;
            //this.mHasTouchSenseFeature = getContext().getPackageManager().hasSystemFeature("asus.hardware.touchsense");
            this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        }

        private void updateDefaultThumb() {
            this.mSeekBar.setIsOverlap(this.mCurrentIndex == this.mInitialIndex || this.mCurrentState == 2);
            //this.mSeekBar.getDefaultThumb().setColorFilter(Utils.getThemeColorCode(getContext()), PorterDuff.Mode.SRC_IN);
            int i = this.mCurrentState;
            if (i == 0 || i == 2) {
                this.mSeekBar.getDefaultThumb().setAlpha(255);
            } else {
                this.mSeekBar.getDefaultThumb().setAlpha(204);
            }
        }

        private void correctSeekBarPos() {
            this.mSeekBar.setLevel(this.mCurrentIndex);
            this.mSeekBar.invalidate();
            stopThumbAnim();
        }

        private void notifyGripVibrate(boolean z) {
            Log.d("GripStrengthAdjust", "notify GripSensorService to vibrate or not");
            Intent intent = new Intent();
            intent.setAction("adjust_vibration");
            intent.setPackage("org.omnirom.device");
            intent.putExtra("can_vibrate", z);
            getContext().sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }

        private void refreshSeekBar(int i) {
            Log.d("GripStrengthAdjust", "refreshSeekBar pressure=" + i);
            if (this.mCurrentState != 2) {
                int i2 = 0;
                while (true) {
                    int[] iArr = this.mValues;
                    if (i2 < iArr.length) {
                        if (i == iArr[i2]) {
                            this.mSeekBar.setProgress(i2);
                        } else if (i < iArr[i2]) {
                            this.mSeekBar.setProgress(i2 - 1);
                            return;
                        } else if (i >= iArr[iArr.length - 1]) {
                            this.mSeekBar.setProgress(iArr.length - 1);
                        }
                        i2++;
                    } else {
                        return;
                    }
                }
            }
        }

        @Override // org.omnirom.device.SettingsPreferenceFragment, androidx.preference.PreferenceFragmentCompat, androidx.fragment.app.Fragment, org.omnirom.devicelib.core.lifecycle.ObservablePreferenceFragment
        public void onSaveInstanceState(Bundle bundle) {
            Log.d("GripStrengthAdjust", "onSaveInstanceState");
            super.onSaveInstanceState(bundle);
            bundle.putInt("current_state", this.mCurrentState);
        }

        private void restoreSavedInstance(Bundle bundle) {
            Log.d("GripStrengthAdjust", "restoreSavedInstance");
            if (bundle != null) {
                this.mCurrentState = bundle.getInt("current_state", 0);
            } else {
                this.mCurrentState = 0;
            }
        }

        @Override // org.omnirom.device.SettingsPreferenceFragment, org.omnirom.device.core.InstrumentedPreferenceFragment, androidx.fragment.app.Fragment, org.omnirom.devicelib.core.lifecycle.ObservablePreferenceFragment
        public void onResume() {
            Log.d("GripStrengthAdjust", "onResume");
            super.onResume();
            refreshUiByState();
            checkTwinView();
        }

        private void checkTwinView() {
            if (!AirTriggerUtils.isTwinViewModeEnabled(getContext()) || this.mDockState == 7) {
                updateButtonPos(false);
            } else {
                updateButtonPos(true);
            }
        }

        @Override // androidx.fragment.app.Fragment, org.omnirom.devicelib.core.lifecycle.ObservablePreferenceFragment
        public void onPause() {
            Log.d("GripStrengthAdjust", "onPause");
            super.onPause();
        }

        @Override // org.omnirom.device.SettingsPreferenceFragment, androidx.fragment.app.Fragment, org.omnirom.devicelib.core.lifecycle.ObservablePreferenceFragment
        public void onDestroy() {
            Log.d("GripStrengthAdjust", "onDestroy");
            super.onDestroy();
            getContext().unregisterReceiver(this.mBroadcastReceiver);
            getContext().unregisterReceiver(this.mDockEventReceiver);
            this.mAirTriggerUtils = null;
        }

        private void refreshUiByState() {
            updateDefaultThumb();
            updateSeekBarState();
            updateHintText();
            updateButton();
        }

        private void updateSeekBarState() {
            if (this.mCurrentState == 2) {
                correctSeekBarPos();
                this.mSeekBar.setEnabled(false);
                notifyGripVibrate(true);
                return;
            }
            this.mSeekBar.setEnabled(true);
            notifyGripVibrate(false);
        }

        private void updateHintText() {
            int i = this.mCurrentState;
            if (i == 0) {
                this.mTxtGripDesc.setText(R.string.gripsensor_grip_strength_adjust_desc);
                this.mLlIntro.setVisibility(8);
            } else if (i == 1) {
                this.mLlIntro.setVisibility(0);
            } else if (i == 2) {
                this.mTxtGripDesc.setText(R.string.grip_notifi_success);
                this.mLlIntro.setVisibility(8);
            }
        }

        private void updateButton() {
            int i = this.mCurrentState;
            if (i == 0) {
                this.mCancelButton.setEnabled(true);
                this.mCancelButton.setText(R.string.cancel);
                this.mDoneButton.setEnabled(false);
                this.mDoneButton.setText(R.string.btn_apply);
            } else if (i == 1) {
                this.mCancelButton.setEnabled(true);
                this.mDoneButton.setEnabled(true);
            } else if (i == 2) {
                this.mCancelButton.setEnabled(true);
                this.mCancelButton.setText(R.string.btn_reset);
                this.mDoneButton.setEnabled(true);
                this.mDoneButton.setText(R.string.done);
            }
        }

        private void commit() {
            this.mAirTriggerUtils.setSqueezeThreshold(this.mCurrentIndex + 1);
        }

        private class onGripSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
            private boolean mSeekByTouch;

            private onGripSeekBarChangeListener() {
                this.mSeekByTouch = false;
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                Log.d("GripStrengthAdjust", "onProgressChanged");
                int unused = GripStrengthAdjust.this.mCurrentIndex = i;
                int unused2 = GripStrengthAdjust.this.mCurrentState = 1;
                OmniVibe.performHapticFeedbackLw(HapticFeedbackConstants.LONG_PRESS, false, getContext());
                GripStrengthAdjust.this.startThumbAnim(true);
                GripStrengthAdjust.this.refreshUiByState();
                statisticSqueezeStrength(i);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("GripStrengthAdjust", "onStartTrackingTouch");
                this.mSeekByTouch = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("GripStrengthAdjust", "onStopTrackingTouch");
                this.mSeekByTouch = false;
            }

            private void statisticSqueezeStrength(int i) {
                int squeezeValue = GripStrengthAdjust.this.mAirTriggerUtils.getSqueezeValue(i + 1);
                if (squeezeValue < GripStrengthAdjust.this.mMinAdjustSqueezeValue) {
                    int unused = GripStrengthAdjust.this.mMinAdjustSqueezeValue = squeezeValue;
                }
                if (squeezeValue > GripStrengthAdjust.this.mMaxAdjustSqueezeValue) {
                    int unused2 = GripStrengthAdjust.this.mMaxAdjustSqueezeValue = squeezeValue;
                }
            }
        }
    }
