import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import de.lmu.ifi.dbs.elki.algorithm.clustering.DBSCAN;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMedoidsPAM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.FirstKInitialMeans;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMedoidsInitialization;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.PAMInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.model.MedoidModel;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.EvaluateSimplifiedSilhouette;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.NoiseHandling;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.FilteredClusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ResponsibilitiesClusterer {

	// Variables

	private Boolean debugMode;
	private FilteredClusterer wekaClusterer;
	private String name;
	private Boolean dummy = false;

	// Constructors

	public ResponsibilitiesClusterer(String arguments, String filterArguments, Boolean debugMode){
		super();
		this.debugMode = debugMode;

		if (arguments == null && filterArguments == null){
			//ERROR
		}else{
			if (arguments != null && filterArguments == null){
				// Solo clusterizador
				this.wekaClusterer = loadClusterer(arguments);				
			}else{
				if (arguments != null && filterArguments != null){
					// Clasificador con filtro
					this.wekaClusterer = loadClusterer(arguments, filterArguments);
				}else{
					//Dummy
					this.wekaClusterer = loadClusterer("weka.clusterers.SimpleKMeans",filterArguments);
					dummy = true;
				}
			}
		}
	}

	// Getters and Setters

	public void setName(String name){
		this.name = name;
	}

	// Methods

	private FilteredClusterer loadClusterer(String arguments){
		FilteredClusterer out = null;
		String classifierName = arguments.split(" ")[0].trim();

		try {
			out = (FilteredClusterer) Class.forName(classifierName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return out;
	}	

	private FilteredClusterer loadClusterer(String arguments, String filterArguments){
		FilteredClusterer out = new FilteredClusterer();

		AbstractClusterer clusterer = null;
		StringToWordVector filter = null;

		// Load filters

		String filterName = filterArguments.split(" ")[0].trim();
		filterArguments = filterArguments.substring(filterArguments.split(" ")[0].length()).trim();

		try {
			filter = (StringToWordVector) Class.forName(filterName).newInstance();
			String[] options = weka.core.Utils.splitOptions(filterArguments);
			filter.setOptions(options);	
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Load the clusterer

		String clustererName = arguments.split(" ")[0].trim();
		arguments = arguments.substring(arguments.split(" ")[0].length()).trim();

		try {
			clusterer = (AbstractClusterer) AbstractClusterer.forName(clustererName, weka.core.Utils.splitOptions(arguments));

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Se adjunta lo cargado
		out.setFilter(filter);
		if (clusterer != null)
			out.setClusterer(clusterer);

		return out;
	}

	public String getName(){
		if (dummy)
			return name;
		return this.wekaClusterer.getClusterer().getClass().getName().split("clusterers\\.")[1];
	}

	private String[] getAssignation(Clustering<?> c, Database db, Instances dataFiltered) {

		// Relation containing the number vectors:
		Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		// We know that the ids must be a continuous range:
		DBIDRange ids = (DBIDRange) rel.getDBIDs();

		String[] assignation = new String[dataFiltered.numInstances()];
		int i = 0;
		for(Cluster<?> clu : c.getAllClusters()) {

			// K-means will name all clusters "Cluster" in lack of noise support:

			System.out.println("\n#" + i + ": " + clu.getNameAutomatic());
			System.out.println("Size: " + clu.size());

			// Iterate over objects:
			System.out.print("Objects: ");
			for(DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
				// To get the vector use:
				//NumberVector v = rel.get(it);

				// Offset within our DBID range: "line number"
				final int offset = ids.getOffset(it);
				System.out.print(" " + offset);
				// Do NOT rely on using "internalGetIndex()" directly!

				assignation[offset] = "cluster_" + i;
			}
			System.out.println();
			++i;
		}

		return assignation;
	}

	private String printResults(String type, Instances data, FilteredClusterer wekaClusterer, String[] assignation, Integer assignationClusters){
		String out = "";

		out+= "@relation " + data.relationName() + "\n\n"; 
		out += "@attribute Instance_number numeric\n";
		for (int i = 0; i < data.numAttributes(); i++){
			out+= data.attribute(i) + "\n";
		}
		out += "@attribute Cluster {";

		switch(type){
		case "weka":{

			try {
				for (int i = 0; i < wekaClusterer.numberOfClusters(); i++){
					out += "cluster_"+i+",";
				}
				out = out.substring(0, out.length() - 1);			
				out += "}\n\n@data\n";

				for (int i = 0; i < data.numInstances(); i++){
					Instance instance = data.instance(i);
					int clusterInstance = this.wekaClusterer.clusterInstance(instance);			
					out += i + "," + instance + ",cluster_"+clusterInstance+"\n";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		}
		default:{

			Vector<String> clusters = new Vector<String>();
			for (int i = 0; i < assignation.length; i++){
				if (!clusters.contains(assignation[i])){
					clusters.add(assignation[i]);
				}
			}
			
			for (int i = 0; i < clusters.size(); i++){
				out += clusters.elementAt(i)+",";
			}
			out = out.substring(0, out.length() - 1);			
			out += "}\n\n@data\n";

			for (int i = 0; i < data.numInstances(); i++){
				out += i + "," + data.instance(i) + ","+assignation[i]+"\n";
			}

			break;
		}
		}

		return out;
	}

	public void clusterization(String arffPath, Integer proyecto){

		String output = arffPath + "\\proyecto_"+proyecto+"_" + getName() +".arff";
		String out = "";

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(arffPath+"\\clustering_"+proyecto+".arff"));
			ArffReader arff = new ArffReader(reader);
			Instances data = arff.getData();		
			data.setClassIndex(data.numAttributes() - 1);

			// generate data for clusterer (w/o class)

			Remove filter = new Remove();
			filter.setAttributeIndices("" + (data.classIndex() + 1));
			filter.setInputFormat(data);
			Instances dataClusterer = Filter.useFilter(data, filter);

			// Se genera el clusterer
			if (this.wekaClusterer != null)
				this.wekaClusterer.buildClusterer(dataClusterer);

			// DBScan
			if (getName().toLowerCase().equals("dbscan")){
				//Se tiene que hacer otro llamado.. pero la escritura del archivo final se mantiene igual

				Instances dataFiltered = Filter.useFilter(dataClusterer, wekaClusterer.getFilter());
				double[][] dataArray = new double[dataFiltered.numInstances()][dataFiltered.numAttributes()];

				// ELKI
				for (int i = 0 ; i < dataFiltered.numInstances(); i++){
					for (int j = 0; j < dataFiltered.numAttributes(); j++){
						dataArray[i][j] = dataFiltered.instance(i).value(j);
					}					
					//System.out.println(dataFiltered.instance(i));
				}						

				DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataArray);
				Database db = new StaticArrayDatabase(dbc, null);
				db.initialize();

				// K-means should be used with squared Euclidean (least squares):
				SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;

				// 15 me parecio el valor correcto.. Hipotesis, el epsilon utilizarlo con el numero de responsabilidades
				// 2 es el minimo, no queremos cluster de tamaño 1
				DBSCAN<NumberVector> dbscan = new DBSCAN<NumberVector>(dist,15,2);
				// Run the algorithm:
				Clustering<Model> c = dbscan.run(db);				

				String [] assignation = getAssignation(c,db,dataFiltered);

				ArrayList<String> clusters = new ArrayList<String>();
				for (int i = 0; i < assignation.length; i++){
					if (!clusters.contains(assignation[i]))
						clusters.add(assignation[i]);
				}

				out = printResults("dbscan",data,wekaClusterer,assignation,clusters.size());

			}else{
				if (getName().toLowerCase().equals("pam")){

					Instances dataFiltered = Filter.useFilter(dataClusterer, wekaClusterer.getFilter());
					double[][] dataArray = new double[dataFiltered.numInstances()][dataFiltered.numAttributes()];

					// ELKI
					for (int i = 0 ; i < dataFiltered.numInstances(); i++){
						for (int j = 0; j < dataFiltered.numAttributes(); j++){
							dataArray[i][j] = dataFiltered.instance(i).value(j);
						}					
						//System.out.println(dataFiltered.instance(i));
					}						

					DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataArray);
					Database db = new StaticArrayDatabase(dbc, null);
					db.initialize();

					// K-means should be used with squared Euclidean (least squares):
					SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;
					//RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
					KMedoidsInitialization<NumberVector> init = new PAMInitialMeans<NumberVector>();

					// en estos casos se repite el proceso variando el k hasta que se maximize el coeficiente de siluete

					Clustering<MedoidModel> c = null;
					Vector<Double> coeficientes = new Vector<Double>();

					for (int k = 1; k < data.numInstances(); k++){
						KMedoidsPAM<NumberVector> pam = new KMedoidsPAM<NumberVector>(dist, k, 1000, init);
						c = pam.run(db);
						Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
						EvaluateSimplifiedSilhouette evalSil = new EvaluateSimplifiedSilhouette(dist, NoiseHandling.MERGE_NOISE, true);
						Double coeficienteSil = evalSil.evaluateClustering(db, rel, c);
						coeficienteSil /= data.numInstances();
						if (coeficienteSil.isNaN())
							coeficientes.add(-1.0);
						else
							coeficientes.add(coeficienteSil);
					}

					Double max = Collections.max(coeficientes);
					int bestK = coeficientes.indexOf(max);
					System.out.println("Best K " + bestK + "   " + coeficientes);

					KMedoidsPAM<NumberVector> pam = new KMedoidsPAM<NumberVector>(dist, bestK, 1000, init);
					c = pam.run(db);

					String [] assignation = getAssignation(c,db,dataFiltered);

					ArrayList<String> clusters = new ArrayList<String>();
					for (int i = 0; i < assignation.length; i++){
						if (!clusters.contains(assignation[i]))
							clusters.add(assignation[i]);
					}

					out = printResults("pam",data,wekaClusterer,assignation,clusters.size());

				}else{
					if (getName().toLowerCase().equals("kmeans")){

						Instances dataFiltered = Filter.useFilter(dataClusterer, wekaClusterer.getFilter());
						double[][] dataArray = new double[dataFiltered.numInstances()][dataFiltered.numAttributes()];

						// ELKI
						for (int i = 0 ; i < dataFiltered.numInstances(); i++){
							for (int j = 0; j < dataFiltered.numAttributes(); j++){
								dataArray[i][j] = dataFiltered.instance(i).value(j);
							}					
							//System.out.println(dataFiltered.instance(i));
						}						

						DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataArray);
						Database db = new StaticArrayDatabase(dbc, null);
						db.initialize();

						// K-means should be used with squared Euclidean (least squares):
						SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;
						//RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
						//KMedoidsInitialization<NumberVector> init = new PAMInitialMeans<NumberVector>();

						// en estos casos se repite el proceso variando el k hasta que se maximize el coeficiente de siluete

						Clustering<KMeansModel> c = null;
						Vector<Double> coeficientes = new Vector<Double>();

						for (int k = 1; k < data.numInstances(); k++){

							KMeansLloyd<NumberVector> kmeans = new KMeansLloyd<NumberVector>(dist, k, 100, new FirstKInitialMeans<>());

							c = kmeans.run(db);
							Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
							EvaluateSimplifiedSilhouette evalSil = new EvaluateSimplifiedSilhouette(dist, NoiseHandling.MERGE_NOISE, true);
							Double coeficienteSil = evalSil.evaluateClustering(db, rel, c);
							coeficienteSil /= data.numInstances();
							if (coeficienteSil.isNaN())
								coeficientes.add(-1.0);
							else
								coeficientes.add(coeficienteSil);
						}

						Double max = Collections.max(coeficientes);
						int bestK = coeficientes.indexOf(max);
						System.out.println("Best K : " + bestK + "   " + coeficientes);

						KMeansLloyd<NumberVector> kmeans = new KMeansLloyd<NumberVector>(dist, bestK, 100, new FirstKInitialMeans<>());
						c = kmeans.run(db);

						String [] assignation = getAssignation(c,db,dataFiltered);

						ArrayList<String> clusters = new ArrayList<String>();
						for (int i = 0; i < assignation.length; i++){
							if (!clusters.contains(assignation[i]))
								clusters.add(assignation[i]);
						}

						out = printResults("kmeans",data,wekaClusterer,assignation,clusters.size());


					}else{

						// evaluate clusterer
						//ClusterEvaluation eval = new ClusterEvaluation();
						//eval.setClusterer(wekaClusterer);
						//eval.evaluateClusterer(data);

						// print results
						//System.out.println(eval.clusterResultsToString());

						out = printResults("weka",data,wekaClusterer,null,0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//System.out.println(out);

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			writer.write(out);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
