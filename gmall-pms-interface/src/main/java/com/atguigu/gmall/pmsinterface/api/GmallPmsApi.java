package com.atguigu.gmall.pmsinterface.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.pmsinterface.vo.ItemGroupVo;
import com.atguigu.gmall.pmsinterface.vo.SaleAttrValueVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("/pms/spu/page")
    ResponseVo<List<SpuEntity>> queryPage(@RequestBody PageParamVo paramVo);

    @GetMapping("/pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> querySkyBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("/pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("/pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @GetMapping("/pms/spuattrvalue/spu/{spuId}")
    ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValuesBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("/pms/skuattrvalue/sku/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValuesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("/pms/category/parent/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentId") Long pId);

    @GetMapping("/pms/category/parent/with/subs/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategoriesWithSubByPid(@PathVariable("parentId")Long pid);

    @GetMapping("/pms/sku/{id}")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/category/all/{cid3}")
    ResponseVo<List<CategoryEntity>> queryCategoriesByCid3(@PathVariable("cid3")Long cid3);

    @GetMapping("/pms/spu/{id}")
    ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("/pms/skuimages/sku/{skuId}")
    ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("/pms/skuattrvalue/spu/{spuId}")
    ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("/pms/skuattrvalue/sku/sale/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValuesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("/pms/skuattrvalue/sku/spu/{spuId}")
    ResponseVo<String> querySkuJsonsBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("/pms/spudesc/{spuId}")
    ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("/pms/attrgroup/item/group")
    ResponseVo<List<ItemGroupVo>> queryItemGroupVoByCidAndSpuIdAndSkuId(
            @RequestParam("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId
    );
}
