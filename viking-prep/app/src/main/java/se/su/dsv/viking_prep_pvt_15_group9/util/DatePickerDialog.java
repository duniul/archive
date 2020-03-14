package se.su.dsv.viking_prep_pvt_15_group9.util;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Det har fragmentet används för att ge användaren möjlighet att välja datum
 * från en kalender istället för att skriva in datum, t.ex. när man anger födelsedatum.
 *
 * @author Daniel
 */
public class DatePickerDialog extends DialogFragment implements android.app.DatePickerDialog.OnDateSetListener {

    final Calendar calendar = Calendar.getInstance();

    // Sträng för att representera det valda datumet i textform.
    String date = "";

    // View som används för att t.ex. skicka datumtext till ett textfält.
    EditText dateField;

    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    /**
     * Öppnar en DatePickerDialog för användaren och låter den välja datum.
     * @param savedInstanceState senast sparade state av fragmentet (är null om det nyss skapats).
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new android.app.DatePickerDialog(getActivity(), this, year, month, day);
    }

    /**
     * Öppnar en DatePickerDialog för användaren och låter den välja datum.
     * @param view den view som är associerad med lyssnaren.
     * @param pYear året användaren valde.
     * @param pMonth månaden användaren valde.
     * @param pDay dagen användaren valde.
     */
    public void onDateSet(DatePicker view, int pYear, int pMonth, int pDay) {
        pMonth++; // +1 eftersom onDateSet räknar månaderna 0-11.
        date = pYear + "-" + pMonth + "-" + pDay;

        year = pYear;
        month = pMonth - 1; // -1 eftersom det ska stämma för kalendern efteråt.
        day = pDay;

        dateField.setText(date);
        dateField.setError(null);
    }

    /**
     * Bestämmer vilket EditText-fält som DatePickern ska modifiera.
     * @param editText fältet som ska modifieras.
     */
    public void setDateField(EditText editText) {
        dateField = editText;
    }
}
