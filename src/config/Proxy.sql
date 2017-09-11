#代理ip相关的数据库
CREATE DATABASE `proxy_info` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use proxy_info;

#快代理 http://www.kuaidaili.com/free/
CREATE TABLE `kuai_proxy` (
  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `ip` varchar(40) NOT NULL default '',
  `port` int NOT NULL default 0,
  `grade` int NOT NULL default 0 COMMENT "ip等级",
  `level` varchar(40) NOT NULL default 0 COMMENT "匿名等级",
  `lastUpdate` DATETIME COMMENT "最后验证时间",
  `speed` int NOT NULL default 0 COMMENT "响应速度",
  `test` int NOT NULL default 0 COMMENT "测试的响应速度",
  `location` varchar(150) NOT NULL default '' COMMENT "代理地址",
  `type` varchar(40) NOT NULL default 0 COMMENT "http/https",
  `insertTime` DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;