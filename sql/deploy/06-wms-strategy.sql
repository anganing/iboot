
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

LOCK TABLES `wms_strategy` WRITE;
/*!40000 ALTER TABLE `wms_strategy` DISABLE KEYS */;
INSERT INTO `wms_strategy` VALUES (209703000000000701,'ST-PUT-ASRS','立库就近上架策略','PUTAWAY',209703000000000101,'V1','{\"sorts\": [{\"field\": \"aisleNo\", \"order\": \"asc\"}, {\"field\": \"levelNo\", \"order\": \"asc\"}], \"conditions\": [{\"op\": \"in\", \"field\": \"matType\", \"value\": [\"RAW\"]}], \"dimensionCheck\": \"FULL_DIM\"}',100,'ENABLED','优先低层近巷道，降低堆垛机行程','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
INSERT INTO `wms_strategy` VALUES (209703000000000702,'ST-PUT-FLAT','平库分区上架策略','PUTAWAY',209703000000000102,'V1','{\"sorts\": [{\"field\": \"locCode\", \"order\": \"asc\"}], \"conditions\": [{\"op\": \"eq\", \"field\": \"zoneType\", \"value\": \"RAW\"}], \"allowMixedLot\": true}',80,'ENABLED','按储位编码顺序上架','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
INSERT INTO `wms_strategy` VALUES (209703000000000703,'ST-PICK-FEFO','先到期先出拣选策略','PICK',0,'V2','{\"sorts\": [{\"field\": \"expiryDate\", \"order\": \"asc\"}, {\"field\": \"produceDate\", \"order\": \"asc\"}], \"conditions\": [{\"op\": \"eq\", \"field\": \"qualityStatus\", \"value\": \"QUALIFIED\"}]}',100,'ENABLED','全局默认，辅料化工品必须 FEFO','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
INSERT INTO `wms_strategy` VALUES (209703000000000704,'ST-PICK-FIFO','先进先出拣选策略','PICK',209703000000000101,'V1','{\"sorts\": [{\"field\": \"createTime\", \"order\": \"asc\"}], \"conditions\": [{\"op\": \"eq\", \"field\": \"qualityStatus\", \"value\": \"QUALIFIED\"}]}',90,'ENABLED','立库硅钢片按入库时间出库','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
INSERT INTO `wms_strategy` VALUES (209703000000000705,'ST-REP-LINE','线边仓补货策略','REPLENISH',209703000000000104,'V1','{\"conditions\": [{\"op\": \"LT\", \"field\": \"qtyAvailable\", \"value\": 50}], \"fromWhCode\": \"WH-FLAT01\", \"replenishQty\": 200}',100,'ENABLED','线边可用低于 50 触发补货 200','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
INSERT INTO `wms_strategy` VALUES (209703000000000706,'ST-PICK-FEFO','先到期先出拣选策略','PICK',0,'V1','{\"sorts\": [{\"field\": \"expiryDate\", \"order\": \"asc\"}], \"conditions\": []}',100,'DISABLED','V2 上线后停用的旧版本','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
INSERT INTO `wms_strategy` VALUES (209703000000000707,'ST-PUT-FG','成品上架策略','PUTAWAY',209703000000000103,'V1','{\"sorts\": [{\"field\": \"locCode\", \"order\": \"asc\"}], \"conditions\": [{\"op\": \"eq\", \"field\": \"matType\", \"value\": \"FINISHED\"}]}',70,'DRAFT','成品区上架规则草稿待评审','1','2026-08-31 20:02:17','1','2026-08-31 20:02:17',_binary '\0');
/*!40000 ALTER TABLE `wms_strategy` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

