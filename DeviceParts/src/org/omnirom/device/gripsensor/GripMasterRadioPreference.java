package org.omnirom.device.gripsensor;

import android.content.Context;
import android.util.AttributeSet;
import org.omnirom.device.widget.MasterRadioPreference;

public class GripMasterRadioPreference extends MasterRadioPreference {
    private String mPath;
    private String mPkg;

    public GripMasterRadioPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public GripMasterRadioPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public GripMasterRadioPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public GripMasterRadioPreference(Context context) {
        super(context);
    }

    public void setPath(String str) {
        this.mPath = str;
    }

    public String getPath() {
        return this.mPath;
    }

    public void setPkg(String str) {
        this.mPkg = str;
    }
}
