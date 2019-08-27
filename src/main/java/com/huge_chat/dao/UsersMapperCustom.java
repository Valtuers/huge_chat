package com.huge_chat.dao;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.vo.FriendRequestVo;
import com.huge_chat.bean.vo.MyFriendsVo;
import com.huge_chat.utils.MyMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UsersMapperCustom extends MyMapper<Users> {

    List<FriendRequestVo> queryFriendRequestList(String acceptUserId);

    List<MyFriendsVo> queryMyFriends(String userId);

    void batchUpdateMsgSigned(List<String> msgIdList);
}