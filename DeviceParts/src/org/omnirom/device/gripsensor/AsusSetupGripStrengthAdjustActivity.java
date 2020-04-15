package org.omnirom.device.gripsensor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.omnirom.device.gripsensor.GripStrengthAdjustActivity;

public class AsusSetupGripStrengthAdjustActivity extends GripStrengthAdjustActivity {

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":device:show_fragment", AsusSetupGripStrengthAdjustFragment.class.getName());
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        Log.d("AsusSetupGripStrengthAdjustActivity", "onCreate");
        ((GripStrengthAdjustActivity) this).mRequestType = 2;
        AirTriggerUtils.getInstance(getApplication()).setSetupWizardPage(true);
        super.onCreate(bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | 2 | 4096);
    }

    public static class AsusSetupGripStrengthAdjustFragment extends GripStrengthAdjust {
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
            return super.onCreateView(layoutInflater, viewGroup, bundle);
        }

        public void onActivityCreated(Bundle bundle) {
            Log.d("AsusSetupGripStrengthAdjustActivity", "onCreate");
            super.onActivityCreated(bundle);
        }
    }
}
