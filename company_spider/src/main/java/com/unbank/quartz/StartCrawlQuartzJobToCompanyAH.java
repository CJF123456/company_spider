package com.unbank.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.unbank.task.AHQyxySpider;
import com.unbank.task.LiaoninQyxySpider;
import com.unbank.task.XinjiangQyxySpider;



@Component
public class StartCrawlQuartzJobToCompanyAH {
	
	private static Log logger = LogFactory
			.getLog(StartCrawlQuartzJobToCompanyAH.class);

	@Autowired
	AHQyxySpider ahQyxySpider;

	/**
	 * 定时启动任务
	 */
	public void executeInternal() {
		try {
			ahQyxySpider.Spider();
		} catch (Exception e) {
			logger.error("新疆数据采集定时任务出错", e);
		}
	}

}
