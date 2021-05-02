package org.virginiaso.file_upload.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringUtil {
	public static final MathContext DURATION_ROUNDING = MathContext.UNLIMITED;
	private static final Logger LOG = LoggerFactory.getLogger(StringUtil.class);

	private StringUtil() {}	// prevent instantiation

	public static <E extends Enum<E>> E convertEnumerator(
			Class<E> enumClass, String enumStr) {
		Objects.requireNonNull(enumClass, "enumClass");
		try {
			return (enumStr == null || enumStr.isBlank())
				? null
				: E.valueOf(enumClass, enumStr.trim());
		} catch (IllegalArgumentException ex) {
			LOG.warn("Bad {} enum value '{}'", enumClass.getSimpleName(), enumStr);
			return null;
		}
	}

	public static int convertInteger(String integerStr) {
		try {
			return (integerStr == null || integerStr.isBlank())
				? -1
				: Integer.parseUnsignedInt(integerStr.trim());
		} catch (NumberFormatException ex) {
			LOG.warn("Bad integer value '{}'", integerStr);
			return -1;
		}
	}

	public static BigDecimal convertDecimal(String decimalStr) {
		try {
			return (decimalStr == null || decimalStr.isBlank())
				? null
				: new BigDecimal(decimalStr.trim(), DURATION_ROUNDING);
		} catch (NumberFormatException ex) {
			LOG.warn("Bad decimal value '{}'", decimalStr);
			return null;
		}
	}

	public static String safeTrim(String str) {
		return (str == null || str.isBlank())
			? null
			: str.trim();
	}
}
