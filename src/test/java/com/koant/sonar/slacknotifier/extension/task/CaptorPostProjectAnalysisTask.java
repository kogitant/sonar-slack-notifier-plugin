package com.koant.sonar.slacknotifier.extension.task;

import org.sonar.api.ce.posttask.PostProjectAnalysisTask;

class CaptorPostProjectAnalysisTask implements PostProjectAnalysisTask {
    private ProjectAnalysis projectAnalysis;

    @Override
    public void finished(ProjectAnalysis analysis) {
        this.projectAnalysis = analysis;
    }

    public ProjectAnalysis getProjectAnalysis() {
        return projectAnalysis;
    }
}
