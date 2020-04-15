package org.omnirom.device.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import java.util.List;

public class LabeledSeekBar extends SeekBar {
    private final ExploreByTouchHelper mAccessHelper;
    private String[] mLabels;
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;
    private final SeekBar.OnSeekBarChangeListener mProxySeekBarListener;

    public LabeledSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, android.R.attr.seekBarStyle);
    }

    public LabeledSeekBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public LabeledSeekBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mProxySeekBarListener = new SeekBar.OnSeekBarChangeListener() {
            /* class org.omnirom.device.widget.LabeledSeekBar.AnonymousClass1 */

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onProgressChanged(seekBar, i, z);
                    LabeledSeekBar.this.sendClickEventForAccessibility(i);
                }
            }
        };
        this.mAccessHelper = new LabeledSeekBarExploreByTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, this.mAccessHelper);
        super.setOnSeekBarChangeListener(this.mProxySeekBarListener);
    }

    public synchronized void setProgress(int i) {
        if (this.mAccessHelper != null) {
            this.mAccessHelper.invalidateRoot();
        }
        super.setProgress(i);
    }

    public void setLabels(String[] strArr) {
        this.mLabels = strArr;
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        this.mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    /* access modifiers changed from: protected */
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mAccessHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent);
    }

    /* access modifiers changed from: private */
    public void sendClickEventForAccessibility(int i) {
        this.mAccessHelper.invalidateRoot();
        this.mAccessHelper.sendEventForVirtualView(i, 1);
    }

    private class LabeledSeekBarExploreByTouchHelper extends ExploreByTouchHelper {
        private boolean mIsLayoutRtl;

        public LabeledSeekBarExploreByTouchHelper(LabeledSeekBar labeledSeekBar) {
            super(labeledSeekBar);
            this.mIsLayoutRtl = labeledSeekBar.getResources().getConfiguration().getLayoutDirection() != 1 ? false : true;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float f, float f2) {
            return getVirtualViewIdIndexFromX(f);
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(List<Integer> list) {
            int max = LabeledSeekBar.this.getMax();
            for (int i = 0; i <= max; i++) {
                list.add(Integer.valueOf(i));
            }
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i == -1 || i2 != 16) {
                return false;
            }
            LabeledSeekBar.this.setProgress(i);
            sendEventForVirtualView(i, 1);
            return true;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setClassName(RadioButton.class.getName());
            accessibilityNodeInfoCompat.setBoundsInParent(getBoundsInParentFromVirtualViewId(i));
            accessibilityNodeInfoCompat.addAction(16);
            accessibilityNodeInfoCompat.setContentDescription(LabeledSeekBar.this.mLabels[i]);
            boolean z = true;
            accessibilityNodeInfoCompat.setClickable(true);
            accessibilityNodeInfoCompat.setCheckable(true);
            if (i != LabeledSeekBar.this.getProgress()) {
                z = false;
            }
            accessibilityNodeInfoCompat.setChecked(z);
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.setClassName(RadioButton.class.getName());
            accessibilityEvent.setContentDescription(LabeledSeekBar.this.mLabels[i]);
            accessibilityEvent.setChecked(i == LabeledSeekBar.this.getProgress());
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForHost(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setClassName(RadioGroup.class.getName());
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateEventForHost(AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.setClassName(RadioGroup.class.getName());
        }

        private int getHalfVirtualViewWidth() {
            return Math.max(0, ((LabeledSeekBar.this.getWidth() - LabeledSeekBar.this.getPaddingStart()) - LabeledSeekBar.this.getPaddingEnd()) / (LabeledSeekBar.this.getMax() * 2));
        }

        private int getVirtualViewIdIndexFromX(float f) {
            int min = Math.min((Math.max(0, (((int) f) - LabeledSeekBar.this.getPaddingStart()) / getHalfVirtualViewWidth()) + 1) / 2, LabeledSeekBar.this.getMax());
            return this.mIsLayoutRtl ? LabeledSeekBar.this.getMax() - min : min;
        }

        private Rect getBoundsInParentFromVirtualViewId(int i) {
            if (this.mIsLayoutRtl) {
                i = LabeledSeekBar.this.getMax() - i;
            }
            int i2 = i * 2;
            int halfVirtualViewWidth = ((i2 - 1) * getHalfVirtualViewWidth()) + LabeledSeekBar.this.getPaddingStart();
            int halfVirtualViewWidth2 = ((i2 + 1) * getHalfVirtualViewWidth()) + LabeledSeekBar.this.getPaddingStart();
            if (i == 0) {
                halfVirtualViewWidth = 0;
            }
            if (i == LabeledSeekBar.this.getMax()) {
                halfVirtualViewWidth2 = LabeledSeekBar.this.getWidth();
            }
            Rect rect = new Rect();
            rect.set(halfVirtualViewWidth, 0, halfVirtualViewWidth2, LabeledSeekBar.this.getHeight());
            return rect;
        }
    }
}
