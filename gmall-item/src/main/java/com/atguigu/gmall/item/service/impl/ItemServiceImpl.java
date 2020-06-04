package com.atguigu.gmall.item.service.impl;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.PmsClient;
import com.atguigu.gmall.item.feign.SmsClient;
import com.atguigu.gmall.item.feign.WmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.pmsinterface.vo.ItemGroupVo;
import com.atguigu.gmall.pmsinterface.vo.SaleAttrValueVo;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private WmsClient wmsClient;

    @Override
    public ItemVo queryItemBySkuId(Long skuId) {

        ItemVo itemVo = new ItemVo();

        ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
        SkuEntity skuEntity = skuEntityResponseVo.getData();
        if(skuEntity == null){
            return null;
        }
        itemVo.setSkuId(skuId);
        itemVo.setTitle(skuEntity.getTitle());
        itemVo.setSubTitle(skuEntity.getSubtitle());
        itemVo.setPrice(skuEntity.getPrice());
        itemVo.setWeight(skuEntity.getWeight());
        itemVo.setDefaultImage(skuEntity.getDefaultImage());

        ResponseVo<List<CategoryEntity>> categoriesByCid3 = pmsClient.queryCategoriesByCid3(skuEntity.getCatagoryId());
        List<CategoryEntity> categories = categoriesByCid3.getData();
        itemVo.setCategories(categories);

        ResponseVo<BrandEntity> brandById = pmsClient.queryBrandById(skuEntity.getBrandId());
        BrandEntity brandEntity = brandById.getData();
        if (brandEntity != null){
            itemVo.setBrandId(brandEntity.getId());
            itemVo.setBrandName(brandEntity.getName());
        }

        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if(spuEntity != null){
            itemVo.setSpuId(spuEntity.getId());
            itemVo.setSpuName(spuEntity.getName());
        }

        ResponseVo<List<SkuImagesEntity>> listResponseVo = pmsClient.queryImagesBySkuId(skuId);
        List<SkuImagesEntity> skuImagesEntities = listResponseVo.getData();
        itemVo.setImages(skuImagesEntities);

        ResponseVo<List<ItemSaleVo>> listResponseVo1 = smsClient.querySaleVosBySkuId(skuId);
        List<ItemSaleVo> saleVoList = listResponseVo1.getData();
        itemVo.setSales(saleVoList);

        ResponseVo<List<WareSkuEntity>> listResponseVo2 = wmsClient.queryWareBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = listResponseVo2.getData();
        if(wareSkuEntities != null){
            boolean store = wareSkuEntities.stream().anyMatch(ware -> ware.getStock() - ware.getStockLocked() > 0);
            itemVo.setStore(store);
        }

        ResponseVo<List<SaleAttrValueVo>> listResponseVo3 = pmsClient.querySaleAttrValuesBySpuId(skuEntity.getSpuId());
        List<SaleAttrValueVo> skuAttrValueEntities = listResponseVo3.getData();
        itemVo.setSkuAttrs(skuAttrValueEntities);

        ResponseVo<List<SkuAttrValueEntity>> listResponseVo4 = pmsClient.querySaleAttrValuesBySkuId(skuId);
        List<SkuAttrValueEntity> skuAttrValueEntitiesList = listResponseVo4.getData();
        if(skuAttrValueEntitiesList != null){
            Map<Long, String> map = skuAttrValueEntitiesList.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
            itemVo.setSaleAttr(map);
        }

        ResponseVo<String> stringResponseVo = pmsClient.querySkuJsonsBySpuId(spuEntity.getId());
        String skuJsons  = stringResponseVo.getData();
        itemVo.setSkuJsons(skuJsons);

        ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(spuEntity.getId());
        SpuDescEntity spuDescEntity  = spuDescEntityResponseVo.getData();
        if(spuDescEntity != null){
            String[] decript  = StringUtils.split(spuDescEntity.getDecript(), ",");
            itemVo.setSpuImages(Arrays.asList(decript));
        }

        ResponseVo<List<ItemGroupVo>> listResponseVo5 = pmsClient.queryItemGroupVoByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), spuEntity.getId(), skuId);
        List<ItemGroupVo> itemGroupVos = listResponseVo5.getData();
        itemVo.setGroups(itemGroupVos);

        return itemVo;
    }
}
