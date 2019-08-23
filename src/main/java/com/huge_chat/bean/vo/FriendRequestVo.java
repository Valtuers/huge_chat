package com.huge_chat.bean.vo;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * 好友请求发送方的信息
 */
public class FriendRequestVo {
    private String sendUserId;
    private String sendUsername;
    private String sendNickname;
    private String sendFaceImage;

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUsername() {
        return sendUsername;
    }

    public void setSendUsername(String sendUsername) {
        this.sendUsername = sendUsername;
    }

    public String getSendNickname() {
        return sendNickname;
    }

    public void setSendNickname(String sendNickname) {
        this.sendNickname = sendNickname;
    }

    public String getSendFaceImage() {
        return sendFaceImage;
    }

    public void setSendFaceImage(String sendFaceImage) {
        this.sendFaceImage = sendFaceImage;
    }
}