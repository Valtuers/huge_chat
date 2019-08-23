package com.huge_chat.service;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.vo.FriendRequestVo;

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
}
