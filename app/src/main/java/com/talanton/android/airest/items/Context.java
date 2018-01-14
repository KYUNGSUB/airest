package com.talanton.android.airest.items;

/**
 * Created by User on 2017-08-19.
 */

public class Context {
    private boolean random;
    private String input_field;
    private Information information;
    private int visit_counter;
    private boolean retrieve_field;
    private String conversation_id;
    private String variables;
    private boolean reprompt;
    private String keyboard;
    private String message;

    public Context() {
        information = new Information();
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public String getInput_field() {
        return input_field;
    }

    public void setInput_field(String input_field) {
        this.input_field = input_field;
    }

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information information) {
        this.information = information;
    }

    public int getVisit_counter() {
        return visit_counter;
    }

    public void setVisit_counter(int visit_counter) {
        this.visit_counter = visit_counter;
    }

    public boolean isRetrieve_field() {
        return retrieve_field;
    }

    public void setRetrieve_field(boolean retrieve_field) {
        this.retrieve_field = retrieve_field;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public boolean isReprompt() {
        return reprompt;
    }

    public void setReprompt(boolean reprompt) {
        this.reprompt = reprompt;
    }

    public String getKeyboard() {
        return keyboard;
    }

    public void setKeyboard(String keyboard) {
        this.keyboard = keyboard;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
