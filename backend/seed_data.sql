-- ============================================================
-- Seed Data for Đặt Món Food Delivery API
-- ============================================================
-- Dữ liệu mẫu cho hệ thống đặt món ăn trực tuyến
-- ============================================================

-- ============================================================
-- 1. CATEGORY - Danh mục món ăn
-- ============================================================
INSERT INTO Category (name, type, description) VALUES
('Cơm', 'main', 'Các món cơm Việt Nam'),
('Phở', 'main', 'Phở truyền thống Việt Nam'),
('Bún', 'main', 'Các loại bún'),
('Bánh mì', 'snack', 'Bánh mì các loại'),
('Gỏi cuốn', 'appetizer', 'Gỏi cuốn tươi'),
('Nước giải khát', 'drink', 'Các loại nước uống'),
('Tráng miệng', 'dessert', 'Bánh ngọt và kem'),
('Đồ ăn nhanh', 'fastfood', 'Burger, pizza, gà rán');

-- ============================================================
-- 2. FAQ - Câu hỏi thường gặp
-- ============================================================
-- Đảm bảo bảng FAQ của bạn có các cột: question, answer, isActive
INSERT INTO FAQ (question, answer, isactive) VALUES 
(
    'Tại sao đơn hàng của tôi giao chậm hơn dự kiến?', 
    'Chào bạn, rất xin lỗi vì sự chậm trễ này! Thời gian giao hàng có thể bị ảnh hưởng do thời tiết, kẹt xe hoặc nhà hàng đang quá tải. Bạn có thể theo dõi vị trí thực tế của tài xế trên bản đồ trong mục "Chi tiết đơn hàng" hoặc gọi điện trực tiếp cho tài xế để được hỗ trợ nhanh nhất nhé.', 
    TRUE
),
(
    'Tôi nhận thiếu món hoặc sai món thì phải làm sao?', 
    'Rất xin lỗi bạn về sự cố này! Bạn vui lòng chụp ảnh món ăn nhận được và nhấn vào nút "Gửi khiếu nại" ngay trong chi tiết đơn hàng. Chúng tôi sẽ kiểm tra và thực hiện hoàn tiền phần món thiếu/sai vào Ví của bạn hoặc yêu cầu nhà hàng giao bù ngay lập tức.', 
    TRUE
),
(
    'Tôi có thể hủy đơn hàng sau khi đã đặt không?', 
    'Bạn có thể tự hủy đơn trong vòng 2 phút kể từ khi đặt. Sau thời gian này, nếu nhà hàng chưa xác nhận chế biến, bạn có thể gọi tổng đài để hỗ trợ. Tuy nhiên, nếu nhà hàng đã bắt đầu nấu món, ứng dụng rất tiếc không thể hỗ trợ hủy đơn để đảm bảo quyền lợi cho cửa hàng.', 
    TRUE
),
(
    'Làm thế nào để áp dụng mã giảm giá/Voucher?', 
    'Tại màn hình "Giỏ hàng", bạn nhấn vào mục "Chọn khuyến mãi". Danh sách các Voucher khả dụng sẽ hiện ra, bạn chỉ cần chọn mã phù hợp nhất và nhấn "Áp dụng". Lưu ý kiểm tra điều kiện về giá trị đơn hàng tối thiểu và hình thức thanh toán của mã nhé!', 
    TRUE
),
(
    'Phí giao hàng được tính như thế nào?', 
    'Phí giao hàng được tính tự động dựa trên khoảng cách từ nhà hàng đến địa chỉ của bạn. Vào các thời điểm mưa lớn hoặc giờ cao điểm (trưa/tối), phí có thể tăng nhẹ để hỗ trợ tài xế di chuyển vất vả hơn. Bạn có thể săn các mã "FreeShip" trong mục Khuyến mãi để tiết kiệm chi phí nhé.', 
    TRUE
),
(
    'Làm sao để thay đổi địa chỉ giao hàng sau khi đặt?', 
    'Nếu tài xế chưa lấy hàng từ quán, bạn hãy gọi điện ngay cho tài xế để nhờ hỗ trợ giao sang địa chỉ mới (nếu gần). Nếu khoảng cách quá xa, bạn vui lòng liên hệ Tổng đài để được hướng dẫn cụ thể. Lưu ý: Thay đổi địa chỉ có thể làm phát sinh thêm phí vận chuyển.', 
    TRUE
);
-- ============================================================
-- 3. USER - Người dùng
-- ============================================================
-- Mật khẩu cho tất cả user: '1'
-- ============================================================
INSERT INTO "User" (name, username, email, password, phone, role) VALUES
('Nguyễn Huy Trung', 'Trung', 'Trung@example.com', '$2b$12$KxS5VITdjjdca3pEpNw0ieO..w5SCDqw.gHVR6kVQDQc4L6I4eEQi', '0901234567', 'customer'),
('Trần Thị Bình', 'b', 'binh@example.com', '$2b$12$KxS5VITdjjdca3pEpNw0ieO..w5SCDqw.gHVR6kVQDQc4L6I4eEQi', '0912345678', 'customer'),
('Lê Hoàng Cường', 'c', 'cuong@example.com', '$2b$12$KxS5VITdjjdca3pEpNw0ieO..w5SCDqw.gHVR6kVQDQc4L6I4eEQi', '0923456789', 'customer'),
('Phạm Thị D', 'd', 'dung@example.com', '$2b$12$KxS5VITdjjdca3pEpNw0ieO..w5SCDqw.gHVR6kVQDQc4L6I4eEQi', '0934567890', 'customer'),
('Hoàng E', 'e', 'em@example.com', '$2b$12$KxS5VITdjjdca3pEpNw0ieO..w5SCDqw.gHVR6kVQDQc4L6I4eEQi', '0945678901', 'customer'),
('Nguyễn A', 'a', 'a@datmon.com', '$2b$12$KxS5VITdjjdca3pEpNw0ieO..w5SCDqw.gHVR6kVQDQc4L6I4eEQi', '0900000000', 'customer');

-- ============================================================
-- 4. SHIPPER - Người giao hàng
-- ============================================================
INSERT INTO Shipper (name, phone, rating, description, status) VALUES
('Nguyễn Văn Ship', '0956789012', 5, 'Giao hàng nhanh, nhiệt tình', 'active'),
('Trần Văn Giao', '0967890123', 4, 'Giao hàng đúng giờ', 'active'),
('Lê Văn Vận', '0978901234', 5, 'Nhiệt tình, vui vẻ', 'active'),
('Phạm Văn Chuyển', '0989012345', 4, 'Giao hàng cẩn thận', 'active'),
('Hoàng Văn Đưa', '0990123456', 5, 'Phục vụ tốt', 'active');

-- ============================================================
-- 5. PROMOTION - Mã giảm giá
-- ============================================================
INSERT INTO Promotion (code, discounttype, discountvalue, expiredate, minordervalue, status) VALUES
('WELCOME10', 'percentage', 10, '2025-12-31', 100000, 'active'),
('FREESHIP', 'fixed', 20000, '2025-12-31', 150000, 'active'),
('SUMMER20', 'percentage', 20, '2025-09-30', 200000, 'active'),
('NEWUSER', 'fixed', 50000, '2025-12-31', 300000, 'active'),
('COMBO15', 'percentage', 15, '2025-12-31', 250000, 'active');

-- ============================================================
-- 6. RESTAURANT - Nhà hàng
-- ============================================================
INSERT INTO Restaurant (name, image_url, address, rating, open_time, close_time, phone_number, status, description) VALUES
('Quán Phở Hà Nội', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/8e26a028-6ebd-4bd8-9a34-9ecc34094c5e.jpg', '123 Nguyễn Huệ, Quận 1, TP.HCM', 5, '06:00:00', '22:00:00', '0281234567', 'open', 'Phở bò truyền thống Hà Nội hơn 50 năm'),
('Quán Cơm HIHI', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/6da75fd4-fccb-4693-a737-e4bdd6a56cbc.jpg', '456 Lê Lợi, Quận 3, TP.HCM', 4, '07:00:00', '21:00:00', '0282345678', 'open', 'Cơm tấm sườn bì chả ngon nhất Sài Gòn'),
('Quán Bún Cô Hóa', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/92def00a-ed14-435c-b664-9d809ded8528.jpg', '789 Trần Hưng Đạo, Quận 5, TP.HCM', 5, '08:00:00', '20:00:00', '0283456789', 'open', 'Bún bò Huế cay nồng đúng vị'),
('Quán Bánh Mì B', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/3ef837b0-edc0-4d33-aa2f-270b3a70137a.jpg', '26 Lê Thị Riêng, Quận 1, TP.HCM', 5, '06:00:00', '23:00:00', '0284567890', 'open', 'Bánh mì nổi tiếng nhất Sài Gòn'),
('Quán Gỏi Cuốn A','https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/0d29ae36-c1b8-4990-9fa1-70c026c2b4a9.jpg', '15 Nguyễn Trãi, Quận 5, TP.HCM', 4, '10:00:00', '22:00:00', '0285678901', 'open', 'Gỏi cuốn tươi ngon, nước chấm đậm đà'),
('Quán Pizza Hut', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/0b37361e-33ff-4fe1-b9bb-c148c7258ebc.jpg', '100 Nguyễn Đình Chiểu, Quận 3, TP.HCM', 4, '10:00:00', '23:00:00', '0286789012', 'open', 'Pizza Ý chính hiệu'),
('Quán KFC', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/50242bcf-bda2-40fe-ad42-7af506c5f1cb.jpg', '200 Cách Mạng Tháng 8, Quận 10, TP.HCM', 4, '09:00:00', '22:00:00', '0287890123', 'open', 'Gà rán Kentucky nổi tiếng thế giới');

-- ============================================================
-- 7. MENUITEM - Món ăn
-- ============================================================
INSERT INTO MenuItem (name, image_url, price, is_available, description, restaurantid, categoryid) VALUES
-- Quán Phở Hà Nội (RestaurantID = 1) - Đa dạng món
('Phở Bò', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/92864d43-a0d3-4dd7-98b7-994a518f2cd8.jpg', 55000, TRUE, 'Phở bò tái mềm, nước dùng trong', 1, 2),
('Phở Gà', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e989b66c-36a5-47aa-8a5a-886f802c7628.jpg', 50000, TRUE, 'Phở gà ta mềm', 1, 2),
('Cơm Rang', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/18c3b344-54ba-4755-b49d-9ad7f2e58b85.jpg', 45000, TRUE, 'Cơm rang dưa bò', 1, 1),
('Trà Đá', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/af41c1dd-3276-4503-86c3-a2bcb9314f8e.jpg', 10000, TRUE, 'Trà đá mát lạnh', 1, 6),
('Coca Cola', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/44fde9b9-5d9f-4576-986a-369ad052a1bc.jpg', 15000, TRUE, 'Coca Cola lon', 1, 6),


-- Quán Cơm HIHI (RestaurantID = 2) - Đa dạng món
('Cơm Tấm', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/eb663c81-07d9-4aab-942a-ebd74964f456.jpg', 45000, TRUE, 'Cơm tấm sườn bì chả', 2, 1),
('Phở Bò', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/92864d43-a0d3-4dd7-98b7-994a518f2cd8.jpg', 55000, TRUE, 'Phở bò tái mềm, nước dùng trong', 2, 2),
('Phở Gà', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e989b66c-36a5-47aa-8a5a-886f802c7628.jpg', 50000, TRUE, 'Phở gà ta mềm', 2, 2),
('Bún Bò Huế', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/4ee83894-d075-493a-966b-1dbb428b09fe.jpg', 50000, TRUE, 'Bún bò Huế cay nồng', 2, 3),
('Bún Chả', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/32d27074-59a7-4a43-b55b-141bc0bcfee4.jpg', 30000, TRUE, 'Bún chả Hà Nội truyền thống', 2, 3),
('Nước Cam', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/75ee9caa-33b2-4ed4-98c3-2a3b4c87c8ec.jpg', 19000, TRUE, 'Nước cam tươi', 2, 6),
('Pepsi', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e960a620-8002-48fa-a934-af6b21fd4258.jpg', 15000, TRUE, 'Pepsi lon', 2, 6),

-- Quán Bún Cô Hóa (RestaurantID = 3) - Đa dạng món
('Bún Bò Huế', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/4ee83894-d075-493a-966b-1dbb428b09fe.jpg', 50000, TRUE, 'Bún bò Huế cay nồng', 3, 3),
('Bún Chả', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/32d27074-59a7-4a43-b55b-141bc0bcfee4.jpg', 30000, TRUE, 'Bún chả Hà Nội truyền thống', 3, 3),
('Bún Thịt Nướng', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/13ef2c00-a6ab-4dc3-bd8e-15d739891c8e.jpg', 45000, TRUE, 'Bún thịt nướng thơm ngon', 3, 3),
('Bún Trộn', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/20abba75-2e00-4d55-a640-9f8760a140d3.jpg', 40000, TRUE, 'Bún trộn chua ngọt', 3, 3),
('Cơm Gà Nướng', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/9742e158-8508-4350-830e-0522372d408b.jpg', 45000, TRUE, 'Cơm gà nướng thơm lừng', 3, 1),
('Nước Cam', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/75ee9caa-33b2-4ed4-98c3-2a3b4c87c8ec.jpg', 19000, TRUE, 'Nước cam tươi', 3, 6),
('Chè Ba Màu', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/0f7aceba-5527-42fa-bae2-b8bd41857fac.jpg', 27000, TRUE, 'Chè ba màu truyền thống', 3, 7),

-- Quán Bánh Mì Huỳnh Hoa (RestaurantID = 4) - Đa dạng món
('Bánh Mì Thịt Nguội', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/397aa94c-a981-42b8-8983-851fbe65e42b.jpg' , 35000, TRUE, 'Bánh mì thịt nguội đặc biệt', 4, 4),
('Bánh Mì Xíu Mại', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/d74b4191-e271-4f08-84f3-86435a834acc.jpg', 30000, TRUE, 'Bánh mì xíu mại thơm ngon', 4, 4),   
('Bánh Mì Gà Nướng', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/ef08f8c5-79be-4fc3-b487-ed7776119884.jpg', 40000, TRUE, 'Bánh mì gà nướng thơm lừng', 4, 4),
('Nước Cam', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/75ee9caa-33b2-4ed4-98c3-2a3b4c87c8ec.jpg', 19000, TRUE, 'Nước cam tươi', 4, 6),
('Pepsi', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e960a620-8002-48fa-a934-af6b21fd4258.jpg', 15000, TRUE, 'Pepsi lon', 4, 6),


-- Quán Gỏi Cuốn Cô Ba (RestaurantID = 5) - Đa dạng món
('Gỏi Cuốn Tôm Thịt', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e9acdc9f-3503-47e4-b3ad-10662fd436aa.jpg', 35000, TRUE, 'Gỏi cuốn tôm thịt tươi', 5, 5),
('Gỏi đu đủ', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/16b618cb-15c9-4d3a-949b-1291d2fa42d9.jpg', 30000, TRUE, 'Gỏi đu đủ chua ngọt', 5, 5),
('Nước Cam', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/75ee9caa-33b2-4ed4-98c3-2a3b4c87c8ec.jpg', 19000, TRUE, 'Nước cam tươi', 5, 6),
('Pepsi', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e960a620-8002-48fa-a934-af6b21fd4258.jpg', 15000, TRUE, 'Pepsi lon', 5, 6),
('Chè Ba Màu', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/0f7aceba-5527-42fa-bae2-b8bd41857fac.jpg', 25000, TRUE, 'Chè ba màu truyền thống', 5, 7),

-- Quán Pizza Hut (RestaurantID = 6) - Đa dạng món
('Pizza Hải Sản', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/c1f2dc04-70c0-4834-aa54-95458a49e4ca.jpg', 179000, TRUE, 'Pizza hải sản tươi ngon', 6, 8),
('Pizza Gà Nấm', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/07b36d93-3ee7-4df6-908a-318ca8f1e75a.jpg'   , 169000, TRUE, 'Pizza gà nấm thơm lừng', 6, 8),
('Mì Ý Sốt', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/d1a50248-67e3-4521-9a1b-71c36e1372a2.jpg'   , 99000, TRUE, 'Mì Ý sốt bò bằm', 6, 8),
('Pepsi', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e960a620-8002-48fa-a934-af6b21fd4258.jpg', 15000, TRUE, 'Pepsi lon', 6, 6),
('Coca Cola', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/44fde9b9-5d9f-4576-986a-369ad052a1bc.jpg', 15000, TRUE, 'Coca Cola lon', 6, 6),
('Kem Chocolate', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/659c9ad1-d857-4473-ac0d-2cfc60ee3e32.jpg', 25000, TRUE, 'Kem chocolate mát lạnh', 6, 7),

-- Quán KFC Việt Nam (RestaurantID = 7) - Đa dạng món
('Gà Rán', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/7d6366df-61ec-42b9-9553-d89f5d3290ef.jpg', 95000, TRUE, 'Gà rán giòn tan', 7, 8),
('Combo Gà Rán', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/ee9c434d-8c2e-4fbc-8e28-6878fb72bfc6.jpg', 150000, TRUE, 'Combo gà rán 5 miếng', 7, 8),
('Burger Gà', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/75157ca4-14ff-43cc-87cb-d18aa6e02411.jpg', 80000, TRUE, 'Burger gà KFC', 7, 8),
('Khoai Tây Chiên', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/0403c309-3232-4603-93fa-373a1c1c7cf5.jpg', 40000, TRUE, 'Khoai tây chiên giòn', 7, 8),
('Pepsi', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/e960a620-8002-48fa-a934-af6b21fd4258.jpg', 15000, TRUE, 'Pepsi lon', 7, 6),
('Coca Cola', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/44fde9b9-5d9f-4576-986a-369ad052a1bc.jpg', 15000, TRUE, 'Coca Cola lon', 7, 6),
('Kem Vanilla', 'https://storage.googleapis.com/androidstudio-8fc98.firebasestorage.app/images/2eec9258-4267-4adf-b96f-6b5cf8d16310.jpg', 25000, TRUE, 'Kem vanilla KFC', 7, 7);

-- ============================================================
-- 8. ADDRESS - Địa chỉ người dùng
-- ============================================================
INSERT INTO Address (detail, phone, userid) VALUES
('123 Nguyễn Huệ, Quận 1, TP.HCM', '0901234567', 1),
('456 Lê Lợi, Quận 3, TP.HCM', '0901234567', 1),
('789 Trần Hưng Đạo, Quận 5, TP.HCM', '0912345678', 2),
('100 Nguyễn Đình Chiểu, Quận 3, TP.HCM', '0923456789', 3),
('200 Cách Mạng Tháng 8, Quận 10, TP.HCM', '0934567890', 4),
('300 Võ Văn Tần, Quận 3, TP.HCM', '0945678901', 5);

-- ============================================================
-- 9. ORDERS - Đơn hàng
-- ============================================================
INSERT INTO orders (status, preorderdate, preordertime, totalprice, restaurantid, addressid, userid) VALUES
('completed', NULL, NULL, 115000, 1, 1, 1),
('completed', NULL, NULL, 90000, 2, 2, 2),
('delivering', NULL, NULL, 165000, 3, 3, 3),
('pending', '2025-04-01', '12:00:00', 179000, 6, 4, 4),
('cancelled', NULL, NULL, 95000, 7, 5, 5),
('completed', NULL, NULL, 120000, 4, 1, 1),
('delivering', NULL, NULL, 200000, 5, 2, 2),
('completed', NULL, NULL, 145000, 1, 1, 1),
('completed', NULL, NULL, 175000, 2, 2, 2),
('completed', NULL, NULL, 198000, 3, 3, 3),
('completed', NULL, NULL, 85000, 4, 1, 1),
('completed', NULL, NULL, 210000, 6, 4, 4),
('delivering', NULL, NULL, 125000, 5, 2, 2),
('completed', NULL, NULL, 155000, 7, 5, 5),
('completed', NULL, NULL, 102000, 1, 1, 2),
('completed', NULL, NULL, 189000, 2, 2, 3),
('completed', NULL, NULL, 132000, 3, 3, 1),
('completed', NULL, NULL, 165000, 4, 1, 2),
('pending', NULL, NULL, 175000, 6, 4, 3),
('delivering', NULL, NULL, 142000, 7, 5, 1),
('completed', NULL, NULL, 198000, 5, 2, 2),
('completed', NULL, NULL, 120000, 2, 2, 2);

-- ============================================================
-- 10. ORDERITEM - Chi tiết đơn hàng
-- ============================================================
INSERT INTO orderitem (quantity, price, menuitemid, orderid) VALUES
-- Đơn hàng 1: Phở Hà Nội (RestaurantID = 1)
(2, 55000, 1, 1),  -- 2x Phở Bò Tái
(1, 10000, 4, 1), -- 1x Trà Đá

-- Đơn hàng 2: Cơm Tấm (RestaurantID = 2)
(2, 45000, 6, 2),  -- 2x Cơm Tấm Sườn Bì Chả

-- Đơn hàng 3: Bún Bò Huế (RestaurantID = 3)
(2, 50000, 13, 3),  -- 2x Bún Bò Huế Cay
(1, 65000, 15, 3), -- 1x Bún Bò Huế Đặc Biệt

-- Đơn hàng 4: Pizza (RestaurantID = 6)
(1, 179000, 30, 4), -- 1x Pizza Hải Sản

-- Đơn hàng 5: KFC (RestaurantID = 7)
(1, 95000, 36, 5),  -- 1x Gà Rán

-- Đơn hàng 6: Bánh Mì (RestaurantID = 4)
(2, 35000, 20, 6),  -- 2x Bánh Mì Thịt Nguội
(2, 25000, 24, 6),  -- 2x Pepsi

-- Đơn hàng 7: Gỏi Cuốn (RestaurantID = 5)
(2, 35000, 25, 7),  -- 2x Gỏi Cuốn Tôm Thịt
(1, 150000, 37, 7), -- 1x Combo Gà Rán

-- Đơn hàng 8: Phở + Nước (RestaurantID = 1)
(3, 55000, 1, 8),   -- 3x Phở Bò
(2, 15000, 5, 8),   -- 2x Coca Cola

-- Đơn hàng 9: Phở Gà + Gỏi (RestaurantID = 2)
(1, 50000, 7, 9),   -- 1x Phở Gà
(2, 30000, 11, 9),  -- 2x Bún Chả

-- Đơn hàng 10: Bún Trộn + Nước (RestaurantID = 3)
(2, 40000, 15, 10), -- 2x Bún Trộn
(1, 27000, 18, 10), -- 1x Chè Ba Màu

-- Đơn hàng 11: Bánh Mì Gà Nướng (RestaurantID = 4)
(2, 40000, 21, 11), -- 2x Bánh Mì Gà Nướng
(1, 5000, 24, 11),  -- 1x Pepsi

-- Đơn hàng 12: Pizza + Mì Ý (RestaurantID = 6)
(1, 169000, 31, 12), -- 1x Pizza Gà Nấm
(1, 99000, 32, 12),  -- 1x Mì Ý Sốt

-- Đơn hàng 13: Gỏi Cuốn Tôm + Nước (RestaurantID = 5)
(3, 35000, 25, 13),  -- 3x Gỏi Cuốn Tôm Thịt
(1, 19000, 27, 13),  -- 1x Nước Cam

-- Đơn hàng 14: Gà Rán Combo (RestaurantID = 7)
(1, 150000, 37, 14), -- 1x Combo Gà Rán
(1, 40000, 39, 14),  -- 1x Khoai Tây Chiên

-- Đơn hàng 15: Cơm Gà Nướng (RestaurantID = 3)
(2, 45000, 16, 15),  -- 2x Cơm Gà Nướng
(2, 19000, 17, 15),  -- 2x Nước Cam

-- Đơn hàng 16: Phở Bò + Phở Gà (RestaurantID = 1)
(1, 55000, 1, 16),   -- 1x Phở Bò
(1, 50000, 2, 16),   -- 1x Phở Gà

-- Đơn hàng 17: Bún Chả + Gỏi Đu Đủ (RestaurantID = 2)
(2, 30000, 10, 17),  -- 2x Bún Chả
(1, 30000, 26, 17),  -- 1x Gỏi Đu Đủ

-- Đơn hàng 18: Bánh Mì Xíu Mại (RestaurantID = 4)
(3, 30000, 20, 18),  -- 3x Bánh Mì Xíu Mại
(1, 19000, 23, 18),  -- 1x Nước Cam

-- Đơn hàng 19: Pizza Hải Sản (RestaurantID = 6)
(1, 179000, 30, 19), -- 1x Pizza Hải Sản
(2, 15000, 33, 19),  -- 2x Pepsi

-- Đơn hàng 20: Bún Bò Huế Cay (RestaurantID = 3)
(2, 50000, 13, 20),  -- 2x Bún Bò Huế
(1, 27000, 18, 20),  -- 1x Chè Ba Màu

-- Đơn hàng 21: Burger Gà + Kem (RestaurantID = 7)
(2, 80000, 38, 21),  -- 2x Burger Gà
(1, 25000, 42, 21),  -- 1x Kem Vanilla

-- Đơn hàng 22: Cơm Tấm + Nước (RestaurantID = 2)
(2, 45000, 6, 22),   -- 2x Cơm Tấm
(2, 15000, 12, 22);  -- 2x Pepsi

-- ============================================================
-- 11. PAYMENT - Thanh toán
-- ============================================================
INSERT INTO payment (status, method, orderid) VALUES
('completed', 'cash', 1),
('completed', 'card', 2),
('pending', 'wallet', 3),
('pending', 'card', 4),
('refunded', 'cash', 5),
('completed', 'wallet', 6),
('pending', 'cash', 7),
('completed', 'card', 8),
('completed', 'cash', 9),
('completed', 'wallet', 10),
('completed', 'card', 11),
('completed', 'card', 12),
('completed', 'cash', 13),
('completed', 'wallet', 14),
('completed', 'card', 15),
('completed', 'cash', 16),
('completed', 'wallet', 17),
('completed', 'card', 18),
('completed', 'card', 19),
('pending', 'wallet', 20),
('completed', 'cash', 21),
('completed', 'card', 22);

-- ============================================================
-- 12. DELIVERY - Giao hàng
-- ============================================================
INSERT INTO delivery (status, deliverytime, orderid, shipperid) VALUES
('completed', '12:30:00', 1, 1),
('completed', '13:15:00', 2, 2),
('delivering', NULL, 3, 3),
('pending', NULL, 4, NULL),
('cancelled', NULL, 5, NULL),
('completed', '19:45:00', 6, 4),
('delivering', NULL, 7, 5),
('completed', '11:20:00', 8, 1),
('completed', '14:00:00', 9, 2),
('completed', '15:30:00', 10, 3),
('completed', '12:45:00', 11, 4),
('completed', '18:15:00', 12, 5),
('delivering', NULL, 13, 1),
('completed', '20:00:00', 14, 2),
('completed', '11:50:00', 15, 3),
('completed', '13:20:00', 16, 4),
('completed', '16:40:00', 17, 5),
('completed', '10:30:00', 18, 1),
('completed', '19:10:00', 19, 2),
('pending', NULL, 20, NULL),
('delivering', NULL, 21, 3),
('completed', '14:25:00', 22, 4);

-- ============================================================
-- 13. REVIEW - Đánh giá
-- ============================================================
INSERT INTO review (rating, comment, menuitemid, restaurantid, userid, orderid) VALUES
(5, 'Phở rất ngon, nước dùng đậm đà! Sẽ đặt lại lần tới', 1, 1, 1, 1),
(4, 'Cơm tấm ngon, sườn nướng vừa miệng, giao hàng nhanh', 6, 2, 2, 2),
(5, 'Bún bò Huế cay nồng, đúng vị Huế, thơm lừng', 13, 3, 3, 3),
(5, 'Bánh mì ngon nhất Sài Gòn! Bánh giòn ráy, nhân no', 20, 4, 1, 6),
(4, 'Gỏi cuốn tươi ngon, nước chấm vừa phải', 25, 5, 2, 7),
(4, 'Pizza ngon, phô mai nhiều và chảy đẹp', 30, 6, 4, 4),
(5, 'Gà rán giòn tan, rất ngon! Sốt nước cốt chanh tuyệt', 36, 7, 5, 5),
(5, 'Phở nóng hổi, thơm lừng, thịt bò tái chín vừa vặn!', 1, 1, 1, 8),
(5, 'Coca Cola lạnh cóng, vị ngon không nước tạo', 5, 1, 1, 8),
(4, 'Phở gà mềm tươi, giao hàng nhanh chóng', 7, 2, 2, 9),
(4, 'Bún chả khô ráo không uột, nước chấm vừa phải', 11, 2, 2, 9),
(5, 'Bún trộn không bị cộp, ăn rất ngon! Chấm vị chuẩn', 15, 3, 3, 10),
(5, 'Chè ba màu lạnh mát, vị tự nhiên không ai', 18, 3, 3, 10),
(5, 'Bánh mì gà nướng thơm lừng, giòn tan, nhân no', 21, 4, 1, 11),
(4, 'Giao hàng nhanh chóng, đóng gói cẩn thận', 21, 4, 1, 11),
(5, 'Pizza gà nấm đầy tinh tế, phô mai dịn chảy tươm', 31, 6, 4, 12),
(5, 'Mì Ý sốt bò bằm chuẩn chế biến, mùi thơm lừng', 32, 6, 4, 12),
(5, 'Gỏi cuốn tôm tươi, rất ngon và sạch sẽ', 25, 5, 2, 13),
(5, 'Nước cam tươi, vị tự nhiên không nước tạo, rất tốt', 27, 5, 2, 13),
(5, 'Combo gà rán đầy đủ, giòn tan cực kỳ, rất no', 37, 7, 5, 14),
(5, 'Khoai tây chiên nóng, giòn rất vừa miệng', 39, 7, 5, 14),
(4, 'Cơm gà nướng chín vừa, có hương thơm nức mũi', 16, 3, 1, 15),
(4, 'Nước cam tự nhiên, không đắng, rất tươi mát', 17, 3, 1, 15),
(5, 'Phở bò tái chín vừa, mềm mà không nát, vị chuẩn', 1, 1, 2, 16),
(5, 'Phở gà dịn thơm, nước dùng trong sâu, ngon quá', 2, 1, 2, 16),
(5, 'Bún chả Hà Nội truyền thống tuyệt vời! Nước chấm đặc', 10, 2, 3, 17),
(5, 'Gỏi đu đủ chua ngọt vừa phải, khéo léo', 26, 5, 3, 17),
(5, 'Bánh mì xíu mại nướng vàng ươm, crust giòn tốt', 21, 4, 2, 18),
(5, 'Nước cam tươi nguyên chất, quanh năm uống được', 23, 4, 2, 18),
(5, 'Pizza hải sản tươi có đầy các hải sản cao cấp', 30, 6, 4, 19),
(5, 'Pepsi lạnh cóng, vị ngon đặc biệt, thích', 33, 6, 4, 19),
(4, 'Bún bò Huế cay nồng đúng chuẩn Huế, ăn ghiền', 13, 3, 3, 20),
(4, 'Chè ba màu ngon, đủ ba lớp đẹp mắt, vị tốt', 18, 3, 3, 20),
(5, 'Burger gà mềm nhân no, phô mai nung chảy ngon', 38, 7, 1, 21),
(5, 'Kem vanilla lạnh mát, vị thanh tươi, rất tốt', 42, 7, 1, 21),
(5, 'Cơm tấm Sài Gòn chuẩn vị, sườn mềm ngon lắm', 6, 2, 2, 22),
(5, 'Pepsi lạnh ngon, giao hàng rất nhanh, hài lòng', 12, 2, 2, 22);

-- ============================================================
-- 14. CHATSESSION - Phiên chat
-- ============================================================
INSERT INTO chatsession (status, userid) VALUES
('active', 1),
('closed', 2),
('active', 3),
('active', 4),
('closed', 5);

-- ============================================================
-- 15. CHATMESSAGE - Tin nhắn chat
-- ============================================================
INSERT INTO chatmessage (senderrole, message, sessionid) VALUES
('user', 'Xin chào, tôi muốn hỏi về đơn hàng', 1),
('bot', 'Chào bạn! Tôi có thể giúp gì cho bạn về đơn hàng #1?', 1),
('user', 'Đơn hàng của tôi đã giao chưa?', 1),
('bot', 'Đơn hàng #1 đã được giao thành công lúc 12:30.', 1),
('user', 'Cảm ơn bạn!', 1),
('user', 'Tôi muốn hủy đơn hàng', 2),
('bot', 'Để hủy đơn hàng, bạn cần liên hệ hotline hoặc hủy trong vòng 5 phút.', 2),
('user', 'Làm sao để đặt hàng?', 4),
('bot', 'Bạn có thể đặt hàng bằng cách chọn nhà hàng, chọn món và nhấn "Đặt hàng".', 4);

-- ============================================================
-- 16. NOTIFICATION - Thông báo
-- ============================================================
-- Mỗi đơn hàng có 4 bước thông báo: Đặt hàng thành công -> Cửa hàng xác nhận -> Đang giao -> Đã nhận
-- (Tuỳ vào trạng thái của đơn hàng: completed có 4, delivering có 3, pending có 1-2, cancelled có 1)
INSERT INTO notification (title, type, content, isread, userid, orderid, sessionid) VALUES
-- Đơn hàng 1 (completed): Phở Hà Nội - User 1
('Đặt hàng thành công!', 'order', 'Đơn hàng #1 của bạn đã được tiếp nhận bởi Quán Phở Hà Nội.', TRUE, 1, 1, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Phở Hà Nội đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 1, 1, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #1 đang được giao đến bạn. Tài xế: Nguyễn Văn Ship', TRUE, 1, 1, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #1 đã được giao thành công. Cảm ơn bạn!', TRUE, 1, 1, NULL),

-- Đơn hàng 2 (completed): Cơm Tấm - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #2 của bạn đã được tiếp nhận bởi Quán Cơm HIHI.', TRUE, 2, 2, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Cơm HIHI đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 2, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #2 đang được giao đến bạn. Tài xế: Trần Văn Giao', TRUE, 2, 2, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #2 đã được giao thành công. Cảm ơn bạn!', TRUE, 2, 2, NULL),

-- Đơn hàng 3 (delivering): Bún Bò Huế - User 3
('Đặt hàng thành công!', 'order', 'Đơn hàng #3 của bạn đã được tiếp nhận bởi Quán Bún Cô Hóa.', TRUE, 3, 3, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bún Cô Hóa đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 3, 3, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #3 đang được giao đến bạn. Tài xế: Lê Văn Vận', FALSE, 3, 3, NULL),

-- Đơn hàng 4 (pending): Pizza - User 4
('Đặt hàng thành công!', 'order', 'Đơn hàng #4 của bạn đã được tiếp nhận bởi Quán Pizza Hut.', TRUE, 4, 4, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Pizza Hut đã xác nhận đơn hàng và bắt đầu chuẩn bị.', FALSE, 4, 4, NULL),

-- Đơn hàng 5 (cancelled): KFC - User 5
('Đặt hàng thành công!', 'order', 'Đơn hàng #5 của bạn đã được tiếp nhận bởi Quán KFC.', TRUE, 5, 5, NULL),
('Đơn hàng đã hủy', 'order', 'Đơn hàng #5 đã bị hủy. Tiền sẽ được hoàn lại vào tài khoản của bạn.', TRUE, 5, 5, NULL),

-- Đơn hàng 6 (completed): Bánh Mì - User 1
('Đặt hàng thành công!', 'order', 'Đơn hàng #6 của bạn đã được tiếp nhận bởi Quán Bánh Mì B.', TRUE, 1, 6, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bánh Mì B đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 1, 6, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #6 đang được giao đến bạn. Tài xế: Phạm Văn Chuyển', TRUE, 1, 6, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #6 đã được giao thành công. Cảm ơn bạn!', TRUE, 1, 6, NULL),

-- Đơn hàng 7 (delivering): Gỏi Cuốn - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #7 của bạn đã được tiếp nhận bởi Quán Gỏi Cuốn A.', TRUE, 2, 7, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Gỏi Cuốn A đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 7, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #7 đang được giao đến bạn. Tài xế: Hoàng Văn Đưa', FALSE, 2, 7, NULL),

-- Đơn hàng 8 (completed): Phở + Nước - User 1
('Đặt hàng thành công!', 'order', 'Đơn hàng #8 của bạn đã được tiếp nhận bởi Quán Phở Hà Nội.', TRUE, 1, 8, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Phở Hà Nội đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 1, 8, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #8 đang được giao đến bạn. Tài xế: Nguyễn Văn Ship', TRUE, 1, 8, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #8 đã được giao thành công. Cảm ơn bạn!', TRUE, 1, 8, NULL),

-- Đơn hàng 9 (completed): Phở Gà + Gỏi - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #9 của bạn đã được tiếp nhận bởi Quán Cơm HIHI.', TRUE, 2, 9, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Cơm HIHI đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 9, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #9 đang được giao đến bạn. Tài xế: Trần Văn Giao', TRUE, 2, 9, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #9 đã được giao thành công. Cảm ơn bạn!', TRUE, 2, 9, NULL),

-- Đơn hàng 10 (completed): Bún Trộn + Nước - User 3
('Đặt hàng thành công!', 'order', 'Đơn hàng #10 của bạn đã được tiếp nhận bởi Quán Bún Cô Hóa.', TRUE, 3, 10, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bún Cô Hóa đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 3, 10, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #10 đang được giao đến bạn. Tài xế: Lê Văn Vận', TRUE, 3, 10, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #10 đã được giao thành công. Cảm ơn bạn!', TRUE, 3, 10, NULL),

-- Đơn hàng 11 (completed): Bánh Mì Gà Nướng - User 1
('Đặt hàng thành công!', 'order', 'Đơn hàng #11 của bạn đã được tiếp nhận bởi Quán Bánh Mì B.', TRUE, 1, 11, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bánh Mì B đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 1, 11, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #11 đang được giao đến bạn. Tài xế: Phạm Văn Chuyển', TRUE, 1, 11, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #11 đã được giao thành công. Cảm ơn bạn!', TRUE, 1, 11, NULL),

-- Đơn hàng 12 (completed): Pizza + Mì Ý - User 4
('Đặt hàng thành công!', 'order', 'Đơn hàng #12 của bạn đã được tiếp nhận bởi Quán Pizza Hut.', TRUE, 4, 12, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Pizza Hut đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 4, 12, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #12 đang được giao đến bạn. Tài xế: Hoàng Văn Đưa', TRUE, 4, 12, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #12 đã được giao thành công. Cảm ơn bạn!', TRUE, 4, 12, NULL),

-- Đơn hàng 13 (delivering): Gỏi Cuốn Tôm + Nước - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #13 của bạn đã được tiếp nhận bởi Quán Gỏi Cuốn A.', TRUE, 2, 13, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Gỏi Cuốn A đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 13, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #13 đang được giao đến bạn. Tài xế: Nguyễn Văn Ship', FALSE, 2, 13, NULL),

-- Đơn hàng 14 (completed): Gà Rán Combo - User 5
('Đặt hàng thành công!', 'order', 'Đơn hàng #14 của bạn đã được tiếp nhận bởi Quán KFC.', TRUE, 5, 14, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán KFC đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 5, 14, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #14 đang được giao đến bạn. Tài xế: Trần Văn Giao', TRUE, 5, 14, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #14 đã được giao thành công. Cảm ơn bạn!', TRUE, 5, 14, NULL),

-- Đơn hàng 15 (completed): Cơm Gà Nướng - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #15 của bạn đã được tiếp nhận bởi Quán Bún Cô Hóa.', TRUE, 2, 15, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bún Cô Hóa đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 15, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #15 đang được giao đến bạn. Tài xế: Lê Văn Vận', TRUE, 2, 15, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #15 đã được giao thành công. Cảm ơn bạn!', TRUE, 2, 15, NULL),

-- Đơn hàng 16 (completed): Phở Bò + Phở Gà - User 3
('Đặt hàng thành công!', 'order', 'Đơn hàng #16 của bạn đã được tiếp nhận bởi Quán Phở Hà Nội.', TRUE, 3, 16, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Phở Hà Nội đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 3, 16, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #16 đang được giao đến bạn. Tài xế: Phạm Văn Chuyển', TRUE, 3, 16, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #16 đã được giao thành công. Cảm ơn bạn!', TRUE, 3, 16, NULL),

-- Đơn hàng 17 (completed): Bún Chả + Gỏi Đu Đủ - User 1
('Đặt hàng thành công!', 'order', 'Đơn hàng #17 của bạn đã được tiếp nhận bởi Quán Cơm HIHI.', TRUE, 1, 17, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Cơm HIHI đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 1, 17, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #17 đang được giao đến bạn. Tài xế: Hoàng Văn Đưa', TRUE, 1, 17, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #17 đã được giao thành công. Cảm ơn bạn!', TRUE, 1, 17, NULL),

-- Đơn hàng 18 (completed): Bánh Mì Xíu Mại - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #18 của bạn đã được tiếp nhận bởi Quán Bánh Mì B.', TRUE, 2, 18, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bánh Mì B đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 18, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #18 đang được giao đến bạn. Tài xế: Nguyễn Văn Ship', TRUE, 2, 18, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #18 đã được giao thành công. Cảm ơn bạn!', TRUE, 2, 18, NULL),

-- Đơn hàng 19 (pending): Pizza Hải Sản - User 3
('Đặt hàng thành công!', 'order', 'Đơn hàng #19 của bạn đã được tiếp nhận bởi Quán Pizza Hut.', TRUE, 3, 19, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Pizza Hut đã xác nhận đơn hàng và bắt đầu chuẩn bị.', FALSE, 3, 19, NULL),

-- Đơn hàng 20 (delivering): Bún Bò Huế Cay - User 1
('Đặt hàng thành công!', 'order', 'Đơn hàng #20 của bạn đã được tiếp nhận bởi Quán Bún Cô Hóa.', TRUE, 1, 20, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Bún Cô Hóa đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 1, 20, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #20 đang được giao đến bạn. Tài xế: Trần Văn Giao', FALSE, 1, 20, NULL),

-- Đơn hàng 21 (completed): Burger Gà + Kem - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #21 của bạn đã được tiếp nhận bởi Quán KFC.', TRUE, 2, 21, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán KFC đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 21, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #21 đang được giao đến bạn. Tài xế: Lê Văn Vận', TRUE, 2, 21, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #21 đã được giao thành công. Cảm ơn bạn!', TRUE, 2, 21, NULL),

-- Đơn hàng 22 (completed): Cơm Tấm + Nước - User 2
('Đặt hàng thành công!', 'order', 'Đơn hàng #22 của bạn đã được tiếp nhận bởi Quán Cơm HIHI.', TRUE, 2, 22, NULL),
('Cửa hàng xác nhận đơn', 'order', 'Quán Cơm HIHI đã xác nhận đơn hàng và bắt đầu chuẩn bị.', TRUE, 2, 22, NULL),
('Đơn hàng đang giao', 'order', 'Đơn hàng #22 đang được giao đến bạn. Tài xế: Phạm Văn Chuyển', TRUE, 2, 22, NULL),
('Đơn hàng đã nhận', 'order', 'Đơn hàng #22 đã được giao thành công. Cảm ơn bạn!', TRUE, 2, 22, NULL);

-- ============================================================
-- 17. USERDEVICE - Thiết bị người dùng
-- ============================================================
INSERT INTO userdevice (device_token, device_type, last_active, userid) VALUES
('cEjdZ2QgR1aqX3pH7mN4Su:APA91bFTQcJTkf2mWE4Mv7YVahZ0m9Gh0vNwK4yOrzt-5bxsB6-OpgVJMMdFxfXy0ENbKJX5kAZMRIr0bGBWM1c3v8Efw9OsZB1IrQoOxukyzPlsEQzeUPg', 'android', '2025-03-27 10:00:00', 1),
('dthlB3fHT0G6QCejWBEJTq:APA91bGpAwoBw0IwIzlskwiDlkIILEidxwFt9CA1K1WUa6L9fQNJFUEhWJ3NcHPTA9k5V1TS2-sqorA9vPI2NsQEl0ky0rs8OcL-MaF0fhxyUBQhJRLGva0', 'android', '2025-03-27 09:45:00', 2),
('dthlB3fHT0G6QCejWBEJTq:APA91bGpAwoBw0IwIzlskwiDlkIILEidxwFt9CA1K1WUa6L9fQNJFUEhWJ3NcHPTA9k5V1TS2-sqorA9vPI2NsQEl0ky0rs8OcL-MaF0fhxyUBQhJRLGva0', 'android', '2025-03-27 08:30:00', 3),
('dthlB3fHT0G6QCejWBEJTq:APA91bGpAwoBw0IwIzlskwiDlkIILEidxwFt9CA1K1WUa6L9fQNJFUEhWJ3NcHPTA9k5V1TS2-sqorA9vPI2NsQEl0ky0rs8OcL-MaF0fhxyUBQhJRLGva0', 'android', '2025-03-26 20:00:00', 4),
('dthlB3fHT0G6QCejWBEJTq:APA91bGpAwoBw0IwIzlskwiDlkIILEidxwFt9CA1K1WUa6L9fQNJFUEhWJ3NcHPTA9k5V1TS2-sqorA9vPI2NsQEl0ky0rs8OcL-MaF0fhxyUBQhJRLGva0', 'android', '2025-03-26 18:00:00', 5),
('dthlB3fHT0G6QCejWBEJTq:APA91bGpAwoBw0IwIzlskwiDlkIILEidxwFt9CA1K1WUa6L9fQNJFUEhWJ3NcHPTA9k5V1TS2-sqorA9vPI2NsQEl0ky0rs8OcL-MaF0fhxyUBQhJRLGva0', 'android', '2025-03-25 19:00:00', 6);

-- ============================================================
-- 18. USERFAQ - Lịch sử xem P
-- ============================================================
INSERT INTO userfaq (viewedat, userid, faqid) VALUES
('2025-03-25 10:00:00', 1, 1),
('2025-03-25 10:05:00', 1, 2),
('2025-03-26 14:30:00', 2, 3),
('2025-03-26 15:00:00', 3, 4),
('2025-03-27 08:00:00', 4, 5),
('2025-03-27 09:00:00', 5, 6);

-- ============================================================
-- 19. USEDPROMOTION - Mã giảm giá đã sử dụng
-- ============================================================
INSERT INTO usedpromotion (usedat, promotionid, orderid) VALUES
('2025-03-20', 1, 1),
('2025-03-21', 2, 2),
('2025-03-22', 3, 3),
('2025-03-23', 4, 6),
('2025-03-24', 5, 7),
('2025-03-25', 1, 8),
('2025-03-26', 2, 9),
('2025-03-27', 3, 10),
('2025-04-01', 4, 12),
('2025-04-02', 5, 14);

-- ============================================================
-- 20. LOYALTYPOINT - Điểm tích lũy
-- ============================================================
INSERT INTO loyaltypoint (points, updatedat, userid, menuitemid) VALUES
(1150, '2025-03-27', 1, 1),
(900, '2025-03-27', 2, 6),
(1650, '2025-03-27', 3, 13),
(1790, '2025-03-27', 4, 33),
(950, '2025-03-27', 5, 41),
(1200, '2025-03-27', 1, 20),
(1450, '2025-04-02', 1, 1),
(1750, '2025-04-02', 2, 6),
(1980, '2025-04-02', 3, 13),
(2100, '2025-04-02', 4, 33),
(1420, '2025-04-03', 1, 11),
(1890, '2025-04-03', 2, 28),
(1320, '2025-04-03', 3, 16);

-- ============================================================
-- 21. SOCIALSHARE - Chia sẻ mạng xã hội
-- ============================================================
INSERT INTO socialshare (sharetype, platform, context, createdat, userid, restaurantid, menuitemid) VALUES
('review', 'facebook', 'Phở Hà Nội ngon tuyệt! Nhất định phải thử', '2025-03-25', 1, 1, 1),
('photo', 'instagram', 'Cơm tấm Sài Gòn chuẩn vị, sườn mềm ngon lắm', '2025-03-26', 2, 2, 6),
('review', 'facebook', 'Bún bò Huế cay nồng đúng vị, khác biệt', '2025-03-26', 3, 3, 13),
('photo', 'instagram', 'Bánh mì Huỳnh Hoa - Vua bánh mì Sài Gòn', '2025-03-27', 1, 4, 20),
('review', 'twitter', 'Gỏi cuốn tươi ngon, nước chấm tuyệt vời', '2025-03-27', 2, 5, 27),
('review', 'facebook', 'Pizza ngon, phô mai chảy, giao hàng nhanh', '2025-04-01', 4, 6, 33),
('photo', 'instagram', 'Gà rán KFC - Crunchy bên ngoài, mềm bên trong', '2025-04-02', 5, 7, 41),
('review', 'facebook', 'Phở gà mềm tươi, mùi thơm lừng', '2025-04-03', 1, 1, 2),
('photo', 'instagram', 'Burger gà KFC thơm lừng, no no', '2025-04-03', 1, 7, 42),
('review', 'twitter', 'Nước cam tươi, vị tự nhiên không nước tạo', '2025-04-04', 2, 2, 12);

-- ============================================================
-- Kết thúc file seed_data.sql
-- ============================================================
