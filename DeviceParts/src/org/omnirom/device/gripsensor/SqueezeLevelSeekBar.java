package org.omnirom.device.gripsensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import org.omnirom.device.R;
import org.omnirom.device.widget.LabeledSeekBar;

public class SqueezeLevelSeekBar extends LabeledSeekBar {
    private boolean isLeftVisible;
    Context mContext;
    Drawable mDefaultThumb;
    Drawable mDefaultThumbBg;
    Drawable mDefaultThumbLeft;
    private int mLeftLevel;
    private int mLevel;
    Drawable mTick;

    public SqueezeLevelSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, android.R.attr.seekBarStyle);
        this.mContext = context;
    }

    public SqueezeLevelSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i, 0);
        this.mContext = context;
        this.mDefaultThumb = context.getResources().getDrawable(R.drawable.grip_seekbar_default_thumb);
        this.mDefaultThumbBg = context.getResources().getDrawable(R.drawable.grip_seekbar_default_thumb_bg);
        this.mDefaultThumbLeft = context.getResources().getDrawable(R.drawable.grip_seekbar_default_thumb_left);
        this.mTick = context.getResources().getDrawable(R.drawable.grip_seekbar_tick);
        initDefaultThumb();
        initDefaultThumbLeft();
        initTick();
        initBg();
    }

    public void setLevel(int i) {
        this.mLevel = i;
    }

    public Drawable getDefaultThumb() {
        return this.mDefaultThumb;
    }

    public Drawable getDefaultLeftThumb() {
        return this.mDefaultThumbLeft;
    }

    public void setLeftDefLevel(int i) {
        this.mLeftLevel = i;
    }

    public void showLeftDefThumb(boolean z) {
        this.isLeftVisible = z;
    }

    public void initDefaultThumbLeft() {
        int intrinsicWidth = this.mDefaultThumbLeft.getIntrinsicWidth();
        int intrinsicHeight = this.mDefaultThumbLeft.getIntrinsicHeight();
        int i = 1;
        int i2 = intrinsicWidth >= 0 ? intrinsicWidth / 2 : 1;
        if (intrinsicHeight >= 0) {
            i = intrinsicHeight / 2;
        }
        this.mDefaultThumbLeft.setBounds(-i2, -i, i2, i);
    }

    public void setIsOverlap(boolean z) {
        Drawable drawable;
        if (z) {
            drawable = this.mContext.getResources().getDrawable(R.drawable.grip_seekbar_default_thumb_overlap);
        } else {
            drawable = this.mContext.getResources().getDrawable(R.drawable.grip_seekbar_default_thumb);
        }
        this.mDefaultThumb = drawable;
        initDefaultThumb();
    }

    private void initDefaultThumb() {
        int intrinsicWidth = this.mDefaultThumb.getIntrinsicWidth();
        int intrinsicHeight = this.mDefaultThumb.getIntrinsicHeight();
        int i = 1;
        int i2 = intrinsicWidth >= 0 ? intrinsicWidth / 2 : 1;
        if (intrinsicHeight >= 0) {
            i = intrinsicHeight / 2;
        }
        this.mDefaultThumb.setBounds(-i2, -i, i2, i);
    }

    private void initBg() {
        int intrinsicWidth = this.mDefaultThumbBg.getIntrinsicWidth();
        int intrinsicHeight = this.mDefaultThumbBg.getIntrinsicHeight();
        int i = 1;
        int i2 = intrinsicWidth >= 0 ? intrinsicWidth / 2 : 1;
        if (intrinsicHeight >= 0) {
            i = intrinsicHeight / 2;
        }
        this.mDefaultThumbBg.setBounds(-i2, -i, i2, i);
    }

    private void initTick() {
        int intrinsicWidth = this.mTick.getIntrinsicWidth();
        int intrinsicHeight = this.mTick.getIntrinsicHeight();
        int i = 1;
        int i2 = intrinsicWidth >= 0 ? intrinsicWidth / 2 : 1;
        if (intrinsicHeight >= 0) {
            i = intrinsicHeight / 2;
        }
        this.mTick.setBounds(-i2, -i, i2, i);
    }

    public void onResolveDrawables(int i) {
        super.onResolveDrawables(i);
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDefaultThumb != null) {
            int max = getMax() - getMin();
            float width = ((float) ((getWidth() - this.mPaddingLeft) - this.mPaddingRight)) / ((float) max);
            drawTick(canvas, max, width);
            drawDefaultThumb(canvas, max, width);
            if (this.isLeftVisible) {
                drawDefaultThumbLeft(canvas, max, width);
            }
        }
    }

    protected void drawDefaultThumb(Canvas canvas, int i, float f) {
        int save = canvas.save();
        canvas.translate(((float) this.mPaddingLeft) + (((float) this.mLevel) * f), (float) (getHeight() / 2));
        this.mDefaultThumb.draw(canvas);
        canvas.restoreToCount(save);
    }

    protected void drawDefaultThumbLeft(Canvas canvas, int i, float f) {
        int save = canvas.save();
        canvas.translate(((float) this.mPaddingLeft) + (((float) this.mLeftLevel) * f), (float) (getHeight() / 2));
        this.mDefaultThumbLeft.draw(canvas);
        canvas.restoreToCount(save);
    }

    protected void drawTick(Canvas canvas, int i, float f) {
        if (this.mTick != null) {
            int save = canvas.save();
            canvas.translate((float) this.mPaddingLeft, (float) getHeight());
            for (int i2 = 0; i2 <= i; i2++) {
                this.mTick.draw(canvas);
                canvas.translate(f, 0.0f);
            }
            canvas.restoreToCount(save);
        }
    }
}
