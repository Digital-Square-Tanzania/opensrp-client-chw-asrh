package org.smartregister.chw.asrh.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.smartregister.chw.asrh.Asrh;
import org.smartregister.chw.asrh.actionhelper.ArtAdherenceCounsellingActionHelper;
import org.smartregister.chw.asrh.actionhelper.CommentsActionHelper;
import org.smartregister.chw.asrh.actionhelper.HealthEducationActionHelper;
import org.smartregister.chw.asrh.actionhelper.HealthEducationOnHivInterventionsActionHelper;
import org.smartregister.chw.asrh.actionhelper.HivHealthEducationAsrhMaterialsActionHelper;
import org.smartregister.chw.asrh.actionhelper.AsrhActivityActionHelper;
import org.smartregister.chw.asrh.actionhelper.AsrhVisitActionHelper;
import org.smartregister.chw.asrh.actionhelper.ServicesSurveyActionHelper;
import org.smartregister.chw.asrh.dao.AsrhDao;
import org.smartregister.chw.asrh.util.Constants;
import org.smartregister.chw.asrh.util.JsonFormUtils;
import org.smartregister.chw.asrh.R;
import org.smartregister.chw.asrh.contract.BaseAsrhVisitContract;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.domain.Visit;
import org.smartregister.chw.asrh.domain.VisitDetail;
import org.smartregister.chw.asrh.model.BaseAsrhVisitAction;
import org.smartregister.chw.asrh.repository.VisitRepository;
import org.smartregister.chw.asrh.util.AppExecutors;
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

    private final Asrh asrh;
    private final LinkedHashMap<String, BaseAsrhVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;
    private Map<String, List<VisitDetail>> details = null;

    private BaseAsrhVisitContract.InteractorCallBack callBack;

    @VisibleForTesting
    public BaseAsrhVisitInteractor(AppExecutors appExecutors, Asrh Asrh, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.asrh = Asrh;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
    }

    public BaseAsrhVisitInteractor() {
        this(new AppExecutors(), Asrh.getInstance(), Asrh.getInstance().getEcSyncHelper());
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
        boolean isFirstVisit;
        if (view.getEditMode()) {
            Visit lastVisit = asrh.visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.ASRH_FOLLOW_UP_VISIT);
            isFirstVisit = asrh.visitRepository().getVisits(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.ASRH_FOLLOW_UP_VISIT).size() < 2;
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(asrh.visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        } else {
            isFirstVisit = asrh.visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.ASRH_FOLLOW_UP_VISIT) == null;
        }

        final Runnable runnable = () -> {
            try {
                if (!isFirstVisit && !memberObject.getHivStatus().contains("positive"))
                    evaluateHivStatus(memberObject, details);

                evaluateSbcActivity(memberObject, details);
                evaluateServicesSurvey(memberObject, details);
                evaluateHealthEducation(memberObject, details);
                evaluateHealthEducationOnHivInterventions(memberObject, details);
                evaluateHealthEducationSbcMaterials(memberObject, details);

                if (memberObject.getHivStatus().contains("positive"))
                    evaluateArtAdherenceCounselling(memberObject, details);

                evaluateComments(memberObject, details);

            } catch (BaseAsrhVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void evaluateHivStatus(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new HivStatusActionHelper(mContext, memberObject);
        String actionName = mContext.getString(R.string.sbc_visit_action_title_hiv_status);
        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_HIV_STATUS).build();
        actionList.put(actionName, action);
    }

    protected void evaluateSbcActivity(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new AsrhActivityActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_sbc_activity);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_ACTIVITY).build();

        actionList.put(actionName, action);
    }

    protected void evaluateServicesSurvey(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new ServicesSurveyActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_services_survey);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_SERVICE_SURVEY).build();

        actionList.put(actionName, action);
    }

    protected void evaluateHealthEducation(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new HealthEducationActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_health_education);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_HEALTH_EDUCATION).build();

        actionList.put(actionName, action);
    }

    protected void evaluateHealthEducationOnHivInterventions(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new HealthEducationOnHivInterventionsActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_health_education_on_hiv_interventions);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_HEALTH_EDUCATION_ON_HIV).build();

        actionList.put(actionName, action);
    }

    protected void evaluateHealthEducationSbcMaterials(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new HivHealthEducationAsrhMaterialsActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_health_education_sbc_materials);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.HEALTH_EDUCATION_SBC_MATERIALS).build();

        actionList.put(actionName, action);
    }

    protected void evaluateArtAdherenceCounselling(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new ArtAdherenceCounsellingActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_art_and_condom_education);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_ART_CONDOM_EDUCATION).build();

        actionList.put(actionName, action);
    }

    protected void evaluateComments(MemberObject memberObject, Map<String, List<VisitDetail>> details) throws BaseAsrhVisitAction.ValidationException {
        AsrhVisitActionHelper actionHelper = new CommentsActionHelper(mContext, memberObject);

        String actionName = mContext.getString(R.string.sbc_visit_action_title_comments);

        BaseAsrhVisitAction action = getBuilder(actionName).withOptional(true).withDetails(details).withHelper(actionHelper).withFormName(Constants.FORMS.SBC_COMMENTS).build();

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

        if (asrh.isSubmitOnSave()) {
            List<Visit> visits = new ArrayList<>(1);
            visits.add(visit);
            VisitUtils.processVisits(visits, asrh.visitRepository(), asrh.visitDetailsRepository());

            Context context = asrh.getInstance().context().applicationContext();

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

        AllSharedPreferences allSharedPreferences = asrh.getInstance().context().allSharedPreferences();

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
        return Asrh.getInstance().visitRepository();
    }

    protected void deleteOldVisit(String visitID) {
        visitRepository().deleteVisit(visitID);
        Asrh.getInstance().visitDetailsRepository().deleteVisitDetails(visitID);

        List<Visit> childVisits = visitRepository().getChildEvents(visitID);
        for (Visit v : childVisits) {
            visitRepository().deleteVisit(v.getVisitId());
            Asrh.getInstance().visitDetailsRepository().deleteVisitDetails(v.getVisitId());
        }
    }


    protected void saveVisitDetails(Visit visit, String payloadType, String payloadDetails) {
        if (visit.getVisitDetails() == null) return;

        for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
            if (entry.getValue() != null) {
                for (VisitDetail d : entry.getValue()) {
                    d.setPreProcessedJson(payloadDetails);
                    d.setPreProcessedType(payloadType);
                    Asrh.getInstance().visitDetailsRepository().addVisitDetails(d);
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
        return Constants.TABLES.ARSH_REGISTER;
    }

    class HivStatusActionHelper extends AsrhVisitActionHelper {
        protected Context context;
        protected MemberObject memberObject;
        protected String hivStatus;

        public HivStatusActionHelper(Context context, MemberObject memberObject) {
            this.context = context;
            this.memberObject = memberObject;
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

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                hivStatus = JsonFormUtils.getValue(jsonObject, "hiv_status");

                if(hivStatus.contains("positive")){
                    evaluateArtAdherenceCounselling(memberObject, details);
                }else{
                    actionList.remove(mContext.getString(R.string.sbc_visit_action_title_art_and_condom_education));
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            return null;
        }

        @Override
        public BaseAsrhVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isNotBlank(hivStatus)) {
                return BaseAsrhVisitAction.Status.COMPLETED;
            } else {
                return BaseAsrhVisitAction.Status.PENDING;
            }
        }
    }

}