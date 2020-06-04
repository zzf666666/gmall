package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.atguigu.gmall.pmsinterface.vo.ItemGroupVo;
import com.atguigu.gmall.pmsinterface.vo.SaleAttrValueVo;
import com.atguigu.gmall.pmsinterface.entity.SkuImagesEntity;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    private List<CategoryEntity> categories;

    private Long brandId;
    private String brandName;

    private Long spuId;
    private String spuName;

    private Long skuId;
    private BigDecimal price;
    private String title;
    private String subTitle;
    private String defaultImage;
    private Integer weight;

    private List<SkuImagesEntity> images;

    private List<ItemSaleVo> sales;

    private Boolean store = false;

    private List<SaleAttrValueVo> skuAttrs;

    // 当前sku的销售属性 {8: 黑色, 9: 12G, 10: 256G}
    private Map<Long,String> saleAttr;
    // 销售属性组合 和 skuId的对应关系 {'黑色, 8G, 256G': 30, '黑色, 12G, 512G': 32}
    private String skuJsons;

    private List<String> spuImages;

    private List<ItemGroupVo> groups;
}
