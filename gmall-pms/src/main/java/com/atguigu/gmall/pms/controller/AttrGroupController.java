package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pmsinterface.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import com.atguigu.gmall.pmsinterface.vo.ItemGroupVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pmsinterface.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("item/group")
    public ResponseVo<List<ItemGroupVo>> queryItemGroupVoByCidAndSpuIdAndSkuId(
            @RequestParam("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId
    ){
        List<ItemGroupVo> itemGroupVo = attrGroupService.queryItemGroupVoByCidAndSpuIdAndSkuId(cid,spuId,skuId);
        return ResponseVo.ok(itemGroupVo);
    }

    @GetMapping("/withattrs/{cid}")
    public ResponseVo<List<AttrGroupVo>> queryAttrByCid(@PathVariable("cid") Long cId){
        List<AttrGroupVo> list = attrGroupService.queryByCid(cId);
        return ResponseVo.ok(list);
    }

    @GetMapping("/category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryGroupByCid(@PathVariable("cid") Long cId){
        List<AttrGroupEntity> attrGroupList = attrGroupService.queryByCidPage(cId);
        return ResponseVo.ok(attrGroupList);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
