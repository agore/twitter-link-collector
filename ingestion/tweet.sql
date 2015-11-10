DROP TABLE IF EXISTS `tweet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweet` (
  `id` bigint(20) NOT NULL,
  `name` varchar(21) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `screen_name` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `orig_name` varchar(21) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `orig_screen_name` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tweet` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(2100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `orig_avatar_url` varchar(2100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ts` datetime NOT NULL,
  `url1` varchar(2100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `url2` varchar(2100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_url` varchar(2100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `read` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
