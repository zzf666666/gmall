package com.atguigu.gmall.wms.service.impl;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gamll.wmsinterface.vo.SkuLockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareMapper;
import com.atguigu.gamll.wmsinterface.entity.WareEntity;
import com.atguigu.gmall.wms.service.WareService;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service("wareService")
public class WareServiceImpl extends ServiceImpl<WareMapper, WareEntity> implements WareService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareEntity>()
        );

        return new PageResultVo(page);
    }

}