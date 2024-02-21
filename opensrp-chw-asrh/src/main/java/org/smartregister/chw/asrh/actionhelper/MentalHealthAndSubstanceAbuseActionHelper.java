package org.smartregister.chw.asrh.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.asrh.util.JsonFormUtils;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;

import timber.log.Timber;

/**
 * ASRH Activity Action Helper
 */
public class MentalHealthAndSubstanceAbuseActionHelper extends AsrhVisitActionHelper {
    protected Context context;
    protected MemberObject memberObject;
    protected String educationOnMentalHealthAndSubstanceAbuseProvided;

    public MentalHealthAndSubstanceAbuseActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    /**
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            educationOnMentalHealthAndSubstanceAbuseProvided = JsonFormUtils.getValue(jsonObject, "education_on_mental_health_and_substance_abuse_provided");
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
        if (StringUtils.isNotBlank(educationOnMentalHealthAndSubstanceAbuseProvided)) {
            return BaseAsrhVisitAction.Status.COMPLETED;
        } else {
            return BaseAsrhVisitAction.Status.PENDING;
        }
    }
}