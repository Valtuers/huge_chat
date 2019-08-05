package com.huge_chat.controller;

import com.huge_chat.bean.Users;
import com.huge_chat.dao.UsersMapper;
import com.huge_chat.service.UserService;
import com.huge_chat.utils.HugeJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UsersMapper usersMapper;

    @RequestMapping("/test")
    public Users test(){
        return usersMapper.selectOne(new Users(){{
            setId("1");
        }});
    }

    @PostMapping("/registOrLogin")
    public HugeJSONResult registOrLogin(@RequestBody Users users){
        //1.用户名和密码不能为空
        if(StringUtils.isBlank(users.getUsername()) || StringUtils.isBlank(users.getPassword())){
            return HugeJSONResult.errorMsg("用户名或密码不能为空...");
        }
        //2.判断用户名是否存在，如果存在就登录，如果不存在就注册
        boolean userNameIsExist = userService.queryUserNameIsExist(users.getUsername());
        Users userResult = null;
        if(userNameIsExist){
            //2.1登录
            userResult = userService.queryUserForLogin(users.getUsername(),users.getPassword());
            if(userResult == null){
                return HugeJSONResult.errorMsg("用户名或密码不正确...");
            }
        }else{
            //2.2注册
        }

        return HugeJSONResult.ok();
    }

}
