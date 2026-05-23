-- ============================================================
-- Script xóa tất cả dữ liệu hiện tại trong database
-- ============================================================
-- Cảnh báo: Script này sẽ xóa TẤT CẢ dữ liệu!
-- Chỉ sử dụng khi cần reset database về trạng thái trống
-- ============================================================

-- Tắt kiểm tra khóa ngoại tạm thời
SET session_replication_role = 'replica';

-- Xóa dữ liệu theo thứ tự ngược lại với seed_data
-- (Bảng con trước, bảng cha sau)

-- 1. Xóa SocialShare
TRUNCATE TABLE socialshare CASCADE;

-- 2. Xóa LoyaltyPoint
TRUNCATE TABLE loyaltypoint CASCADE;

-- 3. Xóa UsedPromotion
TRUNCATE TABLE usedpromotion CASCADE;

-- 4. Xóa UserFAQ
TRUNCATE TABLE userfaq CASCADE;

-- 5. Xóa UserDevice
TRUNCATE TABLE userdevice CASCADE;

-- 6. Xóa Notification
TRUNCATE TABLE notification CASCADE;

-- 7. Xóa ChatMessage
TRUNCATE TABLE chatmessage CASCADE;

-- 8. Xóa ChatSession
TRUNCATE TABLE chatsession CASCADE;

-- 9. Xóa Review
TRUNCATE TABLE review CASCADE;

-- 10. Xóa Delivery
TRUNCATE TABLE delivery CASCADE;

-- 11. Xóa Payment
TRUNCATE TABLE payment CASCADE;

-- 12. Xóa OrderItem
TRUNCATE TABLE orderitem CASCADE;

-- 13. Xóa Orders
TRUNCATE TABLE orders CASCADE;

-- 14. Xóa Address
TRUNCATE TABLE address CASCADE;

-- 15. Xóa MenuItem
TRUNCATE TABLE menuitem CASCADE;

-- 16. Xóa Restaurant
TRUNCATE TABLE restaurant CASCADE;

-- 17. Xóa Promotion
TRUNCATE TABLE promotion CASCADE;

-- 18. Xóa Shipper
TRUNCATE TABLE shipper CASCADE;

-- 19. Xóa User
TRUNCATE TABLE "User" CASCADE;

-- 20. Xóa FAQ
TRUNCATE TABLE faq CASCADE;

-- 21. Xóa Category
TRUNCATE TABLE category CASCADE;

-- Bật lại kiểm tra khóa ngoại
SET session_replication_role = 'origin';

-- Reset sequences (tùy chọn)
ALTER SEQUENCE IF EXISTS category_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS faq_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS "User_id_seq" RESTART WITH 1;
ALTER SEQUENCE IF EXISTS shipper_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS promotion_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS restaurant_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS menuitem_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS address_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS orders_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS orderitem_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS payment_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS delivery_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS review_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS chatsession_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS chatmessage_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS notification_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS userdevice_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS userfaq_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS usedpromotion_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS loyaltypoint_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS socialshare_id_seq RESTART WITH 1;

-- Thông báo hoàn thành
SELECT 'Đã xóa tất cả dữ liệu thành công!' as message;
