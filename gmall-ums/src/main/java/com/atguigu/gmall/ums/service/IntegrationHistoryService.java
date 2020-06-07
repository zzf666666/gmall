package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.umsinterface.entity.IntegrationHistoryEntity;

/**
 * 购物积分记录表
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 16:26:32
 */
public interface IntegrationHistoryService extends IService<IntegrationHistoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

