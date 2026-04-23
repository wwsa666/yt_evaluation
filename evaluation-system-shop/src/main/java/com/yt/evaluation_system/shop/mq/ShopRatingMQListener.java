package com.yt.evaluation_system.shop.mq;

import com.yt.evaluation_system.shop.service.ShopService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RocketMQMessageListener(topic = "shop-rating-update-topic", consumerGroup = "shop-service-consumer-group")
public class ShopRatingMQListener implements RocketMQListener<Long> {

    @Autowired
    private ShopService shopService;

    @Override
    @Transactional
    public void onMessage(Long shopId) {
        // 这里直接调用 shopService 的聚合方法处理评分和评论数更新
        System.out.println("收到 MQ 消息，开始更新商铺信息：" + shopId);
        shopService.updateShopRatingAndComments(shopId);
    }
}

