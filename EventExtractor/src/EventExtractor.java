import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
 

public class EventExtractor {
	
	private HashSet<String> actors = new HashSet<String>();
	private Sentence sentence;
	private TrainingSet train; 
	ArrayList<String> actorsContained = new ArrayList<String>(); 
	
	public EventExtractor() {
		parseFile(); 
		try {
			train = new TrainingSet();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		test();
	}
	
	private void evaluateSuccess(double ratioCorrectActors){//, int ratioIssuesIdentified) {
		// TODO Auto-generated method stub
		System.out.println("Of our " + sentence.actors.size() + " actors, " + ratioCorrectActors + "are verified international actors");
	}

	public void parseFile()
	{
		try {
			File fXmlFile = new File("cmak_sentences/sentence_9.xml");
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	     
	    	doc.getDocumentElement().normalize();
	     
	    	NodeList nList = doc.getElementsByTagName("sentence");
	    	sentence = new Sentence(nList);
	     
	    	System.out.println("----------------------------");
	     
	    	} catch (Exception e) {
	    	e.printStackTrace();
	        }	
	}
	
	public void test()
	{
		System.out.println("This model identifies the following actors \n" + sentence.actors);
		System.out.println("The actors are engaged in the following activities: \n" + sentence.verbs);
		System.out.println("----------------------------");
		System.out.println("This model identifies '" + sentence.action + "' as the main event action.");
		System.out.println("This model identifies '" + sentence.actor + "' as the primary actor.");
		System.out.println("This model identifies '" + sentence.patient + "' as the primary patient.");
		
		System.out.println("----------------------------");
		//		System.out.println(sentence.verbs);
//		System.out.println(sentence.dates);
		System.out.println("\n EVALUATING: \n");
		evaluateSuccess(matchActors());

	}
	
	private double matchActors()
	{
		double numActors = (double) sentence.actors.size();
		
		double numCorrectActors = 0.0; 
		
		for(int i = 0; i < sentence.actors.size(); i++)
			if(train.actors.contains(sentence.actors.get(i).toLowerCase()))
			{
				System.out.println("\"" + sentence.actors.get(i) + "\" is a verified international actor");
				numCorrectActors = numCorrectActors + 1.0; 
			}
				
		double ratio = numCorrectActors / numActors * 100.0; 
		matchIssues();
		return ratio;
	}
	
	private void matchIssues() {
		
		String text = sentence.text; 
		
		int i = 0; 
		int j = 1; 
		int k = 2; 
		
		String[] words = text.split(" ");
		
//		for(i = 0; i < words.length; i++)
//		{
//			if(train.issues.containsKey(words[i].replaceAll("\\s+","")))
//					
//		}
	}
	
	public static void main(String[] args) {
		EventExtractor eventExtractor = new EventExtractor();
	}
}
