CREATE TABLE `rabbitmq_config` (
  `id` bigint(20) NOT NULL,
  `host` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `vhost` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `duracloud_mill`
DROP COLUMN `rabbitmq_host`,
DROP COLUMN `rabbitmq_port`,
DROP COLUMN `rabbitmq_vhost`,
DROP COLUMN `rabbitmq_username`,
DROP COLUMN `rabbitmq_password`,
ADD COLUMN `rabbitmq_config_id` bigint(20) DEFAULT NULL,
ADD KEY `FK_DURACLOUD_MILL_RABBITMQ_CONFIG` (`rabbitmq_config_id`),
ADD CONSTRAINT `FK_DURACLOUD_MILL_RABBITMQ_CONFIG` FOREIGN KEY (`rabbitmq_config_id`) REFERENCES `rabbitmq_config` (`id`);

ALTER TABLE `global_properties`
DROP COLUMN `rabbitmq_host`,
DROP COLUMN `rabbitmq_port`,
DROP COLUMN `rabbitmq_vhost`,
DROP COLUMN `rabbitmq_username`,
DROP COLUMN `rabbitmq_password`,
ADD COLUMN `rabbitmq_config_id` bigint(20) DEFAULT NULL,
ADD KEY `FK_GLOBAL_PROPERTIES_RABBITMQ_CONFIG` (`rabbitmq_config_id`),
ADD CONSTRAINT `FK_GLOBAL_PROPERTIES_RABBITMQ_CONFIG` FOREIGN KEY (`rabbitmq_config_id`) REFERENCES `rabbitmq_config` (`id`);
