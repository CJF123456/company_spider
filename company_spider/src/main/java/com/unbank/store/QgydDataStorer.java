package com.unbank.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.unbank.mybatis.dao.MyBatisConnectionFactory;
import com.unbank.mybatis.dao.QgydDataMapper;
import com.unbank.mybatis.entity.QgydData;


public class QgydDataStorer {
	private static Log logger = LogFactory.getLog(QgydDataStorer.class);

	public void saveNewHgyd(QgydData qgyddata) {
		SqlSession sqlSession = MyBatisConnectionFactory
				.getInstanceSessionFactory().openSession();
		try {
			QgydDataMapper cityMapper = sqlSession.getMapper(QgydDataMapper.class);
			cityMapper.insertSelective(qgyddata);
			sqlSession.commit(true);
		} catch (Exception e) {
			logger.info("", e);
		} finally {
			sqlSession.close();
		}

	}
}
