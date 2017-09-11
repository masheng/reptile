CREATE DATABASE `app_info` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use app_info;

#360app市场
CREATE TABLE `app360` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(40) NOT NULL default '', #app名称
  `download_times` int NOT NULL default 0,  #下载次数
  `lastUpdate` DATE,  #最后更新日期
  `category_name` varchar(40) NOT NULL default '',  #app类别
  `info` text,  #描述
  `rating_fixed` int NOT NULL default 0,  #评分
  `writer` varchar(40) NOT NULL default '', #作者
  `version_name` varchar(40) NOT NULL default '', #版本号
  `size` int NOT NULL default 0,  #大小
  `size_fixed` float NOT NULL default 0,  #大小 按兆算
  `os_version` int NOT NULL default 0,  #手机的最低版本
  `apkid` varchar(100) NOT NULL default '', #所在网站的apkid
  `apid` varchar(40) NOT NULL default '',
  `insertTime` DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
