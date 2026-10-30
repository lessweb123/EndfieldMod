package endfield.util;

import arc.Core;
import arc.math.Mathf;
import arc.util.Strings;

import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Strings2 {
	static final String[] byteUnit = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "BB"};

	static final String printableString = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	static final char[] printableChars = {
			' ', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			':', ';', '<', '=', '>', '?', '@',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'[', '\\', ']', '^', '_', '`',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'{', '|', '}', '~'
	};

	public static final Pattern integerNumeric = Pattern.compile("^[+-]?\\d+$");
	public static final Pattern floatNumeric = Pattern.compile("^[+-]?(\\d+\\.\\d*|\\.\\d+|\\d+)([eE][+-]?\\d+)?$");

	private Strings2() {}

	/**
	 * The {@code char} version of {@code String.repeat(int)}.
	 *
	 * @throws IllegalArgumentException if the {@code count} is negative.
	 */
	public static String repeat(char key, int count) {
		if (count < 0) throw new IllegalArgumentException("Count must be non-negative, but was " + count + ".");
		if (count == 0) return "";

		char[] data = new char[count];
		Arrays.fill(data, key);

		return String.valueOf(data);
	}

	public static String repeat(String key, int count) {
		if (count < 0) throw new IllegalArgumentException("Count must be non-negative, but was " + count + ".");
		if (count == 0) return "";

		StringBuilder buf = new StringBuilder(key.length() * count);
		for (int i = 0; i < count; i++) buf.append(key);

		return buf.toString();
	}

	public static String substringAfter(String str, char delimiter) {
		int index = str.indexOf(delimiter);
		return index == -1 ? str : str.substring(index + 1);
	}

	public static String substringAfter(String str, String delimiter) {
		int index = str.indexOf(delimiter);
		return index == -1 ? str : str.substring(index + delimiter.length());
	}

	public static String substringAfterLast(String str, char delimiter) {
		int index = str.lastIndexOf(delimiter);
		return index == -1 ? str : str.substring(index + 1);
	}

	public static String substringAfterLast(String str, String delimiter) {
		int index = str.lastIndexOf(delimiter);
		return index == -1 ? str : str.substring(index + delimiter.length());
	}

	public static String substringBeforeLast(String str, char delimiter) {
		int index = str.lastIndexOf(delimiter);
		return index == -1 ? str : str.substring(0, index);
	}

	public static String substringBeforeLast(String str, String delimiter) {
		int index = str.lastIndexOf(delimiter);
		return index == -1 ? str : str.substring(0, index);
	}

	public static MatchResult matchAt(Pattern pattern, String input, int index) {
		Matcher matcher = pattern.matcher(input).useAnchoringBounds(false).useTransparentBounds(true).region(index, input.length());
		return matcher.lookingAt() ? matcher.toMatchResult() : null;
	}

	public static int sumOf(CharSequence cs, Selector selector) {
		var sum = 0;
		for (int i = 0; i < cs.length(); i++) {
			sum += selector.get(cs.charAt(i));
		}
		return sum;
	}

	/** Determine whether the string is composed entirely of numbers. */
	public static boolean isNumeric4(String key) {
		return key != null && integerNumeric.matcher(key).matches();
	}

	public static boolean isNumeric(String key) {
		return key != null && floatNumeric.matcher(key).matches();
	}

	/** Randomly generate a string of length within the specified range. */
	public static String randomString(int min, int max) {
		if (min < 0 || max < min || max > 1000000) return Core.bundle.format("text.generate-random-string-2", min, max);

		int length = min + Mathf.random(max - min + 1);
		return randomString(length);
	}

	/**
	 * Randomly generate a string of specified length.
	 *
	 * @throws NegativeArraySizeException If the {@code length} is negative.
	 */
	public static String randomString(int length) {
		return randomString(printableChars, length);
	}

	public static String randomString(CharSequence keys, int length) {
		char[] result = new char[length];
		int range = keys.length() - 1;

		for (int i = 0; i < length; i++) {
			result[i] = keys.charAt(Mathf.random(range));
		}
		return String.valueOf(result);
	}

	/**
	 * @param keys chars
	 */
	public static String randomString(char[] keys, int length) {
		char[] result = new char[length];
		int range = keys.length - 1;

		for (int i = 0; i < length; i++) {
			result[i] = keys[Mathf.random(range)];
		}
		return String.valueOf(result);
	}

	/**
	 * Convert numbers to computer storage capacity count representation without units.
	 *
	 * @param number The number to be converted
	 * @param retain Reserved decimal places
	 */
	public static String toByteFixNonUnit(double number, int retain) {
		boolean isNegative = false;
		if (number < 0) {
			number = -number;
			isNegative = true;
		}

		double base = 1d;
		for (int i = 0; i < byteUnit.length; i++) {
			if (base * 1024 > number) {
				break;
			}
			base *= 1024;
		}

		String str = Double.toString(number / base);
		int dotIndex = str.indexOf('.');

		String integerPart = str.substring(0, dotIndex);
		String fractionalPart = str.substring(dotIndex + 1);

		int realRetain = Math.min(retain, fractionalPart.length());

		StringBuilder builder = new StringBuilder();
		if (isNegative) {
			builder.append('-');
		}
		builder.append(integerPart);
		if (retain != 0) {
			builder.append('.').append(fractionalPart, 0, realRetain);
			for (int i = 1; i < Math.max(0, retain - realRetain); i++) {
				builder.append('0');
			}
		}
		return builder.toString();
	}

	/**
	 * Convert numbers to computer storage capacity count representation.
	 *
	 * @param number The number to be converted
	 * @param retain Reserved decimal places
	 */
	public static String toByteFix(double number, int retain) {
		boolean isNegative = false;
		if (number < 0) {
			number = -number;
			isNegative = true;
		}

		int index = 0;
		double base = 1d;
		for (int i = 0; i < byteUnit.length; i++) {
			if (base * 1024 > number) {
				break;
			}
			base *= 1024;
			index++;
		}

		String str = Double.toString(number / base);
		int dotIndex = str.indexOf('.');

		String integerPart = str.substring(0, dotIndex);
		String fractionalPart = str.substring(dotIndex + 1);

		int realRetain = Math.min(retain, fractionalPart.length());

		StringBuilder builder = new StringBuilder();
		if (isNegative) {
			builder.append('-');
		}
		builder.append(integerPart);
		if (retain != 0) {
			builder.append('.');
			builder.append(fractionalPart, 0, realRetain);
			for (int i = 0; i < Math.max(0, retain - realRetain); i++) {
				builder.append('0');
			}
		}
		builder.append(byteUnit[index]);
		return builder.toString();
	}

	public static String toStoreSize(float num) {
		float v = num;
		int n = 0;

		while (v > 1024) {
			v /= 1024;
			n++;
		}

		return Strings.fixedBuilder(v, 2).append("[lightgray]").append(byteUnit[n]).toString();
	}

	public interface Selector {
		int get(char c);
	}
}
