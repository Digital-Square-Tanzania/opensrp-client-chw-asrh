package org.smartregister.chw.asrh_sample.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.asrh.activity.BaseAsrhProfileActivity;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.util.Constants;


public class AsrhMemberProfileActivity extends BaseAsrhProfileActivity {

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, AsrhMemberProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void recordAsrh(MemberObject memberObject) {
        AsrhVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }
}