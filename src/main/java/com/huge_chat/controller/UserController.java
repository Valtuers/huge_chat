package com.huge_chat.controller;

import com.huge_chat.bean.Users;
import com.huge_chat.bean.vo.UsersVo;
import com.huge_chat.service.UserService;
import com.huge_chat.utils.HugeJSONResult;
import com.huge_chat.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

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

}
