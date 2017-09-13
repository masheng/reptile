CREATE DATABASE IF NOT EXISTS `app_info` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use app_info;

#360app市场
CREATE TABLE IF NOT EXISTS `app360` (
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

#百度app市场
CREATE TABLE IF NOT EXISTS `baidu` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `sname` varchar(40) NOT NULL default '', #app名称
  `score` int NOT NULL default 0,  #评分
  `score_count` int NOT NULL default 0,  #评分总数
  `popularity` int NOT NULL default 0,  #热度
  `display_download` long NOT NULL,  #下载次数
  `yesterday_download_pid` int NOT NULL default 0,  #昨日次数
  `updatetime` DATE,  #最后更新日期
  `brief` text,  #描述
  `manual_short_brief` varchar(1000),  #短评
  `cateid` varchar(50),  #用处未知
  `category` varchar(50),  #类别
  `versionname` varchar(40) NOT NULL default '', #版本号
  `versioncode` varchar(40) NOT NULL default '', #版本号
  `size_ori` int NOT NULL default 0,  #大小
  `platform_version` varchar(40) NOT NULL default 0,  #手机的最低版本
  `package_name` varchar(100) NOT NULL default '', #app包名称
  `packageid` varchar(100) NOT NULL default '', #所在网站的apkid
  `insertTime` DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;