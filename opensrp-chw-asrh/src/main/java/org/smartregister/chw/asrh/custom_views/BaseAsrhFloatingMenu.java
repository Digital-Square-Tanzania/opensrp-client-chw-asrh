package org.smartregister.chw.asrh.custom_views;

import android.app.Activity;
import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.chw.asrh.R;
import org.smartregister.chw.asrh.domain.MemberObject;
import org.smartregister.chw.asrh.fragment.BaseAsrhCallDialogFragment;

public class BaseAsrhFloatingMenu extends LinearLayout implements View.OnClickListener {
    private MemberObject MEMBER_OBJECT;

    public BaseAsrhFloatingMenu(Context context, MemberObject MEMBER_OBJECT) {
        super(context);
        initUi();
        this.MEMBER_OBJECT = MEMBER_OBJECT;
    }

    protected void initUi() {
        inflate(getContext(), R.layout.view_asrh_floating_menu, this);
        FloatingActionButton fab = findViewById(R.id.asrh_fab);
        if (fab != null)
            fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.asrh_fab) {
            Activity activity = (Activity) getContext();
            BaseAsrhCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }  else if (view.getId() == R.id.asrh_refer_to_facility_layout) {
            Activity activity = (Activity) getContext();
            BaseAsrhCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }
    }
}