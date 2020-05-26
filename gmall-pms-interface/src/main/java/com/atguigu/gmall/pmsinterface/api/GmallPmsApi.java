package com.atguigu.gmall.pmsinterface.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
