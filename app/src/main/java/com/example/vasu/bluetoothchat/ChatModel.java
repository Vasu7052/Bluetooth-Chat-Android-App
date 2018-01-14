package com.example.vasu.bluetoothchat;

/**
 * Created by Vasu on 14-01-2018.
 */

public class ChatModel {

    public int type;
    public String message ;

    public ChatModel(){

    }

    public ChatModel(int type,String message){
        this.type = type ;
        this.message = message ;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
