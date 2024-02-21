package org.smartregister.chw.asrh.listener;


import android.view.View;

import org.smartregister.chw.asrh.util.AsrhUtil;
import org.smartregister.chw.asrh.R;
import org.smartregister.chw.asrh.fragment.BaseAsrhCallDialogFragment;

import timber.log.Timber;

public class BaseAsrhCallWidgetDialogListener implements View.OnClickListener {

    private BaseAsrhCallDialogFragment callDialogFragment;

    public BaseAsrhCallWidgetDialogListener(BaseAsrhCallDialogFragment dialogFragment) {
        callDialogFragment = dialogFragment;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.asrh_call_close) {
            callDialogFragment.dismiss();
        } else if (i == R.id.asrh_call_head_phone) {
            try {
                String phoneNumber = (String) v.getTag();
                AsrhUtil.launchDialer(callDialogFragment.getActivity(), callDialogFragment, phoneNumber);
                callDialogFragment.dismiss();
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (i == R.id.call_asrh_client_phone) {
            try {
                String phoneNumber = (String) v.getTag();
                AsrhUtil.launchDialer(callDialogFragment.getActivity(), callDialogFragment, phoneNumber);
                callDialogFragment.dismiss();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
