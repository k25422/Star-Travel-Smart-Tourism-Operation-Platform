INSERT INTO destinations (id, name, province, theme, description, cover_image, rating, popularity_score) VALUES
(1, '张家界天空森林', '湖南', '山岳', '奇峰石柱、玻璃桥和云雾峡谷构成沉浸式山地旅行体验。', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', 4.8, 97),
(2, '三亚珊瑚海岸', '海南', '海岛', '温暖海滩、潜水线路和适合亲子度假的海岛旅程。', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 4.7, 92),
(3, '敦煌丝路古境', '甘肃', '文化', '沙漠落日、石窟艺术和夜市风情串起丝路文化体验。', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80', 4.9, 88),
(4, '杭州西湖慢旅', '浙江', '休闲', '湖畔骑行、茶村漫步和周末轻度假的慢节奏旅程。', 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1200&q=80', 4.6, 85),
(5, '北京长城古道', '北京', '文化', '长城徒步、胡同夜游和非遗手作结合的历史文化路线。', 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?auto=format&fit=crop&w=1200&q=80', 4.8, 94),
(6, '成都熊猫慢城', '四川', '亲子', '熊猫基地、川菜体验和城市茶馆串联成轻松亲子假期。', 'https://images.unsplash.com/photo-1528360983277-13d401cdc186?auto=format&fit=crop&w=1200&q=80', 4.7, 90),
(7, '丽江雪山古城', '云南', '度假', '雪山观景、古城漫游和纳西文化体验适合深度慢游。', 'https://images.unsplash.com/photo-1513415564515-763d91423bdd?auto=format&fit=crop&w=1200&q=80', 4.8, 91),
(8, '桂林漓江山水', '广西', '山水', '漓江游船、阳朔骑行和喀斯特峰林构成经典山水旅行。', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80', 4.7, 89),
(9, '厦门鼓浪屿海风', '福建', '海岛', '海边步道、音乐街区和闽南小吃适合情侣与轻旅行人群。', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 4.5, 83),
(10, '哈尔滨冰雪童话', '黑龙江', '冰雪', '冰雪大世界、中央大街和俄式建筑打造冬季主题旅行。', 'https://images.unsplash.com/photo-1483921020237-2ff51e8e4b22?auto=format&fit=crop&w=1200&q=80', 4.6, 86);

INSERT INTO travel_routes (id, destination_id, title, duration_days, price, available_seats, departure_city, departure_date, guide_name, highlight) VALUES
(1, 1, '张家界天门奇峰深度游', 5, 3699.00, 18, '上海', '2026-07-12', '陈宇', '玻璃桥、峡谷徒步与日出摄影一次体验'),
(2, 2, '三亚珊瑚湾海岛假期', 4, 4299.00, 24, '广州', '2026-07-18', '林楠', '潜水课程、游艇日落和海鲜市场打卡'),
(3, 3, '敦煌丝路文化探索营', 6, 5199.00, 16, '西安', '2026-08-03', '马睿', '莫高窟研学、沙漠露营与驼队古道体验'),
(4, 4, '西湖茶村周末轻旅行', 3, 1899.00, 32, '南京', '2026-06-27', '赵青', '茶村午餐、湖畔骑行和精品民宿体验'),
(5, 5, '长城日落与胡同夜游', 4, 3299.00, 22, '天津', '2026-07-05', '周航', '八达岭长城轻徒步、胡同夜游和京味餐桌'),
(6, 6, '成都熊猫亲子美食营', 4, 2999.00, 28, '重庆', '2026-07-20', '唐佳', '熊猫基地、川菜课堂、宽窄巷子慢游'),
(7, 7, '丽江雪山古城度假线', 5, 4599.00, 14, '昆明', '2026-08-12', '和云', '玉龙雪山观景、古城旅拍和纳西文化体验'),
(8, 8, '桂林漓江山水摄影游', 4, 2699.00, 20, '长沙', '2026-07-30', '陆川', '漓江游船、阳朔骑行和山水摄影指导'),
(9, 9, '厦门鼓浪屿海风周末', 3, 2199.00, 26, '福州', '2026-08-08', '许晴', '鼓浪屿漫步、沙坡尾市集和海边落日'),
(10, 10, '哈尔滨冰雪童话之旅', 5, 3999.00, 12, '沈阳', '2026-12-18', '郭冬', '冰雪大世界、中央大街和俄式建筑打卡'),
(11, 2, '三亚家庭海岛轻奢游', 5, 5599.00, 18, '深圳', '2026-08-22', '林楠', '亲子酒店、沙滩营地、免税店和海上项目'),
(12, 5, '北京研学文化精品线', 5, 3899.00, 30, '济南', '2026-09-03', '周航', '故宫主题讲解、长城研学和非遗手作课堂');

INSERT INTO bookings (route_id, traveler_name, phone, travelers, total_amount, status, created_at) VALUES
(1, '王明', '13800138001', 2, 7398.00, 'CONFIRMED', CURRENT_TIMESTAMP()),
(2, '刘妍', '13800138002', 3, 12897.00, 'PENDING', CURRENT_TIMESTAMP()),
(4, '孙伟', '13800138003', 1, 1899.00, 'CONFIRMED', CURRENT_TIMESTAMP()),
(5, '李娜', '13800138004', 2, 6598.00, 'CONFIRMED', CURRENT_TIMESTAMP()),
(7, '陈晨', '13800138005', 2, 9198.00, 'PENDING', CURRENT_TIMESTAMP()),
(8, '赵阳', '13800138006', 4, 10796.00, 'CONFIRMED', CURRENT_TIMESTAMP()),
(10, '何雪', '13800138007', 2, 7998.00, 'CANCELLED', CURRENT_TIMESTAMP()),
(12, '吴迪', '13800138008', 3, 11697.00, 'CONFIRMED', CURRENT_TIMESTAMP());
