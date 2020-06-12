package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.exception.OrderException;
import com.atguigu.gmall.oms.feign.PmsClient;
import com.atguigu.gmall.oms.feign.SmsClient;
import com.atguigu.gmall.oms.feign.UmsClient;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import com.atguigu.gmall.umsinterface.entity.UserAddressEntity;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.service.OrderService;
import org.springframework.util.CollectionUtils;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    @Autowired
    private UmsClient umsClient;

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public void saveOrder(OrderSubmitVo orderSubmitVo) {
        OrderEntity orderEntity = new OrderEntity();

        if (CollectionUtils.isEmpty(orderSubmitVo.getItems())) {
            throw new OrderException("订单商品项不能为空");
        }

        if (orderSubmitVo != null) {
            orderEntity.setUserId(orderSubmitVo.getUserId());
            orderEntity.setOrderSn(orderSubmitVo.getOrderToken());
            orderEntity.setCreateTime(new Date());

            ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(orderSubmitVo.getUserId());
            UserEntity userEntity = userEntityResponseVo.getData();
            if (userEntity != null) {
                orderEntity.setUsername(userEntity.getUsername());
            }

            orderEntity.setTotalAmount(orderSubmitVo.getTotalPrice());
            orderEntity.setPayAmount(orderSubmitVo.getTotalPrice().subtract(new BigDecimal(orderSubmitVo.getBounds() / 100)));
            orderEntity.setIntegrationAmount(new BigDecimal(orderSubmitVo.getBounds() / 100));
            orderEntity.setPayType(orderSubmitVo.getPayType());
            orderEntity.setSourceType(0);
            orderEntity.setStatus(0);
            orderEntity.setDeliveryCompany(orderSubmitVo.getDeliveryCompany());

            UserAddressEntity address = orderSubmitVo.getAddress();
            if (address != null) {
                orderEntity.setReceiverName(address.getName());
                orderEntity.setReceiverPhone(address.getPhone());
                orderEntity.setReceiverPostCode(address.getPostCode());
                orderEntity.setReceiverProvince(address.getProvince());
                orderEntity.setReceiverCity(address.getCity());
                orderEntity.setReceiverRegion(address.getRegion());
                orderEntity.setReceiverAddress(address.getAddress());
            }
            orderEntity.setDeleteStatus(0);
            orderEntity.setUseIntegration(orderSubmitVo.getBounds().intValue());

        }

        baseMapper.insert(orderEntity);

        List<OrderItemVo> items = orderSubmitVo.getItems();

        items.forEach(item -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();

            orderItemEntity.setOrderId(orderEntity.getId());
            orderItemEntity.setOrderSn(orderEntity.getOrderSn());

            CompletableFuture<SkuEntity> skuEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
                ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
                SkuEntity skuEntity = skuEntityResponseVo.getData();

                if (skuEntity != null) {
                    orderItemEntity.setSkuId(skuEntity.getId());
                    orderItemEntity.setSkuName(skuEntity.getName());
                    orderItemEntity.setSkuPic(skuEntity.getDefaultImage());
                    orderItemEntity.setSkuPrice(skuEntity.getPrice());
                    orderItemEntity.setSkuQuantity(item.getCount());
                    //TODO: 根据skuId查询sms_sku_bounds表里面的成长和购物积分
                }
                return skuEntity;
            });
            CompletableFuture<Void> skuAttrValuesCompletableFuture = CompletableFuture.runAsync(() -> {
                ResponseVo<List<SkuAttrValueEntity>> skuAttrValuesResponseVo = pmsClient.querySkuAttrValuesBySkuId(item.getSkuId());
                List<SkuAttrValueEntity> skuAttrValues = skuAttrValuesResponseVo.getData();
                orderItemEntity.setSkuAttrsVals(JSON.toJSONString(skuAttrValues));
            });

            CompletableFuture<Void> skuEntity1CompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
                if(skuEntity != null){
                    ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
                    SpuEntity spuEntity = spuEntityResponseVo.getData();
                    if (spuEntity != null) {
                        orderItemEntity.setSpuId(spuEntity.getId());
                        orderItemEntity.setSpuName(spuEntity.getName());
                        orderItemEntity.setCategoryId(spuEntity.getCategoryId());
                    }
                }
            });

            CompletableFuture<Void> skuEntity2CompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
                if(skuEntity != null){
                    ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
                    SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
                    if (spuDescEntity != null) {
                        orderItemEntity.setSpuPic(spuDescEntity.getDecript());
                    }
                }
            });


            CompletableFuture<Void> skuEntity3CompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
                if(skuEntity != null){
                    ResponseVo<BrandEntity> brandById = pmsClient.queryBrandById(skuEntity.getSpuId());
                    BrandEntity brandEntity = brandById.getData();
                    if (brandEntity != null) {
                        orderItemEntity.setSpuBrand(brandEntity.getName());
                    }
                }
            });

            CompletableFuture.allOf(skuAttrValuesCompletableFuture,skuEntity1CompletableFuture, skuEntity2CompletableFuture, skuEntity3CompletableFuture);
            orderItemMapper.insert(orderItemEntity);
        });

    }

}