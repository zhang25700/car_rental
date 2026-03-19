package com.carrental.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.carrental.common.BusinessException;
import com.carrental.config.AlipayProperties;
import com.carrental.entity.RentalOrder;
import com.carrental.service.IAlipayService;
import com.carrental.service.IOrderService;
import java.math.RoundingMode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements IAlipayService {
    private final AlipayProperties alipayProperties;
    private final IOrderService orderService;

    @Override
    public String createPagePayForm(Long orderId, Long userId) {
        validateConfig();
        RentalOrder order = orderService.getOwnedOrderDetail(orderId, userId, false);
        if (!"UNPAID".equals(order.getPaymentStatus())) {
            throw new BusinessException(400, "order already paid");
        }
        try {
            AlipayClient client = new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getMerchantPrivateKey(),
                "json",
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
            );
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl());

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(order.getOrderNo());
            model.setSubject("汽车租赁订单-" + order.getOrderNo());
            model.setTotalAmount(order.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            model.setBody("汽车租赁系统支付宝沙盒支付");
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            request.setBizModel(model);
            return client.pageExecute(request).getBody();
        } catch (Exception ex) {
            throw new BusinessException(500, "alipay sandbox request failed: " + ex.getMessage());
        }
    }

    @Override
    public boolean handleNotify(Map<String, String> params) {
        validateConfig();
        try {
            boolean verified = AlipaySignature.rsaCheckV1(
                params,
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getCharset(),
                alipayProperties.getSignType()
            );
            if (!verified) {
                return false;
            }
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                orderService.markPaidByOrderNo(params.get("out_trade_no"), params.get("trade_no"));
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void validateConfig() {
        if (!alipayProperties.isEnabled()
            || StringUtils.isBlank(alipayProperties.getAppId())
            || StringUtils.isBlank(alipayProperties.getMerchantPrivateKey())
            || StringUtils.isBlank(alipayProperties.getAlipayPublicKey())) {
            throw new BusinessException(400, "alipay sandbox config is incomplete");
        }
    }
}
