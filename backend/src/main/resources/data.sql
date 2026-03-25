INSERT INTO sys_user (
  username, password, real_name, phone, email, role, status,
  id_card_no, id_card_front, id_card_back, verify_status, verify_remark, frozen_reason,
  created_at, updated_at
) VALUES
('admin', 'bfd488b5722378eb192076d7a9cacefe', '系统管理员', '13800000000', 'admin@carrental.com', 'ADMIN', 1, NULL, NULL, NULL, 'APPROVED', '管理员账号默认通过审核', NULL, NOW(), NOW()),
('operator', 'bfd488b5722378eb192076d7a9cacefe', '运营主管', '13800000010', 'operator@carrental.com', 'ADMIN', 1, NULL, NULL, NULL, 'APPROVED', '管理员账号默认通过审核', NULL, NOW(), NOW()),
('zhangsan', 'bfd488b5722378eb192076d7a9cacefe', '张三', '13900000001', 'zhangsan@example.com', 'CUSTOMER', 1, '310101199901011234', NULL, NULL, 'APPROVED', '请按需重新上传证件照片', NULL, NOW(), NOW()),
('lisi', 'bfd488b5722378eb192076d7a9cacefe', '李四', '13900000002', 'lisi@example.com', 'CUSTOMER', 1, '310101199806062222', NULL, NULL, 'PENDING', '等待人工复核', NULL, NOW(), NOW()),
('wangwu', 'bfd488b5722378eb192076d7a9cacefe', '王五', '13900000003', 'wangwu@example.com', 'CUSTOMER', 0, '310101199705055678', NULL, NULL, 'REJECTED', '证件信息不完整，请重新上传', '多次违约，暂时冻结', NOW(), NOW()),
('zhaoliu', 'bfd488b5722378eb192076d7a9cacefe', '赵六', '13900000004', 'zhaoliu@example.com', 'CUSTOMER', 1, '310101200001019999', NULL, NULL, 'APPROVED', '请按需重新上传证件照片', NULL, NOW(), NOW());

INSERT INTO pickup_station (name, city, address, contact_phone, longitude, latitude, business_hours, description, status, created_at, updated_at) VALUES
('浦东机场店', '上海', '浦东新区迎宾大道100号', '021-10000001', '121.7998', '31.1518', '08:00-22:00', '适合机场接送和长租业务', 1, NOW(), NOW()),
('虹桥枢纽店', '上海', '闵行区申虹路200号', '021-10000002', '121.3275', '31.1978', '07:30-22:00', '高铁和机场双通道取还车', 1, NOW(), NOW()),
('陆家嘴金融店', '上海', '浦东新区世纪大道88号', '021-10000003', '121.5064', '31.2451', '09:00-21:00', '商务短租高频站点', 1, NOW(), NOW()),
('徐家汇商圈店', '上海', '徐汇区天钥桥路66号', '021-10000004', '121.4377', '31.1884', '08:30-21:30', '热门商圈门店，支持夜间还车', 1, NOW(), NOW()),
('松江大学城店', '上海', '松江区文诚路99号', '021-10000005', '121.2263', '31.0305', '09:00-20:30', '服务学生和周边居民', 1, NOW(), NOW());

INSERT INTO car_brand (name, status, sort, created_at, updated_at) VALUES
('比亚迪', 1, 1, NOW(), NOW()),
('特斯拉', 1, 2, NOW(), NOW()),
('丰田', 1, 3, NOW(), NOW()),
('大众', 1, 4, NOW(), NOW()),
('本田', 1, 5, NOW(), NOW()),
('宝马', 1, 6, NOW(), NOW()),
('别克', 1, 7, NOW(), NOW()),
('理想', 1, 8, NOW(), NOW());

INSERT INTO car_type (name, status, sort, created_at, updated_at) VALUES
('轿车', 1, 1, NOW(), NOW()),
('SUV', 1, 2, NOW(), NOW()),
('MPV', 1, 3, NOW(), NOW()),
('商务车', 1, 4, NOW(), NOW()),
('新能源', 1, 5, NOW(), NOW());

INSERT INTO banner (title, image_url, link_url, sort, status, created_at, updated_at) VALUES
('春季自驾出游季', 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1600&q=80', '/cars?keyword=自驾', 1, 1, NOW(), NOW()),
('新能源专享特惠', 'https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=1600&q=80', '/cars?energyType=纯电', 2, 1, NOW(), NOW()),
('商务接待安心租', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=1600&q=80', '/cars?typeName=商务车', 3, 1, NOW(), NOW());

INSERT INTO notice (title, content, image_url, sort, status, publish_at, created_at, updated_at) VALUES
('清明假期用车提醒', '清明假期订单较多，请至少提前一天完成证件审核和下单支付。', NULL, 1, 1, NOW(), NOW(), NOW()),
('新增虹桥夜间还车服务', '虹桥枢纽店已开通夜间还车服务，22:00前均可办理。', NULL, 2, 1, NOW(), NOW(), NOW()),
('押金退还说明', '订单完成后，若无超时或违章记录，押金将在人工复核后退还。', NULL, 3, 1, NOW(), NOW(), NOW());

INSERT INTO car (
  brand, type_name, model, plate_number, color, transmission, energy_type, seat_count,
  daily_price, deposit, status, station_id, hot_flag, cover_image, description, created_at, updated_at
) VALUES
('比亚迪', '轿车', '秦PLUS DM-i', '沪A10001', '白色', '自动挡', '混动', 5, 218.00, 1000.00, 'AVAILABLE', 1, 1, 'https://images.unsplash.com/photo-1550355291-bbee04a92027?auto=format&fit=crop&w=800&q=80', '市区通勤热门车型，油耗低，续航稳定。', NOW(), NOW()),
('特斯拉', '新能源', 'Model 3', '沪A10002', '黑色', '自动挡', '纯电', 5, 399.00, 2000.00, 'AVAILABLE', 2, 1, 'https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=800&q=80', '适合商务出行和城市短租。', NOW(), NOW()),
('丰田', '轿车', '凯美瑞', '沪A10003', '银色', '自动挡', '燃油', 5, 298.00, 1500.00, 'AVAILABLE', 3, 1, 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=800&q=80', '舒适型中级车，家庭和商务两相宜。', NOW(), NOW()),
('大众', '轿车', '朗逸', '沪A10004', '灰色', '手动挡', '燃油', 5, 168.00, 800.00, 'AVAILABLE', 1, 0, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80', '经济实用，适合预算有限用户。', NOW(), NOW()),
('本田', '轿车', '雅阁', '沪A10005', '蓝色', '自动挡', '燃油', 5, 308.00, 1500.00, 'AVAILABLE', 2, 0, 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80', '空间充足，适合中长途出行。', NOW(), NOW()),
('宝马', '轿车', '320Li', '沪A10006', '白色', '自动挡', '燃油', 5, 458.00, 2500.00, 'AVAILABLE', 3, 0, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80', '豪华品牌轿车，适合商务接待。', NOW(), NOW()),
('丰田', 'SUV', 'RAV4荣放', '沪A10007', '白色', '自动挡', '混动', 5, 318.00, 1500.00, 'AVAILABLE', 3, 1, 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?auto=format&fit=crop&w=800&q=80', '省油耐用，适合郊游和露营。', NOW(), NOW()),
('本田', 'SUV', 'CR-V', '沪A10008', '银色', '自动挡', '混动', 5, 328.00, 1600.00, 'AVAILABLE', 4, 0, 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80', '舒适省心，后备厢空间充足。', NOW(), NOW()),
('比亚迪', '新能源', '宋PLUS EV', '沪A10009', '蓝色', '自动挡', '纯电', 5, 298.00, 1500.00, 'AVAILABLE', 5, 1, 'https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=800&q=80', '纯电SUV，适合城市与周边出行。', NOW(), NOW()),
('别克', 'MPV', 'GL8', '沪A10010', '黑色', '自动挡', '燃油', 7, 558.00, 3000.00, 'AVAILABLE', 1, 0, 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?auto=format&fit=crop&w=800&q=80', '热门商务MPV，机场接送常用车型。', NOW(), NOW()),
('理想', '新能源', 'L7', '沪A10011', '黑色', '自动挡', '混动', 5, 588.00, 3200.00, 'AVAILABLE', 3, 0, 'https://images.unsplash.com/photo-1542282088-fe8426682b8f?auto=format&fit=crop&w=800&q=80', '家庭长途出行首选，配置丰富。', NOW(), NOW()),
('大众', 'SUV', '途观L', '沪A10012', '灰色', '自动挡', '燃油', 5, 286.00, 1400.00, 'AVAILABLE', 5, 0, 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?auto=format&fit=crop&w=800&q=80', '空间大，适合多人乘坐。', NOW(), NOW()),
('大众', '轿车', '速腾', '沪A10013', '白色', '手动挡', '燃油', 5, 178.00, 900.00, 'AVAILABLE', 4, 0, 'https://images.unsplash.com/photo-1486496572940-2bb2341fdbdf?auto=format&fit=crop&w=800&q=80', '手动挡练手车型，租金亲民。', NOW(), NOW()),
('丰田', '轿车', '卡罗拉双擎', '沪A10014', '银色', '自动挡', '混动', 5, 196.00, 900.00, 'AVAILABLE', 1, 0, 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80', '节能稳定，适合日常代步。', NOW(), NOW()),
('特斯拉', '新能源', 'Model Y', '沪A10015', '白色', '自动挡', '纯电', 5, 468.00, 2500.00, 'AVAILABLE', 2, 1, 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80', '空间和续航均衡，热门新能源车型。', NOW(), NOW()),
('比亚迪', '轿车', '海豹', '沪A10016', '灰色', '自动挡', '纯电', 5, 348.00, 1800.00, 'AVAILABLE', 2, 0, 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80', '年轻化纯电轿车，外观动感。', NOW(), NOW()),
('别克', '商务车', '世纪', '沪A10017', '深蓝色', '自动挡', '燃油', 6, 688.00, 3500.00, 'AVAILABLE', 4, 0, 'https://images.unsplash.com/photo-1519643381401-22c77e60520e?auto=format&fit=crop&w=800&q=80', '高端商务接待专用车型。', NOW(), NOW()),
('本田', 'SUV', '皓影', '沪A10018', '白色', '自动挡', '燃油', 5, 258.00, 1300.00, 'AVAILABLE', 5, 0, 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80', '家用SUV，适合近郊出游。', NOW(), NOW());

INSERT INTO favorite (user_id, car_id, created_at) VALUES
(3, 2, NOW()),
(3, 7, NOW()),
(4, 10, NOW());

INSERT INTO review (user_id, car_id, rating, content, created_at) VALUES
(3, 1, 5, '车辆整洁，门店交付效率很高。', NOW()),
(3, 3, 4, '车型不错，车况稳定。', NOW()),
(6, 9, 5, '纯电续航表现很好，体验满意。', NOW());

INSERT INTO feedback (user_id, title, content, status, reply_content, reply_at, created_at, updated_at) VALUES
(3, '希望增加更多手动挡车型', '目前手动挡可选车型偏少，希望增加经济型手动挡。', 'REPLIED', '已加入四月份采购计划，会逐步补充。', NOW(), NOW(), NOW()),
(4, '能否增加校园取车点', '松江大学城区域用车需求较多，希望增加校园网点。', 'PENDING', NULL, NULL, NOW(), NOW());

INSERT INTO rental_order (
  order_no, user_id, car_id, station_id, return_station_id, start_date, end_date, rental_days,
  rent_amount, deposit_amount, extra_fee, settlement_amount, total_amount, order_status, payment_status, payment_method,
  pickup_address, pickup_longitude, pickup_latitude, return_address, return_longitude, return_latitude,
  trade_no, remark, returned_at, settled_at, created_at, updated_at
) VALUES
('CR2024010001', 3, 3, 3, 3, '2024-01-12', '2024-01-14', 3, 894.00, 1500.00, 0.00, 2394.00, 2394.00, 'COMPLETED', 'PAID', 'ALIPAY', '上海市浦东新区世纪大道88号', '121.5064', '31.2451', '上海市浦东新区世纪大道88号', '121.5064', '31.2451', 'TRADE2024010001', '寒假短途出行', '2024-01-14 18:10:00', '2024-01-14 18:10:00', '2024-01-10 09:20:00', '2024-01-14 18:10:00'),
('CR2024040002', 4, 10, 1, 2, '2024-04-03', '2024-04-05', 3, 1674.00, 3000.00, 0.00, 4674.00, 4674.00, 'COMPLETED', 'PAID', 'ALIPAY', '上海市浦东新区迎宾大道100号', '121.7998', '31.1518', '上海市闵行区申虹路200号', '121.3275', '31.1978', 'TRADE2024040002', '清明商务接待', '2024-04-05 19:00:00', '2024-04-05 19:00:00', '2024-04-01 10:00:00', '2024-04-05 19:00:00'),
('CR2024090003', 6, 7, 3, 4, '2024-09-18', '2024-09-20', 3, 954.00, 1500.00, 318.00, 2772.00, 2454.00, 'COMPLETED', 'PAID', 'ALIPAY', '上海市浦东新区世纪大道88号', '121.5064', '31.2451', '上海市徐汇区天钥桥路66号', '121.4377', '31.1884', 'TRADE2024090003', '中秋近郊自驾', '2024-09-21 10:00:00', '2024-09-21 10:00:00', '2024-09-15 08:00:00', '2024-09-21 10:00:00'),
('CR2025010004', 3, 1, 1, 1, '2025-01-05', '2025-01-07', 3, 654.00, 1000.00, 0.00, 1654.00, 1654.00, 'COMPLETED', 'PAID', 'ALIPAY', '上海市浦东新区迎宾大道100号', '121.7998', '31.1518', '上海市浦东新区迎宾大道100号', '121.7998', '31.1518', 'TRADE2025010004', '新年探亲', '2025-01-07 20:00:00', '2025-01-07 20:00:00', '2025-01-03 12:00:00', '2025-01-07 20:00:00'),
('CR2025060005', 6, 15, 2, 2, '2025-06-06', '2025-06-08', 3, 1404.00, 2500.00, 0.00, 3904.00, 3904.00, 'COMPLETED', 'PAID', 'ALIPAY', '上海市闵行区申虹路200号', '121.3275', '31.1978', '上海市闵行区申虹路200号', '121.3275', '31.1978', 'TRADE2025060005', '周末新能源体验', '2025-06-08 18:30:00', '2025-06-08 18:30:00', '2025-06-02 10:35:00', '2025-06-08 18:30:00'),
('CR2025110006', 3, 11, 3, 5, '2025-11-20', '2025-11-22', 3, 1764.00, 3200.00, 588.00, 5552.00, 4964.00, 'COMPLETED', 'PAID', 'ALIPAY', '上海市浦东新区世纪大道88号', '121.5064', '31.2451', '上海市松江区文诚路99号', '121.2263', '31.0305', 'TRADE2025110006', '家庭长途自驾', '2025-11-23 11:00:00', '2025-11-23 11:00:00', '2025-11-15 15:50:00', '2025-11-23 11:00:00'),
('CR2026030007', 3, 2, 2, 3, '2026-03-10', '2026-03-13', 4, 1596.00, 2000.00, 0.00, 3596.00, 3596.00, 'ONGOING', 'PAID', 'ALIPAY', '上海市闵行区申虹路200号', '121.3275', '31.1978', '上海市浦东新区世纪大道88号', '121.5064', '31.2451', '2026031100000001', '商务出差用车', NULL, NULL, '2026-03-08 09:30:00', '2026-03-11 09:30:00'),
('CR2026040008', 4, 5, 2, 4, '2026-04-20', '2026-04-22', 3, 924.00, 1500.00, 0.00, 2424.00, 2424.00, 'BOOKED', 'UNPAID', 'ALIPAY', '上海市闵行区申虹路200号', '121.3275', '31.1978', '上海市徐汇区天钥桥路66号', '121.4377', '31.1884', NULL, '演示待支付订单', NULL, NULL, '2026-04-16 10:20:00', '2026-04-16 10:20:00'),
('CR2026050009', 6, 17, 4, 1, '2026-05-10', '2026-05-12', 3, 2064.00, 3500.00, 0.00, 5564.00, 5564.00, 'BOOKED', 'UNPAID', 'OFFLINE', '上海市徐汇区天钥桥路66号', '121.4377', '31.1884', '上海市浦东新区迎宾大道100号', '121.7998', '31.1518', NULL, '企业客户待线下确认', NULL, NULL, '2026-05-06 15:40:00', '2026-05-06 15:40:00');

UPDATE car SET status = 'RENTED' WHERE id = 2;
