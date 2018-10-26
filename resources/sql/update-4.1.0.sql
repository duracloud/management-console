DROP TABLE IF EXISTS `email_template`;
CREATE TABLE `email_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `subject` varchar(1000)  NOT NULL,
  `body` TEXT,
  `template` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
