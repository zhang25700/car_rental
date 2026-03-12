package com.carrental.controller;

import com.carrental.common.ApiResponse;
import com.carrental.config.AlipayProperties;
import com.carrental.security.AuthContext;
import com.carrental.service.IAlipayService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay/alipay")
public class PaymentController {
    @Autowired
    private IAlipayService alipayService;

    @Autowired
    private AlipayProperties alipayProperties;

    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> config() {
        boolean enabled = alipayProperties.isEnabled();
        boolean ready = enabled
            && notBlank(alipayProperties.getAppId())
            && notBlank(alipayProperties.getMerchantPrivateKey())
            && notBlank(alipayProperties.getAlipayPublicKey());
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", enabled);
        data.put("ready", ready);
        data.put("message", ready ? "支付宝沙盒已配置" : "支付宝沙盒未配置");
        return ApiResponse.success(data);
    }

    @GetMapping("/{orderId}/form")
    public ApiResponse<String> createPayForm(@PathVariable Long orderId) {
        return ApiResponse.success(alipayService.createPagePayForm(orderId, AuthContext.get().getUserId()));
    }

    @PostMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyResult(HttpServletRequest request) {
        return alipayService.handleNotify(extractParams(request)) ? "success" : "failure";
    }

    @GetMapping(value = "/return", produces = "text/html;charset=UTF-8")
    public String returnPage(@RequestParam(required = false) String out_trade_no,
                             @RequestParam(required = false) String trade_no) {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head><meta charset="UTF-8"><title>支付宝沙盒支付</title></head>
            <body style="font-family:Arial,sans-serif;padding:40px;">
              <h2>支付宝沙盒支付已完成返回</h2>
              <p>订单号：%s</p>
              <p>支付宝交易号：%s</p>
              <p>请回到租赁系统刷新订单状态。</p>
            </body>
            </html>
            """.formatted(safe(out_trade_no), safe(trade_no));
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, new String(values[0].getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            }
        });
        return params;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
