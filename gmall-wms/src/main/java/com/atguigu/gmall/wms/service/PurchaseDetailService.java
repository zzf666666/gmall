package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gamll.wmsinterface.entity.PurchaseDetailEntity;

/**
 * 
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-19 21:59:33
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

