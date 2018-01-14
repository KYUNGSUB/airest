package com.talanton.android.airest.items;

import java.util.List;

/**
 * Created by User on 2017-08-19.
 */

public class AiResult {
    private List<Entity> entities;
    private String story_id;
    private Output output;
    private String _id;
    private String timeutc;
    private Intents intents;
    private Channel channel;
    private Input input;
    private Context context;
    private String time;

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public String getStory_id() {
        return story_id;
    }

    public void setStory_id(String story_id) {
        this.story_id = story_id;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTimeutc() {
        return timeutc;
    }

    public void setTimeutc(String timeutc) {
        this.timeutc = timeutc;
    }

    public Intents getIntents() {
        return intents;
    }

    public void setIntents(Intents intents) {
        this.intents = intents;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
