package com.local.deal.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.csvreader.CsvWriter;
//import com.famous.wxcc.common.GlobalConstant;
//import com.famous.wxcc.common.HttpClientUtil;
//import com.famous.wxcc.common.ResponseBody;

public class Thief {

	public static void main(String[] args) throws HttpException, IOException{

		File cf = new File("D:/boohee2.csv");
		CsvWriter cw = new CsvWriter(new BufferedOutputStream(new FileOutputStream(cf)), ',', Charset.forName("utf-8"));;

		List<String> paramList = new LinkedList<String>();
		paramList.add("分类");
		paramList.add("食物");
		paramList.add("热量(大卡)（每100克）");
		paramList.add("碳水化合物(克)（每100克）");
		paramList.add("脂肪(克)（每100克）");
		paramList.add("蛋白质(克)（每100克）");
		paramList.add("纤维素(克)（每100克）");

		cw.writeRecord(paramList.toArray(new String[paramList.size()]));
		cw.flush();

		Document doc = Jsoup.parse(send("http://m.boohee.com/foods"));
		Elements rows = doc.select("ul[class=\"knowledgeTagTableview clearfix\"] li a");
//		System.err.println(HttpClientUtil.get("https://m.boohee.com/foods").getResponseBodyAsString()); 
		for(int i = 0, rlen = rows.size(); i<rlen; i++){
			Element type = rows.get(i);
			int pageCount = 1;
			while(true){
				Document doc2 = Jsoup.parse(send("https://m.boohee.com"+type.attr("href")+"&page="+pageCount));
				Elements foods = doc2.select("#food-list li a");

				if(foods==null || foods.isEmpty()){
					System.err.println("==========type  "+type.text()+"  end==========");
					break;
				}
				pageCount++;

				for(int j = 0, flen = foods.size(); j<flen; j++){
					Element ftEle = foods.get(j);

					paramList = new LinkedList<String>();
					paramList.add(type.text());
					paramList.add(ftEle.select("h3").text());

					Document doc3 = Jsoup.parse(send("https://m.boohee.com"+ftEle.attr("href")));
					Elements infos = doc3.select("#foodData tbody tr td");

					paramList.add(infos.get(0).text());
					paramList.add(infos.get(3).text());
					paramList.add(infos.get(2).text());
					paramList.add(infos.get(1).text());
					paramList.add(infos.get(4).text());

					cw.writeRecord(paramList.toArray(new String[paramList.size()]));
					cw.flush();
				}
			}
		}
		System.err.println("哈哈哈哈哈=====================导出成功！");
		cw.close();
	}
	private static String send(String url) throws IOException{
		try {
			HttpClient httpClient = new HttpClient();
			HttpMethod hm = new GetMethod(url);
			HttpMethodParams param = hm.getParams();
			param.setContentCharset("UTF-8");
			httpClient.executeMethod(hm);
			if(200 == hm.getStatusCode()){
				String str = new String(hm.getResponseBody(), "UTF-8");
				hm.releaseConnection();
				return str;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
