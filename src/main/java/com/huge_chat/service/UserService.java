package com.huge_chat.service;

import com.huge_chat.bean.Users;

public interface UserService {
    //判断用户名是否存在
    public boolean queryUserNameIsExist(String username);

    //查询用户是否存在
    public Users queryUserForLogin(String username,String pwd);
}
