package com.unbank.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.sun.xml.internal.fastinfoset.sax.Features;
import com.unbank.dao.QuotasStorer;
import com.unbank.fetch.Fetcher;
import com.unbank.fetch.Fetchers;
@Component


public class XinjiangQyxySpider {
	static String table="xinjiang";                  
	private static String listurl = "http://gsxt.xjaic.gov.cn:7001/xxcx.do";
	String url="http://gsxt.xjaic.gov.cn:7001/ztxy.do";
	private static Log logger = LogFactory.getLog(XinjiangQyxySpider.class);
	public static Fetcher fetcher = Fetcher.getInstance();
	private final static String charset = "utf-8";
	private final static Map<String, String> sqlMap = new HashMap<String, String>();
	private static QuotasStorer quotasStorer = new QuotasStorer();
	private static int pagenum;
	Features features = new Features();
	private static HashMap<String, String> headers;
	static {
		// 启动日志
		try {
			PropertyConfigurator.configure(XinjiangQyxySpider.class
					.getClassLoader().getResource("").toURI().getPath()
					+ "log4j.properties");
			logger.info("---日志系统启动成功---");
		} catch (Exception e) {
			logger.error("日志系统启动失败:", e);
		}
	}

	public static void main(String[] args) {
		Map<String, Object> baseMap =null;
		Map<String, String> headers=null;
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
		//pagenum = getPaging();
		for (int i = 2187; i <19224; i++) {
			logger.info("现在采集到"+i+"页");
			long today = System.currentTimeMillis();
			String url = listurl+"?method=ycmlIndex&random="+today+"&cxyzm=no&entnameold=&djjg=&maent.entname=&page.currentPageNo="+i+"&yzm=";
		    String html = new Fetchers().getHtml(url);
			Document document=  Jsoup.parse(html,url);
			
			Elements listElements = document.select("body > form > div > div.center-1 > div:nth-child(3)> div > ul ");
		   
			for (int j = 1; j < listElements.size(); j++) {
			    Element element= listElements.get(j);
				Elements nameElement = element.select("li.tb-a1>a");
				if (nameElement.size()>0) {
					// 详细信息
					String onclick = nameElement.attr("onclick");
					String[] unIds = onclick.split("javascript\\:doOpen");
					String unid=unIds[1].replaceAll("[^a-zA-Z0-9-]", "").trim();
					String ent_id = element.select("li.tb-a2").text().trim();
					String name = nameElement.text().trim();
					logger.info(name);
					baseMap= new HashMap<String, Object>();
					baseMap.put("name", name);
					baseMap.put("city", "新疆");
					baseMap.put("ent_id", ent_id);
					int id = quotasStorer.saveQuotas(table+"_company", baseMap);
					headers=new HashMap<String, String>();
					headers.put("method", "jyycInfo");
					headers.put("maent.pripid", unid);
					headers.put("czmk", "czmk6");
					long today1 = System.currentTimeMillis();
					headers.put("random", Long.toString(today1));
					String deurl = "http://gsxt.xjaic.gov.cn:7001/ztxy.do";
					String yichanghtml =new Fetchers().post(deurl, headers, charset);
					
					Document yichang = Jsoup.parse(yichanghtml, deurl);
					getExceptInfo(yichang, ent_id);
					
					headers.put("method", "qyInfo");
					headers.put("maent.pripid", unid);
					headers.put("czmk", "czmk1");
					long today2 = System.currentTimeMillis();
					headers.put("random", Long.toString(today2));
					
					String jibenhtml = new Fetchers().post(deurl, headers, charset);
					Document jiben = Jsoup.parse(jibenhtml, deurl);
					getBaseInfo(jiben, ent_id);
					getToziren(jiben, ent_id); //股東信息
					getUpdate(jiben, ent_id);
					
					
					headers.put("method", "baInfo");
					headers.put("maent.pripid", unid);
					headers.put("czmk", "czmk2");
					long today3 = System.currentTimeMillis();
					headers.put("random", Long.toString(today3));
					String beianhtml = new Fetchers().post(deurl, headers, charset);
					Document beian = Jsoup.parse(beianhtml, deurl);
					getGaoguan(beian, ent_id); //高管
					
					
					headers.put("method", "qygsInfo");
					headers.put("maent.pripid", unid);
					headers.put("czmk", "czmk8");
					long today4 = System.currentTimeMillis();
					headers.put("random", Long.toString(today4));
					String nianbaohtml =  new Fetchers().post(deurl, headers, charset);
					Document nianbao = Jsoup.parse(nianbaohtml, deurl);
					getNianbao(nianbao, ent_id,deurl,unid);
				}	
					
			}
				}
	}
				
	
	private static int getPaging() {
		String urll ="http://gsxt.xjaic.gov.cn:7001/xxcx.do";
		headers=new HashMap<String, String>();
		headers.put("method", "ycmlIndex");
		headers.put("cx", "yes");
		headers.put("djjg", "");
		headers.put("cxyzm", "no");
		headers.put("random", "2110098");
		String nianbaohtml =  new Fetchers().post(urll, headers, charset);
		Document document = Jsoup.parse(nianbaohtml, urll);
		String pagetext = document.select("#pages > ul:nth-child(1) > li:nth-child(15)").first().text().trim();
		pagenum= Integer
				.parseInt(pagetext.replaceAll("[^0-9]", "").trim());
		if (pagenum!=0) {
			pagenum = pagenum / 10;
		}
		pagenum =pagenum+1;
		return pagenum;
	}
	private static void getNianbao(Document nianBaoDocument, String ent_id,String deurl,String unid) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		Elements baseElements = nianBaoDocument.select("div#qynb>#sifapanding");
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
				baseMap.put("year", tdElements.get(1).text().trim());
				baseMap.put("pub_date", tdElements.get(2).text().trim());
				int id = quotasStorer.saveQuotas(table+"_year_examine", baseMap);

				Map<String, String> headers = new HashMap<String, String>();
				String nd=tdElements.get(1).text().trim().replaceAll("[^0-9]", "");
				headers.put("method", "ndbgDetail");
				headers.put("maent.pripid", unid);
				headers.put("maent.nd", nd);
				long today4 = System.currentTimeMillis();
				headers.put("random", Long.toString(today4));
				
				String niabaoHtml =  new Fetchers().post(deurl, headers, charset);
				Document nianBaoDetailDocument = Jsoup.parse(niabaoHtml,deurl);
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
		Elements  infos =infodocument.select("#table_yc");
		Elements trElements = infos.select("tr");
		int  size= trElements.size();
		if (size>3) {
		String reson= 	infos.select("tr[name=yc]>td:nth-child(2)").text();
		String recod_time= 	infos.select("tr[name=yc]>td:nth-child(3)").text();
		String recod_gov= 	infos.select("tr[name=yc]>td:nth-child(4)").text();
		String remDate= 	infos.select("tr[name=yc]>td:nth-child(5)").text();
		String remexcpresName= 	infos.select("tr[name=yc]>td:nth-child(6)").text();
		baseMap.put("reson", reson);
		baseMap.put("recod_time",recod_time);
		baseMap.put("recod_gov", recod_gov);
		baseMap.put("remDate",remDate);
		baseMap.put("remexcpresName", remexcpresName);
		quotasStorer.saveQuotas(table+"_operat_except", baseMap);
		}
		
	}

	// 获取高管信息
	private static void getGaoguan(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id );
		Elements infos = infodocument.select("div#beian>table.detailsList");
		if (infos.size() == 0) {
			return;
		}
		Element  baseElement = infos.first();
		Elements trElements = baseElement.select("tr[name=ry1]");
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
		Elements baseElements = infodocument.select("table#table_bg");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr[name=bg]");
		String pro_content=null;
		for (Element element : trElements) {
			Elements tdElements = element.select("td");
			if (tdElements.size()>0) {
				baseMap.put("up_event", tdElements.get(0).text().trim());
				pro_content=tdElements.get(1).text().trim();
				if (pro_content.contains("更多")) {
					pro_content=tdElements.get(1).select("span:nth-child(2)").text().trim().replaceAll("收起更多", "");
				}
				String up_content=tdElements.get(2).text().trim();
				if (up_content.contains("更多")) {
					   up_content=tdElements.get(2).select("span:nth-child(2)").text().trim().replaceAll("收起更多", "");
				}
				baseMap.put("pro_content",pro_content );
				baseMap.put("up_content", up_content);
				baseMap.put("up_date", tdElements.get(3).text().trim());
				quotasStorer.saveQuotas(table+"_update_event", baseMap);
			}
			
		}
	}

	// 股东信息
	private static void getToziren(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = infodocument.select("#table_fr>tbody>tr[name=fr]");
		if (baseElements.size() == 0) {
			return;
		}
		
		if(baseElements.select("td").size()<=2){
			Element baseElement = baseElements.first();
			Elements trElements = baseElement.select("tr");
			for (Element element : trElements) {
				Elements tdElements = element.select("td");
					baseMap.put("type", tdElements.get(1).text().trim());
					baseMap.put("name", tdElements.get(0 ).text().trim());
					quotasStorer.saveQuotas(table+"_shareholder", baseMap);
			}
		}else{
			Element baseElement = baseElements.first();
			Elements trElements = baseElement.select("tr");
			for (Element element : trElements) {
				Elements tdElements = element.select("td");
					baseMap.put("type", tdElements.get(3).text().trim());
					baseMap.put("name", tdElements.get(0 ).text().trim());
					baseMap.put("card_type", tdElements.get(1).text().trim());
					baseMap.put("card_num", tdElements.get(2).text().trim());
					quotasStorer.saveQuotas(table+"_shareholder", baseMap);
				
			}
		}
		
		
	}
	

	// 基本信息
	private static void getBaseInfo(Document infodocument, String ent_id) {
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
