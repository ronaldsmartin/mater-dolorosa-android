package org.mdssf.materdolorosa.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mdssf.materdolorosa.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * A {@link Fragment} subclass that controls the layout for the Contact section.
 *
 */
public class ParishContactFragment extends Fragment {

    @SuppressWarnings("unused")
    public static final String TAG = "ParishContactFragment";

    public ParishContactFragment() {
        // Required empty public constructor
    }

    //region Office open check
    private static boolean isParishOfficeOpen(String timezoneId) {
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

    private static boolean isCcfOfficeOpen(String timezoneId) {
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

    //region Fragment Lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_parish_contact, container, false);

        // Set the office status text to Open if the user's current time is within the bounds of
        // the office hours in the office's timezone.
        final String timezoneId = getString(R.string.timezone_id);
        if (isParishOfficeOpen(timezoneId))
            setStatusOpen(rootView, R.id.card_parish_currently_open);
        if (isCcfOfficeOpen(timezoneId)) setStatusOpen(rootView, R.id.card_ccf_currently_open);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * Set the TextView with {@code textViewId} in {@code rootView} to display the text
     * 'Currently open' in light green.
     *
     * @param rootView   Layout in which the TextView resides
     * @param textViewId Id of the TextView to alter
     */
    private void setStatusOpen(View rootView, int textViewId) {
        TextView officeStatus =
                (TextView) rootView.findViewById(textViewId);
        officeStatus.setText(R.string.currently_open);
        officeStatus.setTextColor(getResources().getColor(R.color.holo_green_light));
    }
    //endregion
}
