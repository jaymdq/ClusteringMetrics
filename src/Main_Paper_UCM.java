import java.util.ArrayList;

public class Main_Paper_UCM {
	
	// Main Method

	public static void main(String[] args) throws Exception {
		// Aca se leen los archivos clustering, que viene a ser las responsabilidades junto con su clase ( componente que deberia ser
		// asignado en la clusterizacion

		// Se prueban todos los algoritmos de clusterizacion deseados
		String filterArguments = "weka.filters.unsupervised.attribute.StringToWordVector -R first-last -W 100000 -prune-rate -1.0 -C -T -I -N 1 -L -stemmer weka.core.stemmers.NullStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer \"weka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\\'\\\\\\\"()?!\\\"\"";
		ArrayList<ResponsibilitiesClusterer> clusterers = new ArrayList<ResponsibilitiesClusterer>();

		// HierarchicalClusterer
		ResponsibilitiesClusterer clusterer0 = new ResponsibilitiesClusterer("weka.clusterers.HierarchicalClusterer -N 3 -L SINGLE -P -A \"weka.core.EuclideanDistance -R first-last\"",filterArguments,true);
		clusterers.add(clusterer0);
		
		// Xmeans
		ResponsibilitiesClusterer clusterer1 = new ResponsibilitiesClusterer("weka.clusterers.XMeans -I 1 -M 1000 -J 1000 -L 2 -H 4 -B 1.0 -C 0.5 -D \"weka.core.EuclideanDistance -R first-last\" -S 10",filterArguments,true);
		clusterers.add(clusterer1);

		// EM
		ResponsibilitiesClusterer clusterer2 = new ResponsibilitiesClusterer("weka.clusterers.EM -I 100 -N 2 -X 10 -max -1 -ll-cv 1.0E-6 -ll-iter 1.0E-6 -M 1.0E-6 -K 10 -num-slots 1 -S 100",filterArguments,true);
		clusterers.add(clusterer2);

		// DBScan
		ResponsibilitiesClusterer clusterer3 = new ResponsibilitiesClusterer("weka.clusterers.DBSCAN -E 0.9 -M 6 -A \"weka.core.EuclideanDistance -R first-last\"",filterArguments,true);
		clusterers.add(clusterer3);

		// PAM
		ResponsibilitiesClusterer clusterer4 = new DummyResponsibilitiesClusterer("", filterArguments, true, "PAM");
		clusterers.add(clusterer4);
		
		// KMeans
		ResponsibilitiesClusterer clusterer5 = new DummyResponsibilitiesClusterer("", filterArguments, true, "KMeans");
		clusterers.add(clusterer5);

		Boolean simplified = true;
		
		// Se genera los archivos tipo proyecto, que son las responsabilidades, junto a su clase, y el componente asignado
		// Los archivos tipo proyecto sirven para calcular las metricas
	
		for (ResponsibilitiesClusterer clusterer : clusterers){
			clusterer.performSimpleClustering("resources", 2);
			clusterer.performComplexClustering("resources", 2);
			
			/*
			 * En un futuro usar los 17 proyectos!!
			for (int i = 1; i < 18 ; i++){
				clusterer.clusterization("resources", i);
			}			
			*/
			
			/*clusterer.clusterization("resources", 8);
			clusterer.clusterization("resources", 9);
			clusterer.clusterization("resources", 16);
			clusterer.clusterization("resources", 17);
			*/
		}		

		// Esto era para el caso anterior
		//int[] proyectos = {2,8,9,16,17};
		
		//TODO dejar preparado para que el texto que se genere pueda ser facilmente copiado y pegado a un excel
		
		int[] proyectos = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
		for (ResponsibilitiesClusterer clusterer : clusterers){
			for (int i = 0; i < proyectos.length; i++){
				ClusterEvaluation evaluation = new ClusterEvaluation("resources\\proyecto_"+proyectos[i]+"_"+clusterer.getName()+"_1.arff");
				
				System.out.println("\n\nAlgoritmo : " + clusterer.getName());
				System.out.println("Proyecto : " + proyectos[i]);
				System.out.println("Métricas---------------------------------------------------");
				System.out.println();

				System.out.println("Entropy =\t\t[" + evaluation.getEntropy() + "]"); 						// Correcto anda bien
				System.out.println("Purity =\t\t[" + evaluation.getPurity() + "]"); 						// Correcto anda bien
				System.out.println("F-Measure =\t\t[" + evaluation.getFmeasure() + "]");					// Correcto anda bien
				System.out.println("Rand Index =\t\t[" + evaluation.getRandIndex() + "]");					// Correcto anda bien
				System.out.println("Adjusted Rand Index =\t[" + evaluation.getAdjustedRandIndex() + "]");	// Correcto anda bien
				
				System.out.println();
			}			
		}

		

	}

}

