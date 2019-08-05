package com.huge_chat.service.impl;

import com.huge_chat.bean.Users;
import com.huge_chat.dao.UsersMapper;
import com.huge_chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UsersMapper usersMapper;

    @Override
    public boolean queryUserNameIsExist(String username) {
        Users users = new Users(){{
            setUsername(username);
        }};
        Users dbUsers = usersMapper.selectOne(users);

        return dbUsers != null;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserForLogin(String username, String pwd) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",pwd);
        return usersMapper.selectOneByExample(userExample);
    }
}
