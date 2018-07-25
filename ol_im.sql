/*
 Navicat Premium Data Transfer

 Source Server         : database_alibaba
 Source Server Type    : MySQL
 Source Server Version : 50670
 Source Host           : rm-2ze4exm70x4cdt0zto.mysql.rds.aliyuncs.com
 Source Database       : ol_im

 Target Server Type    : MySQL
 Target Server Version : 50670
 File Encoding         : utf-8

 Date: 07/25/2018 09:57:19 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `im_friends`
-- ----------------------------
DROP TABLE IF EXISTS `im_friends`;
CREATE TABLE `im_friends` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone` varchar(50) DEFAULT NULL,
  `friend` varchar(50) DEFAULT NULL,
  `column` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `im_friends`
-- ----------------------------
BEGIN;
INSERT INTO `im_friends` VALUES ('5', '13581574738', null, null);
COMMIT;

-- ----------------------------
--  Table structure for `im_groups`
-- ----------------------------
DROP TABLE IF EXISTS `im_groups`;
CREATE TABLE `im_groups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT '',
  `creater` varchar(50) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `im_groups`
-- ----------------------------
BEGIN;
INSERT INTO `im_groups` VALUES ('2', 'test', '13581574738');
COMMIT;

-- ----------------------------
--  Table structure for `im_groups_user`
-- ----------------------------
DROP TABLE IF EXISTS `im_groups_user`;
CREATE TABLE `im_groups_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(50) DEFAULT NULL,
  `user_phone` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `im_groups_user`
-- ----------------------------
BEGIN;
INSERT INTO `im_groups_user` VALUES ('4', 'test', '13581574738'), ('5', 'test', '13621285412');
COMMIT;

-- ----------------------------
--  Table structure for `im_offline_msg`
-- ----------------------------
DROP TABLE IF EXISTS `im_offline_msg`;
CREATE TABLE `im_offline_msg` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender` varchar(50) DEFAULT '',
  `receiver` varchar(50) DEFAULT '',
  `message` varchar(200) DEFAULT '',
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `im_offline_msg_group`
-- ----------------------------
DROP TABLE IF EXISTS `im_offline_msg_group`;
CREATE TABLE `im_offline_msg_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender` varchar(50) DEFAULT '',
  `receiver` varchar(50) DEFAULT '' COMMENT '用户名称',
  `name` varchar(50) DEFAULT '' COMMENT '群组名称',
  `message` varchar(200) DEFAULT '',
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `im_offline_msg_group`
-- ----------------------------
BEGIN;
INSERT INTO `im_offline_msg_group` VALUES ('41', '13581574738', '13621285412', 'test', '测试', '1970-01-19 01:41:23');
COMMIT;

-- ----------------------------
--  Table structure for `im_user`
-- ----------------------------
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '姓名',
  `age` int(4) DEFAULT '0' COMMENT '年龄',
  `sex` varchar(1) DEFAULT '1' COMMENT '性别',
  `phone` varchar(11) DEFAULT '' COMMENT '电话',
  `passwd` varchar(100) DEFAULT NULL COMMENT '密码',
  `email` varchar(50) DEFAULT '' COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT 'default.jpg' COMMENT '头像',
  `sign` varchar(200) DEFAULT '' COMMENT '个人简介',
  `school` varchar(200) DEFAULT NULL,
  `introduction` varchar(100) DEFAULT '' COMMENT '付费问答 用户说明',
  `realName` varchar(100) DEFAULT '',
  `local` varchar(50) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `im_user`
-- ----------------------------
BEGIN;
INSERT INTO `im_user` VALUES ('15', '', '0', '1', '13581574738', '49ba59abbe56e057', '', 'default.jpg', '', null, '', '', ''), ('16', '', '0', '1', '13621285412', '49ba59abbe56e057', '', 'default.jpg', '', null, '', '', '');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
