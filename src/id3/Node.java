package id3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Node {

	EntrySet en;
	GainElement winner=null;
	Node pChild=null;
	Node nChild=null;
	Node parent=null;
	Boolean reply=null;
	boolean levelPassed=false;


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

		if (best.gain > 0) {
			winner = best;
		}
			//System.out.println("gggggggggggggggggggggggggggggg");

		//	System.out.println("aaaa " + reply);
		//	System.out.println(parent.winner.attribute);
		//	System.out.println(parent.getAncestors());


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
		if(parent!=null){
			line=parent.winner.attribute+" "+parent.getAncestors();
		}
		else{
			line="*";
		}
		return line;
	}


	public void MakeChildren(){
		if(winner==null){
			selectAttribute();
		}
		if(winner!=null) {
			if(pChild==null) {
				pChild = new Node(winner.phalf);
				pChild.parent=this;
			}
			if(nChild==null) {
				nChild = new Node(winner.nhalf);
				nChild.parent=this;
			}

		//	System.out.println(levelPassed);

			if(levelPassed) {
				pChild.MakeChildren();
				nChild.MakeChildren();
				pChild.levelPassed=true;
				nChild.levelPassed=true;
			}
		}
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




}
