package input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.mit.jwi.item.POS;
import model.responsibility.Responsibility;
import utils.Pair;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class InputReader {

	// Variables

	// Constructors
	public InputReader(){

	}

	// Getterns and Setters

	// Methods

	public ArrayList<Responsibility> readResponisibilitiesFromARFF(String basePath, String fileName){
		ArrayList<Responsibility> out = new ArrayList<Responsibility>();

		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader(basePath + File.separator + fileName));
			
			ArffReader arff = new ArffReader(reader);
			Instances data = arff.getData();	
			
			for (Instance instance : data){
				String stringResponsibility = instance.stringValue(0);
				
				ArrayList<Pair<String, POS>> recognitions = getRecognitions(stringResponsibility);
				
				Responsibility newResponsibility = new Responsibility(recognitions);
				out.add(newResponsibility);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out;
	}

	private ArrayList<Pair<String, POS>> getRecognitions(String stringResponsibility) {
		ArrayList<Pair<String, POS>> out = new  ArrayList<Pair<String, POS>>();
		
		for (String part : stringResponsibility.split(" ")){
			Pair<String, POS> newRecognition = new Pair<String, POS>();
			newRecognition.setPair1(part.split("\\|")[0]);
			
			switch (part.split("\\|")[1]){
			case "VB" : {
				newRecognition.setPair2(POS.VERB);
				break;
			}
			case "NN" : {
				newRecognition.setPair2(POS.NOUN);
				break;
			}
			case "ADJ" : {
				newRecognition.setPair2(POS.ADJECTIVE);
				break;
			}
			case "ADV" : {
				newRecognition.setPair2(POS.ADVERB);
				break;
			}
			default : {
				newRecognition.setPair2(null);
				break;
			}
			}
			
			out.add(newRecognition);
			
		}
		
		return out;
	}

}
