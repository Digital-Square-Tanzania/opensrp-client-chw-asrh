package org.smartregister.chw.asrh.presenter;

import android.content.Context;
import androidx.annotation.Nullable;

import org.smartregister.chw.asrh.contract.AsrhProfileContract;
import org.smartregister.chw.asrh.domain.MemberObject;

import java.lang.ref.WeakReference;

import timber.log.Timber;


public class BaseAsrhProfilePresenter implements AsrhProfileContract.Presenter {
    protected WeakReference<AsrhProfileContract.View> view;
    protected MemberObject memberObject;
    protected AsrhProfileContract.Interactor interactor;
    protected Context context;

    public BaseAsrhProfilePresenter(AsrhProfileContract.View view, AsrhProfileContract.Interactor interactor, MemberObject memberObject) {
        this.view = new WeakReference<>(view);
        this.memberObject = memberObject;
        this.interactor = interactor;
    }

    @Override
    public void fillProfileData(MemberObject memberObject) {
        if (memberObject != null && getView() != null) {
            getView().setProfileViewWithData();
        }
    }

    @Override
    public void recordAsrhButton(@Nullable String visitState) {
        if (getView() == null) {
            return;
        }

        if (("OVERDUE").equals(visitState) || ("DUE").equals(visitState)) {
            if (("OVERDUE").equals(visitState)) {
                getView().setOverDueColor();
            }
        } else {
            getView().hideView();
        }
    }

    @Override
    @Nullable
    public AsrhProfileContract.View getView() {
        if (view != null && view.get() != null)
            return view.get();

        return null;
    }

    @Override
    public void refreshProfileBottom() {
        interactor.refreshProfileInfo(memberObject, getView());
    }

    @Override
    public void saveForm(String jsonString) {
        try {
            interactor.saveRegistration(jsonString, getView());
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
