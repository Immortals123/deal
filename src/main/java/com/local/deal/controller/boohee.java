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
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.csvreader.CsvWriter;

public class boohee {
    
    public static void main(String[] args) throws HttpException, IOException{
    	File cf = new File("D:/boohee.csv");
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
		
        String page1 = send("http://www.boohee.com/food/");
        Document doc = Jsoup.parse(page1);
        Elements rows = doc.select("ul[class=\"row\"] li[class=\"col-md-4 col-sm-4 col-xs-12 item\"] div[class=\"text-box\"] h3 a");
        for(int i = 0, rlen = rows.size(); i<rlen; i++){
            Element type = rows.get(i);
            if(type != null){
                String href = "http://www.boohee.com/" + type.attr("href");
                for(int j=1;j<=10;++j){
                	String page2 = send(href);
                	Document doc2 = Jsoup.parse(page2);
                	Elements sws = doc2.select("div[class=\"widget-food-list pull-right\"] ul[class=\"food-list\"] li[class=\"item clearfix\"] div[class=\"text-box pull-left\"] h4 a");
                    for(Element sw : sws){
                    	String page3 = send("http://www.boohee.com/" + sw.attr("href"));
                    	Document doc3 = Jsoup.parse(page3);
                    	Elements infos = doc3.select("div[class=\"content\"] dl dd span[class=\"dd\"]");
                    	
                    	paramList = new LinkedList<String>();
                        paramList.add(type.text());
                        paramList.add(sw.text());
                    	paramList.add(infos.get(2).text());
                    	paramList.add(infos.get(3).text());
                    	paramList.add(infos.get(4).text());
                    	paramList.add(infos.get(5).text());
                    	paramList.add(infos.get(6).text());
                    	
                    	cw.writeRecord(paramList.toArray(new String[paramList.size()]));
                		cw.flush();
                    }
                    Elements nextA = doc2.select("div[class=\"widget-food-list pull-right\"] div[class=\"widget-pagination\"] a[class=\"next_page\"]");
                    href = "http://www.boohee.com/" + nextA.attr("href");
                }
            }
        }
        System.err.println("哈哈哈哈哈=====================导出成功！");
		cw.close();
    }
    private static String send(String url) throws IOException{
		try {
			HttpClient httpClient = new HttpClient();
	    	HttpMethod hm = new PostMethod(url);
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
