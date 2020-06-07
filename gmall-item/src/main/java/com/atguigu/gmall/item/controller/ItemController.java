package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("item/{skuId}")
    public String item(@PathVariable("skuId") Long skuId, Map<String,ItemVo> map){
        ItemVo itemVo = itemService.queryItemBySkuId(skuId);

        map.put("itemVo",itemVo);

        return "item";
    }
}
