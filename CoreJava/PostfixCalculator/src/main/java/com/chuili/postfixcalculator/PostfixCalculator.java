package com.chuili.postfixcalculator;

import java.util.Scanner;
import java.util.Stack;

import com.chuili.postfixcalculator.exception.FormatException;

public class PostfixCalculator {
	
	public static void main (String[] arg) {
		Scanner scanner = new Scanner(System.in);
		String first;
		String[] firstArr = null;
		int numOfColumn = 0;
		int numOfRow = 0;
		int spreadsheetSize = 0;
		String[][] input = null;
		Stack<Double> stack = new Stack<>();
		
        try {
            // Get spreadsheet size from user
            while (firstArr == null || firstArr.length != 2) {
                System.out.println("Enter number of column and number of row: ");
                first = scanner.nextLine();
                firstArr = first.split(" ");
                if (firstArr != null && firstArr.length == 2) {
                    numOfColumn = Integer.parseInt(firstArr[0]);
                    numOfRow = Integer.parseInt(firstArr[1]);
                    spreadsheetSize = numOfColumn * numOfRow;
                    input = new String [numOfRow][numOfColumn];
                }
            }
            
            // Get spreadsheet data from user
            if (spreadsheetSize > 0) {
                System.out.println("Enter spreadsheet data: ");
                for (int i=0; i<input.length; i++) {
                    for (int j=0; j<input[i].length; j++) {
                        input[i][j] = scanner.nextLine();
                    }
                }
                scanner.close();
                calculate(input, stack);
            }
        } catch (FormatException e) {
            System.out.println("FormatException: " + e.getMessage());
        }
		
	}
	
	/**
	 * Process cell data. Split data by space and perform calculation.
	 * @param cellData    String which is entered by user in every cell of spreadsheet
	 * @param output      Byte array of output data
	 * @param stack       Stack used to perform calculation
	 * @return            Result of the calculation in double
	 */
	private static double processCellData(final String cellData, final String[][] output, final Stack<Double> stack) throws FormatException {
//		System.out.println("processCellData() called: " + cellData);
		String [] data = cellData.split(" ");
		
		// return data if the cell consists only single numeric (int/double/negative value)
		if (data.length == 1 && (data[0].matches("[0-9]+") || data[0].matches("[0-9]+.[0-9]+") || data[0].matches("\\-[0-9]+"))) {
			return Double.parseDouble(data[0]);
		}
		
		for (String a : data) {
			if (a.matches("[0-9]+") || data[0].matches("[0-9]+.[0-9]+") || a.matches("\\-[0-9]+")) {
				// For integer data (int/double/negative value)
				stack.push(Double.parseDouble(a));
			} else if (a.matches("[A-Za-z][0-9]+")) {
				// For data referring to another cell
				int rowIndex = alphabetToArrayIndex(a.charAt(0));
				// Get the cell data which it's referring to
				String outputData = output[rowIndex][Integer.parseInt(a.substring(1)) - 1];
				double out = processCellData(outputData, output, stack);
				// Set the output data to the referred cell
				output[rowIndex][Integer.parseInt(a.substring(1)) - 1] = String.format("%.5f", out);
				stack.push(out);
			} else if (a.matches("[\\+\\-\\*\\/]")) {
				// For mathematics operator (+ - * /)
				switch (a) {
				case "+":
//					System.out.println("+");
					stack.push(stack.pop() + stack.pop());
					break;
				case "-":
//					System.out.println("-");
					double first = stack.pop();
					double second = stack.pop();
					stack.push(second - first);
					break;
				case "*":
//					System.out.println("*");
					stack.push(stack.pop() * stack.pop());
					break;
				case "/":
//					System.out.println("/");
					double divisor = stack.pop();
					double dividend = stack.pop();
					stack.push(dividend / divisor);
					break;
				default:
					throw new FormatException("Wrong operator");
				}
			} else {
				throw new FormatException("Wrong format");
			}
		}
//		System.out.println("Stack size: " + stack.size());
		return stack.pop();
	}
	
	/**
	 * Convert the alphabet (capital) to array index starts from 0
	 * @param alphabet
	 * @return
	 */
	private static int alphabetToArrayIndex(final char alphabet) throws FormatException {
		if ((int)alphabet >= 65 && (int)alphabet <= 90) {
			return (int)alphabet - 65;
		} else if ((int)alphabet >= 97 && (int)alphabet <= 122) {
            return (int)alphabet - 97;
        } else {
			System.out.println("Error in converting alphabet to array index. Please check input data.");
            throw new FormatException("Wrong table column format");
		}
	}
	
	/**
	 * Loop through the input array and pass the cell data to process / calculate.
	 * It prints the final output data.
	 * @param data
	 * @param stack
	 */
	private static void calculate(String[][] data, Stack<Double> stack) throws FormatException {
		if (data != null) {
			for (int i=0; i<data.length; i++) {
				for (int j=0; j<data[i].length; j++) {
//					System.out.println("Calculating: " + data[i][j]);
					
					// Empty the stack before a cell data is passed for calculation
					stack.empty();
					data[i][j] = String.format("%.5f", processCellData(data[i][j], data, stack));
				}
			}
			
			// Print output data
			System.out.println("\nFinal output: ");
			for (int i=0; i<data.length; i++) {
				for (int j=0; j<data[i].length; j++) {
					System.out.println(data[i][j]);
				}
			}
		}
	}
	
}
