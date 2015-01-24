package id3;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author Nisansa de Silva
 *
 */
public class id3 {

	public static void main(String[] args) {
		id3 dt = new id3();
	}

	public id3() {

		String[][] training_data = readFile("training_set");
		EntrySet training_set = new EntrySet(training_data);
		String[][] test_data = readFile("test_set");
		EntrySet test_set = new EntrySet(test_data);

		//Train
		Node n = new Node(training_set);
		n.chiLimit=6.635;
		boolean loop=false;
		do {
			loop=n.MakeChildren();
			n.levelPassed = true;
		} while (loop);


		//Test
		Iterator<HashMap<String, Boolean>> itr = test_set.getIterator();
		int count = 0;
		double accuracy = 100;
		while (itr.hasNext()) {
			HashMap<String, Boolean> observation = itr.next();
			if (n.evaluate(observation) == observation.get("Class")) {
				count++;
			}
		}
		accuracy = ( (((double) count * 100) / (double) test_set.Size()));

		//Print on screen
		System.out.println(n.getString(""));
		System.out.println("\nAccuracy : " + accuracy + "%\n");

		//Print file
		writeFile("model",n.getString(""));
	}

	public void writeFile(String fileName,String content){
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName+".model"), "utf-8"));
			writer.write(content);
		} catch (IOException ex) {
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
	}

	public String[][] readFile(String filename) {
		String[][] data=new String[0][0];
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename + ".csv"));
			String line = br.readLine();
			ArrayList<String[]> dataLines=new ArrayList<String[]>();
			while (line != null) {
				dataLines.add(line.split(","));
				line = br.readLine();
			}

			data=new String[dataLines.size()][];
			for (int i = 0; i <dataLines.size() ; i++) {
				data[i]=dataLines.get(i);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public class Node {

		EntrySet en;
		GainElement winner=null;
		Node pChild=null;
		Node nChild=null;
		private Node parent=null;
		Boolean reply=null;
		boolean levelPassed=false;
		double chiLimit=0;

		public Node(String[][] readData) {
			en=new EntrySet(readData);
			calculateRawReply();
		}

		public Node(EntrySet e) {
			en=new EntrySet(e);
			calculateRawReply();
		}

		private void selectAttribute() {
			double entropy = en.calculateEntropy();

			ArrayList<GainElement> gainEls = new ArrayList<GainElement>();
			Iterator<String> itr = en.getKeyItr();
			while (itr.hasNext()) {
				String attr = itr.next();
				if (!attr.equalsIgnoreCase("Class")) {
					EntrySet pHalf = new EntrySet(en);
					EntrySet nHalf = new EntrySet(en);
					pHalf.RetainAttributeValueAndDropName(attr, true);
					nHalf.RetainAttributeValueAndDropName(attr, false);
					double pWeight = ((double) pHalf.Size() / (double) en.Size());
					double nWeight = ((double) nHalf.Size() / (double) en.Size());
					double pEntrophy = pHalf.calculateEntropy();
					double nEntrophy = nHalf.calculateEntropy();
					double remainder = (pWeight * pEntrophy) + (nWeight * nEntrophy);
					double gain = entropy - remainder;
					GainElement ge = new GainElement(attr, gain, pHalf, nHalf, remainder);
					gainEls.add(ge);
				}
			}
			Collections.sort(gainEls);



			GainElement best = gainEls.get(0);

/*		//Add randomization
		ArrayList<GainElement> bestList=new ArrayList<GainElement>();
		double bestGain=gainEls.get(0).gain;
		for (int i = 0; i < gainEls.size(); i++) {
			if(gainEls.get(i).gain>=bestGain){
				bestList.add(gainEls.get(i));
			}
		}
		best=bestList.get(new Random().nextInt(bestList.size())); */

			if (best.gain > 0) {
				winner = best;

				//Calculate Chi-Squared
				int[] thisPN = en.getPNcount();

				int[] pChildPN=winner.phalf.getPNcount();
				double[] pChildExpectedPNprob=calculateExpectedProbs(thisPN,pChildPN);
				double pStatistic= calculateStatisticForSubset(pChildPN,pChildExpectedPNprob);

				int[] nChildPN=winner.nhalf.getPNcount();
				double[] nChildExpectedPNprob=calculateExpectedProbs(thisPN,nChildPN);
				double nStatistic= calculateStatisticForSubset(nChildPN,nChildExpectedPNprob);

				double totalChiSqStatistic=pStatistic+nStatistic;

				if(totalChiSqStatistic<this.chiLimit){
					winner=null; //Pruned
				}
			}

		}


		private double[] calculateExpectedProbs(int[] original,int[] subset){
			return new double[]{calculateExpected(original[0],original,subset),calculateExpected(original[1],original,subset)};
		}

		private double calculateExpected(int lead,int[] original,int[] subset){
			return(lead* (((double)(subset[0]+subset[1]))/((double)(original[0]+original[1]))));
		}


		private double calculateStatisticForSubset(int[] subset,double[] subsetExpected){
			return calculateStatHalf(subset[0],subsetExpected[0])+calculateStatHalf(subset[1],subsetExpected[1]);
		}

		private double calculateStatHalf(double actual,double expected){
			return ((Math.pow((actual-expected),2))/expected);
		}



		private void calculateRawReply(){
			int[] rep = en.getPNcount();
			if (rep[0] > rep[1]) {
				reply = true;
			} else {
				reply = false;
			}
		}


		public boolean evaluate(HashMap<String,Boolean> line){
			if(winner!=null){
				if(line.get(winner.attribute)){
					if(pChild!=null) {
						return (pChild.evaluate(line));
					}
					else{ //Act as a leaf
						return reply;
					}
				}
				else{ //Act as a leaf
					if(nChild!=null) {
						return (nChild.evaluate(line));
					}
					else{
						return reply;
					}
				}
			}
			else{ //IS an actual leaf
				return reply;
			}
		}

		public String getAncestors(){
			String line="";
			if(getParent() !=null){
				line= getParent().winner.attribute+" "+ getParent().getAncestors();
			}
			else{
				line="*";
			}
			return line;
		}

		public boolean MakeChildren(){
			if(winner==null){
				selectAttribute();
			}
			if(winner!=null) {
				if(pChild==null) {
					pChild = new Node(winner.phalf);
					pChild.setParent(this);
				}
				if(nChild==null) {
					nChild = new Node(winner.nhalf);
					nChild.setParent(this);
				}

				if(levelPassed) {
					boolean pBranch= pChild.MakeChildren();
					boolean nBranch= nChild.MakeChildren();
					pChild.levelPassed=true;
					nChild.levelPassed=true;
					return (pBranch||nBranch);
				}
			}

			return (winner!=null);
		}

		public String getString(String indent){
			String line = "";
			if (winner != null) {
				if (!indent.equalsIgnoreCase("")) {
					line += "\n";
				}
				line += indent + winner.attribute + " = 0 : " + nChild.getString(indent + "| ");
				line += "\n";
				line += indent + winner.attribute + " = 1 : " + pChild.getString(indent + "| ");
			}
			else{
				if (reply != null) {
					if (reply) {
						line += 1;
					} else {
						line += 0;
					}
				}
			}
			return line;
		}


		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
			this.chiLimit=parent.chiLimit;
		}
	}

	public class GainElement implements Comparable {

		String attribute="";
		double gain =0;
		EntrySet phalf;
		EntrySet nhalf;
		double remainder;

		public GainElement(String attribute, double gain,EntrySet phalf,EntrySet nhalf,double remainder) {
			this.attribute = attribute;
			this.gain = gain;
			this.phalf = phalf;
			this.nhalf = nhalf;
			this.remainder= remainder;
		}

		@Override
		public int compareTo(Object o) {
			GainElement g=(GainElement)o;
			if(this.gain >g.gain){
				return -1;
			}
			else if(this.gain <g.gain){
				return 1;
			}
			else{
				if(this.attribute.compareToIgnoreCase(g.attribute)>0){
					return 1;
				}
				else if (this.attribute.compareToIgnoreCase(g.attribute)<0){
					return -1;
				}
			}
			return 0;
		}
	}

	public class EntrySet {

		ArrayList<HashMap<String,Boolean>> entries=new ArrayList<HashMap<String,Boolean>>();

		public EntrySet(EntrySet e) {
			this(e.entries);
		}

		public int Size(){
			return entries.size();
		}

		public EntrySet(ArrayList<HashMap<String, Boolean>> e) {
			for (int i = 0; i <e.size() ; i++) {
				HashMap<String, Boolean> entry=e.get(i);
				HashMap<String, Boolean> entryDeepCopy=new HashMap<String, Boolean>();
				Iterator<String> itr=entry.keySet().iterator();
				while(itr.hasNext()){
					String key=itr.next();
					if(entry.get(key)){
						entryDeepCopy.put(key,true);
					}
					else{
						entryDeepCopy.put(key,false);
					}
				}
				entries.add(entryDeepCopy);
			}
		}

		public Iterator<HashMap<String,Boolean>> getIterator(){
			return entries.iterator();
		}

		public EntrySet(String[][] readData) {
			for (int i = 1; i <readData.length ; i++) {
				HashMap<String, Boolean> entry=new HashMap<String, Boolean>();
				for (int j = 0; j <readData[i].length ; j++) {
					int val=Integer.parseInt(readData[i][j]);
					if(val>0) {
						entry.put(readData[0][j], true);
					}
					else{
						entry.put(readData[0][j], false);
					}
				}
				entries.add(entry);
			}
		}

		public void RetainAttributeValueAndDropName(String attr,Boolean val){
			RetainAttributeValueOf(attr,val);
			DropAttribute(attr);
		}

		public void RetainAttributeValueOf(String attr,Boolean val){
			ArrayList<HashMap<String,Boolean>> newEntries=new ArrayList<HashMap<String,Boolean>>();
			for (int i = 0; i < entries.size(); i++) {
				HashMap<String,Boolean> entry=entries.get(i);
				if(entry.get(attr)==val){
					newEntries.add(entry);
				}
			}
			entries=newEntries;
		}

		public void DropAttribute(String attr){
			for (int i = 0; i < entries.size(); i++) {
				entries.get(i).remove(attr);
			}
		}

		public Iterator<String> getKeyItr(){
			return entries.get(0).keySet().iterator();
		}

		public double calculateEntropy(){
			int[] pn=getPNcount();
			return(calculateEntropy(pn[0], pn[1]));
		}

		public int[] getPNcount(){
			int[] counts=new int[2];
			for (int i = 0; i <entries.size() ; i++) {
				if(entries.get(i).get("Class")){
					counts[0]++;
				}
				else{
					counts[1]++;
				}
			}
			return counts;
		}

		public double calculateEntropy(int p, int n){
			return (calculateInfo(p, p+n)+calculateInfo(n, p+n));
		}

		private double calculateInfo(int a, int b){
			double ratio=((double)a)/((double)b);
			if (ratio>0) {
				return (-ratio * log2(ratio));
			}
			else{
				return 0;
			}
		}

		public double log2( double a )
		{
			return logb(a,2);
		}

		public double logb( double a, double b )
		{
			return Math.log(a) / Math.log(b);
		}

	}

}
