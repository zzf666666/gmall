package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import com.atguigu.gmall.smsinterface.vo.SkuSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;

import java.util.List;

/**
 * 商品spu积分设置
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 16:18:56
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void saveSkuSales(SkuSaleVo skuSaleVo);

    List<ItemSaleVo> querySaleVosBySkuId(Long skuId);
}

