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
		String[][] data=new String[9][4];
		data[0]=new String[]{"V1","V2","V3","Class"};
		data[1]=new String[]{"0","0","0","0"};
		data[2]=new String[]{"0","0","1","0"};
		data[3]=new String[]{"0","1","0","0"};
		data[4]=new String[]{"0","1","1","0"};
		data[5]=new String[]{"1","0","0","0"};
		data[6]=new String[]{"1","0","1","0"};
		data[7]=new String[]{"1","1","0","1"};
		data[8]=new String[]{"1","1","1","1"};
		id3 dt=new id3();

		//Train
	//	String[][] data=dt.readFile("training_set");
		EntrySet en=new EntrySet(data);
		Node n=new Node(en);
		n.MakeChildren();
		System.out.println(n.getString(""));

		Iterator<HashMap<String,Boolean>> itr=en.getIterator();
		while(itr.hasNext()){
			//System.out.println("new");
			HashMap<String,Boolean> observation=itr.next();
			System.out.println(n.evaluate(observation)+" "+observation.get("Class"));
		}


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
