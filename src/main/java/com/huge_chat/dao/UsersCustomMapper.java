package com.huge_chat.dao;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.vo.FriendRequestVo;
import com.huge_chat.utils.MyMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UsersCustomMapper extends MyMapper<Users> {

    List<FriendRequestVo> queryFriendRequestList(String acceptUserId);
}