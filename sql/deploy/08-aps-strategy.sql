
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

LOCK TABLES `aps_strategy` WRITE;
/*!40000 ALTER TABLE `aps_strategy` DISABLE KEYS */;
INSERT INTO `aps_strategy` VALUES (2094942648404066305,'AS0001','Timefold 实测策略','V1.0','先保交期，其次让有料工序优先开工，再压缩总完工与换型','{\"weights\": {\"dueDateDelay\": {\"hard\": 0, \"soft\": 100}, \"totalMakespan\": {\"hard\": 0, \"soft\": 10}, \"changeoverTime\": {\"hard\": 0, \"soft\": 5}, \"calendarCapacity\": {\"hard\": 1, \"soft\": 0}, \"materialShortage\": {\"hard\": 0, \"soft\": 20}, \"precedenceIdleGap\": {\"hard\": 0, \"soft\": 1}, \"priorityInversion\": {\"hard\": 0, \"soft\": 5}, \"resourceCapability\": {\"hard\": 1, \"soft\": 0}, \"shadowInconsistent\": {\"hard\": 1, \"soft\": 0}, \"operationPrecedence\": {\"hard\": 1, \"soft\": 0}, \"pinnedTaskImmutable\": {\"hard\": 1, \"soft\": 0}}, \"direction\": \"FORWARD\", \"spentLimitSeconds\": 120, \"materialConstrained\": true, \"unimprovedSpentLimitSeconds\": 30}','ENABLED',NULL,'1','2026-09-02 08:17:04','1','2026-09-03 07:28:39',_binary '\0');
/*!40000 ALTER TABLE `aps_strategy` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

