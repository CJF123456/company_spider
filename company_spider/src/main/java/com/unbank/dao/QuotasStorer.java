package com.unbank.dao;

import java.util.Map;

import com.unbank.mybatis.factory.BaseDao;

public class QuotasStorer extends BaseDao {

	public int saveQuotas(String tableName, Map<String, Object> colums) {
		if (colums == null || colums.size() == 0) {
			return 0;
		}
		String sql = "insert into  " + tableName;
	
		return 	insertReturnPriKey(sql, colums);
	}
	
}
