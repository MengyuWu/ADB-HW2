1) Your name and your partner's name and Columbia UNI
Mengyu Wu: mw2907  Melanie Hsu: mlh2197 

2) A list of all the files that you are submitting
src:
	Classification/WebDatabaseClassification.java
	Classification/getWordsLynx.java
	entity/Category.java
	entity/Query.java
	util/BingSearch.java
	util/QueryHelper.java
lib:
	commons-codec-1.10.jar
	json-20090211.jar

3) A clear description of how to run your program (note that your project must compile/run under Linux in your CS account)
To Run:
	1) ant clean 
	2) ant
	If you receive something similar to the following error:
		[javac] javac: invalid target release: 1.7
		Please do the following:
		1) In Build.xml, update the following two lines to reflect the
		version of Java that you are using:
		 	<property name="target" value="1.6"/>
    	 		<property name="source" value="1.6"/>
    	 2) In Build.xml, if fork="yes" in the line below, change fork="no"
    	 	<java classname="main.bingRun" failonerror="true" fork="no">
	3) ant WebDatabaseClassification -Dargs='<Bing_Account_Key> <t_es> <t_ec> <host>'
	where <Bing_Account_Key> = our Bing Search Account Key
	<t_es> is the specificity threshold (between 0 and 1)
	<t_ec> is the coverage threshold
	<host> is the URL of the database to be classified
	ex. ant WebDatabaseClassification -Dargs='L5ZA7UJt279Hm0QcBPu50yHHWRS1ZNzlifvHTiK5onw 0.6 100 fifa.com'

4) A clear description of the internal design of your project, for each part of the project
Part I uses the following Java files:
	Classification/WebDatabaseClassification.java
	entity/Category.java
	entity/Query.java
	util/BingSearch.java
	util/QueryHelper.java

WebDatabaseClassification class's main method does command-line argument checking,
prepares the category hierarchy, then calls the classify method which returns the
site's categorization. The classify method implements the query-probing classification
algorithm described in Figure 4 of the QProber paper. It is a recursive method that
runs until it either hits a leaf node (a category that doesn't have any subcategories),
or the database fails to meet the user-specified coverage and specificity thresholds
for the current category. WebDatabaseClassification class calls QueryHelper class's
readQueriesOfCategory method to add the query terms found in the provided .txt files
(computers.txt, health.txt, root.txt, and sports.txt) into the appropriate hash map.
Each hash map corresponds to a non-leaf category.

In the classify method (WebDatabaseClassification class), a for loop is executed 
that iterates through all of the subcategories of the current category (for the 
rest of this description, I'll refer to the subcategory as Ci. The hash map 
corresponding to Ci is used to form a query string (the probe) that's sent to 
the BingSearch class (via the getCount method). The BingSearch class uses the 
Bing Search API to query Bing and return the results of the query. This result
is cached into the WebDatabaseClassification class's queryCacheDoc, and the 
number of search results returned for this query (number of matches for the probe)
is cached into WebDatabaseClassification's queryCacheCount. Additionally, the 
number of search results is also returned to the WebDatabaseClassification class, 
which then uses it to calculate the coverage of the current database for Ci.

Next, the classify method calculates the specificity of the current database
for Ci, and compares the Tc, Ts against the user-specified Tc and Ts thresholds.
If this database's Tc and Ts exceed the Tc, Ts specified by the user, then we 
can say that this database can be classified under Ci, and Ci is appended to the
result string. The result string will eventually contain all the categories that
this database can be classified under. If the database cannot be classified under
any subcategories of any category, then the classification will be only Root.

The Category and Query classes are necessary because we need the concept of
Categories and Queries in order to execute this algorithm.

//TODO: talk about NOT queries here or try running w/o them

Part II uses the following Java files:
	Classification/WebDatabaseClassification.java
	Classification/getWordsLynx.java
	entity/Category.java
	entity/Query.java
	util/BingSearch.java
	util/QueryHelper.java

We chose option b for this part (not including multiple-word information in
the content summaries).

For Part II, we prepared a TreeSet (for sample) and TreeMap (for summary)
for each of the non-leaf categories. When the getCount method is called in the
classify method, the parent category is passed as a parameter to the BingSearch
class. In the contentSummary method, called at the end of the main bingSearch
method, any unique URLs (that haven't yet been returned in any of the query 
probes for the current subcategory) are added to the parent category's TreeSet
/sample. The samples set for any category will only accept new unique URLs, 
hence duplicate URLs for a category will be ignored. Additionally, for each query q
associated with the category C, the BingSearch class only queries Bing for the top 4 
webpages for this query, where each query is a page in database D.

At the very end of the WebDatabaseClassification main method,
the the content summary is generated for each non-leaf category that the 
database falls under. Using Classification/getWordsLynx.java, the contentSummary 
method extracts the set of words in the result docs (whose urls are obtained 
from the sample), and takes a count of each word in the word set, where the
count of a word is its document frequency. 

Classification/getWordsLynx.java is almost identical to the one included in
the assignment instructions, and the functionality is identical to what was
specified in the instructions - it converts an HTML doc to lowercase, treats
any character not in the English alphabet as a word separator, and returns the
set of words that appear in the document.

Passing the parent category to the BingSearch class ensures that the specs
in Part 2a: Document Sampling is fulfilled. For example, if a site is classified
under Root/Health, then the Root category .txt file for this site will include
the results of both Root-level queries and Health-level queries, while the 
Health category .txt file will include the Health-level queries. 

Since the category hierarchy is fixed, and it's impossible to know which 
categories the database will be classified as beforehand, several versions of the
root sample set is created, such that rootSample contains only urls from the 
Root-level queries and rootComputerSample, rootHealthSample, and rootSportsSample
contain the urls for both Root-level queries and queries at the Computer, Health,
and Sports levels respectively. After the database has been classified, the 
appropriate root sample TreeSet is used to generate the content summary for the
Root-siteName.txt file.

5) Your Bing account key (so we can test your project)
L5ZA7UJt279Hm0QcBPu50yHHWRS1ZNzlifvHTiK5onw

6) Any additional information that you consider significant 
We restricted t_es to 0 < t_es < 1 because the ref implementation also specified this constraint