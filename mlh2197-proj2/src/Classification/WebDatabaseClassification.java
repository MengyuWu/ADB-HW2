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

	// store query results
	public static HashMap<Query, JSONObject> queryCacheDoc=new HashMap<Query, JSONObject>();

	// store number of search results returned by queries
	public static HashMap<Query, Long> queryCacheCount=new HashMap<Query, Long>();

	// have a summary and sample for each non-leaf category
	public static TreeSet<String> rootSample=new TreeSet<String>();
	public static TreeMap<String,Long> rootSummary=new TreeMap<String,Long>();
	public static TreeSet<String> computerSample=new TreeSet<String>();
	public static TreeMap<String,Long> computerSummary=new TreeMap<String,Long>();
	public static TreeSet<String> healthSample=new TreeSet<String>();
	public static TreeMap<String,Long> healthSummary=new TreeMap<String,Long>();
	public static TreeSet<String> sportsSample=new TreeSet<String>();
	public static TreeMap<String,Long> sportsSummary=new TreeMap<String, Long>();

	// do a generic one for the leaf category
	public static TreeSet<String> sample=new TreeSet<String>();
	public static TreeMap<String,Long> summary=new TreeMap<String,Long>();

	static{ // Build the hash maps
		try {
			QueryHelper.readQueriesOfCategory(rootQueries, "root");
			QueryHelper.readQueriesOfCategory(computerQueries, "computers");
			QueryHelper.readQueriesOfCategory(healthQuereis, "health");
			QueryHelper.readQueriesOfCategory(sportsQuereis, "sports");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static TreeSet<String> getSample(String category) {
		if (category.equals("Root")) {
			System.out.println("Writing to root sample.");
			return rootSample;
		} else if (category.equals("Computers")) {
			System.out.println("Writing to computers sample.");
			return computerSample;
		} else if (category.equals("Health")) {
			System.out.println("Writing to health sample.");
			return healthSample;
		} else if (category.equals("Sports")) {
			System.out.println("Writing to sports sample.");
			return sportsSample;
		} else {
			return sample;
		}
	}

	public static TreeMap<String,Long> getSummary(String category) {
		if (category.equals("Root")) {
			return rootSummary;
		} else if (category.equals("Computers")) {
			return computerSummary;
		} else if (category.equals("Health")) {
			return healthSummary;
		} else if (category.equals("Sports")) {
			return sportsSummary;
		} else {
			return summary;
		}
	}

	// Query using the Bing API, return number of search results (get previous searches from the cache)
	public static long getCount(String query, String site, String category) throws EncoderException, JSONException{
		Query q=new Query(query,site);
		if(queryCacheCount.containsKey(q)){
			return queryCacheCount.get(q);
		}
		return BingSearch.bingSearch(q, category);
	}
	
	// Carries out the algorithm in Fig. 4 of the QProber paper and constructs content summaries
	public static String classify(Category C, String site, double tc, double ts, double ESparent) throws EncoderException, JSONException{
		String result="";
		//for all subset Ci of C, calculate ECoverage(D,ci) and ESpecificity(D,ci)
		String mainCategory=C.getCategory();
		HashMap<String, ArrayList<String>> queryMap=getQueryMap(mainCategory);
		HashMap<String, Long> ECoverage=C.getECoverage();
		HashMap<String, Double> ESpecificity=C.getESpecificity();
		ArrayList<Category> subSet=C.getSubCategories();
		
		//base case, there are no subcategories, return this category
		if(subSet.size()==0){
			return mainCategory;
		}
		
		long total=0;
		for(Category c:subSet){ // go through each of the category's subcategories
			ArrayList<String> queryList=queryMap.get(c.getCategory());
			// have "AND" list and "AND NOT" list (to ignore docs that have been included in the previous query)
			// look at http://vlaurie.com/computers2/Articles/bing_advanced_search.htm
			long count=0;
			String NOTList="";
			for(String query:queryList){
				//Exclude the queries that have already been done, doesn't influence much
				String andQuery=QueryHelper.queryAND(query);
				//System.out.println("AND QUERY: " + andQuery);
				String notQuery="("+QueryHelper.queryAND(NOTList)+")";
				//System.out.println("NOT QUERY: " + notQuery);
				String q=andQuery;
				if(!NOTList.isEmpty()){
					q=q+" AND NOT "+notQuery;
				}
		
				//using a And b AND NOT(c) not very good
 				count+=getCount(query, site, mainCategory);
				//System.out.println("query:"+ q +" count:"+count);
				NOTList+=query+" ";	
			}
			String sub=c.getCategory();
			total+=count;
			ECoverage.put(sub, count); // Record coverage in the DB for this category
		}
		
		for(Category c:subSet){
			String sub=c.getCategory();
			long coverage=ECoverage.get(sub);
			double specificity=(double)ESparent*coverage/total;
			ESpecificity.put(sub,specificity);
			System.out.println("subCategory:"+sub+" coverage:"+coverage+" specificity:"+specificity);
			
			// If coverage, specificity criteria met, go one level deeper
			if(coverage>=tc && specificity>=ts){
				result+=mainCategory+"/"+classify(c,site, tc,ts, specificity);
			}
		}
		if(result.isEmpty()){ // Cannot classify more specifically than this category 
			return mainCategory;
		}
		
		return result;
	}
	
	// store the content summary (in word=count format) into the appropriate text file
	public static void outputSummary(TreeMap<String, Long> map, String filename ) throws FileNotFoundException, IOException{
		Properties properties = new Properties(){
			@Override
		    public synchronized Enumeration<Object> keys() {
		        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
		    }
		};

		Set<Map.Entry<String,Long>> entries=map.entrySet();
		for(Map.Entry<String,Long> entry:entries){
			properties.put(entry.getKey(), entry.getValue().toString());
		}
		properties.store(new FileOutputStream(filename), null);
	}
	
	// Get the appropriate query map for the category
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

	// Generate content summary based on the set of URLs returned by probes for the category
	public static void generateSummary(String category) {
		TreeSet<String> currentSet = getSample(category);
		for (String url:currentSet) {
			TreeSet<String> set=(TreeSet) getWordsLynx.runLynx(url); // get the set of words in the doc
			for(String w:set){ // take a count of each word in the set
		   		if(WebDatabaseClassification.getSummary(category).containsKey(w)){
		    		long count=WebDatabaseClassification.getSummary(category).get(w);
		    		WebDatabaseClassification.getSummary(category).put(w,count+1);
		    	} else{
		    		WebDatabaseClassification.getSummary(category).put(w,(long)1);
		   		}
			}
		}
	}

	// Merge two treeSets (root w/ one of its subcategories)
	public static void mergeSet(TreeSet<String> other) {
		for (String url:other) {
			System.out.println("url: " + url);
			if (!rootSample.contains(url)) {
				rootSample.add(url);
				System.out.println("Added new url to root: " + url);
			}
		}
	}

	public static void main(String[] args) throws IOException, EncoderException, JSONException {
		// Error checking for command-line arguments
		if(args.length<4){
			System.out.println("Please follow the format: accountKey t_es t_ec host");
			System.exit(0);
		}

		String site="hardwarecentral.com";
		double ts=0.6;
		double tc=100;

		try {
			ts=Double.parseDouble(args[1]);
			tc=Double.parseDouble(args[2]);
		} catch (NumberFormatException e) {
			System.out.println("t_es and t_ec should be doubles");
			System.exit(0);
		} // We restricted t_es to 0 < t_es < 1 because the ref implementation also specified this constraint
		if (ts >= 1 || ts <= 0) {
			System.out.println("t_es should be between 0 and 1");
			System.exit(0);
		} 
		if (tc < 1) {
			System.out.println("t_ec should be 1 or greater");
			System.exit(0);
		}
		
		BingSearch.ACCOUNT_KEY=args[0];
		site=args[3];
		
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
		if (category.contains("Computers")) {
			System.out.println("Merge root & computers");
			mergeSet(computerSample);
		} else if (category.contains("Health")) {
			System.out.println("Merge root & health");
			mergeSet(healthSample);
		} else if (category.contains("Sports")) {
			System.out.println("Merge root and sports");
			mergeSet(sportsSample);
		}

		String[] categories = category.split("/");
		for (int i = 0; i < categories.length; i++) {
			String currentCategory = categories[i];
			String filename = "";
			String mainCategory = "";
			if (currentCategory.equals("Root")) {
				mainCategory += "Root";
			} else if (currentCategory.equals("Computers")) {
				mainCategory += "Computers";
			} else if (currentCategory.equals("Health")) {
				mainCategory += "Health";
			} else if (currentCategory.equals("Sports")) {
				mainCategory += "Sports";
			} else {
				continue; // ignore the leaf nodes
			}
			filename += mainCategory+"-"+site+".txt";
			System.out.println("Generating: " + filename);
			try {
				generateSummary(mainCategory);
				outputSummary(getSummary(mainCategory), filename); // output the summary to the text file
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		System.out.println(site+" "+category); // Final output
	}
}
