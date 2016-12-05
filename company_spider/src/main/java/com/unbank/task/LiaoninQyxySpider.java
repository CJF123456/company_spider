package com.unbank.task;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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

/**
 * @name  辽宁
 * @author Administrator
 * @result 已完结
 */
public class LiaoninQyxySpider {
	static String table="liaonin";                  
	private static String listurl = "http://gsxt.lngs.gov.cn/saicpub/entPublicitySC/entPublicityDC/getJyycmlxx.action";
	private static String url = "http://gsxt.lngs.gov.cn/saicpub/entPublicitySC/entPublicityDC/";
	private static Log logger = LogFactory.getLog(LiaoninQyxySpider.class);
	public static Fetcher fetcher = Fetcher.getInstance();
	private final static String charset = "utf-8";
	private final static Map<String, String> sqlMap = new HashMap<String, String>();
	private final static Map<String, String> params = new HashMap<String, String>();
	private static QuotasStorer quotasStorer = new QuotasStorer();
	private static int pagenum;
	private static long sleeptime=500 * 1;
	private static long sleeptime1=1000 * 1;
	public static void main(String[] args) {
		new LiaoninQyxySpider().liaoninSpider();
	}
	public void liaoninSpider(){
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
		/*sqlMap.put("吊销日期", "diao_time");*/
		pagenum = getPaging(listurl);
		for (int i =1; i <5; i++) {
			logger.info("现在采集到"+i+"页");
			params.put("num", Integer.toString(i));
			String html = fetcher.post(listurl, params, null, charset);
			Document document = Jsoup.parse(html, listurl);
			Elements listElements = document.select(".tb-b > ul");
			for (int j = 0; j < listElements.size(); j++) {
			    Element element= listElements.get(j);
				Elements nameElement = element.select("li.tb-a1>a");
				// 详细信息
				if (nameElement.size() > 0) {
					String onclick = nameElement.attr("onclick");
					String[] unIds = onclick.split(",");
					String unId = unIds[0].replaceAll("[^a-zA-Z0-9-]", "").trim().replaceAll("detailYcjyml", "").trim();
					String ent_id = unIds[1].replaceAll("[^a-zA-Z0-9]", "").trim();
					String type = unIds[2].replaceAll("[^a-zA-Z0-9]", "").trim();
					String name = nameElement.text().trim();
					Map<String, Object> baseMap = new HashMap<String, Object>();
					baseMap.put("name", name);
					baseMap.put("city", "辽宁");
					baseMap.put("ent_id", ent_id);
					int id = quotasStorer.saveQuotas(table+"_company", baseMap);
					String yichangurl = url+"getJyycxxAction.action?pripid="+unId+"&type="+type;
					String beianurl = url+"getZyryxxAction.action?pripid="+unId+"&type="+type;
					String jibenurl = url+"getJbxxAction.action?pripid="+unId+"&type="+type;
					String nianbaourl = url+"getQygsQynbxxAction.action?pripid="+unId+"&type="+type;
					String touzirenurl = url+"getTzrxxAction.action?pripid="+unId+"&type="+type;
					String updateurl = url+"getBgxxAction.action?pripid="+unId+"&type="+type;
					
					String yichanghtml = fetcher.get(yichangurl, charset);
					Document yichang = Jsoup.parse(yichanghtml, yichangurl);
					getExceptInfo(yichang, ent_id);
					
					String beianhtml = fetcher.get(beianurl,charset);
					Document beian = Jsoup.parse(beianhtml, beianurl);
					getGaoguan(beian, ent_id,type); //高管
				
					String jibenhtml = fetcher.get(jibenurl, charset);
					Document jiben = Jsoup.parse(jibenhtml, jibenurl);
					getBaseInfo(jiben, ent_id);
					
					String touzihtml = fetcher.get(touzirenurl, charset);
					Document touzi = Jsoup.parse(touzihtml, touzirenurl);
					
					getToziren(touzi, ent_id); //股東信息
					String updatehtml = fetcher.get(updateurl, charset);
					
					Document update = Jsoup.parse(updatehtml, updateurl);
					getUpdate(update, ent_id);
					
					String nianbaohtml = fetcher.get(nianbaourl, charset);
					
					Document nianbao = Jsoup.parse(nianbaohtml, nianbaourl);
					getNianbao(nianbao, ent_id,type);
					//getGudongchuzi(nianBaoDocument, ent_id);	 股东出资
				}
				
			}
		}
	}

	/**
	 * 分页		
	 * @param listurl 列表URl
	 * @return 总页数
	 */
	private static int getPaging( String listurl) {
		int pagenum=0;
		String html = fetcher.post(listurl, params, null, charset);
		Document document = Jsoup.parse(html, listurl);
		String pagetext = document.select("#pages > ul > li:nth-child(13)").first().text().trim();
		pagenum= Integer
				.parseInt(pagetext.replaceAll("[^0-9]", "").trim());
		if (pagenum!=0) {
			pagenum = pagenum / 10;
		}
		pagenum =pagenum+1;
		return pagenum;
	}

	private static void getGudongchuzi(Document nianBaoDocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		Elements baseElements = nianBaoDocument.select("#gdDiv");
		if (baseElements.size() == 0) {
			return;
		}
		Element baseElement = baseElements.first();
		Elements trElements = baseElement.select("tr");
		if (trElements.size() > 3) {
			trElements.remove(0);
			trElements.remove(0);
			trElements.remove(0);
			for (Element element : trElements) {
				Elements tdElements = element.select("td");
				baseMap.put("name", tdElements.get(0).text().trim());
				baseMap.put("renpay", tdElements.get(1).text().trim());
				baseMap.put("shipay", tdElements.get(2).text().trim());
				baseMap.put("ren_type", tdElements.get(3).text().trim());
				baseMap.put("ren_zijin", tdElements.get(4).text().trim());
				baseMap.put("ren_time", tdElements.get(5).text().trim());
				baseMap.put("ren_pubtime", tdElements.get(6).text().trim());
				baseMap.put("shi_type", tdElements.get(7).text().trim());
				baseMap.put("shi_zijin", tdElements.get(8).text().trim());
				baseMap.put("shi_time", tdElements.get(9).text().trim());
				baseMap.put("shi_pubtime", tdElements.get(10).text().trim());
				int id = quotasStorer.saveQuotas(table+"_shareholderchuzi", baseMap);
			}

		}
	}

	//年報信息
	private static void getNianbao(Document nianBaoDocument, String ent_id,String type) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		String  infos =nianBaoDocument.toString();
		String  info1[]=infos.split("\\(\\[\\{");
		     int size=info1.length;
		     String  info=null;
		     if (size>1) {
		    	 info=info1[1];
		    	 info="[{"+info;
			     info=info.split("\\}\\)\\;")[0].split("\\}\\]\\,")[0];
			     info=info+"}]";
			     JSONArray treeArray = JSONArray.fromObject(info);
		for (Object object : treeArray) {
			JSONObject jsonObject = JSONObject.fromObject(object);
			String date = jsonObject.get("anchedateStr").toString();
			String ancheyear = jsonObject.get("ancheyear").toString();
			String artid=jsonObject.getString("artid").toString();
			baseMap.put("pub_date", date);
			baseMap.put("year",ancheyear);
			int ids = quotasStorer.saveQuotas(table+"_year_examine", baseMap);
			getNianbaoText(ids,type,artid);
		}
		}
		
	}

	private static void getNianbaoText(Integer ids,String type,String artid) {
		String nianbaourl = url+"nbDeatil.action?artId="+artid+"&entType="+type;
		String niabaoHtml = fetcher.get(nianbaourl, charset);
		Document nianBaoDetailDocument = Jsoup.parse(niabaoHtml,
				nianbaourl);
		String text = nianBaoDetailDocument.select(".detailsList")
				.toString();
		Map<String, Object> detailMap = new HashMap<String, Object>();
		detailMap.put("id", ids);
		detailMap.put("text", text);
		quotasStorer.saveQuotas(table+"_year_examine_text", detailMap);
		
	}

	// 经营异常信息
	private static void getExceptInfo(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		String  infos =infodocument.toString();
		String  info1[]=infos.split("\\(\\[\\{");
		     int size=info1.length;
		     String  info=null;
		     if (size>1) {
		    	 info=info1[1];
		    	 info="[{"+info;
			     info=info.split("\\}\\)\\;")[0].split("\\}\\]\\,")[0];
			     info=info+"}]";
			     JSONArray treeArray = JSONArray.fromObject(info);
	      for (Object object : treeArray) {
			JSONObject jsonObject = JSONObject.fromObject(object);
			String lrregorgName = jsonObject.get("lrregorgName").toString();
			String remexcpresName = jsonObject.get("remexcpresName").toString();
			String abnDate = jsonObject.get("abnDate").toString();
			String specauseName = jsonObject.get("specauseName").toString();
			String remDate = jsonObject.get("remDate").toString();
			baseMap.put("reson", specauseName);
			baseMap.put("recod_time",abnDate);
			baseMap.put("recod_gov", lrregorgName);
			baseMap.put("remDate", remDate);
			baseMap.put("remexcpresName", remexcpresName);
			quotasStorer.saveQuotas(table+"_operat_except", baseMap);
	      }
		     }
	}

	// 获取高管信息
	private static void getGaoguan(Document infodocument, String ent_id,String type) {
		String name=null;
		String job=null;
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id );
		String  infos =infodocument.toString();
		   String  info1[]=infos.split("\\(\\[\\{");
		     int size=info1.length;
		     String  info=null;
		     if (size>1) {
		    	 info=info1[1];
		    	 info="[{"+info;
			     info=info.split("\\}\\)\\;")[0].split("\\}\\]\\,")[0];
			     info=info+"}]";
			     JSONArray treeArray = JSONArray.fromObject(info);
			     for (Object object : treeArray) {
			    	 JSONObject jsonObject = JSONObject.fromObject(object);
			    	 if (type.contains("9100")||type.equals("9100")) {
			    		 name=jsonObject.get("inv").toString();
			 		}else{
			 			name = jsonObject.get("name").toString();
			 			job = jsonObject.get("positionName").toString();
			 			baseMap.put("job",job);
			 		}
			    	
				    baseMap.put("name", name);
				  
				    quotasStorer.saveQuotas(table+"_senior", baseMap);
			    	 
			     }
		     
	      }
	}

	// 变更信息
	private static void getUpdate(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		String  infos =infodocument.toString();
		String  info1[]=infos.split("\\(\\[\\{");
		     int size=info1.length;
		     String  info=null;
		     if (size>1) {
		    	 info=info1[1];
		    	 info="[{"+info;
			     info=info.split("\\}\\)\\;")[0].split("\\}\\]\\,")[0];
			     info=info+"}]";
			     JSONArray treeArray = JSONArray.fromObject(info);
	      for (Object object : treeArray) {
			JSONObject jsonObject = JSONObject.fromObject(object);
			String altaf = jsonObject.get("altaf").toString();
			String altbe = jsonObject.get("altbe").toString();
			String altitemName = jsonObject.get("altitemName").toString();
			String altdate = jsonObject.get("altdate").toString();
			baseMap.put("up_event", altitemName);
			baseMap.put("pro_content", altaf);
			baseMap.put("up_content", altbe);
			baseMap.put("up_date", altdate);
			quotasStorer.saveQuotas(table+"_update_event", baseMap);
	      }
		     }
	}

	// 股东信息
	private static void getToziren(Document infodocument, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		String  infos =infodocument.toString();
		   String  info1[]=infos.split("\\(\\[\\{");
		     int size=info1.length;
		     String  info=null;
		     if (size>1) {
		    	 info=info1[1];
		    	 info="[{"+info;
			     info=info.split("\\}\\)\\;")[0].split("\\}\\]\\,")[0];
			     info=info+"}]";
			     JSONArray treeArray = JSONArray.fromObject(info);
	      for (Object object : treeArray) {
			JSONObject jsonObject = JSONObject.fromObject(object);
			String type = jsonObject.get("invtypeName").toString();
			String name = jsonObject.get("inv").toString();
			String blictypeName = jsonObject.get("blictypeName").toString();
			String dom = jsonObject.get("dom").toString();
			baseMap.put("type", type);
			baseMap.put("name", name);
			baseMap.put("card_type", blictypeName);
			baseMap.put("dom", dom);
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
				if (thText.contains("吊销日期")) {
					continue;
				}
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
