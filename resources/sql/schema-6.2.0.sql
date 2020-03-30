/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_info`
--

DROP TABLE IF EXISTS `account_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `acct_name` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `org_name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `subdomain` varchar(255) DEFAULT NULL,
  `primary_storage_provider_account_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_rights`
--

DROP TABLE IF EXISTS `account_rights`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_rights` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_pqeqe896mkrol3d5fx83xkcwq` (`account_id`),
  KEY `FK_rse1vjlmaucfng31iaw1fj7iu` (`user_id`),
  CONSTRAINT `FK_pqeqe896mkrol3d5fx83xkcwq` FOREIGN KEY (`account_id`) REFERENCES `account_info` (`id`),
  CONSTRAINT `FK_rse1vjlmaucfng31iaw1fj7iu` FOREIGN KEY (`user_id`) REFERENCES `duracloud_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `account_rights_role`
--

DROP TABLE IF EXISTS `account_rights_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_rights_role` (
  `account_rights_id` bigint(20) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  KEY `FK_91wx1gbltwqx2ma0w10c798td` (`account_rights_id`),
  CONSTRAINT `FK_91wx1gbltwqx2ma0w10c798td` FOREIGN KEY (`account_rights_id`) REFERENCES `account_rights` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `duracloud_group`
--

DROP TABLE IF EXISTS `duracloud_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `duracloud_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_7tt58lodlxiqwjhvbyi9ytlyp` (`account_id`),
  CONSTRAINT `FK_7tt58lodlxiqwjhvbyi9ytlyp` FOREIGN KEY (`account_id`) REFERENCES `account_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `duracloud_mill`
--

DROP TABLE IF EXISTS `duracloud_mill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `duracloud_mill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `audit_log_space_id` varchar(255) NOT NULL,
  `audit_queue` varchar(255) NOT NULL,
  `db_host` varchar(255) NOT NULL,
  `db_name` varchar(255) NOT NULL,
  `db_password` varchar(255) NOT NULL,
  `db_port` int(11) NOT NULL,
  `db_username` varchar(255) NOT NULL,
  `queue_type` varchar(255) DEFAULT 'SQS',
  `rabbitmq_host` varchar(255) DEFAULT NULL,
  `rabbitmq_port` int(11) DEFAULT NULL,
  `rabbitmq_vhost` varchar(255) DEFAULT NULL,
  `rabbitmq_exchange` varchar(255) DEFAULT NULL,
  `rabbitmq_username` varchar(255) DEFAULT NULL,
  `rabbitmq_password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `duracloud_user`
--

DROP TABLE IF EXISTS `duracloud_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `duracloud_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `account_non_expired` tinyint(1) NOT NULL,
  `account_non_locked` tinyint(1) NOT NULL,
  `credentials_non_expired` tinyint(1) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `root` tinyint(1) NOT NULL,
  `security_answer` varchar(255) DEFAULT NULL,
  `security_question` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `allowableipaddress_range` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_template`
--

DROP TABLE IF EXISTS `email_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `subject` varchar(1000) NOT NULL,
  `body` text,
  `template` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `global_properties`
--

DROP TABLE IF EXISTS `global_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `global_properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `instance_notification_topic_arn` varchar(255) DEFAULT NULL,
  `cloud_front_account_id` varchar(255) NOT NULL DEFAULT 'cloud front account id',
  `cloud_front_key_id` varchar(255) NOT NULL DEFAULT 'cloud front key id',
  `cloud_front_key_path` varchar(255) NOT NULL DEFAULT 'cloud front key path',
  `notifier_type` varchar(255) NOT NULL DEFAULT 'SNS',
  `rabbitmq_host` varchar(255) DEFAULT NULL,
  `rabbitmq_port` int(11) DEFAULT NULL,
  `rabbitmq_vhost` varchar(255) DEFAULT NULL,
  `rabbitmq_exchange` varchar(255) DEFAULT NULL,
  `rabbitmq_username` varchar(255) DEFAULT NULL,
  `rabbitmq_password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_user`
--

DROP TABLE IF EXISTS `group_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_user` (
  `group_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`),
  KEY `FK_ns02np32pqhrbm8cwpifjerp9` (`user_id`),
  KEY `FK_dx4jv6mpv63ufnjl3a7pec1vo` (`group_id`),
  CONSTRAINT `FK_dx4jv6mpv63ufnjl3a7pec1vo` FOREIGN KEY (`group_id`) REFERENCES `duracloud_group` (`id`),
  CONSTRAINT `FK_ns02np32pqhrbm8cwpifjerp9` FOREIGN KEY (`user_id`) REFERENCES `duracloud_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `storage_provider_account`
--

DROP TABLE IF EXISTS `storage_provider_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_provider_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `provider_type` varchar(255) DEFAULT NULL,
  `storage_limit` int(11) NOT NULL DEFAULT '1',
  `account_info_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_STORAGE_PROVIDER_ACCOUNT_INFO` (`account_info_id`),
  CONSTRAINT `FK_STORAGE_PROVIDER_ACCOUNT_INFO` FOREIGN KEY (`account_info_id`) REFERENCES `account_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `storage_provider_account_properties`
--

DROP TABLE IF EXISTS `storage_provider_account_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_provider_account_properties` (
  `storage_provider_account_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `map_value` varchar(255) DEFAULT NULL,
  `map_key` varchar(255) NOT NULL,
  PRIMARY KEY (`storage_provider_account_id`,`map_key`),
  CONSTRAINT `FK_storage_provider_account_id` FOREIGN KEY (`storage_provider_account_id`) REFERENCES `storage_provider_account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_invitation`
--

DROP TABLE IF EXISTS `user_invitation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_invitation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified` datetime DEFAULT NULL,
  `account_dep` varchar(255) DEFAULT NULL,
  `account_name` varchar(255) DEFAULT NULL,
  `account_org` varchar(255) DEFAULT NULL,
  `account_subdomain` varchar(255) DEFAULT NULL,
  `admin_username` varchar(255) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `expiration_date` datetime DEFAULT NULL,
  `redemption_code` varchar(255) DEFAULT NULL,
  `user_email` varchar(255) DEFAULT NULL,
  `account_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_nlwoj3r09ksyu15sp9ob5y8gx` (`account_id`),
  CONSTRAINT `FK_nlwoj3r09ksyu15sp9ob5y8gx` FOREIGN KEY (`account_id`) REFERENCES `account_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-03-30 10:56:06
