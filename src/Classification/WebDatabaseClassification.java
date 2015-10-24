package Classification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.EncoderException;
import org.json.JSONException;
import org.json.JSONObject;

import util.QueryRead;
import util.BingSearch;
import entity.Query;

public class WebDatabaseClassification {
	/*rootQueries contains key: Computers, Health, Sports.  Key is category, value is the queries lists of that category
	  computerQueries contains key: Hardware and Programming
	  healthQuereis contains key:Fitness and Diseases
	  sportsQuereis contains key: Basketball and Soccer
	*/
	static HashMap<String, ArrayList<String>> rootQueries=new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> computerQueries=new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> healthQuereis=new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> sportsQuereis=new HashMap<String, ArrayList<String>>();

	public static HashMap<Query, JSONObject> queryCacheDoc=new HashMap<Query, JSONObject>();
	public static HashMap<Query, Integer> queryCacheCount=new HashMap<Query, Integer>();
	
	
	static{
		try {
			QueryRead.readQueriesOfCategory(rootQueries, "root");
			QueryRead.readQueriesOfCategory(computerQueries, "computers");
			QueryRead.readQueriesOfCategory(healthQuereis, "health");
			QueryRead.readQueriesOfCategory(sportsQuereis, "sports");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, EncoderException, JSONException {
		// TODO Auto-generated method stub
		long count=BingSearch.bingSearch("premiership","fifa.com");
		
	}

}
