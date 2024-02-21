package org.smartregister.chw.asrh.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.asrh.util.AsrhUtil;
import org.smartregister.chw.asrh.contract.AsrhRegisterContract;
import org.smartregister.chw.asrh.util.AppExecutors;

public class BaseAsrhRegisterInteractor implements AsrhRegisterContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    BaseAsrhRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseAsrhRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveRegistration(final String jsonString, final AsrhRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            try {
                AsrhUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved());
        };
        appExecutors.diskIO().execute(runnable);
    }
}
