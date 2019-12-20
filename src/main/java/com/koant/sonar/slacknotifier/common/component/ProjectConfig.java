package com.koant.sonar.slacknotifier.common.component;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Created by ak on 17/10/16.
 * Modified by poznachowski
 */
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class ProjectConfig {

    private final String projectHook;
    private final String projectKey;
    private final String slackChannel;
    private final String notify;
    private final boolean qgFailOnly;
    
}
