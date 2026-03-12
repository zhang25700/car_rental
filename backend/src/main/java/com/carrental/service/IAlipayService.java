package com.carrental.service;

import java.util.Map;

public interface IAlipayService {
    String createPagePayForm(Long orderId, Long userId);

    boolean handleNotify(Map<String, String> params);
}
