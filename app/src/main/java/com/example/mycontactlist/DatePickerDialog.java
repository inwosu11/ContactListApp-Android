package com.example.mycontactlist;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialog extends DialogFragment{
    Calendar selectedDate;

    public interface SaveDateListener{
        void didFinishDatePickerDialog(Calendar selectedTime);
    }
    public DatePickerDialog(){
        //Empty Constructor
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.select_date, container);

        getDialog().setTitle("Select Date");
        selectedDate = Calendar.getInstance();

        final CalendarView cv = view.findViewById(R.id.calendarView);
        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView calendarView, int year, int month,
                                            int day) {
                selectedDate.set(month, day, year);
            }
        });
        Button saveButton = view.findViewById(R.id.buttonSelect);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem(selectedDate);
            }
        });
        Button cancelButton = view.findViewById(R.id.buttonCancel);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItem(selectedDate);
                getDialog().dismiss();
            }
        });
        return view;
    }

    private void saveItem(Calendar selectedTime){
        SaveDateListener activity = (SaveDateListener) getActivity();
        activity.didFinishDatePickerDialog(selectedTime);
        getDialog().dismiss();
    }
}
