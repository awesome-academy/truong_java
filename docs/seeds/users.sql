-- Admin password : Admin@123
-- User password  : User@123

INSERT INTO users (id, full_name, email, phone, password_hash, role, is_active, created_at, updated_at)
VALUES
    (uuid_generate_v4(), 'Super Admin',    'admin@sun.com',       '0901000001', crypt('Admin@123', gen_salt('bf', 10)), 'ADMIN', true, NOW(), NOW()),
    (uuid_generate_v4(), 'Nguyễn Văn An', 'an.nguyen@gmail.com', '0912345601', crypt('User@123',  gen_salt('bf', 10)), 'USER',  true, NOW(), NOW()),
    (uuid_generate_v4(), 'Trần Thị Bình', 'binh.tran@gmail.com', '0912345602', crypt('User@123',  gen_salt('bf', 10)), 'USER',  true, NOW(), NOW()),
    (uuid_generate_v4(), 'Lê Minh Châu',  'chau.le@gmail.com',   '0912345603', crypt('User@123',  gen_salt('bf', 10)), 'USER',  true, NOW(), NOW());
