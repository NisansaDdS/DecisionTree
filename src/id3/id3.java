package id3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author Nisansa
 *
 */
public class id3 {

	
	
	public static void main(String[] args) {
	/*	String[][] training_data=new String[9][4];
		training_data[0]=new String[]{"V1","V2","V3","Class"};
		training_data[1]=new String[]{"0","0","0","0"};
		training_data[2]=new String[]{"0","0","1","0"};
		training_data[3]=new String[]{"0","1","0","0"};
		training_data[4]=new String[]{"0","1","1","0"};
		training_data[5]=new String[]{"1","0","0","0"};
		training_data[6]=new String[]{"1","0","1","0"};
		training_data[7]=new String[]{"1","1","0","1"};
		training_data[8]=new String[]{"1","1","1","1"}; */
		id3 dt = new id3();

		//Train
		String[][] training_data = dt.readFile("training_set");
		EntrySet training_set = new EntrySet(training_data);
		String[][] validation_data = dt.readFile("validation_set");
		EntrySet validation_set = new EntrySet(validation_data);
		Node n = new Node(training_set);
		double err = 100;
		int depth=0;
		do {
			depth++;
			n.MakeChildren();
			n.levelPassed = true;

			System.out.println(n.getString(""));

			Iterator<HashMap<String, Boolean>> itr = validation_set.getIterator();
			int count = 0;
			while (itr.hasNext()) {
				HashMap<String, Boolean> observation = itr.next();
				if (n.evaluate(observation) == observation.get("Class")) {
					count++;
				}
			}
			err = (100 - (((double) count * 100) / (double) validation_set.Size()));
			System.out.println("Error : " + err + "%\n");
		} while (err > 0.05);
		System.out.println(depth);


		//String[][] test_set = dt.readFile("training_data");
		//EntrySet training_set = new EntrySet(training_data);

		//System.out.println(n.toString());
	}


	public String[][] readFile(String filename) {
		String[][] data=new String[0][0];
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename + ".csv"));
			StringBuilder sb = new StringBuilder();
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


}
