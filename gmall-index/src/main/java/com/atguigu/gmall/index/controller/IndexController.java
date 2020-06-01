package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Map<String,List<CategoryEntity>> map){
        List<CategoryEntity> categoryEntityList = indexService.queryLvl1Categories();
        map.put("categories", categoryEntityList);
        return "index";
    }

    @GetMapping("/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSubs(@PathVariable("pid")Long pid){
        List<CategoryEntity> categories = indexService.queryLvl2CategoriesWithSubs(pid);
        return ResponseVo.ok(categories);
    }
}
