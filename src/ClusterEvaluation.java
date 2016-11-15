import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Map.Entry;


public class ClusterEvaluation {

	// Variables

	HashMap<String, HashSet<Integer>> classes;
	HashMap<String, HashSet<Integer>> clusters;
	Instances dataset; // annotated dataset
	Integer n; // number of instances

	// Variables agregadas por Brian
	HashMap<String, HashSet<Integer>> clustersCorrecto;
	Vector<Integer> elements = new Vector<Integer>();

	// Constructors

	public ClusterEvaluation(String datasetPath) throws Exception {

		this.classes = new HashMap<String, HashSet<Integer>>();
		this.clusters = new HashMap<String, HashSet<Integer>>();
		this.clustersCorrecto = new HashMap<String, HashSet<Integer>>();

		// Se lee el arff
		BufferedReader reader = new BufferedReader(new FileReader(datasetPath));
		this.dataset = new Instances(reader);
		reader.close();

		Attribute aClass = dataset.attribute("component");
		Attribute aCluster = dataset.attribute("Cluster");
		n = 0;

		for (int index = 0; index < dataset.numInstances(); index++) {
			Instance instance = dataset.instance(index);
			n++;

			elements.add(n);
					
			String iclass = instance.stringValue(aClass);
			String icluster = instance.stringValue(aCluster);
			HashSet<Integer> curr_classes = classes.get(iclass);

			if (curr_classes == null)
				curr_classes = new HashSet<Integer>();
			curr_classes.add(n);
			classes.put(iclass, curr_classes);

			HashSet<Integer> curr_clusters = clusters.get(icluster);
			if (curr_clusters == null)
				curr_clusters = new HashSet<Integer>();
			curr_clusters.add(n);

			clusters.put(icluster, curr_clusters);
			HashSet<Integer> curr_clusters2 = clustersCorrecto.get(iclass);
			if (curr_clusters2 == null)
				curr_clusters2 = new HashSet<Integer>();
			curr_clusters2.add(n);
			clustersCorrecto.put(iclass, curr_clusters2);
		}

		// Obtenido
		System.out.println("--------Clusters--------");
		for (Entry<String, HashSet<Integer>> cluster : clusters.entrySet())
			System.out.println("\t" + cluster.getKey() + ": "
					+ cluster.getValue().size());
		System.out.println("\n--------Classes--------");
		for (Entry<String, HashSet<Integer>> cl : classes.entrySet())
			System.out
			.println("\t" + cl.getKey() + ": " + cl.getValue().size());

		System.out.println();

		// Correcto
		System.out.println("--------Clusters--------");
		for (Entry<String, HashSet<Integer>> cluster : clustersCorrecto.entrySet())
			System.out.println("\t" + cluster.getKey() + ": "
					+ cluster.getValue().size());

		System.out.println();

		// Tablita

		System.out.print("Clase\t");
		for (Entry<String, HashSet<Integer>> cluster : clusters.entrySet()){
			System.out.print(cluster.getKey() + "\t");
		}
		System.out.println();

		for (Entry<String, HashSet<Integer>> cl : classes.entrySet()){
			System.out.print(cl.getKey().substring(0, 3) + "\t");		
			for (Entry<String, HashSet<Integer>> cluster : clusters.entrySet()){
				System.out.print(getIntersect(cluster.getValue(),cl.getValue()) + "\t\t");
			}
			System.out.println();
		}


	}

	// Getters and Setters

	// Methods

	private int getIntersect(HashSet<Integer> a, HashSet<Integer> b) {
		HashSet<Integer> clone = new HashSet<Integer>(a);
		clone.retainAll(b);
		return clone.size();
	}

	public double getPurity() {
		double p = 0.0;
		for (HashSet<Integer> icluster : clusters.values()) {
			double max = 0.0;
			for (HashSet<Integer> iclass : classes.values()) {
				double intersect = getIntersect(icluster, iclass);
				if (intersect > max)
					max = intersect;
			}
			p += max;
		}
		return p / n;
	}

	public String getInversePurity() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getPartitionCoefficient() {
		double pc = 0.0;
		for (HashSet<Integer> icluster : clusters.values()) {
			for (HashSet<Integer> iclass : classes.values()) {
				double intersect = getIntersect(icluster, iclass);
				intersect /= icluster.size();
				pc += Math.pow(intersect, 2);
			}
		}
		return pc / (clusters.size() * classes.size());
	}

	public double getEntropy() {
		double e = 0.0;
		for (HashSet<Integer> icluster : clusters.values()) {
			double r = (double) icluster.size() / n;
			r *= (-1.0) / Math.log(classes.size());
			for (HashSet<Integer> iclass : classes.values()) {
				double intersect = getIntersect(icluster, iclass);
				intersect /= icluster.size();
				if (intersect > 0)
					intersect *= Math.log(intersect);
				e += r * intersect;
			}
		}
		return e;
	}

	public double getFmeasureByCluster() {
		double f = 0.0;
		for (HashSet<Integer> icluster : clusters.values()) {
			double max = 0.0;
			for (HashSet<Integer> iclass : classes.values()) {
				double intersect = getIntersect(icluster, iclass);
				double p = intersect / icluster.size();
				double r = intersect / iclass.size();
				double fm = 2 * p * r / (p + r);
				if (fm > max)
					max = fm;
			}
			f += icluster.size() * max / n;
		}
		return f;
	}

	public double getFmeasure() {
		double f = 0.0;
		for (HashSet<Integer> iclass : classes.values()) {
			double max = 0.0;
			for (HashSet<Integer> icluster : clusters.values()) {
				double intersect = getIntersect(icluster, iclass);
				double p = intersect / icluster.size();
				double r = intersect / iclass.size();
				double fm = 2 * p * r / (p + r);
				if (fm > max)
					max = fm;
			}
			f += iclass.size() * max / n;
		}
		return f;
	}

	public double getRandIndex(){
		double ri = 0.0;

		int TP = 0;
		int TN = 0;
		int FP = 0;
		int FN = 0;

		for (int i = 0; i < elements.size() - 1; i++) {

			for (int j = i+1; j < elements.size(); j++) {

				Integer iElement = elements.get(i);
				Integer jElement = elements.get(j);

				for (HashSet<Integer> iCluster : clusters.values()){
					for (HashSet<Integer> jCluster : clustersCorrecto.values()){

						if (iCluster.contains(iElement) && iCluster.contains(jElement) && jCluster.contains(iElement) && jCluster.contains(jElement))
							TP++;
						else
							if (iCluster.contains(iElement) && !iCluster.contains(jElement) && !jCluster.contains(iElement) && jCluster.contains(jElement))
								TN++;
							else{
								if (iCluster.contains(iElement) && iCluster.contains(jElement) && !jCluster.contains(iElement) && jCluster.contains(jElement))
									FP++;
								else  if (!iCluster.contains(iElement) && iCluster.contains(jElement) && jCluster.contains(iElement) && jCluster.contains(jElement))
									FN++;
							}
					}
				}
			}
		}

		System.out.println("TP = " + TP);
		System.out.println("TN = " + TN);
		System.out.println("FP = " + FP);
		System.out.println("FN = " + FN);


		// Ambas son validas
		ri = ((double) TP + (double) TN) / ((double) TP + (double) TN + (double) FP + (double) FN);
		//ri = ((double) TP + (double) TN) / choose(n, 2).doubleValue();

		return ri;
	}

	public double getAdjustedRandIndex(){
		double ari = 0.0;

		double part1 = 0.0;
		double part2 = 0.0;
		double part3 = 0.0;

		// part1

		for (HashSet<Integer> iclass : classes.values()) {
			for (HashSet<Integer> icluster : clusters.values()) {
				Double intersect = (double) getIntersect(icluster, iclass);
				part1+= choose(intersect.intValue(),2).doubleValue();
			}
		}

		// part2

		Double sumaClase = 0.0;
		for (HashSet<Integer> iclass : classes.values()) {
			Double sumaCluster = 0.0;
			for (HashSet<Integer> icluster : clusters.values()) {
				Double intersect = (double) getIntersect(icluster, iclass);
				sumaClase+= intersect;
				sumaCluster += choose(icluster.size(),2).doubleValue();				
			}
			double sumaClaseTomadoDeADos = choose(sumaClase.intValue(),2).doubleValue();
			part2+= sumaClaseTomadoDeADos * sumaCluster;
			sumaClase = 0.0;
		}

		// part3

		sumaClase = 0.0;
		for (HashSet<Integer> iclass : classes.values()) {
			for (HashSet<Integer> icluster : clusters.values()) {
				Double intersect = (double) getIntersect(icluster, iclass);
				sumaClase+= intersect;				
			}
			double sumaClaseTomadoDeADos = choose(sumaClase.intValue(),2).doubleValue();
			part3+= sumaClaseTomadoDeADos;
			sumaClase = 0.0;
		}
		Double sumaCluster = 0.0;
		for (HashSet<Integer> icluster : clusters.values()) {
			sumaCluster += choose(icluster.size(),2).doubleValue();				
		}
		part3+= sumaCluster;


		// ----

		System.out.println("Part 1 " + part1);
		System.out.println("Part 2 " + part2);
		System.out.println("Part 3 " + part3);

		ari = (part1 - (part2/choose(n,2).doubleValue())) / ((part3/2.0) - ((part2/choose(n,2).doubleValue())));

		return ari;
	}

	private BigInteger choose(int N, int K){
		BigInteger ret = BigInteger.ONE;

		for (int k  = 0; k < K; k++){
			ret = ret.multiply(BigInteger.valueOf(N-k)).divide(BigInteger.valueOf(k+1));
		}

		return ret;

	}

}