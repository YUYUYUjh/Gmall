package com.yy.gmall.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.gmall.common.constant.RedisConst;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.util.IpUtil;
import com.yy.gmall.model.user.UserAddress;
import com.yy.gmall.model.user.UserInfo;
import com.yy.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu
 * @create 2021-10-16 12:41
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    @PostMapping("/passport/login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request){
        if (userInfo != null){
            userInfo = userInfoService.login(userInfo);
            if (userInfo!=null){
                //有用户信息
                //生产一个token,并将token放入cookie中
                String token = UUID.randomUUID().toString();
                //保存用户信息到Redis缓存中 key=token  value=用户id
                String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX+token;
                //分析问题: 只存token有可能会出现token被盗用, 解决:获取到服务器的ip地址,将这个ip地址存储到缓存
                JSONObject userJson = new JSONObject();
                userJson.put("userId", userInfo.getId().toString());
                //防止盗用token信息,保存vue的时候将登录时的ip也存到缓存
                userJson.put("ip", IpUtil.getIpAddress(request));
                redisTemplate.opsForValue().set(loginKey,userJson.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
                Map map = new HashMap();
                map.put("token",token);
                map.put("nickName",userInfo.getNickName());
                return Result.ok(map);
            }
        }
        return Result.fail().message("用户名或密码错误");
    }

    /**
     * 用户退出
     *
     */
    @GetMapping("/passport/logout")
    public Result logout(@RequestHeader("token") String token){
        System.out.println(token);
        //登录的时候,将数据存储到缓存一份,还有token在cookie,请求头中,
        //退出登录,将这些数据删除
        //删除缓存中的key=token
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+token);
        return Result.ok().message("退出登录成功");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/inner/getUserInfo/{userId}")
    public UserInfo getUserInfo(@PathVariable Long userId){
        UserInfo userInfo = userInfoService.getUserInfo(userId);
        return userInfo;
    }

    /**
     * 获取用户地址
     */
    @GetMapping("/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable Long userId){
        List<UserAddress> addressList = userInfoService.findUserAddressListByUserId(userId);
        return addressList;
    }
}
