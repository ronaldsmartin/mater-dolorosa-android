package org.mdssf.materdolorosa.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.mdssf.materdolorosa.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A {@link Fragment} subclass that controls the layout for the Contact section.
 *
 */
public class ParishContactFragment extends Fragment {

    @SuppressWarnings("unused")
    public static final String TAG = "ParishContactFragment";

    //region Views
    @InjectView(R.id.card_parish_currently_open)
    TextView mParishOfficeOpenIndicator;
    @InjectView(R.id.card_ccf_currently_open)
    TextView mCcfOfficeOpenIndicator;
    //endregion

    //region Fragment Lifecycle
    public ParishContactFragment() {
        // Required empty public constructor
    }

    //region Office open check
    public static boolean isParishOfficeOpen(String timezoneId) {
        Date currentTime = Calendar.getInstance().getTime();

        // Return false if it's the weekend
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) return false;

        // Reusable calendar is used to compare the current time to opening/closing times
        Calendar timeMarker = Calendar.getInstance(TimeZone.getTimeZone(timezoneId));
        timeMarker.set(Calendar.MINUTE, 0);
        timeMarker.set(Calendar.SECOND, 0);

        // Return false if it's before 9 a.m.
        timeMarker.set(Calendar.HOUR_OF_DAY, 9);
        if (currentTime.before(timeMarker.getTime())) return false;

        // Return false if it's after 5 p.m.
        timeMarker.set(Calendar.HOUR_OF_DAY, 17);
        if (currentTime.after(timeMarker.getTime())) return false;

        // Return false if it's lunch time (between 12:00 and 13:00)
        timeMarker.set(Calendar.HOUR_OF_DAY, 12);
        boolean lunchHasStarted = currentTime.after(timeMarker.getTime());

        timeMarker.set(Calendar.HOUR_OF_DAY, 13);
        lunchHasStarted &= currentTime.before(timeMarker.getTime());
        return !lunchHasStarted;
    }
    //endregion

    public static boolean isCcfOfficeOpen(String timezoneId) {
        Date currentTime = Calendar.getInstance().getTime();
        boolean isOpen;

        // Reusable calendar is used to compare the current time to opening/closing times
        Calendar timeMarker = Calendar.getInstance(TimeZone.getTimeZone(timezoneId));
        timeMarker.set(Calendar.MINUTE, 0);
        timeMarker.set(Calendar.SECOND, 0);
        switch (timeMarker.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                // Open on the first and third Sundays of the month.
                int dateInMonth = timeMarker.get(Calendar.DAY_OF_MONTH);
                isOpen = dateInMonth < 8 || (15 <= dateInMonth && dateInMonth <= 21);

                // Open from 9 a.m. - 1:30 p.m.
                timeMarker.set(Calendar.HOUR_OF_DAY, 9);
                isOpen &= currentTime.after(timeMarker.getTime());

                timeMarker.set(Calendar.HOUR_OF_DAY, 13);
                timeMarker.set(Calendar.MINUTE, 30);
                isOpen &= currentTime.before(timeMarker.getTime());
                break;
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
                // Open Mon, Tues from 3:30 - 7:30 p.m.
                timeMarker.set(Calendar.HOUR_OF_DAY, 15);
                timeMarker.set(Calendar.MINUTE, 30);
                isOpen = currentTime.after(timeMarker.getTime());

                timeMarker.set(Calendar.HOUR_OF_DAY, 19);
                isOpen &= currentTime.before(timeMarker.getTime());
                break;
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
                // Open Wed, Thurs from 2:30 - 6:30 p.m.
                timeMarker.set(Calendar.HOUR_OF_DAY, 14);
                timeMarker.set(Calendar.MINUTE, 30);
                isOpen = currentTime.after(timeMarker.getTime());

                timeMarker.set(Calendar.HOUR_OF_DAY, 18);
                timeMarker.set(Calendar.MINUTE, 30);
                isOpen &= currentTime.before(timeMarker.getTime());
                break;
            default:
                // Closed on Fridays and Saturdays.
                isOpen = false;
        }
        return isOpen;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_parish_contact, container, false);

        ButterKnife.inject(this, rootView);

        refreshOpenLabels();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshOpenLabels();
    }
    //endregion

    //region Open indicators
    /**
     * Set the office status text to Open if the user's current time is within the bounds of
     * the office hours in the office's timezone.
     */
    private void refreshOpenLabels() {
        final String timezoneId = getString(R.string.timezone_id);

        if (isParishOfficeOpen(timezoneId)) setStatusOpen(mParishOfficeOpenIndicator);
        else setStatusClosed(mParishOfficeOpenIndicator);

        if (isCcfOfficeOpen(timezoneId)) setStatusOpen(mCcfOfficeOpenIndicator);
        else setStatusClosed(mCcfOfficeOpenIndicator);
    }

    /**
     * Set a TextView to display 'Currently closed' in red text.
     *
     * @param openIndicator The TextView to set to closed.
     */
    private void setStatusClosed(TextView openIndicator) {
        openIndicator.setText(R.string.currently_closed);
        openIndicator.setTextColor(getResources().getColor(R.color.holo_red_dark));
    }

    /**
     * Set a TextView to display 'Currently open' in green text.
     *
     * @param openIndicator The TextView to set to open.
     */
    private void setStatusOpen(TextView openIndicator) {
        openIndicator.setText(R.string.currently_open);
        openIndicator.setTextColor(getResources().getColor(R.color.holo_green_light));
    }
    //endregion

    //region OnClick methods
    @SuppressWarnings("unused")
    @OnClick({R.id.btn_call_parish_office, R.id.btn_call_parish_office2, R.id.btn_call_ccf_office})
    public void onClickCall(Button button) {
        String phoneNumber = "tel:";
        switch (button.getId()) {
            case R.id.btn_call_parish_office:
            case R.id.btn_call_parish_office2:
                phoneNumber += getString(R.string.parish_office_phone);
                break;
            case R.id.btn_call_ccf_office:
                phoneNumber += getString(R.string.ccf_office_phone);
                break;
        }

        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse(phoneNumber));
        startActivity(dialIntent);
    }

    @SuppressWarnings("unused")
    @OnClick({R.id.btn_email_parish_office, R.id.btn_email_ccf_office})
    public void onClickEmail(Button button) {
        String email = "mailto:";
        switch (button.getId()) {
            case R.id.btn_email_parish_office:
                email += getString(R.string.parish_office_email);
                break;
            case R.id.btn_email_ccf_office:
                email += getString(R.string.ccf_office_email);
                break;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(email));
        startActivity(emailIntent);
    }

    @SuppressWarnings("unused")
    @OnClick({R.id.btn_navigate_parish_office, R.id.btn_navigate_ccf_office})
    public void onClickNavigate(Button button) {
        String latitude, longitude;
        switch (button.getId()) {
            case R.id.btn_navigate_parish_office:
                latitude = getString(R.string.parish_office_latitude);
                longitude = getString(R.string.parish_office_longitude);
                break;
            case R.id.btn_navigate_ccf_office:
                latitude = getString(R.string.ccf_office_latitude);
                longitude = getString(R.string.ccf_office_longitude);
                break;
            default:
                latitude = getString(R.string.parish_office_latitude);
                longitude = getString(R.string.parish_office_longitude);
        }
        String mapsUri = String.format(getString(R.string.map_url), latitude, longitude);
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUri));
        startActivity(mapsIntent);
    }
    //endregion
}
