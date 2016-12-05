package com.unbank.task;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.sun.xml.internal.fastinfoset.sax.Features;
import com.unbank.dao.QuotasStorer;
import com.unbank.fetch.Fetcher;
import com.unbank.fetch.Fetchers;
@Component


public class JSQyxySpider {
	static String table="js";                  
	private static String listurl = "http://www.jsgsj.gov.cn:58888/province/NoticeServlet.json?QueryExceptionDirectory=true";
	private static String nburl="http://www.jsgsj.gov.cn:58888/ecipplatform/nbServlet.json?nbEnter=true";
	private static String yichangurl="http://www.jsgsj.gov.cn:58888/ecipplatform/commonServlet.json?commonEnter=true";
	private static	String jibenurl="http://www.jsgsj.gov.cn:58888/ecipplatform/ciServlet.json?ciEnter=true";
	private static Map<String, String> headers = new HashMap<String, String>();
	private static Log logger = LogFactory.getLog(JSQyxySpider.class);
	public static Fetcher fetcher = Fetcher.getInstance();
	private final static String charset = "utf-8";
	private final static Map<String, String> sqlMap = new HashMap<String, String>();
	private final static Map<String, String> params = new HashMap<String, String>();
	private static QuotasStorer quotasStorer = new QuotasStorer();
	private static int pagenum;
	private static long sleeptime=1000 * 5;
	private static String url;
	Features features = new Features();

public static void main(String[] args) {
	JSQyxySpider ss= new JSQyxySpider();
	ss.SHSpider();
}
	public void SHSpider(){
		sqlMap.put("注册号/ 统一社会信用代码", "regnum");
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
		sqlMap.put("注册日期", "establishment_date");
		sqlMap.put("成立日期", "establishment_date");
		sqlMap.put("住所", "address");
		sqlMap.put("经营场所", "address");
		sqlMap.put("营业场所", "address");
		sqlMap.put("主要经营场所", "address");
		sqlMap.put("合伙期限自", "operating_from");
		sqlMap.put("营业期限自", "operating_from");
		sqlMap.put("营业期限至", "operating_to");
		sqlMap.put("合伙期限至", "operating_to");
		sqlMap.put("经营范围", "operating_scope");
		sqlMap.put("登记机关", "register_gov");
		sqlMap.put("核准日期", "approval_date");
		sqlMap.put("登记状态", "register_type");
		sqlMap.put("组成形式", "composition_from");
		//pagenum = getPaging(listurl);
		for (int i = 1; i <2; i++) {
			long today3 =System.currentTimeMillis(); 
			//params.put("page", Integer.toString(i));
			params.put("tmp", Long.toString(today3));
			params.put("pageSize", "10");
			params.put("showRecordLine", "1");
			params.put("corpName", "");
			params.put("pageNo", Integer.toString(i));
		    String html= Fetchers.post(listurl, params, charset);
		   System.out.println(html);
		   JSONObject jsonObject = JSONObject.fromObject(html);
		
		   System.out.println(jsonObject);
		   JSONArray jsonArray = jsonObject.getJSONArray("items");
		   System.out.println(jsonArray);
		   System.out.println(jsonArray.size());
		   for (int j = 0; j <jsonArray.size(); j++) {
				JSONObject dataJsonObject = jsonArray.getJSONObject(j);
				String name = dataJsonObject.get("C1").toString();
				String ent_id = dataJsonObject.get("C2").toString();
				String org = dataJsonObject.get("ORG").toString();
				String ids = dataJsonObject.get("ID").toString();
				String seq_id = dataJsonObject.get("SEQ_ID").toString();
				String code_org = dataJsonObject.get("CORP_ORG").toString();
				String corp_id = dataJsonObject.get("CORP_ID").toString();
				String onclickFn=dataJsonObject.get("onclickFn").toString();
				String[] arrs=onclickFn.split(",");
				String sids=arrs[6].replaceAll("[^0-9]", "").trim();
				Map<String, Object> baseMap = new HashMap<String, Object>();
				baseMap.put("name", name);
				baseMap.put("city", "辽宁");
				baseMap.put("ent_id", ent_id);
				int id = quotasStorer.saveQuotas(table+"_company", baseMap);
				String nburl="http://www.jsgsj.gov.cn:58888/ecipplatform/nbServlet.json?nbEnter=true";
				String yichangurl="http://www.jsgsj.gov.cn:58888/ecipplatform/commonServlet.json?commonEnter=true";
 				  String jibenurl="http://www.jsgsj.gov.cn:58888/ecipplatform/ciServlet.json?ciEnter=true";
				
 				headers.clear();
				headers.put("org", code_org);
				headers.put("id", corp_id);
				headers.put("seq_id", sids);
				headers.put("specificQuery", "basicInfo");
				String basichtml= Fetchers.post(jibenurl, headers, charset);
				getBaseInfo(basichtml,ent_id);
				headers.clear();
				headers.put("CORP_ORG", org);
				headers.put("CORP_ID", corp_id);
				headers.put("CORP_SEQ_ID", sids);
				headers.put("specificQuery", "personnelInformation");
				headers.put("showRecordLine", "1");
				headers.put("pageNo", "1");
				headers.put("pageSize", "5");
				String beianhtml= Fetchers.post(jibenurl, headers, charset);
				getGaoguan(beianhtml,ent_id);
				
				headers.clear();
				headers.put("corp_org", org);
				headers.put("corp_id", corp_id);
				headers.put("corp_seq_id", sids);
				headers.put("specificQuery", "commonQuery");
				headers.put("showRecordLine", "1");
				headers.put("propertiesName", "abnormalInfor");
				long today4 =System.currentTimeMillis(); 
				headers.put("tmp", Long.toString(today4));
				headers.put("pageNo", "1");
				headers.put("pageSize", "5");
				String yichanghtml= Fetchers.post(yichangurl, headers, charset);
				getExceptInfo(yichanghtml,ent_id);
				headers.clear();
				headers.put("CORP_ORG", org);
				headers.put("CORP_ID", corp_id);
				headers.put("CORP_SEQ_ID", sids);
				headers.put("specificQuery", "investmentInfor");
				headers.put("showRecordLine", "1");
				headers.put("pageNo", "1");
				headers.put("pageSize", "5");
				String touzirenhtml= Fetchers.post(jibenurl , headers, charset);
				getTouziren(touzirenhtml,ent_id);
				
				headers.clear();
				headers.put("CORP_ORG", org);
				headers.put("CORP_ID", corp_id);
				headers.put("CORP_SEQ_ID", sids);
				headers.put("specificQuery", "investmentInfor");
				headers.put("showRecordLine", "1");
				headers.put("pageNo", "1");
				headers.put("pageSize", "5");
				String updatehtml= Fetchers.post(jibenurl , headers, charset);
				getUpdate(updatehtml,ent_id);
				
				
				headers.clear();
				headers.put("REG_NO", ent_id);
				headers.put("specificQuery","gs_pb");
				headers.put("showRecordLine", "0");
				headers.put("propertiesName","query_report_list");
				long today5 =System.currentTimeMillis(); 
				headers.put("tmp", Long.toString(today5));
				String nianbaohtml= Fetchers.post(nburl , headers, charset);
				getNianbao(nianbaohtml,ent_id);
				
		   }  
		}
	}
	
	private void getBaseInfo(String basichtml,String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		System.out.println(basichtml);
		JSONArray jsonArray = JSONArray.fromObject(basichtml);  
		for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jo = (JSONObject) jsonArray.get(i);
           String regnum=  jo.get("ID").toString();
           String name= jo.get("C2").toString();
           String type= jo.get("C3").toString();
           String legal_person= jo.get("C5").toString();
           String registered_capital= jo.get("C6").toString(); 
           String establishment_date= jo.get("C4").toString();
           String address= jo.get("C7").toString();
           String operating_from= jo.get("C9").toString();
           String operating_to= jo.get("C10").toString();
           String operating_scope= jo.get("C8").toString();
           String register_gov= jo.get("C11").toString();
           String approval_date= jo.get("C12").toString();
           String register_type= jo.get("C13").toString();
		baseMap.put("regnum", regnum);
		baseMap.put("name", name);
		baseMap.put("type", type);
		baseMap.put("legal_person", legal_person);
		baseMap.put("registered_capital", registered_capital);
		baseMap.put("establishment_date", establishment_date);
		baseMap.put("address", address);
		baseMap.put("operating_from", operating_from);
		baseMap.put("operating_to", operating_to);
		baseMap.put("operating_scope", operating_scope);
		baseMap.put("register_gov", register_gov);
		baseMap.put("approval_date", approval_date);
		baseMap.put("register_type", register_type);
		baseMap.put("ent_id", ent_id);
		
		quotasStorer.saveQuotas(table+"_baseinfo", baseMap);

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
		Elements elements = document.select("#upapges > li:nth-child(14)");
	String	pagetext= elements.first().text().trim();
				
		pagenum= Integer
				.parseInt(pagetext.replaceAll("[^0-9]", "").trim());
		if (pagenum!=0) {
			pagenum = pagenum / 10;
		}
		pagenum =pagenum+1;
		return pagenum;
	}

	

	//年報信息
	private static void getNianbao(String nianbaohtml,String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		JSONArray jsonArray = JSONArray.fromObject(nianbaohtml);  
		for (int i = 0; i < jsonArray.size(); i++) {
           JSONObject jo = (JSONObject) jsonArray.get(i);
           String REPORT_RESULT=  jo.get("REPORT_RESULT").toString();
           String REPORT_DATE=  jo.get("REPORT_DATE").toString();
           String nianid=  jo.get("ID").toString();
		   baseMap.put("pub_date", REPORT_RESULT);
		   baseMap.put("year",REPORT_DATE);
		   int ids = quotasStorer.saveQuotas(table+"_year_examine", baseMap);
		   getNianbaoText(nianid,ids);
		}
	}

	private static void getNianbaoText(String artid,Integer ids) {
		headers.clear();
		headers.put("ID", artid);
		headers.put("showRecordLine", "0");
		headers.put("specificQuery","gs_pb");
		headers.put("propertiesName","query_ById");
		long today4 =System.currentTimeMillis(); 
		headers.put("tmp",Long.toString(today4));
		String nianbaohtml= Fetchers.post(nburl , headers, charset);
		System.out.println(nianbaohtml);
		Map<String, Object> detailMap = new HashMap<String, Object>();
		detailMap.put("id", ids);
		detailMap.put("text", nianbaohtml);
		quotasStorer.saveQuotas(table+"_year_examine_text", detailMap);
		
	}

	// 经营异常信息
	private static void getExceptInfo(String yichanghtml, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		JSONObject jsonObject = JSONObject.fromObject(yichanghtml);
		   JSONArray jsonArray = jsonObject.getJSONArray("items");
		   int size=jsonArray.size();
		   if (size>0) {
			   for (int j = 0; j <size; j++) {
					JSONObject dataJsonObject = jsonArray.getJSONObject(j);
					String reson = dataJsonObject.get("C1").toString();
					String recod_time = dataJsonObject.get("C2").toString();
					String remDate = dataJsonObject.get("C3").toString();
					String remexcpresName = dataJsonObject.get("C4").toString();
					String recod_gov = dataJsonObject.get("C5").toString();
				baseMap.put("reson", reson);
				baseMap.put("recod_time",recod_time);
				baseMap.put("recod_gov", recod_gov);
				baseMap.put("remDate", remDate);
				baseMap.put("remexcpresName", remexcpresName);
				quotasStorer.saveQuotas(table+"_operat_except", baseMap);
		      }
		}
		   
	}

	// 获取高管信息
	private static void getGaoguan(String beianhtml, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		//items
		JSONObject jsonObject = JSONObject.fromObject(beianhtml);
		
		   System.out.println(jsonObject);
		   JSONArray jsonArray = jsonObject.getJSONArray("items");
		 //  JSONObject  item =JSONObject.fromObject( jsonArray.get(0));
		  // quotasStorer.saveQuotas(table+"_operat_except", baseMap);
	}

	// 变更信息
	private static void getUpdate(String updatehtml, String ent_id) {
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		JSONObject jsonObject = JSONObject.fromObject(updatehtml);
		   JSONArray jsonArray = jsonObject.getJSONArray("items");
		   int size=jsonArray.size();
		   if (size >0) {
			   for (int j = 0; j <size; j++) {
					JSONObject dataJsonObject = jsonArray.getJSONObject(j);
		/*	String altaf = jsonObject.get("altaf").toString();
			String altbe = jsonObject.get("altbe").toString();
			String altitemName = jsonObject.get("altitemName").toString();
			String altdate = jsonObject.get("altdate").toString();
			baseMap.put("up_event", altitemName);
			baseMap.put("pro_content", altaf);
			baseMap.put("up_content", altbe);
			baseMap.put("up_date", altdate);*/
			quotasStorer.saveQuotas(table+"_update_event", baseMap);
			   }
	   }
	}

	// 股东信息
	private static void getTouziren(String touzirenhtml, String ent_id) {
		System.out.println(touzirenhtml);
		Map<String, Object> baseMap = new HashMap<String, Object>();
		baseMap.put("ent_id", ent_id);
		JSONObject jsonObject = JSONObject.fromObject(touzirenhtml);
		   JSONArray jsonArray = jsonObject.getJSONArray("items");
		   int size=jsonArray.size();
		   if (size>0) {
			   for (int j = 0; j <size; j++) {
					JSONObject dataJsonObject = jsonArray.getJSONObject(j);
					String type = dataJsonObject.get("C1").toString();
					String name = dataJsonObject.get("C2").toString();
					String card_type = dataJsonObject.get("C3").toString();
					String card_num = dataJsonObject.get("C4").toString();
				baseMap.put("type", type);
				baseMap.put("name", name);
				baseMap.put("card_type", card_type);
				baseMap.put("card_num", card_num);
				quotasStorer.saveQuotas(table+"_shareholder", baseMap);
		      }
		   	}
	}

}
