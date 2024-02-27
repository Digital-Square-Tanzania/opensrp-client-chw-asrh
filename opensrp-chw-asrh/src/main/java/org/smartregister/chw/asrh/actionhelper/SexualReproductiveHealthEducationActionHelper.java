package org.smartregister.chw.asrh.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.domain.VisitDetail;
import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;
import org.smartregister.chw.asrh.util.JsonFormUtils;
import org.smartregister.client.utils.constants.JsonFormConstants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * ASRH Activity Action Helper
 */
public class SexualReproductiveHealthEducationActionHelper extends AsrhVisitActionHelper {
    protected Context context;

    protected MemberObject memberObject;
    protected String providedSexualReproductiveHealthEducation;

    private JSONObject jsonForm;

    public SexualReproductiveHealthEducationActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }


    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        super.onJsonFormLoaded(jsonString, context, details);
        try {
            jsonForm = new JSONObject(jsonString);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        try {
            jsonForm.getJSONObject(JsonFormConstants.JSON_FORM_KEY.GLOBAL).put("age", memberObject.getAge());
            jsonForm.getJSONObject(JsonFormConstants.JSON_FORM_KEY.GLOBAL).put("sex", memberObject.getGender());
            return jsonForm.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            providedSexualReproductiveHealthEducation = JsonFormUtils.getValue(jsonObject, "provided_sexual_reproductive_health_health_education");
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
        if (StringUtils.isNotBlank(providedSexualReproductiveHealthEducation)) {
            return BaseAsrhVisitAction.Status.COMPLETED;
        } else {
            return BaseAsrhVisitAction.Status.PENDING;
        }
    }
}