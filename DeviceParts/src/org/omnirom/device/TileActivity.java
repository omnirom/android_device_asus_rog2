package org.omnirom.device;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class TileActivity extends Activity {
    public void onCreate(Bundle bundle) {
        char c;
        super.onCreate(bundle);
        Intent intent = new Intent();
        String className = ((ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME")).getClassName();
        switch (className.hashCode()) {
            case -216413448:
                if (className.equals("org.omnirom.device.gripsensorservice.DevelopmentTiles$SwitchTile")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            intent.setPackage("org.omnirom.device");
            intent.setAction("org.omnirom.device.AIR_TRIGGER_SETTING_PAGE");
            startActivity(intent);
        finish();
        }
    }
}
