package wordnet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class WordNetInterface {

	// Variables
	private IDictionary dict = null;


	// Constructors

	public WordNetInterface(String dictionaryHome){

		String path = dictionaryHome + File.separator + "dict";
		URL url;

		try {

			url = new URL ("file", null , path );

			// construct the dictionary object and open it
			dict = new Dictionary (url);
			dict.open();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Getters and Setters

	// Methods

	public void openConnection(){
		try {
			dict.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection(){
		dict.close();
	}

	public void testWord(String wordStr){

		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(wordStr, POS.NOUN);

		if (idxWord == null)
			return;

		for (int i = 0; i < idxWord.getWordIDs().size(); i++){

			System.out.println("Meaning " + i);

			IWordID wordID = idxWord.getWordIDs().get(i);
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();
			ArrayList<String> aux = new ArrayList<String>();
			for(IWord w : synset.getWords()){
				aux.add(w.getLemma());
				
			}
			
			//System.out.println ("Id = " + wordID );
			System.out.println ("  Lemma = " + word.getLemma());
			System.out.println ("  Gloss = " + word.getSynset().getGloss());
			System.out.println ("  Synonyms = " + aux.toString() + "\n");

		}

	}

	public ArrayList<ArrayList<String>> getSynonyms (String wordStr, POS pos){

		ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();

		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(wordStr, pos);

		if (idxWord == null)
			return null;		

		for (int i = 0; i < idxWord.getWordIDs().size(); i++){

			//System.out.println("Meaning " + i);

			ArrayList<String> aux = new ArrayList<String>();

			IWordID wordID = idxWord.getWordIDs().get(i); // 1st meaning
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();
			//int   count = dict.getSenseEntry(word.getSenseKey()).getTagCount();
			//System.out.println("Frequency: " + count);
			// iterate over words associated with the synset
			for(IWord w : synset.getWords()){
				aux.add(w.getLemma());
				//System.out.println("  " + w.getLemma());
			}

			out.add(aux);			
		}

		return out;
	}

	public ArrayList<String> getHyperonyms (String wordStr, POS pos){
		ArrayList<String> out = new ArrayList<String>();


		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(wordStr, pos);

		if (idxWord == null)
			return null;		

		for (int i = 0; i < idxWord.getWordIDs().size(); i++){

			System.out.println("Meaning " + i);

			IWordID wordID = idxWord.getWordIDs().get(i); // 1st meaning
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset();

			// get the hypernyms
			List <ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

			// print out each h y p e r n y m s id and synonyms
			List <IWord > words ;
			for( ISynsetID sid : hypernyms){

				words = dict.getSynset(sid).getWords();

				for(Iterator <IWord > it = words.iterator(); it.hasNext();){

					String hyperonym = it.next().getLemma();

					System.out.println("  " + hyperonym);
					out.add(hyperonym);
				}
			}
		}
		return out;
	}



	public boolean isOk(String wordStr, POS pos) {

		// look up first sense of the word
		IIndexWord idxWord = dict.getIndexWord(wordStr, pos);

		return idxWord != null;
	}


}
