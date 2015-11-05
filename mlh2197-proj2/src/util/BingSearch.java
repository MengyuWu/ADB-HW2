package util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Query;
import Classification.WebDatabaseClassification;
import Classification.getWordsLynx;


public class BingSearch {
	 //Provide your account key here. 
	 public static String ACCOUNT_KEY="L5ZA7UJt279Hm0QcBPu50yHHWRS1ZNzlifvHTiK5onw";
		
	  public static String queryTermsStr="premiership";
	  public static String site="fifa.com";
	  
	  public static long binSearch(String category) throws EncoderException, JSONException{
		  return bingSearch(queryTermsStr, site, category);
	  }
		
	  public static long bingSearch(Query q, String category) throws EncoderException, JSONException{
		  return bingSearch(q.getQuery(),q.getSite(),category);
	  }
	  
	  public static long  bingSearch(String query, String site, String category) throws EncoderException, JSONException{
		String bingUrl="https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?$top=4&$format=json";
		
		byte[] accountKeyBytes = Base64.encodeBase64((ACCOUNT_KEY+ ":" +ACCOUNT_KEY).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URLCodec urlCoder=new URLCodec();
		
		// Form the URL
		URL url=null;
		// %27(')
		try {
			//Query=%27site%3afifa.com%20premiership%27
			url = new URL(bingUrl+"&Query=%27"+"site%3a"+site+"%20"+urlCoder.encode(query)+"%27");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// Create connection object
		URLConnection urlConnection=null;
		try {
			urlConnection = url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc); // modify general request props
			
		// Now that connection has been established, retrieve the contents from the remote object 	
		InputStream inputStream=null;
		try {
			inputStream = (InputStream) urlConnection.getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		// Read the data from the input stream
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		try {
			inputStream.read(contentRaw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String content = new String(contentRaw);

		// Create a JSON object, extract JSONArray objects
		JSONObject jsonObj = new JSONObject(content);
	    JSONArray resultArray = jsonObj.getJSONObject("d").getJSONArray("results");
	    
	    JSONObject result=resultArray.getJSONObject(0);
	    long num=getWebTotal(result);
	    
	    // put into local hashmap
	    Query q=new Query(queryTermsStr,site);
	    WebDatabaseClassification.queryCacheDoc.put(q,result); // cache query result
	    WebDatabaseClassification.queryCacheCount.put(q,num); // how many results for this query
	    System.out.println("QUERY: [" + query + "]");
	    contentSummary(result, category); // this is static , need to pass in the category
	    
	    return num;
	  }
	  
	  // summarize the content of the web results for this query
	  public static void contentSummary(JSONObject obj, String category){
		JSONArray webs;
		try { // go through the web results
			webs = obj.getJSONArray("Web");
			for(int i=0; i<webs.length(); i++){
		    	JSONObject o=webs.getJSONObject(i);
		    	String url=o.getString("Url"); // get the URL of the site
		    	System.out.println("Getting page: "+url);
		    	// only add URLs that haven't already been seen
		    	if(!WebDatabaseClassification.getSample(category).contains(url)){ // pass in the hash map
		    		 WebDatabaseClassification.getSample(category).add(url);
		    	}
		    	//System.out.println("words count:"+WebDatabaseClassification.getSummary(category).size());
		     }			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	  }
	  
	  // Obtain the number of search results obtained from querying this database
	  public static long getWebTotal(JSONObject obj) throws JSONException{
		  String webTotalStr=obj.getString("WebTotal").toString();
		  long webTotal=Long.parseLong(webTotalStr);
		  return webTotal;
	  }
	  
	  

}
