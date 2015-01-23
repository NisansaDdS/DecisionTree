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
		id3 dt = new id3();

		//Train
		String[][] training_data = dt.readFile("training_set");
		EntrySet training_set = new EntrySet(training_data);
		String[][] test_data = dt.readFile("test_set");
		EntrySet test_set = new EntrySet(test_data);


		Node n = new Node(training_set);
		n.chiLimit=6.635;


		double accuracy = 100;
		int depth=0;
		boolean loop=false;
		do {
			depth++;
			loop=n.MakeChildren();
			n.levelPassed = true;

			System.out.println(n.getString(""));

			Iterator<HashMap<String, Boolean>> itr = test_set.getIterator();
			int count = 0;
			while (itr.hasNext()) {
				HashMap<String, Boolean> observation = itr.next();
				if (n.evaluate(observation) == observation.get("Class")) {
					count++;
				}
			}
			accuracy = ( (((double) count * 100) / (double) test_set.Size()));
			System.out.println("Accuracy : " + accuracy + "%\n");
		} while (loop);
		System.out.println(depth);



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


}
