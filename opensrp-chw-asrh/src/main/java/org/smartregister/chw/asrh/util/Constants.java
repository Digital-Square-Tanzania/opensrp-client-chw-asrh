package org.smartregister.chw.asrh.util;

public interface Constants {

    int REQUEST_CODE_GET_JSON = 2244;
    String ENCOUNTER_TYPE = "encounter_type";
    String STEP_ONE = "step1";
    String STEP_TWO = "step2";

    interface JSON_FORM_EXTRA {
        String JSON = "json";
        String ENCOUNTER_TYPE = "encounter_type";

        String DELETE_EVENT_ID = "deleted_event_id";

        String DELETE_FORM_SUBMISSION_ID = "deleted_form_submission_id";
    }

    interface EVENT_TYPE {
        String ASRH_REGISTRATION = "ASRH Registration";
        String ASRH_FOLLOW_UP_VISIT = "ASRH Follow-up Visit";

        String VOID_EVENT = "Void Event";

        String DELETE_EVENT = "Delete Event";
    }

    interface FORMS {
        String ASRH_ENROLLMENT = "asrh_enrollment";

        String SBC_HIV_STATUS = "sbc_hiv_status";

        String SBC_ACTIVITY = "sbc_activity";

        String SBC_HEALTH_EDUCATION = "sbc_health_education";

        String SBC_HEALTH_EDUCATION_ON_HIV = "sbc_health_education_hiv_intervention";

        String HEALTH_EDUCATION_SBC_MATERIALS = "sbc_health_education_hiv_materials";

        String SBC_SERVICE_SURVEY = "sbc_service_survey";

        String SBC_ART_CONDOM_EDUCATION = "sbc_art_condom_education";

        String SBC_COMMENTS = "sbc_comments";

        String SBC_MOBILIZATION_SESSION= "sbc_health_education_mobilization";

        String SBC_MONTHLY_SOCIAL_MEDIA_REPORT= "sbc_monthly_social_media_report";
    }

    interface TABLES {
        String ARSH_REGISTER = "ec_asrh_register";

        String ASRH_FOLLOW_UP = "ec_asrh_follow_up_visit";

        String ASRH_MOBILIZATION_SESSIONS = "ec_asrh_mobilization_session";

        String ASRH_MONTHLY_SOCIAL_MEDIA_REPORT = "ec_asrh_monthly_social_media_report";

    }

    interface ACTIVITY_PAYLOAD {
        String BASE_ENTITY_ID = "BASE_ENTITY_ID";
        String FAMILY_BASE_ENTITY_ID = "FAMILY_BASE_ENTITY_ID";
        String ACTION = "ACTION";
        String ASRH_FORM_NAME = "ASRH_FORM_NAME";
        String EDIT_MODE = "editMode";
        String MEMBER_PROFILE_OBJECT = "MemberObject";

    }

    interface ACTIVITY_PAYLOAD_TYPE {
        String REGISTRATION = "REGISTRATION";
        String FOLLOW_UP_VISIT = "FOLLOW_UP_VISIT";
    }

    interface CONFIGURATION {
        String ASRH_REGISTRATION_CONFIGURATION = "asrh_registration";
    }

}