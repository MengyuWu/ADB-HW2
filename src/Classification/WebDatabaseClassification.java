package Classification;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.codec.EncoderException;
import org.json.JSONException;
import org.json.JSONObject;

import util.QueryHelper;
import util.BingSearch;
import entity.Category;
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
	public static HashMap<Query, Long> queryCacheCount=new HashMap<Query, Long>();
	
	public static TreeSet<String> samples=new TreeSet<String>();
	public static TreeMap<String,Long> summary=new TreeMap<String,Long>();
	
	static{
		try {
			QueryHelper.readQueriesOfCategory(rootQueries, "root");
			QueryHelper.readQueriesOfCategory(computerQueries, "computers");
			QueryHelper.readQueriesOfCategory(healthQuereis, "health");
			QueryHelper.readQueriesOfCategory(sportsQuereis, "sports");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static long getCount(String query, String site) throws EncoderException, JSONException{
		Query q=new Query(query,site);
		if(queryCacheCount.containsKey(q)){
			return queryCacheCount.get(q);
		}
		
		return BingSearch.bingSearch(q);
		
	}
	
	
	public static String classify(Category C, String site, double tc, double ts, double ESparent) throws EncoderException, JSONException{
		String result="";
		//for all subset Ci of C, calculate  ECoverage(D,ci) and ESpecificity(D,ci)
		String mainCategory=C.getCategory();
		HashMap<String, ArrayList<String>> queryMap=getQueryMap(mainCategory);
		HashMap<String, Long> ECoverage=C.getECoverage();
		HashMap<String, Double> ESpecificity=C.getESpecificity();
		ArrayList<Category> subSet=C.getSubCategories();
		
		//base case
		if(subSet.size()==0){
			return mainCategory;
		}
		
		long total=0;
		for(Category c:subSet){
			ArrayList<String> queryList=queryMap.get(c.getCategory());
			//TODO: have "AND" list and "AND NOT"list(to ignore docs that have been included in the previous query);
			// look at http://vlaurie.com/computers2/Articles/bing_advanced_search.htm
			long count=0;
			String NOTList="";
			for(String query:queryList){
				
				//Doesn't influence much
				String andQuery=QueryHelper.queryAND(query);
				String notQuery="("+QueryHelper.queryAND(NOTList)+")";
				String q=andQuery;
				if(!NOTList.isEmpty()){
					q=q+" AND NOT "+notQuery;
				}
		
				//count+=getCount(q, site);
				//using  a And b AND NOT(c) not very good
 				count+=getCount(query, site);
				//System.out.println("query:"+ q +" count:"+count);
				NOTList+=query+" ";
				
			}
			String sub=c.getCategory();
			total+=count;
			ECoverage.put(sub, count);
		}
		
		//the content summary for mainCategory has been done
		String filename=mainCategory+"-"+site+".txt";
		System.out.println("output:"+filename);
		try {
			outputSummary(summary, filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//clean up for next level summary
		samples.clear();
		summary.clear();
		
		for(Category c:subSet){
			String sub=c.getCategory();
			long coverage=ECoverage.get(sub);
			double specificity=(double)ESparent*coverage/total;
			ESpecificity.put(sub,specificity );
			System.out.println("subCategory:"+sub+" coverage:"+coverage+" specificity:"+specificity);
			
			if(coverage>=tc && specificity>=ts){
				result+=mainCategory+"/"+classify(c,site, tc,ts, specificity);
			}
		
		}
		
		if(result.isEmpty()){
			return mainCategory;
		}
		
		
		return result;
		
	}
	
	public static void outputSummary(TreeMap<String, Long> map, String filename ) throws FileNotFoundException, IOException{
		Properties properties = new Properties(){
			@Override
		    public synchronized Enumeration<Object> keys() {
		        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
		    }
		};
//		properties.putAll(map);
//		properties.store(new FileOutputStream(filename),null);
		
		//System.out.println("map"+map);
		Set<Map.Entry<String,Long>> entries=map.entrySet();
		for(Map.Entry<String,Long> entry:entries){
			properties.put(entry.getKey(), entry.getValue().toString());
			//System.out.println("term:"+entry.getKey()+" frequency:"+entry.getValue());
		}
		properties.store(new FileOutputStream(filename), null);
	}
	
	public static HashMap<String, ArrayList<String>> getQueryMap(String category){
		if(category.equals("Root")){
			return rootQueries;
		}else if(category.equals("Computers")){
			return computerQueries;
		}else if(category.equals("Health")){
			return healthQuereis;
		}else if(category.equals("Sports")){
			return sportsQuereis;
		}else{
			return null;
		}
		
	}
	
	public static void main(String[] args) throws IOException, EncoderException, JSONException {
		// TODO Auto-generated method stub
		
		//String site="health.com";
		//String site="yahoo.com";
		//String site="fifa.com";
		//String site="diabetes.org";
		String site="hardwarecentral.com";
		double tc=100;
		double ts=0.6;
		
		//Create Category hierarchy 
		Category hardware=new Category("Hardware");
		Category programming=new Category("Programming");
		Category computers=new Category("Computers");
		computers.addSubCategory(hardware);
		computers.addSubCategory(programming);
		
		Category fitness=new Category("Fitness");
		Category diseases=new Category("Diseases");
		Category health=new Category("Health");
		health.addSubCategory(fitness);
		health.addSubCategory(diseases);
		
		Category basketball=new Category("Basketball");
		Category soccer=new Category("Soccer");
		Category sports=new Category("Sports");
		sports.addSubCategory(basketball);
		sports.addSubCategory(soccer);
		
		Category root=new Category("Root");
		root.addSubCategory(computers);
		root.addSubCategory(health);
		root.addSubCategory(sports);
	
		String category=classify(root,site, tc,ts,1);
		System.out.println(site+" "+category);
	
	}
	
	

}
