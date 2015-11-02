package entity;

import java.util.ArrayList;
import java.util.HashMap;

public class Category {
  String category="";

  ArrayList<Category> subCategories; // a list of the subcategories for this Category
  HashMap<String, Long> ECoverage; //ECoverage(D,C): <EC(D,C1),EC(D,C2)>
  HashMap<String, Double> ESpecificity; //ESpecificity(D,C):

	public Category(String category) {
		this.category = category;
		this.subCategories=new ArrayList<Category>();
		this.ECoverage=new HashMap<String, Long> ();
		this.ESpecificity=new HashMap<String, Double>();
	}
	
	public void addSubCategory(Category c){
		subCategories.add(c);
	}
	
	public String getCategory() {
		return category;
	}

	public ArrayList<Category> getSubCategories() {
		return subCategories;
	}

	public HashMap<String, Long> getECoverage() {
		System.out.println("ECOVERAGE: " + ECoverage);
		return ECoverage;
	}

	public HashMap<String, Double> getESpecificity() {
		System.out.println("ESPECIFICITY: " + ESpecificity);
		return ESpecificity;
	}

}
