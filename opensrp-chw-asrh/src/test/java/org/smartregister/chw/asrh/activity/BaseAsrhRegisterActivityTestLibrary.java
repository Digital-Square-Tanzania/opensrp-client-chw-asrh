package org.smartregister.chw.asrh.activity;

import android.content.Intent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BaseAsrhRegisterActivityTestLibrary {
    @Mock
    public Intent data;
    @Mock
    private BaseAsrhRegisterActivity baseTestRegisterActivity = new BaseAsrhRegisterActivity();

    @Test
    public void assertNotNull() {
        Assert.assertNotNull(baseTestRegisterActivity);
    }

    @Test
    public void testFormConfig() {
        Assert.assertNull(baseTestRegisterActivity.getFormConfig());
    }

    @Test
    public void checkIdentifier() {
        Assert.assertNotNull(baseTestRegisterActivity.getViewIdentifiers());
    }

    @Test(expected = Exception.class)
    public void onActivityResult() throws Exception {
        Whitebox.invokeMethod(baseTestRegisterActivity, "onActivityResult", 2244, -1, data);
        Mockito.verify(baseTestRegisterActivity.presenter()).saveForm(null);
    }

}
