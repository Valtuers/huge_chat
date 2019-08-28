package com.huge_chat.service;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.vo.FriendRequestVo;
import com.huge_chat.bean.vo.MyFriendsVo;
import com.huge_chat.netty.ChatMsg;

import java.util.List;

public interface UserService {
    //判断用户名是否存在
    public boolean queryUserNameIsExist(String username);

    //查询用户是否存在
    public Users queryUserForLogin(String username,String pwd);

    //用户注册
    public Users saveUser(Users users) throws Exception;

    //修改用户信息
    public Users updateUserInfo(Users users);

    //搜索朋友的前置条件
    public Integer perconditionSearchFriends(String myUserId,String friendUsername);

    //根据用户名查询用户对象
    public Users queryUserInfoByUsername(String username);

    //添加好友请求记录
    public void sendFriendRequest(String myUserId, String friendUsername);

    //查询好友请求
    List<FriendRequestVo> queryFriendRequestList(String acceptUserId);

    //删除好友请求
    public void deleteFriendRequest(String sendUserId,String acceptUserId);

    //通过好友请求
    //1.保存好友
    //2.逆向保存好友
    //3.删除好友请求记录
    public void passFriendRequest(String sendUserId,String acceptUserId);

    //查询好友列表
    public List<MyFriendsVo> queryMyFriends(String userId);

    //保存聊天消息到数据库
    public String saveMsg(ChatMsg chatMsg);

    //批量签收消息
    public void updateMsgSigned(List<String> msgIdList);

    //获取未签收消息列表
    public List<com.huge_chat.bean.ChatMsg> getUnReadMsgList(String acceptUserId);
}
