package com.huge_chat.service.impl;

import com.huge_chat.bean.FriendsRequest;
import com.huge_chat.bean.MyFriends;
import com.huge_chat.bean.Users;
import com.huge_chat.bean.vo.FriendRequestVo;
import com.huge_chat.bean.vo.MyFriendsVo;
import com.huge_chat.dao.*;
import com.huge_chat.enums.MsgSignFlagEnum;
import com.huge_chat.enums.SearchFriendsStatusEnum;
import com.huge_chat.netty.ChatMsg;
import com.huge_chat.service.UserService;
import com.huge_chat.utils.FastDFSClient;
import com.huge_chat.utils.FileUtils;
import com.huge_chat.utils.MD5Utils;
import com.huge_chat.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class UserServiceImpl implements UserService {

    @Autowired
    UsersMapper usersMapper;

    @Autowired
    UsersMapperCustom usersMapperCustom;

    @Autowired
    MyFriendsMapper myFriendsMapper;

    @Autowired
    FriendsRequestMapper friendsRequestMapper;

    @Autowired
    ChatMsgMapper chatMsgMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    FastDFSClient fastDFSClient;

    @Override
    public boolean queryUserNameIsExist(String username) {
        Users users = new Users(){{
            setUsername(username);
        }};
        Users dbUsers = usersMapper.selectOne(users);

        return dbUsers != null;
    }

    @Override
    public Users queryUserForLogin(String username, String pwd) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",pwd);
        return usersMapper.selectOneByExample(userExample);
    }

    @Override
    public Users saveUser(Users users) throws Exception {
        String userId = sid.nextShort();
        users.setNickname(users.getUsername());
        users.setFaceImage("");
        users.setFaceImageBig("");
        users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
        //TODO 为每个用户生成一个唯一的二维码
        System.out.println(System.getProperty("user.dir"));
        String qrCodePath = "/images/qrcode/user"+userId+"qrcode.png";
        qrCodeUtils.createQRCode(System.getProperty("user.dir")+"/src/main/resources/static"+qrCodePath, "huge_qrcode:"+ users.getUsername());
//        MultipartFile qrFile = FileUtils.fileToMultipart(System.getProperty("user.dir")+"/src/main/resources/static"+qrCodePath);
//        String qrUrl = fastDFSClient.uploadQRCode(qrFile);
        users.setQrcode(qrCodePath);

        users.setId(userId);
        usersMapper.insert(users);
        return users;
    }

    @Override
    public Users updateUserInfo(Users users) {
        usersMapper.updateByPrimaryKeySelective(users);
        return queryUserById(users.getId());
    }

    @Override
    public Integer perconditionSearchFriends(String myUserId, String friendUsername) {
        //1.搜索的用户如果不存在，返回[无此用户]
        Users users = queryUserInfoByUsername(friendUsername);
        if(users == null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //2.搜索账户是你自己，返回[不能添加自己]
        if(users.getId().equals(myUserId)){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //3.搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Example mfe = new Example(MyFriends.class);
        Example.Criteria mfc = mfe.createCriteria();
        mfc.andEqualTo("myUserId",myUserId);
        mfc.andEqualTo("myFriendUserId",users.getId());
        MyFriends mfsRel = myFriendsMapper.selectOneByExample(mfe);
        if(mfsRel != null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public Users queryUserInfoByUsername(String username){
        Example ue = new Example(Users.class);
        Example.Criteria criteria = ue.createCriteria();
        criteria.andEqualTo("username",username);
        return usersMapper.selectOneByExample(ue);
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {
        //根据用户名把朋友信息查询出来
        Users friend = queryUserInfoByUsername(friendUsername);

        //1.查询发送好友请求记录表
        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId",myUserId);
        frc.andEqualTo("acceptUserId",friend.getId());
        FriendsRequest request = friendsRequestMapper.selectOneByExample(fre);
        if(request == null){
            //2.如果不是你的好友，并且好友记录没有添加，则新增记录
            friendsRequestMapper.insert(new FriendsRequest(){{
                setId(sid.nextShort());
                setSendUserId(myUserId);
                setAcceptUserId(friend.getId());
                setRequestDateTime(new Date());
            }});
        }
    }

    @Override
    public List<FriendRequestVo> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(fre);
    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);
        deleteFriendRequest(sendUserId,acceptUserId);
    }

    @Override
    public List<MyFriendsVo> queryMyFriends(String userId) {
        return usersMapperCustom.queryMyFriends(userId);
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.huge_chat.bean.ChatMsg msgDB = new com.huge_chat.bean.ChatMsg(){{
            setId(sid.nextShort());
            setSendUserId(chatMsg.getSenderId());
            setAcceptUserId(chatMsg.getReceiverId());
            setMsg(chatMsg.getMsg());
            setCreateTime(new Date());
            setSignFlag(MsgSignFlagEnum.unsign.type);
        }};
        chatMsgMapper.insert(msgDB);
        return msgDB.getId();
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    private Users queryUserById(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }

    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends(){{
            setId(sid.nextShort());
            setMyFriendUserId(acceptUserId);
            setMyUserId(sendUserId);
        }};
        myFriendsMapper.insert(myFriends);
    }
}
