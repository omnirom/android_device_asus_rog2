package org.omnirom.device.gripsensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AirTriggerActivity extends Activity {

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(super.getIntent());
        intent.putExtra(":device:show_fragment", GripSensorSettings.class.getName());
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }
}
