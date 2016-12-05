package com.unbank.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.unbank.dao.QuotasStorer;
import com.unbank.fetch.Fetcher;
@Component
/*
 * 访问过于频繁
 * */
public class AHQyxySpider {
	static String table="ah";                  
	private static String listurl = "http://www.ahcredit.gov.cn/exceptionInfoSelect.jspx";
	private static Log logger = LogFactory.getLog(AHQyxySpider.class);
	public static Fetcher fetcher = Fetcher.getInstance();
	private final static String charset = "utf-8";
	private final static Map<String, String> sqlMap = new HashMap<String, String>();
	private final static Map<String, String> params = new HashMap<String, String>();
	private static QuotasStorer quotasStorer = new QuotasStorer();
	private static int pagenum;
	public static void main(String[] args) {
		new AHQyxySpider().Spider();
	}
	public void Spider(){
		sqlMap.put("注册号/ 统一社会信用代码", "regnum");
		sqlMap.put("注册号/统一社会信用代码", "regnum");
		sqlMap.put("统一社会信用代码/注册号", "regnum");
		sqlMap.put("注册号", "regnum");
		sqlMap.put("统一社会信用代码", "creditnum");
		sqlMap.put("名称", "name");
		sqlMap.put("类型", "type");
		sqlMap.put("执行事务合伙人", "legal_person");
		sqlMap.put("投资人", "legal_person");
		sqlMap.put("经营者", "legal_person");
		sqlMap.put("负责人", "legal_person");
		sqlMap.put("法定代表人", "legal_person");
		sqlMap.put("成员出资总额", "registered_capital");
		sqlMap.put("注册资本", "registered_capital");
		sqlMap.put("注册资金", "registered_capital");
		sqlMap.put("注册日期", "establishment_date");
		sqlMap.put("成立日期", "establishment_date");
		sqlMap.put("住所", "address");
		sqlMap.put("经营场所", "address");
		sqlMap.put("营业场所", "address");
		sqlMap.put("主要经营场所", "address");
		sqlMap.put("合伙期限自", "operating_from");
		sqlMap.put("营业期限自", "operating_from");
		sqlMap.put("经营期限自", "operating_from");
		sqlMap.put("营业期限至", "operating_to");
		sqlMap.put("经营期限至", "operating_to");
		sqlMap.put("合伙期限至", "operating_to");
		sqlMap.put("经营范围", "operating_scope");
		sqlMap.put("业务范围", "operating_scope");
		sqlMap.put("登记机关", "register_gov");
		sqlMap.put("核准日期", "approval_date");
		sqlMap.put("登记状态", "register_type");
		sqlMap.put("组成形式", "composition_from");
		//sqlMap.put("吊销日期", "diao_time");
		pagenum = getPaging(listurl);
		for (int i = 105; i < pagenum; i++) {
			logger.info("现在采集到"+i+"页！");
			Map<String, String> params = new HashMap<String, String>();
			params.put("pageNo", i + "");
			params.put("gjz", "");
			String html = fetcher.post(listurl, params, null, charset);
			Document document = Jsoup.parse(html, listurl);
			Elements listElements = document.select("div.tb-b,div.tb-c");
			for (Element element : listElements) {
				Element nameElement = element.select("li.tb-a1 > a").first();
				// 详细信息
				String infoHref = nameElement.absUrl("href");
				if (!infoHref.contains("http://www.ahcredit.gov.cn/businessPublicity.jspx?id=&sourceType=1")) {
					String name = nameElement.text().trim();
					Element regcodeElement = element.select("li.tb-a2").first();
					String ent_id = regcodeElement.text().trim();
					Map<String, Object> baseMap = new HashMap<String, Object>();
					baseMap.put("name", name);
					baseMap.put("city", "安徽");
					baseMap.put("ent_id", ent_id);
					int id = quotasStorer.saveQuotas(table+"_company", baseMap);
					String infohtml = fetcher.get(infoHref, charset);
					Document infodocument = Jsoup.parse(infohtml, infoHref);
					getBaseInfo(infodocument, ent_id);
					getToziren(infodocument, ent_id);
					getUpdate(infodocument, ent_id);
					getGaoguan(infodocument, ent_id);
					getExceptInfo(infodocument, ent_id);
					// 年报
					String nianbaoUrl = infoHref.replace("businessPublicity",
							"enterprisePublicity");
					String niabaoHtml = fetcher.get(nianbaoUrl, charset);
					Document nianBaoDocument = Jsoup.parse(niabaoHtml, nianbaoUrl);
					getNianbao(nianBaoDocument, ent_id);
					
				}	
				
			}
		}

	}

	
	private int getPaging(String listurl) {
		int pagenum=0;
		String html = fetcher.post(listurl, params, null, charset);
		Document document = Jsoup.parse(html, listurl);
		String pagetext = document.select("#pages > ul:nth-child(1) > li:nth-child(13)").first().text().trim();
		pagenum= Integer
				.parseInt(pagetext.replaceAll("[^0-9]", "").trim());
		if (pagenum!=0) {
			pagenum = pagenum / 10;
		}
		pagenum =pagenum+1;
		return pagenum;
	}
	private static void getNianbao(Document nianBaoDocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = nianBaoDocument.select("#qiyenianbao");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr");
		if (trElements.size() > 2) {
			trElements.remove(0);
			trElements.remove(0);
			for (Element element : trElements) {
				Elements tdElements = element.select("td");

				Elements aElements = tdElements.get(1).select("a");
				if (aElements.size() == 0) {
					continue;
				}
				String nianbaoDetailUrl = aElements.first().absUrl("href");
				baseMap.put("year", tdElements.get(1).text().trim());
				baseMap.put("pub_date", tdElements.get(2).text().trim());
				int id = quotasStorer.saveQuotas(table+"_year_examine", baseMap);

				String niabaoHtml = fetcher.get(nianbaoDetailUrl, charset);
				Document nianBaoDetailDocument = Jsoup.parse(niabaoHtml,
						nianbaoDetailUrl);
				String text = nianBaoDetailDocument.select("#details")
						.toString();

				Map<String, Object> detailMap = new HashMap<String, Object>();

				detailMap.put("id", id);
				detailMap.put("text", text);
				quotasStorer.saveQuotas(table+"_year_examine_text", detailMap);
			}

		}
	}

	// 经营异常信息
	private static void getExceptInfo(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = infodocument.select("#excTab");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr");
		for (Element element : trElements) {
			Elements tdElements = element.select("td");
			baseMap.put("reson", tdElements.get(1).text().trim());
			baseMap.put("recod_time", tdElements.get(2).text().trim());
			baseMap.put("recod_gov", tdElements.get(5).text().trim());
			quotasStorer.saveQuotas(table+"_operat_except", baseMap);
		}

	}

	// 获取高管信息
	private static void getGaoguan(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = infodocument.select("div#memDiv>table");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr");
		for (Element element : trElements) {
			Elements tdElements = element.select("td");
			if (tdElements.size() == 4) {
				baseMap.put("name", tdElements.get(1).text().trim());
				quotasStorer.saveQuotas(table + "_senior", baseMap);
			} else {

				baseMap.put("name", tdElements.get(1).text().trim());
				baseMap.put("job", tdElements.get(2).text().trim());
				quotasStorer.saveQuotas(table + "_senior", baseMap);
				try {
					if (!tdElements.get(4).text().trim().isEmpty()) {
						baseMap.put("name", tdElements.get(4).text().trim());
						baseMap.put("job", tdElements.get(5).text().trim());
						quotasStorer.saveQuotas(table + "_senior", baseMap);
					}
				} catch (Exception e) {

					e.printStackTrace();
					System.out.println(element);
					// continue;
				}
			}
		}


	}

	// 变更信息
	private static void getUpdate(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = infodocument.select("#altDiv");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr");
		for (Element element : trElements) {
			Elements tdElements = element.select("td");
			baseMap.put("up_event", tdElements.get(0).text().trim());
			baseMap.put("pro_content", tdElements.get(1).text().trim());
			baseMap.put("up_content", tdElements.get(2).text().trim());
			baseMap.put("up_date", tdElements.get(3).text().trim());
			quotasStorer.saveQuotas(table+"_update_event", baseMap);
		}

	}

	// 股东信息
	private static void getToziren(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = infodocument.select("#invDiv");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr");
		for (Element element : trElements) {
			Elements tdElements = element.select("td");
			if (tdElements.size() >= 4) {
				baseMap.put("type", tdElements.get(3).text().trim());
				baseMap.put("name", tdElements.get(0).text().trim());
				baseMap.put("card_type", tdElements.get(1).text().trim());
				baseMap.put("card_num", tdElements.get(2).text().trim());
				quotasStorer.saveQuotas(table + "_shareholder", baseMap);
			}
		}

	}
	private void getBaseInfo(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Element baseElement = infodocument.select("#jibenxinxi > table")
				.first();
		Elements trElements = baseElement.select("tr");
		for (Element trElement : trElements) {
			Elements thElments = trElement.select("th");
			for (Element thElement : thElments) {
				String thText = thElement.text().trim();
				if (thText.isEmpty()) {
					continue;
				}
				Element tdElement = thElement.nextElementSibling();
				if (tdElement != null && tdElement.tagName().equals("td")) {
					baseMap.put(sqlMap.get(thText), tdElement.text().trim()
							.isEmpty() ? "" : tdElement.text().trim());
				}

			}
		}
		quotasStorer.saveQuotas(table+"_baseinfo", baseMap);
		
	
	}
	



}

	
	
