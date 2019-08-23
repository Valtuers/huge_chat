package com.huge_chat.controller;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.bo.UsersBo;
import com.huge_chat.bean.vo.UsersVo;
import com.huge_chat.enums.SearchFriendsStatusEnum;
import com.huge_chat.service.UserService;
import com.huge_chat.utils.FastDFSClient;
import com.huge_chat.utils.FileUtils;
import com.huge_chat.utils.HugeJSONResult;
import com.huge_chat.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    FastDFSClient fastDFSClient;

    @RequestMapping("/t")
    public String test(){
        return "哈哈哈";
    }

    @PostMapping("/registOrLogin")
    public HugeJSONResult registOrLogin(@RequestBody Users users) throws Exception {
        //1.用户名和密码不能为空
        if(StringUtils.isBlank(users.getUsername()) || StringUtils.isBlank(users.getPassword())){
            return HugeJSONResult.errorMsg("用户名或密码不能为空...");
        }
        //2.判断用户名是否存在，如果存在就登录，如果不存在就注册
        boolean userNameIsExist = userService.queryUserNameIsExist(users.getUsername());
        Users userResult = null;
        if(userNameIsExist){
            //2.1登录
            userResult = userService.queryUserForLogin(users.getUsername(),MD5Utils.getMD5Str(users.getPassword()));
            if(userResult == null){
                return HugeJSONResult.errorMsg("用户名或密码不正确...");
            }
        }else{
            //2.2注册
            userResult = userService.saveUser(users);
        }
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(userResult,usersVo);
        return HugeJSONResult.ok(usersVo);
    }

    @PostMapping("/uploadFaceBase64")
    public HugeJSONResult uploadFaceBase64(@RequestBody UsersBo usersBo) throws Exception{
        //获取前端传过来的base64字符串，然后转换为文件对象再上传
        String base64Data = usersBo.getFaceData();
        String userFacePath = "D:\\"+usersBo.getUserid()+"userface64.png";
        if(FileUtils.base64ToFile(userFacePath, base64Data)){
            //上传文件到fastdfs
            MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
            String url = fastDFSClient.uploadBase64(faceFile);
            System.out.println(url);
            //获取缩略图的url
            String thump = "_80x80.";
            String[] arr = url.split("\\.");
            String thumpImgUrl = arr[0]+thump+arr[1];
            //更新用户头像
            Users users = new Users();
            users.setId(usersBo.getUserid());
            users.setFaceImageBig(url);
            users.setFaceImage(thumpImgUrl);
            return HugeJSONResult.ok(userService.updateUserInfo(users));
        }
        return HugeJSONResult.errorMsg("头像上传失败！！！");
    }

    /**
     * 设置用户昵称
     * @param usersBo
     * @return
     * @throws Exception
     */
    @PostMapping("/setNickName")
    public HugeJSONResult setNickName(@RequestBody UsersBo usersBo) throws Exception{
        //更新用户昵称
        Users users = new Users();
        users.setId(usersBo.getUserid());
        users.setNickname(usersBo.getNickname());
        return HugeJSONResult.ok(userService.updateUserInfo(users));
    }

    /**
     * 搜索好友接口，根据账号做匹配查询而不是模糊查询
     */
    @GetMapping("/search")
    public HugeJSONResult searchUser(String myUserId,String friendUsername){
        //0.判断myUserid,friendUsername不能为空
        if(StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return HugeJSONResult.errorMsg("");
        }
        //前置条件 - 1.搜索的用户如果不存在，返回[无此用户]
        //前置条件 - 2.搜索账户是你自己，返回[不能添加自己]
        //前置条件 - 3.搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.perconditionSearchFriends(myUserId, friendUsername);
        if(status.equals(SearchFriendsStatusEnum.SUCCESS.status)){
            Users userFriend = userService.queryUserInfoByUsername(friendUsername);
            UsersVo vo = new UsersVo();
            BeanUtils.copyProperties(userFriend, vo);
            return HugeJSONResult.ok(vo);
        }else{
            return HugeJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        }
    }

    @GetMapping("/addFriendRequest")
    public HugeJSONResult addFriendRequest(String myUserId,String friendUsername){
//0.判断myUserid,friendUsername不能为空
        if(StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return HugeJSONResult.errorMsg("");
        }
        //前置条件 - 1.搜索的用户如果不存在，返回[无此用户]
        //前置条件 - 2.搜索账户是你自己，返回[不能添加自己]
        //前置条件 - 3.搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.perconditionSearchFriends(myUserId, friendUsername);
        if(status.equals(SearchFriendsStatusEnum.SUCCESS.status)){
            userService.sendFriendRequest(myUserId,friendUsername);
        }else{
            return HugeJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        }
        return HugeJSONResult.ok();
    }

    @PostMapping("/queryFriendRequest")
    public HugeJSONResult queryFriendRequest(String userId){
        if(userId == null){
            return HugeJSONResult.errorMsg("");
        }
        //1.查询用户接收到的朋友申请
        return HugeJSONResult.ok(userService.queryFriendRequestList(userId));
    }
}
