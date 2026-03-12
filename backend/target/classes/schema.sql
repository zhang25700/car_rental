DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS rental_order;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS pickup_station;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(64) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email VARCHAR(100),
  role VARCHAR(20) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE pickup_station (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  city VARCHAR(50) NOT NULL,
  address VARCHAR(255) NOT NULL,
  contact_phone VARCHAR(20),
  longitude VARCHAR(30),
  latitude VARCHAR(30),
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE car (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand VARCHAR(50) NOT NULL,
  model VARCHAR(100) NOT NULL,
  plate_number VARCHAR(30) NOT NULL UNIQUE,
  color VARCHAR(30),
  transmission VARCHAR(20),
  energy_type VARCHAR(20),
  seat_count INT,
  daily_price DECIMAL(10,2) NOT NULL,
  deposit DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  station_id BIGINT,
  cover_image VARCHAR(255),
  description VARCHAR(500),
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE rental_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  car_id BIGINT NOT NULL,
  station_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  rental_days INT NOT NULL,
  rent_amount DECIMAL(10,2) NOT NULL,
  deposit_amount DECIMAL(10,2) NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  order_status VARCHAR(20) NOT NULL,
  payment_status VARCHAR(20) NOT NULL,
  pickup_address VARCHAR(255),
  pickup_longitude VARCHAR(30),
  pickup_latitude VARCHAR(30),
  trade_no VARCHAR(64),
  remark VARCHAR(500),
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  car_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL
);

CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  car_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(500) NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL
);

INSERT INTO sys_user (username, password, real_name, phone, email, role, status, created_at, updated_at) VALUES
('admin', 'bfd488b5722378eb192076d7a9cacefe', '系统管理员', '13800000000', 'admin@carrental.com', 'ADMIN', 1, NOW(), NOW()),
('zhangsan', 'bfd488b5722378eb192076d7a9cacefe', '张三', '13900000001', 'zhangsan@example.com', 'CUSTOMER', 1, NOW(), NOW()),
('lisi', 'bfd488b5722378eb192076d7a9cacefe', '李四', '13900000002', 'lisi@example.com', 'CUSTOMER', 1, NOW(), NOW()),
('wangwu', 'bfd488b5722378eb192076d7a9cacefe', '王五', '13900000003', 'wangwu@example.com', 'CUSTOMER', 1, NOW(), NOW());

INSERT INTO pickup_station (name, city, address, contact_phone, longitude, latitude, status, created_at, updated_at) VALUES
('浦东机场店', '上海', '浦东新区机场大道100号', '021-10000001', '121.7998', '31.1518', 1, NOW(), NOW()),
('虹桥枢纽店', '上海', '闵行区申虹路200号', '021-10000002', '121.3275', '31.1978', 1, NOW(), NOW()),
('陆家嘴店', '上海', '浦东新区世纪大道88号', '021-10000003', '121.5064', '31.2451', 1, NOW(), NOW()),
('徐家汇店', '上海', '徐汇区天钥桥路66号', '021-10000004', '121.4377', '31.1884', 1, NOW(), NOW()),
('松江大学城店', '上海', '松江区文诚路99号', '021-10000005', '121.2263', '31.0305', 1, NOW(), NOW());

INSERT INTO car (brand, model, plate_number, color, transmission, energy_type, seat_count, daily_price, deposit, status, station_id, cover_image, description, created_at, updated_at) VALUES
('比亚迪', '秦PLUS DM-i', '沪A10001', '白色', '自动挡', '混动', 5, 218.00, 1000.00, 'AVAILABLE', 1, 'https://images.unsplash.com/photo-1550355291-bbee04a92027?auto=format&fit=crop&w=800&q=80', '适合城市通勤，油耗低，配置均衡。', NOW(), NOW()),
('特斯拉', 'Model 3', '沪A10002', '黑色', '自动挡', '纯电', 5, 399.00, 2000.00, 'AVAILABLE', 2, 'https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=800&q=80', '适合商务出行的纯电轿车。', NOW(), NOW()),
('丰田', '凯美瑞', '沪A10003', '银色', '自动挡', '燃油', 5, 298.00, 1500.00, 'AVAILABLE', 3, 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=800&q=80', '经典舒适型中级轿车。', NOW(), NOW()),
('大众', '朗逸', '沪A10004', '灰色', '手动挡', '燃油', 5, 168.00, 800.00, 'AVAILABLE', 1, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80', '经济实用，适合短途代步。', NOW(), NOW()),
('本田', '雅阁', '沪A10005', '蓝色', '自动挡', '燃油', 5, 308.00, 1500.00, 'AVAILABLE', 2, 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80', '空间宽敞，适合商务接待。', NOW(), NOW()),
('宝马', '320Li', '沪A10006', '白色', '自动挡', '燃油', 5, 458.00, 2500.00, 'AVAILABLE', 3, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80', '豪华品牌轿车，驾驶质感出色。', NOW(), NOW()),
('奥迪', 'A4L', '沪A10007', '黑色', '自动挡', '燃油', 5, 438.00, 2500.00, 'AVAILABLE', 4, 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?auto=format&fit=crop&w=800&q=80', '商务与家用兼顾。', NOW(), NOW()),
('奔驰', 'C260L', '沪A10008', '黑色', '自动挡', '混动', 5, 498.00, 3000.00, 'AVAILABLE', 4, 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80', '舒适性和品牌感兼具。', NOW(), NOW()),
('蔚来', 'ET5', '沪A10009', '灰色', '自动挡', '纯电', 5, 468.00, 2500.00, 'AVAILABLE', 5, 'https://images.unsplash.com/photo-1619767886558-efdc259cde1a?auto=format&fit=crop&w=800&q=80', '智能化配置较高，续航表现不错。', NOW(), NOW()),
('小鹏', 'P7', '沪A10010', '绿色', '自动挡', '纯电', 5, 388.00, 2200.00, 'AVAILABLE', 5, 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80', '造型运动，适合年轻用户。', NOW(), NOW()),
('吉利', '星越L', '沪A10011', '白色', '自动挡', '燃油', 5, 268.00, 1200.00, 'AVAILABLE', 1, 'https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=800&q=80', '家用SUV，空间大。', NOW(), NOW()),
('哈弗', 'H6', '沪A10012', '红色', '手动挡', '燃油', 5, 228.00, 1200.00, 'AVAILABLE', 2, 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80', '热门SUV，性价比较高。', NOW(), NOW()),
('丰田', 'RAV4荣放', '沪A10013', '白色', '自动挡', '混动', 5, 318.00, 1500.00, 'AVAILABLE', 3, 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?auto=format&fit=crop&w=800&q=80', '省油耐用，适合郊游。', NOW(), NOW()),
('本田', 'CR-V', '沪A10014', '银色', '自动挡', '混动', 5, 328.00, 1600.00, 'AVAILABLE', 4, 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80', '油耗友好，乘坐舒适。', NOW(), NOW()),
('比亚迪', '宋PLUS EV', '沪A10015', '蓝色', '自动挡', '纯电', 5, 298.00, 1500.00, 'AVAILABLE', 5, 'https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=800&q=80', '纯电SUV，适合城市与周边出行。', NOW(), NOW()),
('别克', 'GL8', '沪A10016', '黑色', '自动挡', '燃油', 7, 558.00, 3000.00, 'AVAILABLE', 1, 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?auto=format&fit=crop&w=800&q=80', '商务接待常用7座MPV。', NOW(), NOW()),
('丰田', '赛那', '沪A10017', '灰色', '自动挡', '混动', 7, 628.00, 3500.00, 'AVAILABLE', 2, 'https://images.unsplash.com/photo-1519643381401-22c77e60520e?auto=format&fit=crop&w=800&q=80', '适合家庭和机场接送。', NOW(), NOW()),
('福特', '全顺', '沪A10018', '白色', '手动挡', '燃油', 9, 688.00, 4000.00, 'AVAILABLE', 3, 'https://images.unsplash.com/photo-1489824904134-891ab64532f1?auto=format&fit=crop&w=800&q=80', '多人出行和团队活动首选。', NOW(), NOW()),
('MINI', 'Cooper', '沪A10019', '黄色', '自动挡', '燃油', 4, 368.00, 1800.00, 'AVAILABLE', 4, 'https://images.unsplash.com/photo-1525609004556-c46c7d6cf023?auto=format&fit=crop&w=800&q=80', '个性小车，适合情侣出游。', NOW(), NOW()),
('马自达', 'CX-5', '沪A10020', '红色', '自动挡', '燃油', 5, 278.00, 1300.00, 'AVAILABLE', 5, 'https://images.unsplash.com/photo-1517524008697-84bbe3c3fd98?auto=format&fit=crop&w=800&q=80', '操控感不错的家用SUV。', NOW(), NOW()),
('雷克萨斯', 'ES200', '沪A10021', '棕色', '自动挡', '燃油', 5, 528.00, 3000.00, 'AVAILABLE', 2, 'https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=800&q=80', '安静舒适，适合高端接待。', NOW(), NOW()),
('现代', '伊兰特', '沪A10022', '白色', '手动挡', '燃油', 5, 158.00, 800.00, 'AVAILABLE', 1, 'https://images.unsplash.com/photo-1493238792000-8113da705763?auto=format&fit=crop&w=800&q=80', '入门经济型轿车。', NOW(), NOW()),
('起亚', 'K5', '沪A10023', '黑色', '自动挡', '燃油', 5, 208.00, 900.00, 'AVAILABLE', 3, 'https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?auto=format&fit=crop&w=800&q=80', '外观时尚，适合城市使用。', NOW(), NOW()),
('雪佛兰', '迈锐宝XL', '沪A10024', '蓝色', '自动挡', '燃油', 5, 228.00, 1000.00, 'AVAILABLE', 4, 'https://images.unsplash.com/photo-1486496572940-2bb2341fdbdf?auto=format&fit=crop&w=800&q=80', '适合长途和家庭出行。', NOW(), NOW()),
('日产', '轩逸', '沪A10025', '银色', '手动挡', '燃油', 5, 148.00, 800.00, 'AVAILABLE', 5, 'https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?auto=format&fit=crop&w=800&q=80', '省油舒适，租赁热门车型。', NOW(), NOW()),
('比亚迪', '海豹', '沪A10026', '灰色', '自动挡', '纯电', 5, 348.00, 1800.00, 'AVAILABLE', 2, 'https://images.unsplash.com/photo-1553440569-bcc63803a83d?auto=format&fit=crop&w=800&q=80', '运动风格纯电轿车。', NOW(), NOW()),
('理想', 'L7', '沪A10027', '黑色', '自动挡', '混动', 5, 588.00, 3200.00, 'AVAILABLE', 3, 'https://images.unsplash.com/photo-1542282088-fe8426682b8f?auto=format&fit=crop&w=800&q=80', '适合家庭长途出游。', NOW(), NOW()),
('深蓝', 'S7', '沪A10028', '橙色', '自动挡', '纯电', 5, 336.00, 1800.00, 'AVAILABLE', 4, 'https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=800&q=80', '新能源SUV，外观年轻。', NOW(), NOW()),
('大众', '途观L', '沪A10029', '灰色', '自动挡', '燃油', 5, 286.00, 1400.00, 'AVAILABLE', 5, 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?auto=format&fit=crop&w=800&q=80', '空间宽敞，适合多人乘坐。', NOW(), NOW()),
('丰田', '卡罗拉', '沪A10030', '白色', '手动挡', '混动', 5, 196.00, 900.00, 'AVAILABLE', 1, 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=800&q=80', '适合预算有限但追求省油的用户。', NOW(), NOW());

INSERT INTO rental_order (order_no, user_id, car_id, station_id, start_date, end_date, rental_days, rent_amount, deposit_amount, total_amount, order_status, payment_status, pickup_address, pickup_longitude, pickup_latitude, trade_no, remark, created_at, updated_at) VALUES
('CR2026000001', 2, 2, 2, '2026-03-10', '2026-03-13', 4, 1596.00, 2000.00, 3596.00, 'ONGOING', 'PAID', '上海市闵行区申虹路200号', '121.3275', '31.1978', '2026031100000001', '商务出差用车', NOW(), NOW()),
('CR2026000002', 3, 6, 3, '2026-03-15', '2026-03-17', 3, 1374.00, 2500.00, 3874.00, 'BOOKED', 'UNPAID', '上海市浦东新区世纪大道88号', '121.5064', '31.2451', NULL, '周末自驾出游', NOW(), NOW()),
('CR2026000003', 4, 16, 1, '2026-03-20', '2026-03-22', 3, 1674.00, 3000.00, 4674.00, 'BOOKED', 'PAID', '上海市浦东新区机场大道100号', '121.7998', '31.1518', '2026031100000002', '团队机场接送', NOW(), NOW());

UPDATE car SET status = 'RENTED' WHERE id = 2;
