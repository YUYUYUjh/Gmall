package com.yy.gmall.user.client.impl;

import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.model.user.UserInfo;
import com.yy.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * @author Yu
 * @create 2021-10-19 18:24
 */
@Component
public class UserDegradeFeignClient implements UserFeignClient {
    @Override
    public UserInfo getUserInfo(Long userId) {
        return null;
    }

    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        return null;
    }
}
