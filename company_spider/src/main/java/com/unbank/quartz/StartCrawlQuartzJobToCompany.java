package com.unbank.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.unbank.task.LiaoninQyxySpider;



@Component
public class StartCrawlQuartzJobToCompany {
	
	private static Log logger = LogFactory
			.getLog(StartCrawlQuartzJobToCompany.class);

	@Autowired
	LiaoninQyxySpider liaoninQyxySpider ;

	/**
	 * 定时启动任务
	 */
	public void executeInternal() {
		try {
			liaoninQyxySpider.liaoninSpider();
		} catch (Exception e) {
			logger.error("辽宁数据采集定时任务出错", e);
		}
	}

}
