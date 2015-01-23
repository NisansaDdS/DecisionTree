package id3;

import java.util.*;

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
				//DecimalFormat df = new DecimalFormat("#.######");
				//System.out.println(attr+" "+df.format(gain));
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

			System.out.println("Chi of "+winner.attribute+" : "+totalChiSqStatistic);
			System.out.println("Against "+this.chiLimit);
			if(totalChiSqStatistic<this.chiLimit){
				winner=null; //Pruned
				//System.out.println("Pruned!");
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

		//	System.out.println(levelPassed);

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
			line += "\n" + indent + winner.attribute + " = 0: " + nChild.getString(indent + "| ");
			line += "\n" + indent + winner.attribute + " = 1: " + pChild.getString(indent + "| ");
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
