DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS rental_order;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS car_type;
DROP TABLE IF EXISTS car_brand;
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
  id_card_no VARCHAR(30),
  id_card_front VARCHAR(255),
  id_card_back VARCHAR(255),
  verify_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  verify_remark VARCHAR(255),
  frozen_reason VARCHAR(255),
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
  business_hours VARCHAR(50),
  description VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE car_brand (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE car_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE car (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  brand VARCHAR(50) NOT NULL,
  type_name VARCHAR(50) NOT NULL,
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
  hot_flag TINYINT NOT NULL DEFAULT 0,
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
  return_station_id BIGINT,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  rental_days INT NOT NULL,
  rent_amount DECIMAL(10,2) NOT NULL,
  deposit_amount DECIMAL(10,2) NOT NULL,
  extra_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  settlement_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(10,2) NOT NULL,
  order_status VARCHAR(20) NOT NULL,
  payment_status VARCHAR(20) NOT NULL,
  payment_method VARCHAR(20),
  pickup_address VARCHAR(255),
  pickup_longitude VARCHAR(30),
  pickup_latitude VARCHAR(30),
  return_address VARCHAR(255),
  return_longitude VARCHAR(30),
  return_latitude VARCHAR(30),
  trade_no VARCHAR(64),
  remark VARCHAR(500),
  returned_at DATETIME,
  settled_at DATETIME,
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

CREATE TABLE banner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  link_url VARCHAR(255),
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  image_url VARCHAR(255),
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  publish_at DATETIME,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  reply_content VARCHAR(1000),
  reply_at DATETIME,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);
