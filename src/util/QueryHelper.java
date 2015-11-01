package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryHelper {
	
	
	public static String queryAND(String queries){
		String res=queries.replace(" "," AND ");
		return res;		
	}
	
	//Read query terms for parent category from the provided .txt files and add the terms to the appropriate hash map
	public static void readQueriesOfCategory(HashMap<String,ArrayList<String>> hm, String category) throws IOException{
		String path="queries/"+category+".txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			StringBuilder sb = new StringBuilder();
		    
		    while (line != null) {		    	
		    	String key=line.substring(0,line.indexOf(' ')); // category
		    	String queries=line.substring(line.indexOf(' ')+1); // queries
		    	
		    	if(hm.containsKey(key)){
		    		hm.get(key).add(queries);
		    	}else{
		    		ArrayList<String> queryList=new ArrayList<String>();
		    		queryList.add(queries);
		    		hm.put(key, queryList);
		    	}
		        line = br.readLine();
		        
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
