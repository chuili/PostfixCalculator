package com.chuili.postfixcalculator.internal;

import android.content.Context;
import android.content.res.AssetManager;
import android.nfc.FormatException;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Stack;

public class Spreadsheet {
    private static final String TAG = Spreadsheet.class.getName();

    private Context context;
    private final String filename;
    private final Stack<Double> stack = new Stack<>();
    private String input[][] = null;
    private String output[][] = null;
    private int currRow = 0;
    private int currCol = 0;

    public Spreadsheet(String filename) {
        this.filename = filename;
    }

    public String[][] getInput() {
        if (input != null && input.length > 0) {
            String[][] result = new String [input.length][input[0].length];
            for (int i=0; i<input.length; i++) {
                for (int j=0; j<input[i].length; j++) {
                    result[i][j] = input[i][j];
                }
            }
            return result;
        } else {
            return null;
        }
    }

    public String[][] getOutput() {
        if (output != null && output.length > 0) {
            String[][] result = new String [output.length][output[0].length];
            for (int i=0; i<output.length; i++) {
                for (int j=0; j<output[i].length; j++) {
                    result[i][j] = output[i][j];
                }
            }
            return result;
        } else {
            return null;
        }
    }

    public void postfixCalculation(Context context)
            throws IOException, FormatException {
        if (context != null) {
            this.context = context;
        }

        parseSpreadsheet();
        calculate();
    }

    private void parseSpreadsheet() throws IOException, FormatException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(filename);

        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        int numOfSheet = workbook.getNumberOfSheets();
        if (numOfSheet == 0) {
            throw new FormatException("Workbook does not contain sheet");
        }

        HSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterRow = sheet.rowIterator();

        if (!iterRow.hasNext()) {
            throw new FormatException("Empty sheet");
        }

        Row row;
        Cell cell;
        String cellData;
        while (iterRow.hasNext()) {
            row = iterRow.next();
            Iterator<Cell> iterCell = row.cellIterator();
            currCol = 0;
            while (iterCell.hasNext()) {
                stack.empty();
                cell = iterCell.next();
                if(cell.getCellTypeEnum() == CellType.STRING){
                    cellData = cell.getStringCellValue();
                }
                else {
                    cellData = String.valueOf(cell.getNumericCellValue());
                }

                if (input == null) {
                    Log.d(TAG, "input is null");
                    int numOfRow = sheet.getPhysicalNumberOfRows();
                    int numOfCol = row.getPhysicalNumberOfCells();
                    input = new String[numOfRow][numOfCol];
                    output = new String[numOfRow][numOfCol];
                }

                input[currRow][currCol] = cellData;
                currCol++;
            }
            currRow++;
        }

        if (input != null && input.length > 0) {
            Log.d(TAG, "Number of rows: " + input.length);
            Log.d(TAG, "Number of columns: " + input[0].length);
            for (int i=0; i<input.length; i++) {
                for (int j=0; j<input[i].length; j++) {
                    Log.d(TAG, "input[" + i + "][" + j +"]: " + input[i][j]);
                    output[i][j] = input[i][j];
                }
            }
        }
    }

    private void calculate() throws FormatException {
        if (output != null) {
            for (int i=0; i<output.length; i++) {
                for (int j=0; j<output[i].length; j++) {
                    Log.d(TAG, "calculating: " + output[i][j]);
                    output[i][j] = String.format("%.5f", processCellData(output[i][j]));
                }
            }

            Log.d(TAG, "Number of rows: " + output.length);
            Log.d(TAG, "Number of columns: " + output[0].length);
            for (int i=0; i<output.length; i++) {
                for (int j=0; j<output[i].length; j++) {
                    Log.d(TAG, "Final Output[" + i + "][" + j +"]: " + output[i][j]);
                }
            }
        }
    }

    private int alphabetToArrayIndex(char alphabet) throws FormatException {
        Log.d(TAG, "alphabetToArrayIndex - start: " + alphabet);
        if ((int)alphabet >= 65 && (int)alphabet <= 90) {
            Log.d(TAG, "between 65 and 90");
            return (int)alphabet - 65;
        } else if ((int)alphabet >= 97 && (int)alphabet <= 122) {
            Log.d(TAG, "between 65 and 90");
            return (int)alphabet - 97;
        } else {
            Log.d(TAG, "Wrong table column format");
            throw new FormatException("Wrong table column format");
        }
    }

    /**
     * Process cell data. Split data by space and perform calculation.
     *
     * @param cellData
     * @return
     */
    private double processCellData(final String cellData) throws FormatException {
        Log.d(TAG, "processCellData() started");
        Log.d(TAG, "cellData: " + cellData);
        String[] data = cellData.split(" ");

        // return data if the cell consists only single numeric (int/double/negative value)
        if (data.length == 1
                && (data[0].matches("[0-9]+")
                || data[0].matches("[0-9]+.[0-9]+")
                || data[0].matches("\\-[0-9]+"))) {
            return Double.parseDouble(data[0]);
        }

        for (String a : data) {
            if (a.matches("[0-9]+")
                    || data[0].matches("[0-9]+.[0-9]+")
                    || a.matches("\\-[0-9]+")) {
                stack.push(Double.parseDouble(a));
            } else if (a.matches("[A-Z][0-9]+")) {
                int rowIndex = alphabetToArrayIndex(a.charAt(0));
                String outputData = output[rowIndex][Integer.parseInt(a.substring(1)) - 1];
                double out = processCellData(outputData);
                output[rowIndex][Integer.parseInt(a.substring(1)) - 1] = String.format("%.5f", out);
                stack.push(out);
            } else if (a.matches("[\\+\\-\\*\\/]")) {
                switch (a) {
                    case "+":
                        Log.d(TAG, "+");
                        stack.push(stack.pop() + stack.pop());
                        break;
                    case "-":
                        Log.d(TAG, "-");
                        double first = stack.pop();
                        double second = stack.pop();
                        stack.push(second - first);
                        break;
                    case "*":
                        stack.push(stack.pop() * stack.pop());
                        break;
                    case "/":
                        Log.d(TAG, "/");
                        double divisor = stack.pop();
                        double dividend = stack.pop();
                        stack.push(dividend / divisor);
                        break;
                    default:
                        throw new FormatException("Wrong operator");
                }
            } else {
                Log.d(TAG, "Wrong format: " + a);
                throw new FormatException("Wrong format");
            }
        }
        Log.d(TAG, "size: " + stack.size());
        return stack.pop();
    }
}