package com.timothy.dottychat;

public class Messages {

    private String message, type, from;
    private Long time;
    private boolean seen;


    public Messages ( String message, boolean seen, String type, String from, Long time){

        this.message = message;
        this.from = from;
        this.seen = seen;
        this.type = type;
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public  Messages(){

    }
}
