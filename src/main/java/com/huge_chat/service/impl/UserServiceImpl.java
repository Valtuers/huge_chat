package com.huge_chat.service.impl;

import com.huge_chat.bean.Users;
import com.huge_chat.dao.UsersMapper;
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

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class UserServiceImpl implements UserService {

    @Autowired
    UsersMapper usersMapper;

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
        String qrCodePath = "D://user"+userId+"qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, "huge_qrcode:"+ users.getUsername());
        MultipartFile qrFile = FileUtils.fileToMultipart(qrCodePath);
        String qrUrl = fastDFSClient.uploadQRCode(qrFile);
        users.setQrcode(qrUrl);

        users.setId(userId);
        usersMapper.insert(users);
        return users;
    }

    @Override
    public Users updateUserInfo(Users users) {
        usersMapper.updateByPrimaryKeySelective(users);
        return queryUserById(users.getId());
    }

    private Users queryUserById(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }
}
