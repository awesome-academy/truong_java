-- Tầng 1: Root
INSERT INTO categories (id, parent_id, name, slug, description, is_active, created_at)
VALUES
    ('11111111-0000-0000-0000-000000000001', NULL, 'Du lịch Miền Bắc',  'du-lich-mien-bac',   'Khám phá vẻ đẹp vùng đất phía Bắc Việt Nam',  true, NOW()),
    ('11111111-0000-0000-0000-000000000002', NULL, 'Du lịch Miền Trung', 'du-lich-mien-trung', 'Hành trình qua các di sản văn hóa miền Trung', true, NOW()),
    ('11111111-0000-0000-0000-000000000003', NULL, 'Du lịch Miền Nam',   'du-lich-mien-nam',   'Trải nghiệm sông nước và nắng vàng phương Nam', true, NOW()),
    ('11111111-0000-0000-0000-000000000004', NULL, 'Du lịch Quốc tế',   'du-lich-quoc-te',    'Khám phá thế giới cùng SUN Travel',             true, NOW());

-- Tầng 2: Miền Bắc
INSERT INTO categories (id, parent_id, name, slug, description, is_active, created_at)
VALUES
    ('22222222-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001', 'Hà Nội',    'ha-noi',    'Thủ đô ngàn năm văn hiến',            true, NOW()),
    ('22222222-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000001', 'Hạ Long',   'ha-long',   'Kỳ quan thiên nhiên thế giới',         true, NOW()),
    ('22222222-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000001', 'Sapa',      'sapa',      'Thị trấn sương mù và ruộng bậc thang', true, NOW()),
    ('22222222-0000-0000-0000-000000000004', '11111111-0000-0000-0000-000000000001', 'Ninh Bình', 'ninh-binh', 'Hạ Long trên cạn – Tràng An, Tam Cốc', true, NOW());

-- Tầng 2: Miền Trung
INSERT INTO categories (id, parent_id, name, slug, description, is_active, created_at)
VALUES
    ('22222222-0000-0000-0000-000000000005', '11111111-0000-0000-0000-000000000002', 'Đà Nẵng',   'da-nang',   'Thành phố đáng sống bên bờ biển Mỹ Khê', true, NOW()),
    ('22222222-0000-0000-0000-000000000006', '11111111-0000-0000-0000-000000000002', 'Hội An',    'hoi-an',    'Phố cổ đèn lồng di sản UNESCO',           true, NOW()),
    ('22222222-0000-0000-0000-000000000007', '11111111-0000-0000-0000-000000000002', 'Huế',       'hue',       'Cố đô với hệ thống lăng tẩm hoàng gia',   true, NOW()),
    ('22222222-0000-0000-0000-000000000008', '11111111-0000-0000-0000-000000000002', 'Nha Trang', 'nha-trang', 'Thành phố biển nắng vàng cát trắng',      true, NOW());

-- Tầng 2: Miền Nam
INSERT INTO categories (id, parent_id, name, slug, description, is_active, created_at)
VALUES
    ('22222222-0000-0000-0000-000000000009', '11111111-0000-0000-0000-000000000003', 'TP. Hồ Chí Minh', 'tp-ho-chi-minh', 'Thành phố năng động nhất Việt Nam',            true, NOW()),
    ('22222222-0000-0000-0000-000000000010', '11111111-0000-0000-0000-000000000003', 'Phú Quốc',        'phu-quoc',       'Đảo ngọc biển Tây với bãi biển hoang sơ',      true, NOW()),
    ('22222222-0000-0000-0000-000000000011', '11111111-0000-0000-0000-000000000003', 'Đà Lạt',          'da-lat',         'Thành phố ngàn hoa trong sương mù cao nguyên', true, NOW()),
    ('22222222-0000-0000-0000-000000000012', '11111111-0000-0000-0000-000000000003', 'Miền Tây',        'mien-tay',       'Sông nước, chợ nổi, vườn trái cây miền Tây',   true, NOW());

-- Tầng 2: Quốc tế
INSERT INTO categories (id, parent_id, name, slug, description, is_active, created_at)
VALUES
    ('22222222-0000-0000-0000-000000000013', '11111111-0000-0000-0000-000000000004', 'Thái Lan',  'thai-lan',  'Xứ sở chùa vàng – Bangkok, Phuket, Chiang Mai', true, NOW()),
    ('22222222-0000-0000-0000-000000000014', '11111111-0000-0000-0000-000000000004', 'Nhật Bản',  'nhat-ban',  'Đất nước mặt trời mọc – Tokyo, Kyoto, Osaka',   true, NOW()),
    ('22222222-0000-0000-0000-000000000015', '11111111-0000-0000-0000-000000000004', 'Hàn Quốc',  'han-quoc',  'Seoul, Jeju – xứ sở kimchi và K-pop',           true, NOW()),
    ('22222222-0000-0000-0000-000000000016', '11111111-0000-0000-0000-000000000004', 'Singapore', 'singapore', 'Đảo quốc sư tử – trung tâm tài chính châu Á',  true, NOW());
