package com.unbank.tools;

import java.util.Map;
import java.util.Set;

public class MysqlTableMaker {

	public static void makeTableSql(String tableName,
			Map<String, Object> resultMap) {
		StringBuffer createTableSQL = new StringBuffer();
		createTableSQL.append("CREATE TABLE " + tableName + " ( ");
		createTableSQL.append(tableName+"id int(11) NOT NULL AUTO_INCREMENT,");
		Set<String> keyset = resultMap.keySet();
		for (String string : keyset) {
			if (string.equals("uniqueid")) {
				createTableSQL.append(string + " varchar(255) DEFAULT NULL,");
			} else {
				createTableSQL.append(string + " varchar(255) DEFAULT NULL,");
			}

		}
		createTableSQL.append("time datetime NOT NULL default  NOW(), ");
		createTableSQL.append(" PRIMARY KEY ("+tableName+"id) ,UNIQUE KEY (uniqueid)");
		createTableSQL.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		System.out.println(createTableSQL.toString());
	}
}
