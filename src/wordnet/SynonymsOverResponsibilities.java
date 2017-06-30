package wordnet;

import java.util.ArrayList;
import java.util.HashMap;

import edu.mit.jwi.item.POS;
import model.responsibility.Responsibility;
import utils.Pair;
import weka.core.stemmers.Stemmer;

public class SynonymsOverResponsibilities {

	// Variables
	
	// Constructors
	
	public SynonymsOverResponsibilities(){
		
	}
	
	// Getters and Setters
	
	// Methods
	
	public ArrayList<Responsibility> performWSDOverResponsibilities(ArrayList<Responsibility> responsibilities){
		
		System.out.println("Responsabilidades");

		for (int i = 0; i < responsibilities.size(); i++){
			Responsibility responsibility = responsibilities.get(i);
			System.out.println("  [" + (i+1) + "] " + responsibility.getCompleteResponsibility());
		}

		System.out.println("-------------------------------------------------------------------------------------------------\n");

		/*

		Responsibility r1 = new Responsibility("add", "course");
		ArrayList<Pair<String, POS>> rec1 = new ArrayList<Pair<String, POS>>();
		rec1.add(new Pair("add", POS.VERB));
		rec1.add(new Pair("new", POS.ADJECTIVE));
		rec1.add(new Pair("course", POS.NOUN));
		r1.setRecognitions(rec1);

		Responsibility r2 = new Responsibility("add", "class");
		ArrayList<Pair<String, POS>> rec2 = new ArrayList<Pair<String, POS>>();
		rec2.add(new Pair("add", POS.VERB));
		rec2.add(new Pair("class", POS.NOUN));
		r2.setRecognitions(rec2);

		Responsibility r3 = new Responsibility("add", "course");
		ArrayList<Pair<String, POS>> rec3 = new ArrayList<Pair<String, POS>>();
		rec3.add(new Pair("add", POS.VERB));
		rec3.add(new Pair("course", POS.NOUN));
		r3.setRecognitions(rec3);

		responsibilities.add(r1);
		responsibilities.add(r2);
		responsibilities.add(r3);

		 */

		//-------------------------------------------------------------------------------------------

		// (1)

		//  Separo las palabras existentes
		ArrayList<String> nouns = new ArrayList<String>();
		ArrayList<String> verbs = new ArrayList<String>();
		ArrayList<String> adjectives = new ArrayList<String>();
		ArrayList<String> adverbs = new ArrayList<String>();

		// Itero sobre las responsabilidades de entrada (las de un proyecto especifico)
		for (Responsibility responsibility : responsibilities){

			// Separo cada grupo de palabras de acuerdo a su POS
			ArrayList<Pair<String, POS>> recognitions = responsibility.getRecognitions();
			for (int i = 0; i < recognitions.size(); i++){
				String word = recognitions.get(i).getPair1();
				POS pos = recognitions.get(i).getPair2();

				if (pos != null){
					if (pos.equals(POS.NOUN)){
						if (!nouns.contains(word))
							nouns.add(word);
					}else
						if (pos.equals(POS.VERB)){
							if (!verbs.contains(word))
								verbs.add(word);
						}else
							if (pos.equals(POS.ADJECTIVE)){
								if (!adjectives.contains(word))
									adjectives.add(word);
							}else{
								if (pos.equals(POS.ADVERB)){
									if (!adverbs.contains(word))
										adverbs.add(word);
								}
							}
				}
			}
		}

		// (2)

		// Por cada grupo de palabras, hago una contra todas, busco si existen sinonimos
		// Si los hay, tengo que determinar PARES
		// Finalmente, determinar cual es el sinonimo optimo e ir hacia ese mismo.
		// Pal_1 es sinonimo de Pal_2; Pal_3 es sinonimo de Pal_2, pero Pal_1 no es sinonimo de Pal_3 (Entonces pasar todo a palabra 3)

		System.out.println("1) Grupos de palabras");
		System.out.println("  Nouns: \t"+ nouns);
		System.out.println("  Verbs: \t"+ verbs);
		System.out.println("  Adjectives: \t"+ adjectives);
		System.out.println("  Adverbs: \t"+ adverbs);
		System.out.println("-------------------------------------------------------------------------------------------------\n");

		ArrayList<ArrayList<String>> resultsWordnetNouns = searchWordnet(nouns, POS.NOUN);
		ArrayList<ArrayList<String>> resultsWordnetVerbs = searchWordnet(verbs, POS.VERB);
		ArrayList<ArrayList<String>> resultsWordnetAdjectives = searchWordnet(adjectives, POS.ADJECTIVE);
		ArrayList<ArrayList<String>> resultsWordnetAdverbs = searchWordnet(adverbs, POS.ADVERB);

		System.out.println("2) Resultados de Wordnet");
		System.out.println("  Nouns: \t" + resultsWordnetNouns);
		System.out.println("  Verbs: \t" + resultsWordnetVerbs);
		System.out.println("  Adjectives: \t"+ resultsWordnetAdjectives);
		System.out.println("  Adverb: \t"+ resultsWordnetAdverbs);
		System.out.println("-------------------------------------------------------------------------------------------------\n");

		// (3)

		// Por grupo de sinonimos, determinar cuales son los sinonimos prioritarios

		ArrayList<Pair<String, String>> synonymsForNouns = searchSynonyms(nouns, resultsWordnetNouns);
		ArrayList<Pair<String, String>> synonymsForVerbs = searchSynonyms(verbs, resultsWordnetVerbs);
		ArrayList<Pair<String, String>> synonymsForAdjectives = searchSynonyms(adjectives, resultsWordnetAdjectives);
		ArrayList<Pair<String, String>> synonymsForAdverbs = searchSynonyms(adverbs, resultsWordnetAdverbs);

		System.out.println("3) Resultados de buscar palabras para intercambiar");
		System.out.println("  Nouns: \t" + synonymsForNouns);
		System.out.println("  Verbs: \t" + synonymsForVerbs);
		System.out.println("  Adjectives: \t"+ synonymsForAdjectives);
		System.out.println("  Adverbs: \t"+ synonymsForAdverbs);
		System.out.println("-------------------------------------------------------------------------------------------------\n");

		// (4)

		// Determinamos las palabras prioritarias

		System.out.println("4) Priorización de palabras");

		HashMap<String, Integer> prioritizedNouns = prioritizeGroups(synonymsForNouns, nouns , resultsWordnetNouns);
		HashMap<String, Integer> prioritizedVerbs = prioritizeGroups(synonymsForVerbs, verbs , resultsWordnetVerbs);
		HashMap<String, Integer> prioritizedAdjectives = prioritizeGroups(synonymsForAdjectives, adjectives, resultsWordnetAdjectives);
		HashMap<String, Integer> prioritizedAdverbs = prioritizeGroups(synonymsForAdverbs, adverbs, resultsWordnetAdverbs);

		System.out.println("-------------------------------------------------------------------------------------------------\n");

		// (5)

		// Eliminamos todas las conversiones que no son necesarias (prioritarias)

		System.out.println("5) Definición de conversiones");

		HashMap<String, String> changesNouns = new HashMap<String, String>();
		HashMap<String, String> changesVerbs = new HashMap<String, String>();
		HashMap<String, String> changesAdjectives = new HashMap<String, String>();
		HashMap<String, String> changesAdverbs = new HashMap<String, String>();

		defineConvertions(synonymsForNouns, prioritizedNouns, changesNouns);
		defineConvertions(synonymsForVerbs, prioritizedVerbs, changesVerbs);
		defineConvertions(synonymsForAdjectives, prioritizedAdjectives, changesAdjectives);
		defineConvertions(synonymsForAdverbs, prioritizedAdverbs, changesAdverbs);

		System.out.println("-------------------------------------------------------------------------------------------------\n");

		// (6)
		// Hacemos el cambio en todas las responsabilidades
		// En un par, el par2 se reemplaza con el par1

		ArrayList<Responsibility> resultsResponsibilities = new ArrayList<Responsibility>();

		for (Responsibility responsibility : responsibilities){

			Responsibility newResponsibility = performTheConvertion(changesNouns, responsibility, POS.NOUN);
			newResponsibility = performTheConvertion(changesVerbs, newResponsibility, POS.VERB);
			newResponsibility = performTheConvertion(changesAdjectives, newResponsibility, POS.ADJECTIVE);
			newResponsibility = performTheConvertion(changesAdverbs, newResponsibility, POS.ADVERB);

			resultsResponsibilities.add(newResponsibility);
		}

		System.out.println("6) Responsabilidades resultantes");
		for (int i = 0; i < resultsResponsibilities.size(); i++){
			System.out.println("  + ("+ (i+1) + ") " + resultsResponsibilities.get(i).getCompleteResponsibility());
		}

		return resultsResponsibilities;
	}
	
	private static ArrayList<ArrayList<String>> searchWordnet(ArrayList<String> grupo, POS pos) {

		ArrayList<ArrayList<String>> synonymsList = new ArrayList<ArrayList<String>>();

		Stemmer stemmer = new weka.core.stemmers.SnowballStemmer();
		WordNetInterface wni = new WordNetInterface("C:");

		for (String word : grupo){

			String stemmedWord = stemmer.stem(word);
			String workingWord = null;

			if (word.equals(stemmedWord)){
				// Son iguales entonces se las trata igual
				workingWord = word;
			}else{
				// No son iguales, se intenta primero con la versión original
				if (wni.isOk(word, pos)){
					// seguimos laburando con word
					workingWord = word;
				}else{
					if (wni.isOk(stemmedWord, pos)){
						// seguimos con stemmedWord
						workingWord = stemmedWord;
					}else{
						// Ambas fallaron..
						//System.out.println("Ninguna de las dos palabras es valida");
						// No se haria desambiguación para esa palabra
					}
				}
			}

			/*
			System.out.println("Word: \""  + word + "\"");
			System.out.println("Stem: \""  + stemmedWord + "\"");
			System.out.println("Working Word : " + workingWord);
			 */

			ArrayList<ArrayList<String>> synonyms = null;

			try{
				synonyms = wni.getSynonyms(workingWord, pos);
			}catch (Exception e){
				// Con esta palabra no se hace nada
			}

			// TODO todos o solo el primer nivel?
			if (synonyms != null)
				synonymsList.add(synonyms.get(0));
			else
				synonymsList.add(null);
		}

		wni.closeConnection();

		return synonymsList;
	}
	

	private static ArrayList<Pair<String, String>> searchSynonyms(ArrayList<String> words, ArrayList<ArrayList<String>> synonymsInGroup) {
		ArrayList<Pair<String, String>> out = new ArrayList<Pair<String, String>>();

		for (int wid = 0; wid < words.size(); wid++){

			String actualWorkingWord = words.get(wid);
			ArrayList<String> allWordsExceptWID = (ArrayList<String>) words.clone();
			allWordsExceptWID.remove(wid);

			ArrayList<String> widSynonyms = synonymsInGroup.get(wid);
			if (widSynonyms !=null){
				for (String otherWord : allWordsExceptWID){

					if (widSynonyms.contains(otherWord)){
						Pair<String, String> synonymFound = new Pair<String, String>(actualWorkingWord, otherWord);
						out.add(synonymFound);
						//System.out.println(" Sinonimos : " + actualWorkingWord + " " + otherWord);
					}
				}
			}
		}

		return out;
	}
	
	private static HashMap<String, Integer> prioritizeGroups(ArrayList<Pair<String, String>> synonymsForNouns, ArrayList<String> nouns, ArrayList<ArrayList<String>> resultsWordnetNouns) {
		HashMap<String, Integer> out = new HashMap<String, Integer>();

		for (Pair<String, String> pair : synonymsForNouns){

			if (out.containsKey(pair.getPair1())){
				out.put(pair.getPair1(), out.get(pair.getPair1()) + 1);
			}else{
				out.put(pair.getPair1(), 1);
			}
		}

		// Se procede con los que se encuentran en el segundo par
		for (Pair<String, String> pair : synonymsForNouns){

			if (!out.containsKey(pair.getPair2())){
				out.put(pair.getPair2(), 0);
			}
		}	


		for (String key : out.keySet()){
			System.out.println("  Key: [" + key + "] Cantidad: [" + out.get(key) + "]");
		}

		return out;
	}
	
	private static void defineConvertions(ArrayList<Pair<String, String>> synonyms,	HashMap<String, Integer> prioritized, HashMap<String, String> convertions) {
		ArrayList<Pair<String, String>> denials = new ArrayList<Pair<String, String>>();
		for (Pair<String, String> synonym : synonyms){

			Integer value1 = prioritized.get(synonym.getPair1());
			Integer value2 = prioritized.get(synonym.getPair2());

			

			if (value1 > value2){
				convertions.put(synonym.getPair2(), synonym.getPair1());
				System.out.println("  " + synonym.getPair2() + " -> " + synonym.getPair1());
			}else{
				if (value1 == value2){

					boolean continuar = true;
					for (Pair<String, String> pair : denials){
						if (pair.getPair1().equals(synonym.getPair1()) && pair.getPair2().equals(synonym.getPair2())){
							continuar = false;
						}
					}

					if (continuar){
						convertions.put(synonym.getPair2(), synonym.getPair1());
						System.out.println("  " + synonym.getPair2() + " -> " + synonym.getPair1());

						denials.add(new Pair<String, String>(synonym.getPair2(), synonym.getPair1()));
					}
				}
			}
		}			


	}

	private static Responsibility performTheConvertion(HashMap<String, String> change, Responsibility responsibility, POS pos) {

		ArrayList<Pair<String, POS>> newRecognitions = new ArrayList<Pair<String, POS>>();

		for (Pair<String, POS> recognition : responsibility.getRecognitions()){

			if (change.get(recognition.getPair1()) != null){

				// Se tiene que hacer el cambio
				Pair<String, POS> newRecognition = new Pair<String, POS>();
				newRecognition.setPair1(change.get(recognition.getPair1()));
				newRecognition.setPair2(recognition.getPair2());
				newRecognitions.add(newRecognition);

			}else{
				newRecognitions.add(recognition);
			}

		}

		Responsibility newResponsibility = new Responsibility(newRecognitions);
		return newResponsibility;
	}

	
	
}
