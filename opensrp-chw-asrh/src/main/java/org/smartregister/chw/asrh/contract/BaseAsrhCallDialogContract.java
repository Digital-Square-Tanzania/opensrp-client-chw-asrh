package org.smartregister.chw.asrh.contract;

import android.content.Context;

public interface BaseAsrhCallDialogContract {

    interface View {
        void setPendingCallRequest(Dialer dialer);
        Context getCurrentContext();
    }

    interface Dialer {
        void callMe();
    }
}
