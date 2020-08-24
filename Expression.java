package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	String[] expression = expr.replace(" ", "").split("");
    	String varname = "";
    	for (int i = 0; i < expression.length; i++) {
    		if (Character.isAlphabetic(expression[i].charAt(0)))
    			varname += expression[i];
    		else if (expression[i].charAt(0) == '[') {
    			arrays.add(new Array(varname));
    			varname = "";
    		}
    		else if (varname != ""){
    			Variable v = new Variable(varname);
    			vars.add(v);
    			varname = "";
    		}
    	}
    	if (varname != "") {
    		Variable v = new Variable(varname);
			vars.add(v);
    	}
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	expr = expr.replace(" ", "");
    	String[] expression = expr.split("");
    	String entity = "", operatorlist = "+-*/";
    	Stack<Float> operands = new Stack<Float>(), oprTmp = new Stack<Float>();
    	Stack<String> operators = new Stack<String>(), oprtrTmp = new Stack<String>();
    	
    	for (int i = 0; i < expression.length; i++) {
    		if (Character.isAlphabetic(expression[i].charAt(0))) { // ALPHABET
    			entity += expression[i];
    			if (vars.contains(new Variable(entity))) {
    				int val = vars.get(vars.indexOf(new Variable(entity))).value;
    				operands.push(Float.valueOf(val));
    				entity = "";
    			}
    		}
    		else if (operatorlist.indexOf(expression[i].charAt(0)) != -1) { // OPERATOR
    			if (entity != "") operands.push(Float.valueOf(entity));
    			entity = "";
    			operators.push(expression[i]);
    		}
    		else if (Character.isDigit(expression[i].charAt(0))) { // DIGIT
    			while (i < expression.length && Character.isDigit(expression[i].charAt(0))) {
    				entity += expression[i];
    				i++;
    			}
    			operands.push(Float.valueOf(entity));
    			entity = "";
    			i--;
    		}
    		else if (expression[i].charAt(0) == '(') { // BRACKET
    			int skip = 1, j = i + 1;
				for (; j < expr.length(); j++) {
					if (skip == 0) break;
					if (expr.charAt(j) == '(') skip++;
					if (expr.charAt(j) == ')') skip--;
				}
    			operands.push(evaluate(expr.substring(i + 1, j - 1), vars, arrays));
    			entity = "";
    			i = j - 1;
    		}
    		else if (expression[i].charAt(0) == '[') { // ARRAY
    			int entIndex = arrays.indexOf(new Array(entity));
    			if (entIndex != -1) {
    				int skip = 1, j = i + 1;
    				for (; j < expr.length(); j++) {
    					if (skip == 0) break;
    					if (expr.charAt(j) == '[') skip++;
    					if (expr.charAt(j) == ']') skip--;
    				}
    				int arrIndex = (int) (evaluate(expr.substring(i + 1, j - 1), vars, arrays)); 
    				operands.push(Float.valueOf(arrays.get(entIndex).values[arrIndex]));
    				entity = "";
    				i = j - 1;
    			}
    		}
    		// DIV/MUL
    		if (operands.size() > 1 && operators.peek().equals("/") && expr.charAt(i) != '/') {
    			operators.pop();
    			float right = operands.pop(), left = operands.pop(), temp = 0;
    			temp = (float) left / (float) right;
    			operands.push(temp);    			
    		}
    		else if (operands.size() > 1 && operators.peek().equals("*") && expr.charAt(i) != '*') {
    			operators.pop();
    			float right = operands.pop(), left = operands.pop(), temp = 0;
    			temp = (float) left * (float) right;
    			operands.push(temp);    			
    		}
    	}
    	
    	if (entity != "") { // RESIDUE
    		if (vars.contains(new Variable(entity))) {
				int val = vars.get(vars.indexOf(new Variable(entity))).value;
				operands.push(Float.valueOf(val));
				entity = "";
			}
    		else if (Character.isDigit(entity.charAt(0)))
    			operands.push(Float.valueOf(entity));
    	}
    	
    	while (operands.size() != 0) oprTmp.push(operands.pop());
    	while (operators.size() != 0) oprtrTmp.push(operators.pop());
    	
    	while (oprTmp.size() > 1) { // ADD/SUB
    		float right = oprTmp.pop(), left = oprTmp.pop(), temp = 0;
    		if (oprtrTmp.peek().equals("+")) {
    			oprtrTmp.pop();
    			temp = (float) right + (float) left;
    			oprTmp.push(temp);
    		}
    		else if (oprtrTmp.peek().equals("-")) {
    			oprtrTmp.pop();
    			temp = (float) right - (float) left;
    			oprTmp.push(temp);
    		}
    	}
    	
    	return oprTmp.pop();
    }
}
