package com.koant.sonar.slacknotifier.extension.task;

import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class DecimalFormattingLearningTest {


    DecimalFormat percentageFormat = new DecimalFormat();
    
    @Before
    public void before(){
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        percentageFormat.setDecimalFormatSymbols(symbols);
    }

    @Test
    public void howIsStringWithDotAsDecimalSeparatorTreated(){
        Double d = Double.parseDouble("75.5000009");
        String actual = percentageFormat.format(d);
        String expected = "75.5";
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void howIsStringWithDotAsDecimalSeparatorTreated_case2(){
        Double d = Double.parseDouble("75.5100009");
        String actual = percentageFormat.format(d);
        String expected = "75.51";
        assertThat(actual).isEqualTo(expected);

    }


    @Test
    public void howIsStringWithDotAsDecimalSeparatorTreated_case3(){
        percentageFormat.setMaximumFractionDigits(2);
        Double d = Double.parseDouble("86.6666666");
        String actual = percentageFormat.format(d);
        String expected = "86.67";
        assertThat(actual).isEqualTo(expected);

    }



}
