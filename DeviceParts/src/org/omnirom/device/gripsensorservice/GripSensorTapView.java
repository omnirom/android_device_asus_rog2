package org.omnirom.device.gripsensorservice;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import org.omnirom.device.R;

public class GripSensorTapView extends LinearLayout {
    private View mContentView = null;
    private Context mContext;
    private GradientDrawable mDrawableLeft;
    private GradientDrawable mDrawableRight;
    private ImageView mTapLeft;
    private ImageView mTapRight;
    private ImageView mTapShadowLeft;
    private ImageView mTapShadowRight;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;

    public GripSensorTapView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    private void init() {
        this.mWindowManager = (WindowManager) this.mContext.getApplicationContext().getSystemService("window");
        this.mWindowLayoutParams = getWindowLayoutParams();
        initView();
        this.mWindowManager.addView(this, this.mWindowLayoutParams);
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 26) {
            layoutParams.type = 2038;
        } else {
            layoutParams.type = 2002;
        }
        layoutParams.format = 1;
        layoutParams.flags = 312;
        layoutParams.gravity = 17;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = -1;
        layoutParams.height = -1;
        return layoutParams;
    }

    private void initView() {
        View view = this.mContentView;
        if (view != null) {
            removeView(view);
        }
        this.mContentView = LayoutInflater.from(this.mContext).inflate(R.layout.grip_sensor_tap_view, (ViewGroup) null);
        this.mTapLeft = (ImageView) this.mContentView.findViewById(R.id.tap_left);
        this.mTapRight = (ImageView) this.mContentView.findViewById(R.id.tap_right);
        this.mTapShadowLeft = (ImageView) this.mContentView.findViewById(R.id.tap_shadow_left);
        this.mTapShadowRight = (ImageView) this.mContentView.findViewById(R.id.tap_shadow_right);
        this.mDrawableLeft = (GradientDrawable) this.mTapShadowLeft.getDrawable();
        this.mDrawableRight = (GradientDrawable) this.mTapShadowRight.getDrawable();
        if (getDeviceOrientation() != 2) {
            setVisibility(8);
        } else {
            setVisibility(0);
        }
        ((RelativeLayout.LayoutParams) this.mTapRight.getLayoutParams()).setMargins(((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_right_tap_view_margin_left)) + 170, (int) this.mContext.getResources().getDimension(R.dimen.gripsensor_tap_view_margin_top), 0, 0);
        ((RelativeLayout.LayoutParams) this.mTapShadowRight.getLayoutParams()).setMargins(((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_right_tap_view_shadow_margin_left)) + 170, 0, 0, 0);
        addView(this.mContentView, -1, -1);
    }

    public void show(int i) {
        setVisibility(0);
        if (i == 1) {
            Log.d("GripSensorTapView", "LEFT show");
            this.mTapLeft.setVisibility(0);
            this.mTapShadowLeft.setVisibility(0);
            updateDrawable(this.mTapShadowLeft, this.mDrawableLeft);
            return;
        }
        Log.d("GripSensorTapView", "RIGHT show");
        this.mTapRight.setVisibility(0);
        this.mTapShadowRight.setVisibility(0);
        updateDrawable(this.mTapShadowRight, this.mDrawableRight);
    }

    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (getDeviceOrientation() != 2) {
            setVisibility(8);
        } else {
            setVisibility(0);
        }
    }

    private int getDeviceOrientation() {
        int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
        if (rotation == 1) {
            return 2;
        }
        return rotation == 3 ? 3 : 1;
    }

    public void hide(int i) {
        if (i == 1) {
            Log.d("GripSensorTapView", "LEFT hide");
            this.mTapLeft.setVisibility(8);
            this.mTapShadowLeft.setVisibility(8);
            return;
        }
        Log.d("GripSensorTapView", "RIGHT hide");
        this.mTapRight.setVisibility(8);
        this.mTapShadowRight.setVisibility(8);
    }

    public void hide() {
        Log.d("GripSensorTapView", "hide");
        setVisibility(8);
    }

    private void updateDrawable(ImageView imageView, GradientDrawable gradientDrawable) {
        gradientDrawable.setGradientRadius(this.mContext.getResources().getDisplayMetrics().density * 45.0f);
        imageView.setAlpha(0.3f);
    }

    public void release() {
        if (this.mWindowManager != null && this.mContentView != null) {
            Log.d("GripSensorTapView", "release");
            this.mWindowManager.removeView(this);
        }
    }
}
