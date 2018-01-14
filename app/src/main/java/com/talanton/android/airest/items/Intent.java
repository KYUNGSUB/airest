package com.talanton.android.airest.items;

/**
 * Created by User on 2017-08-19.
 */

public class Intent {
    private String intent;
    private String confidence;
    private String example;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
