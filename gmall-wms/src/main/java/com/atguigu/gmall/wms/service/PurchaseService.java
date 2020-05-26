package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gamll.wmsinterface.entity.PurchaseEntity;

/**
 * 采购信息
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-19 21:59:33
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

