package org.smartregister.chw.asrh.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;
import org.smartregister.chw.asrh.util.JsonFormUtils;

import timber.log.Timber;

/**
 * AYSRH Activity Action Helper
 */
public class SetNextAppointmentDateActionHelper extends AsrhVisitActionHelper {
    protected Context context;
    protected MemberObject memberObject;
    protected String nextAppointmentDate;

    public SetNextAppointmentDateActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            nextAppointmentDate = JsonFormUtils.getValue(jsonObject, "next_appointment_date");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseAsrhVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(nextAppointmentDate)) {
            return BaseAsrhVisitAction.Status.COMPLETED;
        } else {
            return BaseAsrhVisitAction.Status.PENDING;
        }
    }
}