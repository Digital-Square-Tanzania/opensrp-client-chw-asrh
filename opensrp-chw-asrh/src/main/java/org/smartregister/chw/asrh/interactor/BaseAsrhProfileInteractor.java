package org.smartregister.chw.asrh.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.asrh.AsrhLibrary;
import org.smartregister.chw.asrh.util.Constants;
import org.smartregister.chw.asrh.util.AsrhUtil;
import org.smartregister.chw.asrh.contract.AsrhProfileContract;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.domain.Visit;
import org.smartregister.chw.asrh.util.AppExecutors;

public class BaseAsrhProfileInteractor implements AsrhProfileContract.Interactor {
    protected AppExecutors appExecutors;

    @VisibleForTesting
    BaseAsrhProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseAsrhProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void refreshProfileInfo(MemberObject memberObject, AsrhProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> {
            callback.refreshMedicalHistory(getVisit(Constants.EVENT_TYPE.ASRH_FOLLOW_UP_VISIT, memberObject) != null);
        });
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final String jsonString, final AsrhProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            try {
                AsrhUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
        appExecutors.diskIO().execute(runnable);
    }

    private Visit getVisit(String eventType, MemberObject memberObject) {
        try {
            return AsrhLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
        } catch (Exception e) {
            return null;
        }
    }
}
