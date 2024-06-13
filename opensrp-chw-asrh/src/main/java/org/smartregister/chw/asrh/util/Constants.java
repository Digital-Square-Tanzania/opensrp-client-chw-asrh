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
        String ASRH_REGISTRATION = "AYSRH Enrollment";
        String ASRH_FOLLOW_UP_VISIT = "AYSRH Follow-up Visit";

        String VOID_EVENT = "Void Event";

        String DELETE_EVENT = "Delete Event";
    }

    interface FORMS {
        String ASRH_ENROLLMENT = "asrh_enrollment";

        String ASRH_CLIENT_STATUS = "asrh_client_status";

        String ASRH_HEALTH_EDUCATION = "asrh_health_education";

        String ASRH_SEXUAL_REPRODUCTIVE_HEALTH_EDUCATION = "asrh_sexual_reproductive_health_education";

        String ASRH_MENTAL_HEALTH_AND_SUBSTANCE_ABUSE = "asrh_mental_health_and_substance_abuse";

        String ASRH_REFERRALS_TO_OTHER_SERVICE = "asrh_referrals_to_other_services";

        String ASRH_FACILITATION_METHOD = "asrh_facilitation_methods";

    }

    interface TABLES {
        String ASRH_REGISTER = "ec_asrh_register";

        String ASRH_FOLLOW_UP = "ec_asrh_follow_up_visit";

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