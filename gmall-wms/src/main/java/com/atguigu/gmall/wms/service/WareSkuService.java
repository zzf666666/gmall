package com.atguigu.gmall.wms.service;

import com.atguigu.gamll.wmsinterface.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;

import java.util.List;

/**
 * 商品库存
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-19 21:59:33
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuLockVO> lockWare(List<SkuLockVO> orderItems, String orderToken);
}

