package com.huge_chat.netty;

import java.io.Serializable;

public class DataContent implements Serializable {

    private static final long serialVersionUID = 7772397532967967862L;

    private Integer action;     //动作类型
    private ChatMsg chatMsg;    //用户的聊天内容bean
    private String extend;      //扩展字段

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
