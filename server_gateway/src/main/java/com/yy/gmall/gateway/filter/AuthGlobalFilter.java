package com.yy.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yy.gmall.common.result.Result;
import com.yy.gmall.common.result.ResultCodeEnum;
import com.yy.gmall.common.util.IpUtil;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Yu
 * @create 2021-10-17 12:13
 * 自定义过滤器
 *
 *      网关: 默认情况下 九大过滤器 (网关自带)
 *      过滤器执行顺序: order数值 越小越先执行
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate redisTemplate;

    //  获取配置文件中配置的需要登录才能跳转的页面
    //  url: trade.html,myOrder.html,list.html
    @Value("${authUrls.url}")
    private String authUrls;

    //创建一个对象,容器中没有,需要自己实例化(new)  匹配路径的工具类
    private AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 全局过滤器的执行方法 , 发出的所有请求都要经过下面的方法
     * @param exchange 能够获取到web的请求与响应的对象
     * @param chain 过滤器链
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取用户访问的url
        //exchange.getRequest().getURI() = http://item.gmall.com/api/product/inner/getSkuInfo/17
        String path = exchange.getRequest().getURI().getPath();
        //path = /api/product/inner/getSkuInfo/17
        //响应
        ServerHttpResponse response = exchange.getResponse();
        //1.判断用户访问是否内部数据接口  /**/inner/**
        //第一个参数相当于key , 第二个参数相当于/api/product/inner/getSkuInfo/17
        //以"/"开始找,匹配是否包含模板中的路径
        if (matcher.match("/**/inner/**",path)){
            //路径匹配:
            return out(response, ResultCodeEnum.PERMISSION);
        }
        ServerHttpRequest request = exchange.getRequest();
        //获取用户id 从request中
        String userId = this.getUserId(request);
        //获取用户的临时id
        String userTempId = this.getUserTempId(request);
        //防止token被盗用
        if ("-1".equals(userId)){
            return out(response, ResultCodeEnum.LOGIN_IP_ERROR);
        }
        //2.判断用户访问是否/api/**/auth/**
        if (matcher.match("/api/**/auth/**",path)){
            //路径匹配
            //用户必须是登录的情况下才能访问 , 不是不能登录
            if (StringUtils.isEmpty(userId)){
                //停止运行
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //3.用户访问web-all中哪些控制器需要跳转到登录页面
        String[] split = authUrls.split(",");
        for (String url : split) {
            //判断请求路径是不是需要登录才能访问的页面
            if (path.indexOf(url)!=-1 && StringUtils.isEmpty(userId)){
                //是需要登录的页面且用户id不存在,跳转到登录
                //设置响应的参数
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //设置请求头
                try {
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+ URLEncoder.encode(request.getURI().toString(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //代表重定向
                return response.setComplete();
            }
        }

        //将用户id放入请求头
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)){
            if (!StringUtils.isEmpty(userId)){
                //判断用户id 不为空 userId跟request是同一个类型
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)){
                //判断临时id不为空 也扔入请求头
                request.mutate().header("userTempId", userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        //默认返回
        return chain.filter(exchange);
    }

    // 统一返回用户提示信息
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //输入的内容在resultCodeEnum
        Result<String> result = Result.build(null, resultCodeEnum);
        //将输入的内容转为JSON字符串
        String str = JSON.toJSONString(result);
        //将JSOn字符串转为DataBuffer
        DataBuffer wrap = response.bufferFactory().wrap(str.getBytes());
        //设置请求头 , 字符集
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        //参数需要一个数据流
        Mono<Void> voidMono = response.writeWith(Mono.just(wrap));
        return voidMono;
    }

    // 获取用户id
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        //用户id在缓存中,缓存的key是由token组成,因此必须先获取到token
        //token在cookie中, 也有可能在header中
        //将token存在两个位置是因为,如果面向移动端开发没有cookie,
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie httpCookie = cookies.getFirst("token");
        if (httpCookie!=null){
            token = httpCookie.getValue();
        }else {
            List<String> list = request.getHeaders().get("token");
            if (!CollectionUtils.isEmpty(list)){
                token = list.get(0);
            }
        }
        //获取到token之后 , 组成缓存的key 来获取缓存中的值
        if (!StringUtils.isEmpty(token)){
            String userKey = "user:login:"+token;
            String strJson = (String) this.redisTemplate.opsForValue().get(userKey);
            if (StringUtils.isEmpty(strJson)){
                return null;
            }
            //需要将strJson转换为JSONObject
            JSONObject jsonObject = JSONObject.parseObject(strJson);
            String ip = (String) jsonObject.get("ip");
            //判断ip地址是否一致
            if (ip.equals(IpUtil.getGatwayIpAddress(request))){
                String userId = (String) jsonObject.get("userId");
                return userId;
            }else {
                //ip地址不一致 返回-1
                return "-1";
            }
        }

        return null;
    }

    //获取用户的临时id
    private String getUserTempId(ServerHttpRequest request){
        //临时用户id 被存入到了cookie中, 也存入到了请求头中
        String userTempId = "";
        HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
        if(httpCookie!=null){
            userTempId = httpCookie.getValue();
        }else{
            List<String> list = request.getHeaders().get("userTempId");
            if (!CollectionUtils.isEmpty(list)){
                userTempId = list.get(0);
            }
        }
        return userTempId;
    }

    /**
     * 自定义过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
