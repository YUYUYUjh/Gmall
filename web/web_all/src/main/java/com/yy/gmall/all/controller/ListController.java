package com.yy.gmall.all.controller;

import com.yy.gmall.common.result.Result;
import com.yy.gmall.list.client.ListFeignClient;
import com.yy.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yu
 * @create 2021-10-13 16:11
 */
@Controller
@RequestMapping
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model){
        //需要存储 searchParam , trademarkParam....
        Result<Map> result = listFeignClient.searchData(searchParam);

        model.addAllAttributes(result.getData());
        model.addAttribute("searchParam",searchParam);

        //获取用户点击哪些平台属性值 , 拼接完整请求url
        String urlParam = this.makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);

        //品牌面包屑
        String trademarkParam = this.makeTrademarkParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam",trademarkParam);

        //平台属性面包屑 存储一个集合
        if (searchParam.getProps()!=null){
            List<Map> propsParamList = this.makePropsParamList(searchParam.getProps());
            model.addAttribute("propsParamList",propsParamList);
        }


        //设置排序规则
        Map<String,Object> orderMap = this.orderMap(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        return "list/index";
    }

    //排序规则
    private Map<String, Object> orderMap(String order) {
        Map<String,Object> map = new HashMap<>();
        if (!StringUtils.isEmpty(map)){
           // order=2:desc
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                map.put("type",split[0]);
                map.put("sort",split[1]);
            }else {
                //没传排序的值默认排序
                map.put("type","1");
                map.put("sort","desc");
            }
        }
        return map;
    }

    //平台属性面包屑处理
    private List<Map> makePropsParamList(String[] props) {
        List<Map> mapList = new ArrayList<>();
        if (props.length>0 && props!=null){
            //遍历数组
            for (String prop : props) {
                //获取到的是单个字符串对象,  3:4GB:运行内存
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    HashMap<String,Object> map = new HashMap<>();
                    //将分割后的字符串按照顺序放进map
                    map.put("attrId",split[0]);
                    map.put("attrValue",split[1]);
                    map.put("attrName",split[2]);
                    mapList.add(map);
                }
            }
        }
        return mapList;
    }

    //获取品牌的面包屑
    private String makeTrademarkParam(String trademark) {
        //判断
        if (!StringUtils.isEmpty(trademark)){
            // trademark = 1:小米 分割
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                //返回面包屑
                return "品牌:"+split[1];
            }
        }
        return null;
    }

    //制作url参数地址
    private String makeUrlParam(SearchParam searchParam) {

        //创建一个对象
        StringBuilder sb = new StringBuilder();

        //需要知道按照哪个入口进行的检索
        //按照关键字检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            sb.append("keyword=").append(searchParam.getKeyword());
        }

        //按照3级分类id检索
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){

            sb.append("category3Id=").append(searchParam.getCategory3Id());
        }

        //按照2级分类id检索
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){

            sb.append("category2Id=").append(searchParam.getCategory2Id());
        }

        //按照1级分类id检索
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){

            sb.append("category1Id=").append(searchParam.getCategory1Id());
        }

        //按照平台属性值检索过滤
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            //遍历
            for (String prop : props) {
                //判断
                if (sb.length() > 0){
                    sb.append("&props=").append(prop);
                }
            }
        }

        //http://list.atguigu.cn/list.html?category3Id=61&trademark=2:苹果&order=
        //按照品牌检索
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            if (sb.length()>0){
                sb.append("&trademark=").append(trademark);
            }
        }
        return "list.html?"+sb.toString();
    }
}
