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


5) Your Bing account key (so we can test your project)
L5ZA7UJt279Hm0QcBPu50yHHWRS1ZNzlifvHTiK5onw

6) Any additional information that you consider significant 
