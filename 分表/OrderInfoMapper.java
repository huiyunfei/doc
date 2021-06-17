package com.fhgl.shop.mapper;

import com.fhgl.shop.annotation.TableSeg;
import com.fhgl.shop.entity.TbShopOrder;
import com.fhgl.shop.modal.TodayOrderInfo;
import com.fhgl.shop.modal.params.ShopOrderParams;
import com.fhgl.shop.modal.results.OrderFinanceResults;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderInfoMapper {

   

    /**
     * 获得商家所有订单并分组
     *
     * @return
     */
    @TableSeg(shardBy = "begainTime,endTime")
    List<TbShopOrder> findSellAllOrder(ShopOrderParams params);

    

    /**
     * 获得商家订单详情并分组
     *
     * @param orderId
     * @return
     */
    @TableSeg(shardBy = "orderId")
    TbShopOrder findSellAllorderDeatilByGroup(@Param("orderId") String orderId);

}
