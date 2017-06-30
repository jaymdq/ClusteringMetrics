import java.util.ArrayList;

import edu.mit.jwi.item.POS;
import weka.core.stemmers.Stemmer;
import wordnet.WordNetInterface;

public class Main2 {


	public static void main(String[] args) {

		ArrayList<ArrayList<String>> synonymsList = new ArrayList<ArrayList<String>>();
		ArrayList<String> workingWords = new ArrayList<String>();

		Stemmer stemmer = new weka.core.stemmers.SnowballStemmer();
		WordNetInterface wni = new WordNetInterface("C:");
		POS pos = POS.NOUN;

		ArrayList<String> words = new ArrayList<String>();
		words.add("course");
		words.add("classes");
		words.add("court"); // este va de trampa	
		
		for (String word : words){

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
						System.err.println("Ninguna de las dos palabras es valida");
						// No se haria desambiguación para esa palabra
					}
				}
			}

			System.out.println("Word: \""  + word + "\"");
			System.out.println("Stem: \""  + stemmedWord + "\"");
			System.out.println("Working Word : " + workingWord);

			workingWords.add(workingWord);

			//System.out.println("\n=== Test Word ===\n");
			//wni.testWord(workingWord);

			//System.out.println("\n=== getSynonyms ===\n");

			ArrayList<ArrayList<String>> synonyms = wni.getSynonyms(workingWord, POS.NOUN);
			synonymsList.add(synonyms.get(0));

			/*for (int i = 0; i < synonyms.size(); i++){
				System.out.println("Meaning " + i);
				System.out.println("\t" + synonyms.get(i).toString() + "\n");
			}
			 */
			//System.out.println("\n=== getHyperonyms ===\n");
			//wni.getHyperonyms(workingWord, POS.NOUN);

		}

		wni.closeConnection();

		//------------------------------------------------------------------------------------------------

		// Acá se comenzzaria a procesar

		for (int wid = 0; wid < workingWords.size(); wid++){

			String actualWorkingWord = workingWords.get(wid);
			ArrayList<String> allWordsExceptWID = (ArrayList<String>) workingWords.clone();
			allWordsExceptWID.remove(wid);

			ArrayList<String> widSynonyms = synonymsList.get(wid);

			for (String otherWord : allWordsExceptWID){

				if (widSynonyms.contains(otherWord)){
					System.out.println(" Sinonimos : " + actualWorkingWord + " " + otherWord);
				}
			}	
		}
		
		
		
	}


}
