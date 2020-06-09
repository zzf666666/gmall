package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.PmsClient;
import com.atguigu.gmall.cart.feign.SmsClient;
import com.atguigu.gmall.cart.feign.WmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX = "cart:info:";

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Cart cart(Integer skuId) {
        return null;
    }

    @Override
    public void addCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        String uid = null;
        if(userInfo.getUserId() != null){
            uid = userInfo.getUserId().toString();
        }

        String userId = uid;
        if(userId == null){
            userId = userInfo.getUserKey();
        }

        String key = PREFIX + userId;

        Long skuId = cart.getSkuId();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        Integer count = cart.getCount();
        try {
            String cartJson = (String)hashOps.get(skuId.toString());
            if(StringUtils.isNotBlank(cartJson)){
                cart = MAPPER.readValue(cartJson, Cart.class);
            }

            if(hashOps.hasKey(skuId.toString())){
                cart.setCount(cart.getCount() + count);
                cartJson = MAPPER.writeValueAsString(cart);
                hashOps.put(skuId.toString(), cartJson);
                cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id",skuId));
            }else{
                cart.setCheck(true);
                cart.setUserId(userId.toString());

                ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
                SkuEntity skuEntity = skuEntityResponseVo.getData();
                if(skuEntity != null){
                    cart.setTitle(skuEntity.getTitle());
                    cart.setDefaultImage(skuEntity.getDefaultImage());
                    cart.setPrice(skuEntity.getPrice());
                }

                ResponseVo<List<SkuAttrValueEntity>> attrValueResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuId);
                List<SkuAttrValueEntity> attrValueEntities = attrValueResponseVo.getData();
                cart.setSaleAttrs(MAPPER.writeValueAsString(attrValueEntities));

                ResponseVo<List<ItemSaleVo>> SalesResponseVo = smsClient.querySaleVosBySkuId(skuId);
                List<ItemSaleVo> saleVos = SalesResponseVo.getData();
                cart.setSales(MAPPER.writeValueAsString(saleVos));


                ResponseVo<List<WareSkuEntity>> StoreResponseVo = wmsClient.queryWareBySkuId(skuId);
                List<WareSkuEntity> storeList = StoreResponseVo.getData();

                cart.setStore(storeList.stream().anyMatch(store -> store.getStock() - store.getStockLocked() > 0));

                hashOps.put(skuId.toString(), MAPPER.writeValueAsString(cart));
                cartMapper.insert(cart);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}
