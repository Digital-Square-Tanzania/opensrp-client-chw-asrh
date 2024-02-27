package org.smartregister.chw.asrh.presenter;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.smartregister.chw.asrh.contract.AsrhProfileContract;
import org.smartregister.chw.asrh.domain.MemberObject;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class BaseAsrhProfilePresenterTestLibrary {

    @Mock
    private AsrhProfileContract.View view = Mockito.mock(AsrhProfileContract.View.class);

    @Mock
    private AsrhProfileContract.Interactor interactor = Mockito.mock(AsrhProfileContract.Interactor.class);

    @Mock
    private MemberObject memberObject = new MemberObject();

    private BaseAsrhProfilePresenter profilePresenter = new BaseAsrhProfilePresenter(view, interactor, memberObject);


    @Test
    public void fillProfileDataCallsSetProfileViewWithDataWhenPassedMemberObject() {
        profilePresenter.fillProfileData(memberObject);
        verify(view).setProfileViewWithData();
    }

    @Test
    public void fillProfileDataDoesntCallsSetProfileViewWithDataIfMemberObjectEmpty() {
        profilePresenter.fillProfileData(null);
        verify(view, never()).setProfileViewWithData();
    }

    @Test
    public void malariaTestDatePeriodIsLessThanSeven() {
        profilePresenter.recordAsrhButton("");
        verify(view).hideView();
    }

    @Test
    public void malariaTestDatePeriodGreaterThanTen() {
        profilePresenter.recordAsrhButton("OVERDUE");
        verify(view).setOverDueColor();
    }

    @Test
    public void malariaTestDatePeriodIsMoreThanFourteen() {
        profilePresenter.recordAsrhButton("EXPIRED");
        verify(view).hideView();
    }

    @Test
    public void refreshProfileBottom() {
        profilePresenter.refreshProfileBottom();
        verify(interactor).refreshProfileInfo(memberObject, profilePresenter.getView());
    }

    @Test
    public void saveForm() {
        profilePresenter.saveForm(null);
        verify(interactor).saveRegistration(null, view);
    }
}
