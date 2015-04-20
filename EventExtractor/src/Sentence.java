import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Sentence {
	private String word;
	private String lemma;
	private String ner;
	private String pos;
	
	private ArrayList<String> actors;
	private ArrayList<String> dates;
	private ArrayList<String> verbs;
	
	/**
	 * Checks named entities for location and actors
	 */
	private void checkNER() {
		if (ner.equals("O") == false) {		
			// if NER is an actor
			if (ner.equals("PERSON") || ner.equals("LOCATION") || ner.equals("ORGANIZATION") || ner.equals("MISC")) {
				if ( lemma.charAt(0) != lemma.toLowerCase().charAt(0) ) {				
					System.out.println(word + " is an actor");					
				}
			}
			// if NER is a date
			else if (ner.equals("DATE") || ner.equals("TIME") || ner.equals("DURATION") || ner.equals("SET")) {
				System.out.println(word + " is a date");
			}		
		}
	}
	
	/**
	 * Checks part of speech for verbs
	 */
	private void checkPOS() {
		if (pos.length() > 2) {
			if (pos.substring(0, 2).equals("VB")) {
				System.out.println(word + " is a verb");
			}		
		}
	}
	
	public Sentence(NodeList nList) {
		// initialize actors, dates, verbs lists
		actors = new ArrayList<String>();
		dates = new ArrayList<String>();
		verbs = new ArrayList<String>();
		
    	// runs through each <sentence> node
    	for (int temp = 0; temp < nList.getLength(); temp++) {
    		Node nNode = nList.item(temp);	// gets the current <sentence>     
    		System.out.println("\nCurrent Element :" + nNode.getNodeName() + ": " + ((Element)nNode).getAttribute("id"));
     
    		// get the first thing in <sentence> which is <tokens>
    		if (nNode.getNodeType() == Node.ELEMENT_NODE) { 
    			Element eElement = (Element) nNode;
    			
    			// get a list of <token> under <tokens>
    			NodeList tList = eElement.getElementsByTagName("token");
    			// run through all the <token>'s
    			for (int t = 0; t < tList.getLength(); t++) {
    				Node tNode = tList.item(t);	// the current token
    				
    				if (tNode.getNodeType() == Node.ELEMENT_NODE) {
    					Element tElement = (Element) tNode;
    					ner = tElement.getElementsByTagName("NER").item(0).getTextContent();
    					lemma = tElement.getElementsByTagName("lemma").item(0).getTextContent();
    					word = tElement.getElementsByTagName("word").item(0).getTextContent();
    					pos = tElement.getElementsByTagName("POS").item(0).getTextContent();

    					checkNER();
    					checkPOS();
    				}
    			}    			
    		}
    	}
	}

}
