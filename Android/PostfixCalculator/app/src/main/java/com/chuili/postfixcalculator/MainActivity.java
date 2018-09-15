package com.chuili.postfixcalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.nfc.FormatException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.chuili.postfixcalculator.internal.Spreadsheet;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Spreadsheet spreadsheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        setContentView(R.layout.activity_main);
        spreadsheet = new Spreadsheet("Sample.xls");

        try {
            spreadsheet.postfixCalculation(context);
            displayDataInTable(spreadsheet.getInput(), spreadsheet.getOutput());
        } catch (IOException | FormatException e) {
            showAlertDialog(e.getMessage());
        }
    }

    private void displayDataInTable(String[][] input, String[][] output) {
        Log.d("chuili", "displayDataInTable - start");
        if (output != null) {
            TableLayout tableLayout = (TableLayout) findViewById(R.id.displayTable);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            int rowIndex = 0;

            // Table header
            TableRow header = new TableRow(context);
            header.setLayoutParams(layoutParams);
            TextView headerInput = new TextView(context);
            TextView headerOutput = new TextView(context);
            headerInput.setText("Input");
            headerOutput.setText("Output");
            header.addView(headerInput);
            header.addView(headerOutput);
            header.setBackgroundColor(Color.parseColor("#afafaf"));
            tableLayout.addView(header, rowIndex++);

            // 1st row
            Log.d("chuili", "displayDataInTable - first row");
            TableRow firstRow = new TableRow(context);
            firstRow.setLayoutParams(layoutParams);
            TextView firstRowInput = new TextView(context);
            TextView firstRowOutput = new TextView(context);
            String text = output[0].length + " " + output.length;
            firstRowInput.setText(text);
            firstRowOutput.setText(text);
            firstRowInput.setTextColor(0xAFAFAFAF);
            firstRowOutput.setTextColor(0xAFAFAFAF);
            firstRow.addView(firstRowInput);
            firstRow.addView(firstRowOutput);
            tableLayout.addView(firstRow, rowIndex++);

            for (int i=0; i<output.length; i++) {
                for (int j=0; j<output[i].length; j++) {
                    TableRow tableRow = new TableRow(context);
                    tableRow.setLayoutParams(layoutParams);
                    TextView inputColumn = new TextView(context);
                    TextView outputColumn = new TextView(context);
                    inputColumn.setText(input[i][j]);
                    outputColumn.setText(output[i][j]);
                    inputColumn.setTextColor(0xAFAFAFAF);
                    outputColumn.setTextColor(0xAFAFAFAF);
                    tableRow.addView(inputColumn);
                    tableRow.addView(outputColumn);
                    tableLayout.addView(tableRow, rowIndex++);
                }
            }
        }
    }

    private void alertError(String errorMessage) {
        TextView errorTextView = (TextView) findViewById(R.id.infoTextView);
        errorTextView.setText(errorMessage);
    }

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}