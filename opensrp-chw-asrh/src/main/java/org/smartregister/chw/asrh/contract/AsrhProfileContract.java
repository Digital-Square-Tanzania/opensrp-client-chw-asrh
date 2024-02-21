package org.smartregister.chw.asrh.contract;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.asrh.domain.MemberObject;

public interface AsrhProfileContract {
    interface View extends InteractorCallBack {

        void setProfileViewWithData();

        void setOverDueColor();

        void openMedicalHistory();

        void recordAsrh(MemberObject memberObject);

        void showProgressBar(boolean status);

        void hideView();
    }

    interface Presenter {

        void fillProfileData(@Nullable MemberObject memberObject);

        void saveForm(String jsonString);

        @Nullable
        View getView();

        void refreshProfileBottom();

        void recordAsrhButton(String visitState);
    }

    interface Interactor {

        void refreshProfileInfo(MemberObject memberObject, InteractorCallBack callback);

        void saveRegistration(String jsonString, final AsrhProfileContract.InteractorCallBack callBack);
    }


    interface InteractorCallBack {
        void refreshMedicalHistory(boolean hasHistory);
    }
}