package org.smartregister.chw.asrh.presenter;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.asrh.R;
import org.smartregister.chw.asrh.contract.AsrhRegisterContract;

import java.lang.ref.WeakReference;
import java.util.List;

import timber.log.Timber;

public class BaseAsrhRegisterPresenter implements AsrhRegisterContract.Presenter, AsrhRegisterContract.InteractorCallBack {

    public static final String TAG = BaseAsrhRegisterPresenter.class.getName();

    protected WeakReference<AsrhRegisterContract.View> viewReference;
    private AsrhRegisterContract.Interactor interactor;
    protected AsrhRegisterContract.Model model;

    public BaseAsrhRegisterPresenter(AsrhRegisterContract.View view, AsrhRegisterContract.Model model, AsrhRegisterContract.Interactor interactor) {
        viewReference = new WeakReference<>(view);
        this.interactor = interactor;
        this.model = model;
    }

    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        if (StringUtils.isBlank(entityId)) {
            return;
        }

        JSONObject form = model.getFormAsJson(formName, entityId, currentLocationId);
        getView().startFormActivity(form);
    }

    @Override
    public void saveForm(String jsonString) {
        try {
            getView().showProgressDialog(R.string.saving_dialog_title);
            interactor.saveRegistration(jsonString, this);
        } catch (Exception e) {
            Timber.tag(TAG).e(Log.getStackTraceString(e));
        }
    }

    @Override
    public void onRegistrationSaved() {
        getView().hideProgressDialog();

    }

    @Override
    public void registerViewConfigurations(List<String> list) {
//        implement
    }

    @Override
    public void unregisterViewConfiguration(List<String> list) {
//        implement
    }

    @Override
    public void onDestroy(boolean b) {
//        implement
    }

    @Override
    public void updateInitials() {
//        implement
    }

    private AsrhRegisterContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }
}
