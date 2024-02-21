package org.smartregister.chw.asrh.model;

import org.json.JSONObject;
import org.smartregister.chw.asrh.contract.AsrhRegisterContract;
import org.smartregister.chw.asrh.util.AsrhJsonFormUtils;

public class BaseAsrhRegisterModel implements AsrhRegisterContract.Model {

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = AsrhJsonFormUtils.getFormAsJson(formName);
        AsrhJsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }

}
