package com.atguigu.gmall.pmsinterface.vo;

import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVo {

    private String groupName;
    private List<AttrValueVo> attrValues;
}
