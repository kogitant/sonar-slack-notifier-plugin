package com.astrebel.sonarslack.message;

import java.util.ArrayList;
import java.util.List;

public class SlackAttachment {
    private List<String> reasons = new ArrayList<>();


    public List<String> getReasons() {
        return reasons;
    }

    public void addReasons
            (List<String> reasons) {
        this.reasons.addAll(reasons);
    }

    public void addReason(String s){
        this.reasons.add(s);
    }

    public String formatAsText() {
        String s = "";
        for (String reason : reasons) {
            s+= "- " + reason + "\\n";
        }
        return s;
    }

    public String formatAsFallback() {
        String s = "Reasons: ";
        for (String reason : reasons) {
            s+= reason + ", ";
        }
        return s;
    }
}
