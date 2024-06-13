package org.smartregister.chw.asrh.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.asrh.AsrhLibrary;
import org.smartregister.chw.asrh.R;
import org.smartregister.chw.asrh.actionhelper.AsrhVisitActionHelper;
import org.smartregister.chw.asrh.actionhelper.ClientStatusActionHelper;
import org.smartregister.chw.asrh.actionhelper.FacilitationMethodsActionHelper;
import org.smartregister.chw.asrh.actionhelper.HealthEducationActionHelper;
import org.smartregister.chw.asrh.actionhelper.MentalHealthAndSubstanceAbuseActionHelper;
import org.smartregister.chw.asrh.actionhelper.ReferralsToOtherServicesActionHelper;
import org.smartregister.chw.asrh.actionhelper.SexualReproductiveHealthEducationActionHelper;
import org.smartregister.chw.asrh.contract.BaseAsrhVisitContract;
import org.smartregister.chw.asrh.dao.AsrhDao;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.domain.Visit;
import org.smartregister.chw.asrh.domain.VisitDetail;
import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;
import org.smartregister.chw.asrh.repository.VisitRepository;
import org.smartregister.chw.asrh.util.AppExecutors;
import org.smartregister.chw.asrh.util.Constants;
import org.smartregister.chw.asrh.util.JsonFormUtils;
import org.smartregister.chw.asrh.util.NCUtils;
import org.smartregister.chw.asrh.util.VisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class BaseAsrhVisitInteractor implements BaseAsrhVisitContract.Interactor {

    private final AsrhLibrary asrhLibrary;
    private final LinkedHashMap<String, BaseAsrhVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;
    private Map<String, List<VisitDetail>> details = null;

    private BaseAsrhVisitContract.InteractorCallBack callBack;

    @VisibleForTesting
    public BaseAsrhVisitInteractor(AppExecutors appExecutors, AsrhLibrary AsrhLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.asrhLibrary = AsrhLibrary;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
    }

    public BaseAsrhVisitInteractor() {
        this(new AppExecutors(), AsrhLibrary.getInstance(), AsrhLibrary.getInstance().getEcSyncHelper());
    }

    @Override
    public void reloadMemberDetails(String memberID, BaseAsrhVisitContract.InteractorCallBack callBack) {
        MemberObject memberObject = getMemberClient(memberID);
        if (memberObject != null) {
            final Runnable runnable = () -> {
                appExecutors.mainThread().execute(() -> callBack.onMemberDetailsReloaded(memberObject));
            };
            appExecutors.diskIO().execute(runnable);
        }
    }

    /**
     * Override this method and return actual member object for the provided user
     *
     * @param memberID unique identifier for the user
     * @return MemberObject wrapper for the user's data
     */
    @Override
    public MemberObject getMemberClient(String memberID) {
        return AsrhDao.getMember(memberID);
    }

    @Override
    public void saveRegistration(String jsonString, boolean isEditMode, BaseAsrhVisitContract.InteractorCallBack callBack) {
        Timber.v("saveRegistration");
    }

    @Override
    public void calculateActions(final BaseAsrhVisitContract.View view, MemberObject memberObject, final BaseAsrhVisitContract.InteractorCallBack callBack) {
        mContext = view.getContext();
        this.callBack = callBack;
        if (view.getEditMode()) {
            Visit lastVisit = asrhLibrary.visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.ASRH_FOLLOW_UP_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(asrhLibrary.visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        final Runnable runnable = () -> {
            try {
                evaluateClientStatus(memberObject, details);
            } catch (BaseAsrhVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void evaluateClientStatus(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new ClientStatusActionHelper(mContext, memberObject) {
            @Override
            public void processClientStatus(String clientStatus) {
                if (clientStatus.equals("active")) {
                    try {
                        evaluateHealthEducation(memberObject, details);
                        evaluateSexualReproductiveHealthEducation(memberObject, details);
                        evaluateMentalHealthAndSubstanceAbuse(memberObject, details);
                        evaluateFacilitationMethod(memberObject, details);
                        evaluateReferralsToOtherServices(memberObject, details);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                } else {
                    actionList.remove(mContext.getString(R.string.asrh_health_education));
                    actionList.remove(mContext.getString(R.string.asrh_sexual_reproductive_health_education));
                    actionList.remove(mContext.getString(R.string.asrh_mental_health_and_substance_abuse));
                    actionList.remove(mContext.getString(R.string.asrh_facilitation_methods));
                }

                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));

            }
        };
        String actionName = mContext.getString(R.string.asrh_client_status);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.ASRH_CLIENT_STATUS).build();
        actionList.put(actionName, action);
    }

    protected void evaluateHealthEducation(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new HealthEducationActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.asrh_health_education);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.ASRH_HEALTH_EDUCATION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateSexualReproductiveHealthEducation(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new SexualReproductiveHealthEducationActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.asrh_sexual_reproductive_health_education);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.ASRH_SEXUAL_REPRODUCTIVE_HEALTH_EDUCATION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateMentalHealthAndSubstanceAbuse(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new MentalHealthAndSubstanceAbuseActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.asrh_mental_health_and_substance_abuse);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.ASRH_MENTAL_HEALTH_AND_SUBSTANCE_ABUSE).build();
        actionList.put(actionName, action);
    }

    protected void evaluateReferralsToOtherServices(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new ReferralsToOtherServicesActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.asrh_referrals_to_other_services);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.ASRH_REFERRALS_TO_OTHER_SERVICE).build();
        actionList.put(actionName, action);
    }

    protected void evaluateFacilitationMethod(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new FacilitationMethodsActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.asrh_facilitation_methods);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.ASRH_FACILITATION_METHOD).build();
        actionList.put(actionName, action);
    }

    public BaseAsrhVisitAction.Builder getBuilder(String title) {
        return new BaseAsrhVisitAction.Builder(mContext, title);
    }

    @Override
    public void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseAsrhVisitAction> map, final BaseAsrhVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            boolean result = true;
            try {
                submitVisit(editMode, memberID, map, "");
            } catch (Exception e) {
                Timber.e(e);
                result = false;
            }

            final boolean finalResult = result;
            appExecutors.mainThread().execute(() -> callBack.onSubmitted(finalResult));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseAsrhVisitAction> map, String parentEventType) throws Exception {
        // create a map of the different types

        Map<String, BaseAsrhVisitAction> externalVisits = new HashMap<>();
        Map<String, String> combinedJsons = new HashMap<>();
        String payloadType = null;
        String payloadDetails = null;

        // aggregate forms to be processed
        for (Map.Entry<String, BaseAsrhVisitAction> entry : map.entrySet()) {
            String json = entry.getValue().getJsonPayload();
            if (StringUtils.isNotBlank(json)) {
                // do not process events that are meant to be in detached mode
                // in a similar manner to the the aggregated events

                BaseAsrhVisitAction action = entry.getValue();
                BaseAsrhVisitAction.ProcessingMode mode = action.getProcessingMode();

                if (mode == BaseAsrhVisitAction.ProcessingMode.SEPARATE && StringUtils.isBlank(parentEventType)) {
                    externalVisits.put(entry.getKey(), entry.getValue());
                } else {
                    if (action.getActionStatus() != BaseAsrhVisitAction.Status.PENDING)
                        combinedJsons.put(entry.getKey(), json);
                }

                payloadType = action.getPayloadType().name();
                payloadDetails = action.getPayloadDetails();
            }
        }

        String type = StringUtils.isBlank(parentEventType) ? getEncounterType() : getEncounterType();

        // persist to database
        Visit visit = saveVisit(editMode, memberID, type, combinedJsons, parentEventType);
        if (visit != null) {
            saveVisitDetails(visit, payloadType, payloadDetails);
            processExternalVisits(visit, externalVisits, memberID);
        }

        if (asrhLibrary.isSubmitOnSave()) {
            List<Visit> visits = new ArrayList<>(1);
            visits.add(visit);
            VisitUtils.processVisits(visits, asrhLibrary.visitRepository(), asrhLibrary.visitDetailsRepository());

            Context context = asrhLibrary.getInstance().context().applicationContext();

        }
    }

    /**
     * recursively persist visits to the db
     *
     * @param visit
     * @param externalVisits
     * @param memberID
     * @throws Exception
     */
    protected void processExternalVisits(Visit visit, Map<String, BaseAsrhVisitAction> externalVisits, String memberID) throws Exception {
        if (visit != null && !externalVisits.isEmpty()) {
            for (Map.Entry<String, BaseAsrhVisitAction> entry : externalVisits.entrySet()) {
                Map<String, BaseAsrhVisitAction> subEvent = new HashMap<>();
                subEvent.put(entry.getKey(), entry.getValue());

                String subMemberID = entry.getValue().getBaseEntityID();
                if (StringUtils.isBlank(subMemberID)) subMemberID = memberID;

                submitVisit(false, subMemberID, subEvent, visit.getVisitType());
            }
        }
    }

    protected @Nullable Visit saveVisit(boolean editMode, String memberID, String encounterType, final Map<String, String> jsonString, String parentEventType) throws Exception {

        AllSharedPreferences allSharedPreferences = asrhLibrary.getInstance().context().allSharedPreferences();

        String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        Event baseEvent = JsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());

        // only tag the first event with the date
        if (StringUtils.isBlank(parentEventType)) {
            prepareEvent(baseEvent);
        } else {
            prepareSubEvent(baseEvent);
        }

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);

            String visitID = (editMode) ? visitRepository().getLatestVisit(memberID, getEncounterType()).getVisitId() : JsonFormUtils.generateRandomUUIDString();

            // reset database
            if (editMode) {
                Visit visit = visitRepository().getVisitByVisitId(visitID);
                if (visit != null) baseEvent.setEventDate(visit.getDate());

                VisitUtils.deleteProcessedVisit(visitID, memberID);
                deleteOldVisit(visitID);
            }

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visit.setParentVisitID(getParentVisitEventID(visit, parentEventType));

            visitRepository().addVisit(visit);
            return visit;
        }
        return null;
    }

    protected String getParentVisitEventID(Visit visit, String parentEventType) {
        return visitRepository().getParentVisitEventID(visit.getBaseEntityId(), parentEventType, visit.getDate());
    }

    @VisibleForTesting
    public VisitRepository visitRepository() {
        return AsrhLibrary.getInstance().visitRepository();
    }

    protected void deleteOldVisit(String visitID) {
        visitRepository().deleteVisit(visitID);
        AsrhLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(visitID);

        List<Visit> childVisits = visitRepository().getChildEvents(visitID);
        for (Visit v : childVisits) {
            visitRepository().deleteVisit(v.getVisitId());
            AsrhLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(v.getVisitId());
        }
    }


    protected void saveVisitDetails(Visit visit, String payloadType, String payloadDetails) {
        if (visit.getVisitDetails() == null) return;

        for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
            if (entry.getValue() != null) {
                for (VisitDetail d : entry.getValue()) {
                    d.setPreProcessedJson(payloadDetails);
                    d.setPreProcessedType(payloadType);
                    AsrhLibrary.getInstance().visitDetailsRepository().addVisitDetails(d);
                }
            }
        }
    }

    /**
     * Injects implementation specific changes to the event
     *
     * @param baseEvent
     */
    protected void prepareEvent(Event baseEvent) {
        if (baseEvent != null) {
            // add sbc date obs and last
            List<Object> list = new ArrayList<>();
            list.add(new Date());
            baseEvent.addObs(new Obs("concept", "text", "vmmc_visit_date", "", list, new ArrayList<>(), null, "vmmc_visit_date"));
        }
    }

    /**
     * injects additional meta data to the event
     *
     * @param baseEvent
     */
    protected void prepareSubEvent(Event baseEvent) {
        Timber.v("You can add information to sub events");
    }

    protected String getEncounterType() {
        return Constants.EVENT_TYPE.ASRH_FOLLOW_UP_VISIT;
    }

    protected String getTableName() {
        return Constants.TABLES.ASRH_FOLLOW_UP;
    }

}