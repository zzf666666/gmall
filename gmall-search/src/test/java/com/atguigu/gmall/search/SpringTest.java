package com.atguigu.gmall.search;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrVo;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.repository.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class SpringTest {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GoodsRepository repository;

    @Test
    public void test(){
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        Integer pageNum = 1;
        Integer pageSize = 100;

        do{
            ResponseVo<List<SpuEntity>> spuPage = gmallPmsClient.queryPage(new PageParamVo(1, 100, null));
            List<SpuEntity> spuList = spuPage.getData();
            if(CollectionUtils.isEmpty(spuList)){
                return;
            }

            spuList.forEach(spu -> {
                ResponseVo<List<SkuEntity>> skuPage = gmallPmsClient.querySkyBySpuId(spu.getId());
                List<SkuEntity> skuList = skuPage.getData();
                if(!CollectionUtils.isEmpty(skuList)){
                    List<Goods> goodsList = skuList.stream().map(sku -> {
                        Goods goods = new Goods();
                        goods.setCreateTime(new Date());

                        goods.setSkuId(sku.getId());
                        goods.setPrice(sku.getPrice());
                        goods.setTitle(sku.getTitle());
                        goods.setSubTitle(sku.getSubtitle());
                        goods.setDefaultImage(sku.getDefaultImage());

                        ResponseVo<BrandEntity> brandById = gmallPmsClient.queryBrandById(sku.getBrandId());
                        BrandEntity brand = brandById.getData();
                        if(brand != null){
                            goods.setBrandId(brand.getId());
                            goods.setBrandName(brand.getName());
                            goods.setLogo(brand.getLogo());
                        }

                        ResponseVo<CategoryEntity> categoryById = gmallPmsClient.queryCategoryById(sku.getCatagoryId());
                        CategoryEntity category = categoryById.getData();
                        if(category != null){
                            goods.setCategoryId(category.getId());
                            goods.setCategoryName(category.getName());
                        }

                        ArrayList<SearchAttrVo> searchAttrs = new ArrayList<>();
                        ResponseVo<List<SpuAttrValueEntity>> spuAttrById = gmallPmsClient.querySpuAttrValuesBySpuId(spu.getId());
                        List<SpuAttrValueEntity> spuAttr = spuAttrById.getData();
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrById = gmallPmsClient.querySkuAttrValuesBySkuId(sku.getId());
                        List<SkuAttrValueEntity> skuAttr = skuAttrById.getData();

                        if(!CollectionUtils.isEmpty(spuAttr)){
                            searchAttrs.addAll(spuAttr.stream().map(spuattr -> {
                                SearchAttrVo searchAttrVo = new SearchAttrVo();
                                BeanUtils.copyProperties(spuattr, searchAttrVo);
                                return searchAttrVo;
                            }).collect(Collectors.toList()));
                        }

                        if(!CollectionUtils.isEmpty(skuAttr)){
                            searchAttrs.addAll(skuAttr.stream().map(skuattr -> {
                                SearchAttrVo searchAttrVo = new SearchAttrVo();
                                BeanUtils.copyProperties(skuattr, searchAttrVo);
                                return searchAttrVo;
                            }).collect(Collectors.toList()));
                        }

                        goods.setSearchAttrs(searchAttrs);

                        ResponseVo<List<WareSkuEntity>> wareById = gmallWmsClient.queryWareBySkuId(sku.getId());
                        List<WareSkuEntity> ward = wareById.getData();
                        if(!CollectionUtils.isEmpty(ward)){
                            goods.setSales(ward.stream().map(WareSkuEntity::getSales).reduce(Long::sum).get());
                            goods.setStore(ward.stream().anyMatch(w -> (w.getStock()-w.getStockLocked())>0));
                        }
                        return goods;
                    }).collect(Collectors.toList());

                    repository.saveAll(goodsList);
                }
            });

            pageNum++;
            pageSize = spuList.size();
        }while(pageSize == 100);

    }
}
