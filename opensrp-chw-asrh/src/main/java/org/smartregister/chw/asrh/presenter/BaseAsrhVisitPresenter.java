package org.smartregister.chw.asrh.presenter;

import org.json.JSONObject;
import org.smartregister.chw.asrh.util.JsonFormUtils;
import org.smartregister.chw.asrh.R;
import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;
import org.smartregister.chw.asrh.contract.BaseAsrhVisitContract;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.util.FormUtils;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

import timber.log.Timber;

public class BaseAsrhVisitPresenter implements BaseAsrhVisitContract.Presenter, BaseAsrhVisitContract.InteractorCallBack {

    protected WeakReference<BaseAsrhVisitContract.View> view;
    protected BaseAsrhVisitContract.Interactor interactor;
    protected MemberObject memberObject;

    public BaseAsrhVisitPresenter(MemberObject memberObject, BaseAsrhVisitContract.View view, BaseAsrhVisitContract.Interactor interactor) {
        this.view = new WeakReference<>(view);
        this.interactor = interactor;
        this.memberObject = memberObject;
    }

    @Override
    public void startForm(String formName, String memberID, String currentLocationId) {
        try {
            if (view.get() != null) {
                JSONObject jsonObject = FormUtils.getInstance(view.get().getContext()).getFormJson(formName);
                JsonFormUtils.getRegistrationForm(jsonObject, memberID, currentLocationId);
                view.get().startFormActivity(jsonObject);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public boolean validateStatus() {
        return false;
    }

    @Override
    public void initialize() {
        view.get().displayProgressBar(true);
        view.get().redrawHeader(memberObject);
        interactor.calculateActions(view.get(), memberObject, this);
    }

    @Override
    public void submitVisit() {
        if (view.get() != null) {
            view.get().displayProgressBar(true);
            interactor.submitVisit(view.get().getEditMode(), memberObject.getBaseEntityId(), view.get().getPmtctHomeVisitActions(), this);
        }
    }

    @Override
    public void reloadMemberDetails(String memberID) {
        view.get().displayProgressBar(true);
        interactor.reloadMemberDetails(memberID, this);
    }

    @Override
    public void onMemberDetailsReloaded(MemberObject memberObject) {
        if (view.get() != null) {
            this.memberObject = memberObject;
            view.get().displayProgressBar(false);
            view.get().onMemberDetailsReloaded(memberObject);
        }
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        Timber.v("onRegistrationSaved");
    }

    @Override
    public void preloadActions(LinkedHashMap<String, BaseAsrhVisitAction> map) {
        if (view.get() != null)
            view.get().initializeActions(map);
    }

    @Override
    public void onSubmitted(boolean successful) {
        if (view.get() != null) {
            view.get().displayProgressBar(false);
            if (successful) {
                view.get().submittedAndClose();
            } else {
                view.get().displayToast(view.get().getContext().getString(R.string.error_unable_save_home_visit));
            }
        }
    }
}
