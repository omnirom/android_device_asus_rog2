package android.os;

import android.content.Intent;

public abstract class AsusPointerListenerProxyInternal {
    public abstract void register(Intent intent);

    public abstract void unregister(Intent intent);
}
