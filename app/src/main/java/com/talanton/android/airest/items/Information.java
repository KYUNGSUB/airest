package com.talanton.android.airest.items;

import java.util.List;

/**
 * Created by User on 2017-08-19.
 */

public class Information {
    private int conversation_counter;
    private int user_request_counter;
    private List<Conversation> conversation_stack;

    public int getConversation_counter() {
        return conversation_counter;
    }

    public void setConversation_counter(int conversation_counter) {
        this.conversation_counter = conversation_counter;
    }

    public int getUser_request_counter() {
        return user_request_counter;
    }

    public void setUser_request_counter(int user_request_counter) {
        this.user_request_counter = user_request_counter;
    }

    public List<Conversation> getConversation_stack() {
        return conversation_stack;
    }

    public void setConversation_stack(List<Conversation> conversation_stack) {
        this.conversation_stack = conversation_stack;
    }
}
