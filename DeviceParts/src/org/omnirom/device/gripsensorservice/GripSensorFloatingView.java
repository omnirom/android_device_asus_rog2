package org.omnirom.device.gripsensorservice;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import org.omnirom.device.R;

public class GripSensorFloatingView extends LinearLayout {
    private final int MAX = 255;
    private View mContentView = null;
    private Context mContext;
    GradientDrawable mDrawableLeft;
    GradientDrawable mDrawableRight;
    private ImageView mImgLeft;
    private ImageView mImgRight;
    private boolean mIsShowing = false;
    float mMaxRadius;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;

    public GripSensorFloatingView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    private void init() {
        initScreenWidthHeight();
        initWindowLayoutParams();
        prepareForAddView();
    }

    private void initScreenWidthHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager = (WindowManager) this.mContext.getApplicationContext().getSystemService("window");
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }

    private void initWindowLayoutParams() {
        this.mWindowLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 26) {
            this.mWindowLayoutParams.type = 2038;
        } else {
            this.mWindowLayoutParams.type = 2002;
        }
        WindowManager.LayoutParams layoutParams = this.mWindowLayoutParams;
        layoutParams.format = 1;
        layoutParams.flags = 312;
        layoutParams.gravity = 17;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = -1;
        layoutParams.height = -1;
    }

    private void prepareForAddView() {
        initView();
        this.mWindowManager.addView(this, this.mWindowLayoutParams);
    }

    private void initView() {
        View view = this.mContentView;
        if (view != null) {
            removeView(view);
        }
        this.mContentView = LayoutInflater.from(this.mContext).inflate(R.layout.grip_sensor_floating_view, (ViewGroup) null);
        this.mImgLeft = (ImageView) this.mContentView.findViewById(R.id.img_left);
        this.mImgRight = (ImageView) this.mContentView.findViewById(R.id.img_right);
        if (getDeviceOrientation() == 3) {
            this.mImgLeft.setLayoutParams(updateParam((RelativeLayout.LayoutParams) this.mImgLeft.getLayoutParams()));
            this.mImgRight.setLayoutParams(updateParam((RelativeLayout.LayoutParams) this.mImgRight.getLayoutParams()));
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mImgLeft.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mImgRight.getLayoutParams();
        if (getDeviceOrientation() == 1) {
            layoutParams.setMargins(0, ((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_margin_top)) + 220, 0, 0);
            layoutParams2.setMargins(0, ((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_margin_top)) + 220, 0, 0);
        } else if (getDeviceOrientation() == 2) {
            layoutParams.setMargins(((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_margin_top)) + 220, 0, 0, 0);
            layoutParams2.setMargins(((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_margin_top)) + 220, 0, 0, 0);
        }
        this.mDrawableLeft = (GradientDrawable) this.mImgLeft.getDrawable();
        this.mDrawableRight = (GradientDrawable) this.mImgRight.getDrawable();
        this.mMaxRadius = this.mContext.getResources().getDimension(R.dimen.gripsensor_gradient_radius);
        addView(this.mContentView, -1, -1);
    }

    public RelativeLayout.LayoutParams updateParam(RelativeLayout.LayoutParams layoutParams) {
        layoutParams.addRule(11, -1);
        layoutParams.removeRule(9);
        layoutParams.setMargins(0, 0, ((int) this.mContext.getResources().getDimension(R.dimen.gripsensor_margin_top)) + 220, 0);
        return layoutParams;
    }

    public void show(int i, int i2) {
        Log.d("AsusSettingsGripSensorFloatingView", "show");
        setVisibility(0);
        updateDrawable(i, (float) i2);
        this.mIsShowing = true;
    }

    private int getDeviceOrientation() {
        int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
        if (rotation == 1) {
            return 2;
        }
        return rotation == 3 ? 3 : 1;
    }

    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initView();
    }

    public void updateDrawable(int i, float f) {
        float f2 = (float) 0;
        float f3 = f > f2 ? (f - f2) / 255.0f : 0.0f;
        float f4 = (0.5f * f3) + 0.3f;
        if (i == 1) {
            this.mDrawableLeft.setGradientRadius(this.mMaxRadius * f3);
            this.mImgLeft.setAlpha(f4);
            if (getDeviceOrientation() == 1) {
                this.mImgLeft.setScaleY(1.5f);
            } else {
                this.mImgLeft.setScaleX(1.5f);
            }
        } else {
            this.mDrawableRight.setGradientRadius(this.mMaxRadius * f3);
            this.mImgRight.setAlpha(f4);
            if (getDeviceOrientation() == 1) {
                this.mImgRight.setScaleY(1.5f);
            } else {
                this.mImgRight.setScaleX(1.5f);
            }
        }
    }

    public void hide() {
        Log.d("AsusSettingsGripSensorFloatingView", "hide");
        setVisibility(8);
        this.mIsShowing = false;
    }

    public void release() {
        if (this.mWindowManager != null && this.mContentView != null) {
            Log.d("AsusSettingsGripSensorFloatingView", "release");
            this.mWindowManager.removeView(this);
            this.mIsShowing = false;
        }
    }
}
