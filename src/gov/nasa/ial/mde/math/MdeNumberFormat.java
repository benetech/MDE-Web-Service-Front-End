/* 
 * Copyright 2006, United States Government as represented by the Administrator
 * for the National Aeronautics and Space Administration. No copyright is
 * claimed in the United States under Title 17, U.S. Code. All Other Rights
 * Reserved. 
 * 
 * Created on Mar 22, 2004
 */
package gov.nasa.ial.mde.math;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

/**
 * The MDE Number Format.
 *
 * @author Dr. Robert Shelton
 * @version 1.0
 * @since 1.0
 */
public class MdeNumberFormat {

    private NumberFormat nf;

    /**
     * Default Constructor.
     */
    public MdeNumberFormat() {
        nf = null;
    } // end MdeNumberFormat

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getAvailableLocales()
     */
    public static Locale[] getAvailableLocales() {
        return NumberFormat.getAvailableLocales();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getCurrencyInstance(java.util.Locale)
     */
    public static MdeNumberFormat getCurrencyInstance(Locale inLocale) {
        MdeNumberFormat r = new MdeNumberFormat();
        r.nf = NumberFormat.getCurrencyInstance(inLocale);
        return r;
    }

    /**
     * Returns an instance of the MDE number format class.
     * 
     * @return an instance of the MDE number format class.
     */
    public static MdeNumberFormat getInstance() {
        MdeNumberFormat r = new MdeNumberFormat();
        r.nf = NumberFormat.getInstance();
        return r;
    } // end getInstance

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getInstance(java.util.Locale)
     */
    public static MdeNumberFormat getInstance(Locale inLocale) {
        MdeNumberFormat r = new MdeNumberFormat();
        r.nf = NumberFormat.getInstance(inLocale);
        return r;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getIntegerInstance(java.util.Locale)
     */
    public static MdeNumberFormat getIntegerInstance(Locale inLocale) {
        MdeNumberFormat r = new MdeNumberFormat();
        r.nf = NumberFormat.getIntegerInstance(inLocale);
        return r;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getNumberInstance(java.util.Locale)
     */
    public static MdeNumberFormat getNumberInstance(Locale inLocale) {
        MdeNumberFormat r = new MdeNumberFormat();
        r.nf = NumberFormat.getNumberInstance(inLocale);
        return r;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getPercentInstance(java.util.Locale)
     */
    public static MdeNumberFormat getPercentInstance(Locale inLocale) {
        MdeNumberFormat r = new MdeNumberFormat();
        r.nf = NumberFormat.getPercentInstance(inLocale);
        return r;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return nf.equals(obj);
    }

    /**
     * Formats the specified floating point value.
     * 
     * @param number the floating point value to format.
     * @return the formated floating point value.
     */
    public String format(double number) {
        if (Double.isInfinite(number)) {
            return (number > 0.0) ? "infinity" : "-infinity";
        }
        if (number == 0.0) {
            return "0";
        }

        String str = nf.format(number);
        if ("-0".equals(str)) {
            return "0";
        }

        return str;
    } // end format

    /**
     * Formats the specified floating point value.
     * 
     * @param number the floating point number to format.
     * @param toAppendTo the StringBuffer to append the formated number to.
     * @param pos the FieldPosition for the number format.
     */
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        if (Math.abs(number) == Double.POSITIVE_INFINITY) {
            return new StringBuffer(new Double(number).toString());
        }
        if (number == 0.0) {
            return new StringBuffer("0");
        }
        StringBuffer strBuff = nf.format(number, toAppendTo, pos);
        if ("-0".equals(strBuff.toString())) {
            return new StringBuffer("0");
        }
        return strBuff;
    }

    /**
     * Formats the specified fixed point value.
     * 
     * @param number the fixed point number to format.
     * @param toAppendTo the StringBuffer to append the formated number to.
     * @param pos the FieldPosition for the number format.
     */
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        return nf.format(number, toAppendTo, pos);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.Format#formatToCharacterIterator(java.lang.Object)
     */
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        return nf.formatToCharacterIterator(obj);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getCurrency()
     */
    public Currency getCurrency() {
        return nf.getCurrency();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getMaximumFractionDigits()
     */
    public int getMaximumFractionDigits() {
        return nf.getMaximumFractionDigits();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getMaximumIntegerDigits()
     */
    public int getMaximumIntegerDigits() {
        return nf.getMaximumIntegerDigits();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getMinimumFractionDigits()
     */
    public int getMinimumFractionDigits() {
        return nf.getMinimumFractionDigits();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#getMinimumIntegerDigits()
     */
    public int getMinimumIntegerDigits() {
        return nf.getMinimumIntegerDigits();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#hashCode()
     */
    public int hashCode() {
        return nf.hashCode();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#isGroupingUsed()
     */
    public boolean isGroupingUsed() {
        return nf.isGroupingUsed();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#isParseIntegerOnly()
     */
    public boolean isParseIntegerOnly() {
        return nf.isParseIntegerOnly();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#parse(java.lang.String)
     */
    public Number parse(String source) throws ParseException {
        return nf.parse(source);
    }

    /**
     * Returns the Number given the source string and parse position.
     * 
     * @param source the source string.
     * @param parsePosition the parse position.
     * @return the Number.
     */
    public Number parse(String source, ParsePosition parsePosition) {
        return nf.parse(source, parsePosition);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.Format#parseObject(java.lang.String)
     */
    public Object parseObject(String source) throws ParseException {
        return nf.parseObject(source);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setCurrency(java.util.Currency)
     */
    public void setCurrency(Currency currency) {
        nf.setCurrency(currency);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setGroupingUsed(boolean)
     */
    public void setGroupingUsed(boolean newValue) {
        nf.setGroupingUsed(newValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setMaximumFractionDigits(int)
     */
    public void setMaximumFractionDigits(int newValue) {
        nf.setMaximumFractionDigits(newValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setMaximumIntegerDigits(int)
     */
    public void setMaximumIntegerDigits(int newValue) {
        nf.setMaximumIntegerDigits(newValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setMinimumFractionDigits(int)
     */
    public void setMinimumFractionDigits(int newValue) {
        nf.setMinimumFractionDigits(newValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setMinimumIntegerDigits(int)
     */
    public void setMinimumIntegerDigits(int newValue) {
        nf.setMinimumIntegerDigits(newValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.text.NumberFormat#setParseIntegerOnly(boolean)
     */
    public void setParseIntegerOnly(boolean value) {
        nf.setParseIntegerOnly(value);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return nf.toString();
    }

}