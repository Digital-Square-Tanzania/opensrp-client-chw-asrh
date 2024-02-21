package org.smartregister.chw.asrh_sample.interactor;

import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.interactor.BaseAsrhVisitInteractor;
import org.smartregister.chw.asrh_sample.activity.EntryActivity;

public class AsrhVisitInteractor extends BaseAsrhVisitInteractor {
    @Override
    public MemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
