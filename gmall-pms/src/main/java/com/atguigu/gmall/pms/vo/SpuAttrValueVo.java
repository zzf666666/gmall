package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {
    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        setAttrValue(StringUtils.join(valueSelected,","));
    }
}
