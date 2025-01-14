import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
 
/**
 * CMAK event extractor model: it's like PETRARCH but better in some cases, and not
 * as good in other cases. Created as a final project for Heather Pon-Barry's
 * Natural Language Processing elective at Mount Holyoke College. 
 * May 1, 2015
 * @author camillemalonzo + areebakamal
 *
 */
public class CMAK {
	
	// DATA MEMBERS ===============================================================
	
	private Sentence sentence;	// Sentence class of the current sentence to extract event from
	private TrainingSet train; 	// Training set for this run
	
	// array of all actors found in this sentence
	ArrayList<String> actorsContained = new ArrayList<String>(); 
	
	// string array of all issues found (one word, two word, or three word)
	ArrayList<String> issuesString = new ArrayList<String>(); 
	
	// string array of all issues found as they are stored without spaces in the training set
	ArrayList<String> issuesKey = new ArrayList<String>(); 
	
	// string array storing corresponding topics to issues
	ArrayList<ArrayList<String>> issuesTopics = new ArrayList<ArrayList<String>>(); 
	
	private String path_to_xml ;
	
	/**
	 * Constructor
	 */
	public CMAK(String path) {
		path_to_xml = path;
		
		parseFile(); // parse the sentence from a .xml file
		
		try {
			train = new TrainingSet(); // get training set
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
			
		getResults(); // attain and then subsequently print out results
		
	}
	
	/**
	 * Gets and subsequently prints out the results of CMAK model
	 */
	private void getResults(){

		System.out.println("Sentence text is: \"" + sentence.text + "\"");
		
		// ACTORS
		System.out.println("\nActors----------------------------\n");
		System.out.println("Our model identifies the following actors \n" + sentence.actors);
		System.out.println("\nThis model identifies '" + sentence.actor + "' as the primary actor.");
		System.out.println("This model identifies '" + sentence.patient + "' as the primary patient.");
		System.out.println("\nChecking actors against training data ... ");
		int numCorrectActors = matchActors();
		double numCorrectActorsdbl = (double) numCorrectActors; 
		double numActorsdbl = (double) sentence.actors.size(); 
		double ratio = numCorrectActorsdbl / numActorsdbl * 100.0; 
		System.out.println("Of our " + sentence.actors.size() + " actors, " + 
				ratio + "% are verified international actors\n");
		
		// ISSUES
		System.out.println("Issue/Action----------------------------\n");
		int issuecount = matchIssues();
		System.out.println("We have found " + issuecount + " issue keyword(s) in our data, by matching it against our training data");
		
		for(int a = 0; a < issuesString.size(); a++)
			System.out.println("WORD: [" + issuesString.get(a) + "] under the TOPIC ID" + train.issues.get(issuesKey.get(a))); 
		System.out.println("\nThis model identifies '" + sentence.action + "' as the main event action/issue.\n");

		
		System.out.println("Verdict----------------------------\n");
		// EVENT OR NOT
		boolean eventOrNot = false; 
		
		if(numCorrectActors >= 1)
		{
			if(issuesString.size() >= 1)
				eventOrNot = true;
			else if(sentence.action != null && !sentence.action.equals("") && !sentence.action.equals("\\s+"))
					// action is not null hence event occurred with actors and action
				eventOrNot = true;
			else
				eventOrNot = false;
		}
		else
			eventOrNot = false; 
		
		if(eventOrNot)
			System.out.println("EVENT DETECTED? \nYes - event detected with " + numCorrectActors + " and " + issuesString.size() + " issue keywords.");
		else
			System.out.println("EVENT DETECTED? \n No - No event detected due to too few issue keywords or verified actors");
		
		
	}

	/**
	 * 
	 */
	public void parseFile()
	{
		try {
			File fXmlFile = new File(path_to_xml);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	     
	    	doc.getDocumentElement().normalize();
	        NodeList sentenceList = doc.getElementsByTagName("sentence");
	    	sentence = new Sentence(sentenceList);
	     
	     
	    	} catch (Exception e) {
	    	e.printStackTrace();
	        }	
	}
	
	/**
	 * 
	 * @return
	 */
	private int matchActors()
	{	
		int numCorrectActors = 0; 
		
		for(int i = 0; i < sentence.actors.size(); i++)
			if ( sentence.actors.get(i) != null   ) {
				if( train.actors.contains(sentence.actors.get(i).toLowerCase())) {
//					System.out.println("According to the training set \"" + sentence.actors.get(i) + "\" is a verified international actor");
					actorsContained.add(sentence.actors.get(i));
					numCorrectActors = numCorrectActors + 1; 
				}
			}
				
		return numCorrectActors;
	}
	
	/**
	 * 
	 * @return
	 */
	private int matchIssues() {
		
		int numIssues = 0; 
		String text = sentence.text; 
		
		int i = 0; 
		int j = 1; 
		int k = 2; 
		
		String[] words = text.split("\\s+");
		
		for(i = 0, j = 1, k = 2; i < words.length; i++, j++, k++)
		{
			
			// unigram walkthrough
			// hashmap is <key = issue without space, value = issue with space>
			// if this word is in issues, then increment num issue words, put it
			// in a hashmap with its corresponding, spaceless key and rank it
			
			
			String nextword = words[i].replace(".", "").trim();
//			System.out.println("nw" + nextword);
			if(train.issues.containsKey(nextword))
			{
				numIssues++; 
				issuesKey.add(nextword);
//				System.out.println(issuesKey);
				issuesString.add(nextword);
				issuesTopics.add(train.issues.get(issuesKey));
			}		
			
			// bigram walkthrough
			if(j < words.length)
			{
				String issue = words[i].replace(".", "").trim() + words[j].replace(".", "").trim();
				if(train.issues.containsKey(issue))
				{
					numIssues++; 
					String issuestr = words[i] + " " + words[j]; 
					issuesKey.add(issue.trim());
					issuesString.add(issuestr); 
					issuesTopics.add(train.issues.get(issuesKey));
	
				}	
			}
			
			// trigram walkthrough
			if(k < words.length)
			{
				String issue = words[i].replace(".", "").trim() + words[j].replace(".", "").trim() + words[k].replace(".", "").trim();
				if(train.issues.containsKey(issue))
				{
					numIssues++; 
					String issuestr = words[i] + " " + words[j] + " " + words[k]; 
					issuesKey.add(issue.trim());
					issuesString.add(issuestr);
					issuesTopics.add(train.issues.get(issuesKey));
				}	
			}
		}
		
		return numIssues;
	}
	
	public static void main(String[] args) {
		new CMAK(args[0]);
	}
}
