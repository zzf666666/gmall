package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pmsinterface.entity.AttrEntity;
import com.atguigu.gmall.pmsinterface.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupVo extends AttrGroupEntity {
    private List<AttrEntity> attrEntities;
}
