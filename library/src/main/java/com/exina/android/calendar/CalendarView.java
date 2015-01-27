/*
 * Copyright (C) 2011 Chris Gao <chris@exina.net>
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarView
        extends ImageView {
    private static int WEEK_TOP_MARGIN = 74;
    private static int WEEK_LEFT_MARGIN = 40;
    private static int WEEK_HEIGHT = 15;
    private static int WEEK_WIDTH = 406;
    private static int CELL_WIDTH = 58;
    private static int CELL_HEIGHT = 53;
    private static int CELL_MARGIN_TOP = 92;
    private static int CELL_MARGIN_LEFT = 39;
    private static float CELL_TEXT_SIZE;

    private static final String TAG = "CalendarView";
    private Calendar mRightNow = null;
    private Drawable mWeekTitle = null;
    private String[] mWeekTitles = null;
    private Cell mToday = null;
    private Cell mSelected = null;
    private Cell[][] mCells = new Cell[6][7];
    private OnCellTouchListener mOnCellTouchListener = null;
    MonthDisplayHelper mHelper;
    Drawable mDecoration = null;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("E");

    /**
     * How we notify users the date has changed.
     */
    private OnDateChangedListener mOnDateChangedListener;

    /**
     * The callback used to indicate the user changes the date.
     */
    public interface OnDateChangedListener {

        /**
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility with
         *                    {@link java.util.Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateChanged(CalendarView view, int year, int monthOfYear, int dayOfMonth);
    }

    public interface OnCellTouchListener {
        public void onTouch(Cell cell);
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDecoration = context.getResources().getDrawable(R.drawable.typeb_calendar_today);
        initCalendarView();
    }

    private void initCalendarView() {
        mRightNow = Calendar.getInstance();
        // prepare static vars
        Resources res = getResources();
        WEEK_TOP_MARGIN = (int) res.getDimension(R.dimen.week_top_margin);
        WEEK_LEFT_MARGIN = (int) res.getDimension(R.dimen.week_left_margin);
        WEEK_HEIGHT = (int) res.getDimension(R.dimen.week_height);
        WEEK_WIDTH = (int) res.getDimension(R.dimen.week_width);

        CELL_WIDTH = (int) res.getDimension(R.dimen.cell_width);
        CELL_HEIGHT = (int) res.getDimension(R.dimen.cell_height);
        CELL_MARGIN_TOP = (int) res.getDimension(R.dimen.cell_margin_top);
        CELL_MARGIN_LEFT = (int) res.getDimension(R.dimen.cell_margin_left);

        CELL_TEXT_SIZE = res.getDimension(R.dimen.cell_text_size);
        // set background
        setImageResource(R.drawable.background);

        // generate weekday titles
        Calendar weektitleCalendar = (Calendar) mRightNow.clone();
        weektitleCalendar.set(Calendar.DAY_OF_WEEK, weektitleCalendar.getFirstDayOfWeek());

        mWeekTitles = new String[7];
        for (int i = 0; i < mWeekTitles.length; i++) {
            mWeekTitles[i] = mFormatter.format(weektitleCalendar.getTime());
            weektitleCalendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        mWeekTitle = createWeekTitleDrawable();

        mHelper = new MonthDisplayHelper(mRightNow.get(Calendar.YEAR), mRightNow.get(Calendar.MONTH), mRightNow.getFirstDayOfWeek());

    }

    private Drawable createWeekTitleDrawable() {
        Paint paint = new Paint(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        paint.setTextSize(15f);

        Bitmap bitmap = Bitmap.createBitmap(WEEK_WIDTH, WEEK_HEIGHT, Config.ARGB_8888);
        bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
        Canvas canvas = new Canvas(bitmap);

        int x = (int) CELL_WIDTH / 2;
        int y = (int) bitmap.getHeight() / 2;
        int dy = (int) (-paint.ascent() - paint.descent()) / 2;

        for (int i = 0; i < mWeekTitles.length; i++) {
            int dx = (int) paint.measureText(mWeekTitles[i]) / 2;
            canvas.drawText(mWeekTitles[i], x - dx, y + dy + 2, paint);
            x += CELL_WIDTH;
        }

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void initCells() {
        class _calendar {
            public int day;
            public boolean thisMonth;

            public _calendar(int d, boolean b) {
                day = d;
                thisMonth = b;
            }

            public _calendar(int d) {
                this(d, false);
            }
        }
        ;
        _calendar tmp[][] = new _calendar[6][7];

        for (int i = 0; i < tmp.length; i++) {
            int n[] = mHelper.getDigitsForRow(i);
            for (int d = 0; d < n.length; d++) {
                if (mHelper.isWithinCurrentMonth(i, d))
                    tmp[i][d] = new _calendar(n[d], true);
                else
                    tmp[i][d] = new _calendar(n[d]);

            }
        }

        Calendar today = Calendar.getInstance();
        int thisDay = 0;
        mToday = null;
        if (mHelper.getYear() == today.get(Calendar.YEAR) && mHelper.getMonth() == today.get(Calendar.MONTH)) {
            thisDay = today.get(Calendar.DAY_OF_MONTH);
        }
        // build cells
        Rect Bound = new Rect(CELL_MARGIN_LEFT, CELL_MARGIN_TOP, CELL_WIDTH + CELL_MARGIN_LEFT, CELL_HEIGHT
                + CELL_MARGIN_TOP);
        for (int week = 0; week < mCells.length; week++) {
            for (int day = 0; day < mCells[week].length; day++) {
                if (tmp[week][day].thisMonth) {
                    if (day == 0 || day == 6) {
                        mCells[week][day] = new RedCell(tmp[week][day].day, new Rect(Bound), CELL_TEXT_SIZE);
                        mCells[week][day].setSelected(tmp[week][day].day == mRightNow.get(Calendar.DAY_OF_MONTH));
                    } else {
                        mCells[week][day] = new Cell(tmp[week][day].day, new Rect(Bound), CELL_TEXT_SIZE);
                        mCells[week][day].setSelected(tmp[week][day].day == mRightNow.get(Calendar.DAY_OF_MONTH));
                    }
                } else
                    mCells[week][day] = new GrayCell(tmp[week][day].day, new Rect(Bound), CELL_TEXT_SIZE);

                Bound.offset(CELL_WIDTH, 0); // move to next column

                // get today
                if (tmp[week][day].day == thisDay && tmp[week][day].thisMonth) {
                    mToday = mCells[week][day];
                    mDecoration.setBounds(mToday.getBound());
                }

                // get selected cell
                if (mCells[week][day].isSelected()) {
                    mSelected = mCells[week][day];
                }
            }
            Bound.offset(0, CELL_HEIGHT); // move to next row and first column
            Bound.left = CELL_MARGIN_LEFT;
            Bound.right = CELL_MARGIN_LEFT + CELL_WIDTH;
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        android.util.Log.d(TAG, "left=" + left);
        Rect re = getDrawable().getBounds();
        WEEK_LEFT_MARGIN = CELL_MARGIN_LEFT = (right - left - re.width()) / 2;
        mWeekTitle.setBounds(WEEK_LEFT_MARGIN, WEEK_TOP_MARGIN, WEEK_LEFT_MARGIN + mWeekTitle.getMinimumWidth(),
                WEEK_TOP_MARGIN + mWeekTitle.getMinimumHeight());
        initCells();
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setTimeInMillis(long milliseconds) {
        mRightNow.setTimeInMillis(milliseconds);
        initCells();
        this.invalidate();
        notifyDateChanged();
    }

    public int getYear() {
        return mHelper.getYear();
    }

    public int getMonth() {
        return mHelper.getMonth();
    }

    public void nextMonth() {
        mHelper.nextMonth();
        mRightNow.set(Calendar.MONTH, mHelper.getMonth());
        mRightNow.set(Calendar.YEAR, mHelper.getYear());
        initCells();
        invalidate();
        notifyDateChanged();
    }

    public void previousMonth() {
        mHelper.previousMonth();
        mRightNow.set(Calendar.MONTH, mHelper.getMonth());
        mRightNow.set(Calendar.YEAR, mHelper.getYear());
        initCells();
        invalidate();
        notifyDateChanged();
    }

    public boolean firstDay(int day) {
        return day == 1;
    }

    public boolean lastDay(int day) {
        return mHelper.getNumberOfDaysInMonth() == day;
    }

    public void goToday() {
        Calendar cal = Calendar.getInstance();
        mHelper = new MonthDisplayHelper(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        initCells();
        invalidate();
        notifyDateChanged();
    }

    public Calendar getDate() {
        return mRightNow;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (Cell[] week : mCells) {
            for (Cell day : week) {
                if (day.hitTest((int) event.getX(), (int) event.getY())) {
                    if (mSelected != day) {
                        day.setSelected(true);
                        mSelected.setSelected(false);
                        mSelected = day;

                        mRightNow.set(Calendar.DAY_OF_MONTH, mSelected.getDayOfMonth());

                        invalidate();
                        notifyDateChanged();
                    }

                    if (mOnCellTouchListener != null) {
                        mOnCellTouchListener.onTouch(day);
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void setOnCellTouchListener(OnCellTouchListener p) {
        mOnCellTouchListener = p;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw background
        super.onDraw(canvas);
        mWeekTitle.draw(canvas);

        // draw cells
        for (Cell[] week : mCells) {
            for (Cell day : week) {
                day.draw(canvas);
            }
        }

        // draw today
        if (mDecoration != null && mToday != null) {
            mDecoration.draw(canvas);
        }
    }

    public class GrayCell
            extends Cell {
        public GrayCell(int dayOfMon, Rect rect, float s) {
            super(dayOfMon, rect, s, Color.LTGRAY);
        }
    }

    public class RedCell
            extends Cell {
        public RedCell(int dayOfMon, Rect rect, float s) {
            super(dayOfMon, rect, s, 0xDDDD0000);
        }

    }

    private void notifyDateChanged() {
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(CalendarView.this, mHelper.getYear(), mHelper.getMonth(),
                    mSelected.getDayOfMonth());
        }
    }

    void setDateChangedListener(OnDateChangedListener onDateChangedListener) {
        this.mOnDateChangedListener = onDateChangedListener;
    }

    public boolean belongsToPreviousMonth(Cell cell) {
        int week = 0;

        for (int day = 0; day < mCells[week].length; day++) {
            if (cell == mCells[week][day]) {
                return !mHelper.isWithinCurrentMonth(week, day);
            }
        }

        return false;
    }

    public boolean belongsToNextMonth(Cell cell) {

        for (int week = mCells.length - 1; week > 0; week--) {
            for (int day = mCells[week].length - 1; day > 0; day--) {
                if (cell == mCells[week][day]) {
                    return week > 0 && !mHelper.isWithinCurrentMonth(week, day);
                }
            }
        }

        return false;
    }
}
