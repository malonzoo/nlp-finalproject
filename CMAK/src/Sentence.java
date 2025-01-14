import java.util.ArrayList;
import java.util.HashMap; 
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Sentence {
	
	private String word;
	private String lemma;
	private String ner;
	private String pos;
	public  ArrayList<String> actors;
	public ArrayList<String> dates;
	public ArrayList<String> verbs;
	public HashMap<String, ArrayList<String[]>> dependencypairs; 
	public String text = ""; 
	
	// EVENT EXTRACTING
		public String action;
		public String actor;
		public String patient;
	
	public Sentence(NodeList nList) 
	{	
		// initialize actors, dates, verbs, dependency pairs
		actors = new ArrayList<String>();
		dates = new ArrayList<String>();
		verbs = new ArrayList<String>();
		dependencypairs = new HashMap<String, ArrayList<String[]>>(); 
		init(nList); 
	}
	
	public void init(NodeList nList) {	
    	// runs through each <sentence> node
    	for (int temp = 0; temp < nList.getLength(); temp++) 
    	{
    		Node nNode = nList.item(temp);	// gets the current <sentence>     
     
    		// get the first thing in <sentence> which is <tokens>
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) 
    		{ 
    			Element eElement = (Element) nNode;
    			
    			// get a list of <token> under <tokens>
    			NodeList tList = eElement.getElementsByTagName("token");
    			// run through all the <token>'s
    			for (int t = 0; t < tList.getLength(); t++) {
    				Node tNode = tList.item(t);	// the current token
    				
    				if (tNode.getNodeType() == Node.ELEMENT_NODE)
    				{
    					Element tElement = (Element) tNode;
    					ner = tElement.getElementsByTagName("NER").item(0).getTextContent();
    					lemma = tElement.getElementsByTagName("lemma").item(0).getTextContent();
    					word = tElement.getElementsByTagName("word").item(0).getTextContent();
    					pos = tElement.getElementsByTagName("POS").item(0).getTextContent();
    					if(!word.equals("."))
    						text = text.concat(" ");
    					text = text.concat(word); 
    					extractNamedEntities();
    					extractVerbs();
    				}
    			} 
    			
    			// CREATE DEPENDENCY PAIRS
    			NodeList dList = eElement.getElementsByTagName("dependencies");
    			dList = (NodeList)dList.item(0);
    			// run through all the <dep>'s
    			for (int d = 0; d < dList.getLength(); d++) 
    			{
    				Node dNode = dList.item(d);	// the current token
    				
    				if (dNode.getNodeType() == Node.ELEMENT_NODE) 
    				{
    					Element dElement = (Element) dNode; 	
    					String depentype = dElement.getAttribute("type");
    					String dependent = dElement.getElementsByTagName("dependent").item(0).getTextContent();
    					String governor = dElement.getElementsByTagName("governor").item(0).getTextContent();
    					
    					// System.out.println(depentype + ": " + "( " + dependent + ", " + governor + " )");
    					
    					createDependencyPairs(depentype, dependent, governor); 
    				}
    			}
    			
    			// CHECK DEPENDENCIES	
    			// run through all the <dep>'s
    			for (int d = 0; d < dList.getLength(); d++) 
    			{
    				Node dNode = dList.item(d);	// the current token
    				
    				if (dNode.getNodeType() == Node.ELEMENT_NODE) 
    				{
    					Element dElement = (Element) dNode; 	
    					String depentype = dElement.getAttribute("type");
    					String dependent = dElement.getElementsByTagName("dependent").item(0).getTextContent();
    					String governor = dElement.getElementsByTagName("governor").item(0).getTextContent();
    					
    					checkDependencies(depentype, dependent, governor); 
    				}
    			} 
    		}    			
    	}

    	
    	checkConjunction();
    	refineActor();
    	if (patient != null) {
    		refinePatient();
    	}
    	
    	// final check to see if identified actor and patient are in our data structure
    	
    	if(patient != null && !actors.contains(patient))
    		actors.add(patient);
    	
    	if(patient != null && !actors.contains(actor))
    		actors.add(actor);
    	
    	text = text.substring(1);
    }
	
	/**
	 * Refine the actor if necessary
	 */
	private void refineActor() {
		String found0 = "";
		String found1 = "";
		
		// search through values of the dependencypairs
		Iterator<ArrayList<String[]>> iterator = dependencypairs.values().iterator();
		while (iterator.hasNext()) {
			ArrayList<String[]> next = iterator.next();
			for (int i = 0; i < next.size(); i++) {
				if (next.get(i)[1].equals(actor) && !(next.get(i)[0].equals("det")) && !(next.get(i)[0].equals("cc"))) {
					found0 = next.get(i)[0];
					found1 = next.get(i)[1];
				}
			}
		}
		// go through the keys to find the correct dependent
		Iterator<String> itr_keys = dependencypairs.keySet().iterator();
		while (itr_keys.hasNext()) {
			String key = itr_keys.next();
			ArrayList<String[]> value = dependencypairs.get(key);
			for (int x = 0; x < value.size(); x++ ) {
				if (value.get(x)[0].equals(found0) && value.get(x)[1].equals(found1)) {
					actor = key + " " + actor;
				}
			}
		}
	}
	
	/**
	 * Refine the actor if necessary
	 */
	private void refinePatient() {
		String found0 = "";
		String found1 = "";
		
		// search through values of the dependencypairs
		Iterator<ArrayList<String[]>> iterator = dependencypairs.values().iterator();
		while (iterator.hasNext()) {
			ArrayList<String[]> next = iterator.next();
			for (int i = 0; i < next.size(); i++) {
				if (next.get(i)[1].equals(patient) && !(next.get(i)[0].equals("det")) && !(next.get(i)[0].equals("cc"))) {
					found0 = next.get(i)[0];
					found1 = next.get(i)[1];
				}
			}
		}
		// go through the keys to find the correct dependent
		Iterator<String> itr_keys = dependencypairs.keySet().iterator();
		while (itr_keys.hasNext()) {
			String key = itr_keys.next();
			ArrayList<String[]> value = dependencypairs.get(key);
			for (int x = 0; x < value.size(); x++ ) {
				if (value.get(x)[0].equals(found0) && value.get(x)[1].equals(found1)) {
					patient = key + " " + patient;
				}
			}
		}
	}
	
	private void checkConjunction() {
		String found0 = "";
		String found1 = "";
		
		// search through values of the dependencypairs
		Iterator<ArrayList<String[]>> iterator = dependencypairs.values().iterator();
		while (iterator.hasNext()) {
			ArrayList<String[]> next = iterator.next();
			for (int i = 0; i < next.size(); i++) {
				if (next.get(i)[1].equals(actor) && next.get(i)[0].equals("conj")) {
					found0 = next.get(i)[0];
					found1 = next.get(i)[1];
				}
			}
		}
		// go through the keys to find the correct dependent
		Iterator<String> itr_keys = dependencypairs.keySet().iterator();
		while (itr_keys.hasNext()) {
			String key = itr_keys.next();
			ArrayList<String[]> value = dependencypairs.get(key);
			for (int x = 0; x < value.size(); x++ ) {
				if (value.get(x)[0].equals(found0) && value.get(x)[1].equals(found1)) {
					patient = key;
				}
			}
		}
		
	}
	/**
	 * Checks named entities for location and actors
	 */
	private void extractNamedEntities() 
	{	
		if (ner.equals("O") == false) 
		{	
			// if NER is an actor
			if (ner.equals("PERSON") || ner.equals("LOCATION") || ner.equals("ORGANIZATION") || ner.equals("MISC")) 
				if ( lemma.charAt(0) != lemma.toLowerCase().charAt(0) ) 		
					actors.add(word);

			// if NER is a date
			else if (ner.equals("DATE") || ner.equals("TIME") || ner.equals("DURATION") || ner.equals("SET")) 
				dates.add(word.toLowerCase());	
		}
	}
	
	/**
	 * Checks part of speech for verbs
	 */
	private void extractVerbs() 
	{
		if (pos.length() > 2) 
			if (pos.substring(0, 2).equals("VB")) 
				verbs.add(word.toLowerCase());
	}
	
	/**
	 * Extracts all dependencies
	 */
	private void checkDependencies(String depentype, String dependent, String governor) 
	{
		// find the main action of the sentence
		if (depentype.equals("root")) {
			action = dependent;
		}
		// find the main actor
		else if (depentype.equals("nsubj")) {
			// if the governor string is the action of the verb
			if (governor.equals(action) ) {
				// check if the dependent is in the list of actors
				if(actors.contains(dependent)) {
					// if we get here, then the dependent is the main actor of the word
					actor = dependent;
				}
			}
		}
		// find the direct object of the main action
		else if (depentype.equals("dobj")) {
			if (governor.equals(action)) {
				// check if the dependent is in the list of actors
				if (actors.contains(dependent)) {
					// if we get here, then the dobj is a patient
					patient = dependent;
				}
				else {
					// this means that the direct object is not a patient, but part of the action
					action = action + " " + dependent;
				}
			}
		}	
	}
	
	private void createDependencyPairs(String depentype, String dependent, String governor) {
		
		// create the dependency pair
		String[] pair = new String[2];
		pair[0] = depentype;
		pair[1] = governor;
		
		// if the dependencypairs map already has dependent as a key
		if (dependencypairs.containsKey(dependent)) {
			dependencypairs.get(dependent).add(pair);
		}
		// if the dependencypairs map does not have dependent as a key
		else {
			ArrayList<String[]> list = new ArrayList<String[]>(); 	
			list.add(pair);
			dependencypairs.put(dependent, list);
		}	
	}
	
	
}
