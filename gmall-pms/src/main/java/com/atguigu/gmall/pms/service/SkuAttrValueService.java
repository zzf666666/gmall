package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pmsinterface.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);


    List<SkuAttrValueEntity> querySkuAttrValuesBySkuId(Long skuId);

    List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId);

    String querySkuJsonsBySpuId(Long spuId);
}

