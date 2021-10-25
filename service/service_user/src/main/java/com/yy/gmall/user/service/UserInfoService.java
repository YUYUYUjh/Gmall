package com.yy.gmall.user.service;

import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.model.user.UserInfo;

import java.util.List;

/**
 * @author Yu
 * @create 2021-10-16 15:15
 */
public interface UserInfoService {
    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    UserInfo getUserInfo(Long userId);

    /**
     * 获取用户地址
     * @param userId
     * @return
     */
    List<UserAddress> findUserAddressListByUserId(Long userId);
}
