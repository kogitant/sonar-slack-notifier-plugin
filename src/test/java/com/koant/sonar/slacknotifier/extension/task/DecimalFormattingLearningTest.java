package com.koant.sonar.slacknotifier.extension.task;

import org.junit.Test;
import org.sonar.api.internal.apachecommons.lang.math.NumberUtils;

import java.text.DecimalFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class DecimalFormattingLearningTest {


    @Test
    public void howIsStringWithDotAsDecimalSeparatorTreated(){
        DecimalFormat percentageFormat = new DecimalFormat();

        Double d = NumberUtils.createDouble("75.5000009");
        String actual = percentageFormat.format(d);
        String expected = "75.5";
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void howIsStringWithDotAsDecimalSeparatorTreated_case2(){
        DecimalFormat percentageFormat = new DecimalFormat();

        Double d = NumberUtils.createDouble("75.5100009");
        String actual = percentageFormat.format(d);
        String expected = "75.51";
        assertThat(actual).isEqualTo(expected);

    }



}
