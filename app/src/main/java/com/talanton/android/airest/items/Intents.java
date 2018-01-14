package com.talanton.android.airest.items;

import java.util.List;

/**
 * Created by User on 2017-08-19.
 */

public class Intents {
    private List<Intent> all;
    private Intent selected;
    private List<Intent> candidates;

    public List<Intent> getAll() {
        return all;
    }

    public void setAll(List<Intent> all) {
        this.all = all;
    }

    public Intent getSelected() {
        return selected;
    }

    public void setSelected(Intent selected) {
        this.selected = selected;
    }

    public List<Intent> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Intent> candidates) {
        this.candidates = candidates;
    }
}
