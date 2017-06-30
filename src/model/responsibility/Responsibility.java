package model.responsibility;

import java.util.ArrayList;

import edu.mit.jwi.item.POS;
import utils.Pair;

public class Responsibility {

	// Variables
	private String verb;
	private String dobj;	
	private ArrayList<Pair<String, POS>> recognitions;
		
	// Constructors
	public Responsibility(String verb, String dobj){
		setVerb(verb);
		setDobj(dobj);
		this.recognitions = new ArrayList<Pair<String, POS>>();
	}
	
	public Responsibility(ArrayList<Pair<String, POS>> recognitions){
		setVerb("");
		setDobj(dobj);
		this.recognitions = recognitions;
	}
	
	// Getters and Setters

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getDobj() {
		return dobj;
	}

	public void setDobj(String dobj) {
		this.dobj = dobj;
	}

	public ArrayList<Pair<String, POS>> getRecognitions() {
		return recognitions;
	}

	public void setRecognitions(ArrayList<Pair<String, POS>> recognitions) {
		this.recognitions = recognitions;
	}
		
	// Methods
	
	public String getSimplifiedResponsibility(){
		return this.getVerb().trim() + " " + this.getDobj().trim();
	}
	
	public String getCompleteResponsibility() {
		String completeResponsibility = "";
		
		for (Pair<String, POS> pair : recognitions){
			completeResponsibility += pair.getPair1() + " ";
		}
			
		return completeResponsibility;
	}

	
	
}
