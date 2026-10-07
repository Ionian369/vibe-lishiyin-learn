-- 预置器材字典数据：每次启动重置为标准三件套（预约记录不受影响）
DELETE FROM device;
INSERT INTO device (id, name, description) VALUES (1, '投影仪', '高亮度教学投影仪，支持 HDMI / VGA 输入');
INSERT INTO device (id, name, description) VALUES (2, '音响', '移动式蓝牙音响，满足中型教室扩音');
INSERT INTO device (id, name, description) VALUES (3, '无线话筒', '领夹式无线话筒，含接收主机一台');
