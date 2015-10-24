package util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Query;
import Classification.WebDatabaseClassification;


public class BingSearch {
	//Provide your account key here. 
	   static String ACCOUNT_KEY="pb71DGWbKoLI5Vki6bTSeAIM4otYkmdXMqSV+s/WvP0";
		
	  public static String queryTermsStr="premiership";
	  public static String site="fifa.com";
	  
	  public static long binSearch() throws EncoderException, JSONException{

		  return bingSearch(queryTermsStr, site);
	  }
		
	  public static long bingSearch(Query q) throws EncoderException, JSONException{
		  return bingSearch(q.getQuery(),q.getSite());
	  }
	  
	  public static long  bingSearch(String query, String site) throws EncoderException, JSONException{
		
		String bingUrl="https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?$top=10&$format=json";
		
		byte[] accountKeyBytes = Base64.encodeBase64((ACCOUNT_KEY+ ":" +ACCOUNT_KEY).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URLCodec urlCoder=new URLCodec();
		
		URL url=null;
		// %27(')
		try {
		
			//Query=%27site%3afifa.com%20premiership%27
			url = new URL(bingUrl+"&Query=%27"+"site%3a"+site+"%20"+urlCoder.encode(query)+"%27");
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		}
		
		URLConnection urlConnection=null;
		try {
			urlConnection = url.openConnection();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
				
		InputStream inputStream=null;
		try {
			inputStream = (InputStream) urlConnection.getContent();
		} catch (IOException e) {
		
			e.printStackTrace();
		}		
		
		
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		try {
			inputStream.read(contentRaw);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		String content = new String(contentRaw);
		//System.out.println("content "+content);
		JSONObject jsonObj = new JSONObject(content);
	    JSONArray resultArray = jsonObj.getJSONObject("d").getJSONArray("results");
	    //System.out.println("jsonarray:"+resultArray);
	    
	    JSONObject result=resultArray.getJSONObject(0);
	    //System.out.println("result:"+result);
	    long num=getWebTotal(result);
	    
	    //put into local hashmap
	    Query q=new Query(queryTermsStr,site);
	    WebDatabaseClassification.queryCacheDoc.put(q,result);
	    WebDatabaseClassification.queryCacheCount.put(q,num);
	    
	    return num;
		
	  }
	  
	  public static long getWebTotal(JSONObject obj) throws JSONException{
		  String webTotalStr=obj.getString("WebTotal").toString();
		  long webTotal=Long.parseLong(webTotalStr);
		  //System.out.println("webTotal:"+webTotal);
		  return webTotal;
	  }
	  
	  

}
