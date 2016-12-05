package com.unbank.mybatis.factory;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.unbank.mybatis.entity.SQLAdapter;
import com.unbank.mybatis.mapper.SQLAdapterMapper;

public class BaseDao {

	private static Log logger = LogFactory.getLog(BaseDao.class);

	public void executeMapSQL(String sql, Map<String, Object> colums) {
		SqlSession sqlSession = DynamicConnectionFactory
				.getInstanceSessionFactory("development").openSession();
		try {
			SQLAdapterMapper sqlAdapterMaper = sqlSession
					.getMapper(SQLAdapterMapper.class);
			SQLAdapter sqlAdapter = new SQLAdapter();
			sqlAdapter.setSql(sql);
			sqlAdapter.setObj(colums);
			sqlAdapterMaper.executeMapSQL(sqlAdapter);
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("执行SQL异常", e);
			sqlSession.rollback(true);
		} finally {
			sqlSession.close();
		}
	}

	public int insertReturnPriKey(String sql, Map<String, Object> colums) {
		SqlSession sqlSession = DynamicConnectionFactory
				.getInstanceSessionFactory("development").openSession();
		int id = 0;
		try {
			SQLAdapterMapper sqlAdapterMaper = sqlSession
					.getMapper(SQLAdapterMapper.class);
			SQLAdapter sqlAdapter = new SQLAdapter();
			sqlAdapter.setSql(sql);
			System.out.println(sql);
			sqlAdapter.setObj(colums);
			sqlAdapterMaper.insertReturnPriKey(sqlAdapter);
			id = sqlAdapter.getPrikey();
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("执行SQL异常", e);
			sqlSession.rollback(true);
		} finally {
			sqlSession.close();
		}
		return id;
	}

	public void executeSQL(String sql) {
		SqlSession sqlSession = DynamicConnectionFactory
				.getInstanceSessionFactory("development").openSession();
		try {
			SQLAdapterMapper sqlAdapterMaper = sqlSession
					.getMapper(SQLAdapterMapper.class);
			SQLAdapter sqlAdapter = new SQLAdapter();
			sqlAdapter.setSql(sql);
			sqlAdapterMaper.executeSQL(sqlAdapter);
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("执行SQL异常", e);
			sqlSession.rollback(true);
		} finally {
			sqlSession.close();
		}
	}
}
