package com.yy.gmall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yy.gmall.common.util.MD5;
import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.model.user.UserInfo;
import com.yy.gmall.user.mapper.UserAddressMapper;
import com.yy.gmall.user.mapper.UserInfoMapper;
import com.yy.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Yu
 * @create 2021-10-16 15:15
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        //根据用户名和加密后的密码查询
        return userInfoMapper.selectOne(new QueryWrapper<UserInfo>()
                .eq("login_name",userInfo.getLoginName())
                .eq("passwd", MD5.encrypt(userInfo.getPasswd())));
    }

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    @Override
    public UserInfo getUserInfo(Long userId) {
        return userInfoMapper.selectById(userId);
    }

    /**
     * 查询用户地址
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        return userAddressMapper.selectList(new QueryWrapper<UserAddress>()
                .eq("user_id",userId));
    }
}
