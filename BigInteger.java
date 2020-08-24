package bigint;

/**
 * This class encapsulates a BigInteger, i.e. a positive or negative integer with 
 * any number of digits, which overcomes the computer storage length limitation of 
 * an integer.
 * 
 */
public class BigInteger {

	/**
	 * True if this is a negative integer
	 */
	boolean negative;
	
	/**
	 * Number of digits in this integer
	 */
	int numDigits;
	
	/**
	 * Reference to the first node of this integer's linked list representation
	 * NOTE: The linked list stores the Least Significant Digit in the FIRST node.
	 * For instance, the integer 235 would be stored as:
	 *    5 --> 3  --> 2
	 *    
	 * Insignificant digits are not stored. So the integer 00235 will be stored as:
	 *    5 --> 3 --> 2  (No zeros after the last 2)        
	 */
	DigitNode front;
	
	/**
	 * Initializes this integer to a positive number with zero digits, in other
	 * words this is the 0 (zero) valued integer.
	 */
	public BigInteger() {
		negative = false;
		numDigits = 0;
		front = null;
	}
	
	/**
	 * Parses an input integer string into a corresponding BigInteger instance.
	 * A correctly formatted integer would have an optional sign as the first 
	 * character (no sign means positive), and at least one digit character
	 * (including zero). 
	 * Examples of correct format, with corresponding values
	 *      Format     Value
	 *       +0            0
	 *       -0            0
	 *       +123        123
	 *       1023       1023
	 *       0012         12  
	 *       0             0
	 *       -123       -123
	 *       -001         -1
	 *       +000          0
	 *       
	 * Leading and trailing spaces are ignored. So "  +123  " will still parse 
	 * correctly, as +123, after ignoring leading and trailing spaces in the input
	 * string.
	 * 
	 * Spaces between digits are not ignored. So "12  345" will not parse as
	 * an integer - the input is incorrectly formatted.
	 * 
	 * An integer with value 0 will correspond to a null (empty) list - see the BigInteger
	 * constructor
	 * 
	 * @param integer Integer string that is to be parsed
	 * @return BigInteger instance that stores the input integer.
	 * @throws IllegalArgumentException If input is incorrectly formatted
	 */
	public static BigInteger parse(String integer) 
	throws IllegalArgumentException {
		
		if (integer.length() < 1)
			throw new IllegalArgumentException();

		int length = 0, zeroTracker = 0;
		boolean negative = false, signed = false;
		DigitNode iterator = new DigitNode(0, null), front = iterator;
		
		for (int i = integer.length() - 1; i >= 0; i--) {
			char temp = integer.charAt(i);
			if (Character.isDigit(temp)) {
				if (temp != '0') {
					while (zeroTracker > 0) {
						DigitNode newNode = new DigitNode(0, null);
						iterator.next = newNode;
						iterator = newNode;
						--zeroTracker;
						length++;
					}
					DigitNode newNode = new DigitNode(Character.getNumericValue(temp), null);
					iterator.next = newNode;
					iterator = newNode;
					length++;
				}
				else
					zeroTracker++;
			}
			else if (temp == '+' || temp == '-') {
				if (signed)
					throw new IllegalArgumentException();
				else {
					negative = temp == '-';
					signed = true;
				}
			}
			else
				throw new IllegalArgumentException();
		}
		
		BigInteger num = new BigInteger();
		num.negative = negative;
		num.numDigits = length;
		num.front = front.next;
		
		return num;
		
	}
	
	/**
	 * Adds the first and second big integers, and returns the result in a NEW BigInteger object. 
	 * DOES NOT MODIFY the input big integers.
	 * 
	 * NOTE that either or both of the input big integers could be negative.
	 * (Which means this method can effectively subtract as well.)
	 * 
	 * @param first First big integer
	 * @param second Second big integer
	 * @return Result big integer
	 */
	public static BigInteger add(BigInteger first, BigInteger second) {

		DigitNode itrFirst = first.front, itrSecond = second.front;
		DigitNode  itrResult = new DigitNode(0, null), result = itrResult;
		int carry = 0, length = 0;
		
		/*
		 * Case where both are the same signs
		 */
		
		if (first.negative == second.negative) { 

			while (itrFirst != null && itrSecond != null) {
				int addition = itrFirst.digit + itrSecond.digit;
				DigitNode resultDigit = new DigitNode((addition % 10) + carry, null);
				carry = (addition) / 10;
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
				length++;
				itrFirst = itrFirst.next;
				itrSecond = itrSecond.next;
			}


			while (itrFirst != null) {
				DigitNode resultDigit = new DigitNode(itrFirst.digit + carry, null);
				carry = 0;
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
				length++;
				itrFirst = itrFirst.next;
			}

			while (itrSecond != null) {
				DigitNode resultDigit = new DigitNode(itrSecond.digit + carry, null);
				carry = 0;
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
				length++;
				itrSecond = itrSecond.next;
			}

			BigInteger answer = new BigInteger();
			answer.negative = first.negative;
			answer.numDigits = length;
			answer.front = result.next;
			return answer;
		
		}
		
		/* 
		 * Case where different signs
		 */
		
		boolean bigger = false;
		
		while (itrFirst != null && itrSecond != null) {
			if (itrFirst.digit < itrSecond.digit)
				bigger = true;
			itrFirst = itrFirst.next;
			itrSecond = itrSecond.next;
		}
		
		if (itrFirst != null)
			bigger = false;
		else if (itrSecond != null)
			bigger = true;
		
		if (bigger) {
			itrFirst = second.front;
			itrSecond = first.front;
		}
		else {
			itrFirst = first.front;
			itrSecond = second.front;
		}

		int zeroTracker = 0;
		
		while (itrFirst != null && itrSecond != null) {
			int topNum = itrFirst.digit;
			if (carry > 1) {
				topNum += 9;
				carry--;
			}
			else if (carry == 1) {
				topNum -= 1;
				carry = 0;
			}
			if (topNum - itrSecond.digit < 0) {
				DigitNode tempF = itrFirst.next;
				carry++;
				while (tempF.digit == 0 && tempF != null) {
					tempF = tempF.next;
					carry++;
				}
				topNum += 10;
			}
			if (topNum != 0) {
				if (topNum - itrSecond.digit == 0) {
					zeroTracker++;
					itrFirst = itrFirst.next;
					itrSecond = itrSecond.next;
				}
				else {

					while (zeroTracker > 0) {
						DigitNode resultDigit = new DigitNode(0, null);
						itrResult.next = resultDigit;
						itrResult = itrResult.next;
						length++;					
						zeroTracker--;
					}
				DigitNode resultDigit = new DigitNode(topNum - itrSecond.digit, null);
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
				itrFirst = itrFirst.next;
				itrSecond = itrSecond.next;
				length++;
				}
			}
			else {
				zeroTracker++;
				itrFirst = itrFirst.next;
				itrSecond = itrSecond.next;
			}
		}
		
		while (itrFirst != null) {
			int topNum = itrFirst.digit;
			if (carry > 1) {
				topNum += 9;
				carry--;
			}
			else if (carry == 1) {
				topNum -= 1;
				carry = 0;
			}
			if (topNum != 0) {
				while (zeroTracker > 0) {
					DigitNode resultDigit = new DigitNode(0, null);
					itrResult.next = resultDigit;
					itrResult = itrResult.next;
					length++;
					zeroTracker--;
				}
				DigitNode resultDigit = new DigitNode(topNum, null);
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
				itrFirst = itrFirst.next;
				length++;				
			}
			else {
				zeroTracker++;
				itrFirst = itrFirst.next;
			}
		}
		
		BigInteger answer = new BigInteger();
		answer.negative = bigger ? second.negative : first.negative;
		answer.front = result.next;
		answer.numDigits = length;
		return answer;
		
	}
	
	/**
	 * Returns the BigInteger obtained by multiplying the first big integer
	 * with the second big integer
	 * 
	 * This method DOES NOT MODIFY either of the input big integers
	 * 
	 * @param first First big integer
	 * @param second Second big integer
	 * @return A new BigInteger which is the product of the first and second big integers
	 */
	public static BigInteger multiply(BigInteger first, BigInteger second) {
		
		DigitNode itrFirst = first.front, itrSecond = second.front;
		DigitNode resultSum = null;
		
		boolean bigger = false;
		
		while (itrFirst != null && itrSecond != null) {
			if (itrFirst.digit < itrSecond.digit)
				bigger = true;
			itrFirst = itrFirst.next;
			itrSecond = itrSecond.next;
		}
		
		if (itrFirst != null)
			bigger = false;
		else if (itrSecond != null)
			bigger = true;
		
		if (bigger) {
			itrFirst = second.front;
			itrSecond = first.front;
		}
		else {
			itrFirst = first.front;
			itrSecond = second.front;
		}

		BigInteger sum = new BigInteger();
		int location = 0, zeroTracker = 0;
		
		while (itrSecond != null) {
			DigitNode tempTop = itrFirst;
			DigitNode itrResult = new DigitNode(0, null), result = itrResult;
			zeroTracker = location++;
			while (zeroTracker > 0) {
				DigitNode resultDigit = new DigitNode(0, null);
				itrResult.next = resultDigit;
				itrResult = itrResult.next;		
				zeroTracker--;
			}

			int carry = 0;
			while (tempTop != null) {
				int multiplication = tempTop.digit * itrSecond.digit;
				DigitNode resultDigit = new DigitNode((multiplication % 10) + carry, null);
				carry = (multiplication) / 10;
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
				tempTop = tempTop.next;
			}
			
			if (carry != 0) {
				DigitNode resultDigit = new DigitNode(carry, null);
				itrResult.next = resultDigit;
				itrResult = itrResult.next;
			}
			
			if (resultSum == null)
				resultSum = result.next;
			else {
				sum = new BigInteger();
				sum.front = resultSum;
				BigInteger toAdd = new BigInteger();
				toAdd.front = result.next;
				sum = BigInteger.add(sum, toAdd);
				resultSum = sum.front;
			}
			
			itrSecond = itrSecond.next;
		}
		
		BigInteger answer = new BigInteger();
		answer.negative = first.negative ^ second.negative;
		answer.numDigits = sum.numDigits;
		answer.front = resultSum;
		return answer;
		
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (front == null) {
			return "0";
		}
		String retval = front.digit + "";
		for (DigitNode curr = front.next; curr != null; curr = curr.next) {
				retval = curr.digit + retval;
		}
		
		if (negative) {
			retval = '-' + retval;
		}
		return retval;
	}
}
