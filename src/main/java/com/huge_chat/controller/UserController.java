package com.huge_chat.controller;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.bo.UsersBo;
import com.huge_chat.bean.vo.MyFriendsVo;
import com.huge_chat.bean.vo.UsersVo;
import com.huge_chat.enums.OperatorFriendRequestTypeEnum;
import com.huge_chat.enums.SearchFriendsStatusEnum;
import com.huge_chat.service.UserService;
import com.huge_chat.utils.FastDFSClient;
import com.huge_chat.utils.FileUtils;
import com.huge_chat.utils.HugeJSONResult;
import com.huge_chat.utils.MD5Utils;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
        String userFacePath = "/images/faceimg/"+usersBo.getUserid()+"userface64.png";
        if(FileUtils.base64ToFile(System.getProperty("user.dir")+"/src/main/resources/static"+userFacePath, base64Data)){

            Thumbnails.of(System.getProperty("user.dir")+"/src/main/resources/static"+userFacePath)
                    .size(80, 80)
                    .toFile(System.getProperty("user.dir")+"/src/main/resources/static/images/faceimg/"+usersBo.getUserid() + "userface64_80x80.png");

            //上传文件到fastdfs
            //MultipartFile faceFile = FileUtils.fileToMultipart(System.getProperty("user.dir")+"/src/main/resources/static"+userFacePath);
            //String url = fastDFSClient.uploadBase64(faceFile);
            System.out.println(userFacePath);
            //获取缩略图的url
            String thump = "_80x80.";
            String[] arr = userFacePath.split("\\.");
            String thumpImgUrl = arr[0]+thump+arr[1];
            //更新用户头像
            Users users = new Users();
            users.setId(usersBo.getUserid());
            users.setFaceImageBig(userFacePath);
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

    @PostMapping("/operFriendRequest")
    public HugeJSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType){
        //0.acceptUserId sendUserId判断不能为空
        if(StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null){
            return HugeJSONResult.errorMsg("");
        }
        //1.如果operType没有对应的枚举值，则直接抛出空错误信息
        if(StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return HugeJSONResult.errorMsg("");
        }
        if(operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)){
            //2.判断如果忽略好友请求，这直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        }else if(operType.equals(OperatorFriendRequestTypeEnum.PASS.type)){
            //3.判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        //4.数据库查询好友列表
        List<MyFriendsVo> myFriends = userService.queryMyFriends(acceptUserId);
        return HugeJSONResult.ok(myFriends);
    }

    /**
     * 查询我的好友列表
     */
    @PostMapping("/myFriends")
    public HugeJSONResult myFriends(String userId){
        if(StringUtils.isBlank(userId)){
            return HugeJSONResult.errorMsg("");
        }
        //1.数据库查询好友列表
        List<MyFriendsVo> myFriends = userService.queryMyFriends(userId);
        return HugeJSONResult.ok(myFriends);
    }
}
