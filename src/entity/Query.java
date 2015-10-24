package entity;

public class Query {
	private String query;
	private String site;
	
	public Query(String query, String site) {
		super();
		this.query = query;
		this.site = site;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
	
	//use object as key, need to write own hashCode and equal methods
	@Override
	public int hashCode(){
		return query.hashCode()^site.hashCode();
	}
	
	 @Override
	public boolean equals(Object obj){
		if(this==obj){
			return true;
		}
		if(obj==null){
			return false;
		}
		
		if(getClass()!=obj.getClass()){
			return false;
		}
		
		final Query q=(Query)obj;
		
		return q.query.equals(query) && q.site.equals(site);
		
	}
	
}
