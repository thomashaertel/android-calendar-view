/*
 * Copyright (C) 2011 Thomas Haertel <mail@thomashaertel.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exina.android.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class CalendarPickerDialog extends AlertDialog implements OnClickListener, CalendarView.OnDateChangedListener, CalendarView.OnCellTouchListener {

    public static final String MIME_TYPE = "vnd.android.cursor.dir/vnd.exina.android.calendar.date";

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private final java.text.DateFormat mTitleDateFormat;

    protected CalendarView mView;
    protected TextView mHint;
    protected TextView mHit;
    protected Handler mHandler = new Handler();

    protected OnDateSetListener mCallBack;

    private Calendar mCalendar;

    private int mInitialYear;
    private int mInitialMonth;
    private int mInitialDay;

    public interface OnDateSetListener {
        void onDateSet(CalendarView view, int year, int monthOfYear, int dayOfMonth);
    }

    public CalendarPickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        this(context, callBack, null, year, monthOfYear, dayOfMonth);
    }

    public CalendarPickerDialog(Context context, OnDateSetListener callBack, String hint, int year, int monthOfYear,
                                int dayOfMonth) {
        super(context);

        mTitleDateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL);

        mInitialYear = year;
        mInitialMonth = monthOfYear;
        mInitialDay = dayOfMonth;

        mCalendar = Calendar.getInstance();
        updateTitle(mInitialYear, mInitialMonth, mInitialDay);

        setButton(BUTTON_POSITIVE, context.getText(R.string.date_time_set), this);
        setButton(BUTTON_NEGATIVE, context.getText(R.string.cancel), (OnClickListener) null);
        setIcon(R.drawable.ic_dialog_time);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendar, null);
        setView(view);

        mView = (CalendarView) view.findViewById(R.id.calendar);
        mView.setDateChangedListener(this);
        mView.setOnCellTouchListener(this);
        mView.setTimeInMillis(mCalendar.getTimeInMillis());

        mCallBack = callBack;

        mHint = (TextView) view.findViewById(R.id.hint);

        if (hint == null || hint.length() == 0) {
            mHint.setVisibility(View.INVISIBLE);
        } else {
            mHint.setText(hint);
        }
    }

    public void onTouch(Cell cell) {
        // mCallBack.onDateSet(mView, mView.getYear(), mView.getMonth(),
        // cell.getDayOfMonth());

        if (mView.belongsToPreviousMonth(cell))
            mView.previousMonth();
        else if (mView.belongsToNextMonth(cell))
            mView.nextMonth();
        else
            return;

        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(CalendarPickerDialog.this.getContext(),
                        DateUtils.getMonthString(mView.getMonth(), DateUtils.LENGTH_LONG) + " " + mView.getYear(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDateChanged(CalendarView view, int year, int month, int day) {
        updateTitle(year, month, day);
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mInitialYear = year;
        mInitialMonth = monthOfYear;
        mInitialDay = dayOfMonth;
        mView.setTimeInMillis(new Date(year, monthOfYear, dayOfMonth).getTime());
    }

    private void updateTitle(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        setTitle(mTitleDateFormat.format(mCalendar.getTime()));
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallBack != null) {
            mView.clearFocus();
            Calendar date = mView.getDate();
            mCallBack.onDateSet(mView, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        Calendar date = mView.getDate();
        state.putInt(YEAR, date.get(Calendar.YEAR));
        state.putInt(MONTH, date.get(Calendar.MONTH));
        state.putInt(DAY, date.get(Calendar.DAY_OF_MONTH));
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mView.setTimeInMillis(new Date(year, month, day).getTime());
        updateTitle(year, month, day);
    }

}
