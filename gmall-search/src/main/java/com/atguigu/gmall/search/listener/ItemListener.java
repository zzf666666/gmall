package com.atguigu.gmall.search.listener;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrVo;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemListener {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GoodsRepository repository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL_SEARCH_QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.*"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {

        try {
            ResponseVo<List<SkuEntity>> skuPage = gmallPmsClient.querySkyBySpuId(spuId);
            List<SkuEntity> skuList = skuPage.getData();
            System.out.println("-------------------------->>" + skuList);
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
                    ResponseVo<List<SpuAttrValueEntity>> spuAttrById = gmallPmsClient.querySpuAttrValuesBySpuId(spuId);
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
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }catch (Exception e){
            e.printStackTrace();
            if(message.getMessageProperties().getRedelivered()){
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
        }


    }

}
