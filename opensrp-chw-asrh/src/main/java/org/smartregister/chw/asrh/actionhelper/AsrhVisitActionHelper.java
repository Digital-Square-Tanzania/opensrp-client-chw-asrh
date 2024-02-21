package org.smartregister.chw.asrh.actionhelper;

import android.content.Context;

import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;
import org.smartregister.chw.asrh.domain.VisitDetail;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Designed to set default methods for the ASRH Action Helper
 * This object must remain inert to the Home Visit action. Its designed primarily for extension by simple visit actions
 */
public abstract class AsrhVisitActionHelper implements BaseAsrhVisitAction.AsrhVisitActionHelper {
    protected Context context;

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
    }

    /**
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        return null;
    }

    /**
     * set schedule status to be inert
     *
     * @return null
     */
    @Override
    public BaseAsrhVisitAction.ScheduleStatus getPreProcessedStatus() {
        return BaseAsrhVisitAction.ScheduleStatus.DUE;
    }

    /**
     * set schedule status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    /**
     * prevent post processing
     *
     * @return null
     */
    @Override
    public String postProcess(String jsonPayload) {
        return null;
    }

    /**
     * Do nothing on payload received
     *
     * @param baseAsrhVisitAction
     */
    @Override
    public void onPayloadReceived(BaseAsrhVisitAction baseAsrhVisitAction) {
        Timber.v("onPayloadReceived");
    }

    public Context getContext() {
        return context;
    }
}