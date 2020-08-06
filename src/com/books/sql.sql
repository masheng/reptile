CREATE DATABASE IF NOT EXISTS `reptile_book` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use reptile_book;

CREATE TABLE IF NOT EXISTS `book` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `bookName` varchar(200) NOT NULL default '',
  `bookFormat`  varchar(29) NOT NULL default '',
  `downloadUrl` varchar(2000) NOT NULL default '',  /*下载url地址 格式:{"网盘类型||提取码||url",...}  以||分割*/
  `pageUrl` varchar(200) NOT NULL default '',       #原始页面地址
  `insertTime` TIMESTAMP,                           #第一次插入时的时间
  `info` varchar(500),
  `redundancy` varchar (100),   #冗余字段
  `downDone` INT default 0,                        #是否存储完成
  `savePath` varchar(500) NOT NULL default '',  /*书保存的地址 格式:{"网盘类型||提取码||url",...}  以||分割*/
  `saveTime` TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*扫描各网站的时间
将记录每个网站目录扫描的页数

如一个条目上次记录总共100页
这次扫描到有150页 则扫描前50页就可以了
*/
CREATE TABLE IF NOT EXISTS `scan_info` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `site` varchar(50) NOT NULL default '',
  `bookAdd` int,
  `cateInfo` varchar(300) NOT NULL default '',
  `insertTime` TIMESTAMP,                         #开始扫描的时间
  `duration` int       #扫描花费的时间
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/**
每一个详情页面url都会存入
防止重复请求
*/
CREATE TABLE IF NOT EXISTS `repetition` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `md5` varchar(50) NOT NULL default '',
  `url` varchar(300) NOT NULL default '',
  `insertTime` TIMESTAMP                         #扫描的时间
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

delete from book;
delete from scan_info;
delete from repetition;


