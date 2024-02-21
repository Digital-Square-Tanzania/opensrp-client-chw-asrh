package org.smartregister.chw.asrh_sample.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONObject;
import org.smartregister.chw.asrh.activity.BaseAsrhVisitActivity;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.presenter.BaseAsrhVisitPresenter;
import org.smartregister.chw.asrh.util.Constants;
import org.smartregister.chw.asrh_sample.interactor.AsrhVisitInteractor;

public class AsrhVisitActivity extends BaseAsrhVisitActivity {
    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, AsrhVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    protected void registerPresenter() {
        presenter = new BaseAsrhVisitPresenter(memberObject, this, new AsrhVisitInteractor());
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, SampleJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }
}
