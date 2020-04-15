package org.omnirom.device.gripsensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.omnirom.device.gripsensorservice.GripSensorService;

public class AirTriggerReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("AirTriggerReceiver", "onReceive() - action : " + action);
        boolean isMainSwitchEnable = AirTriggerUtils.isMainSwitchEnable(context);
        if (!action.equals("android.intent.action.BOOT_COMPLETED")) {
            if (action.equals("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON")) {
                isMainSwitchEnable = true;
                Log.d("AirTriggerReceiver", "receive set AirTrigger ON intent: org.omnirom.device.SYSTEMUI_AIR_TRIGGER_ON");
            } else if (action.equals("org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF")) {
                Log.d("AirTriggerReceiver", "receive set AirTrigger OFF intent: org.omnirom.device.SYSTEMUI_AIR_TRIGGER_OFF");
                isMainSwitchEnable = false;
            }
            AirTriggerUtils.getInstance(context).setMainSwitchEnable(isMainSwitchEnable);
        } else if (AirTriggerUtils.getInstance(context).isGripHardwareSupport()) {
            Log.d("AirTriggerReceiver", "BOOT_COMPLETE start GripSensorService!!!");
            AirTriggerUtils.getInstance(context).setGameSpaceEnable(false);
            GripSensorService.enqueueWork(context, new Intent(context, GripSensorService.class));
        }
    }
}
