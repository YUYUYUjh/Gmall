package com.yy.gmall.user.client;

import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.model.user.UserInfo;
import com.yy.gmall.user.client.impl.UserDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author Yu
 * @create 2021-10-19 18:22
 */
@FeignClient(value = "service-user",fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/api/user/inner/getUserInfo/{userId}")
    public UserInfo getUserInfo(@PathVariable Long userId);

    /**
     * 获取用户地址
     */
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId);
}
