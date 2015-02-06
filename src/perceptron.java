import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Nisansa on 15/02/01.
 * @author Nisansa de Silva
 *
 */
@SuppressWarnings("unchecked")
public class perceptron {
    String baseString="Base";
    HashMap<String,Double> weights;

    public static void main(String[] args) {
        perceptron per;
        if (args.length != 4) {
            System.err.println("Error in arguments");
            per = new perceptron("spambase-train.csv", "spambase-test.csv", 1 , "model.model");
        } else {
            if (!args[0].contains(".csv")) {
                args[0] = args[0] + ".csv";
            }
            if (!args[1].contains(".csv")) {
                args[1] = args[1] + ".csv";
            }
            double eta=Double.valueOf(args[2]);
            if (!args[3].contains(".model")) {
                args[3] = args[3] + ".model";
            }
            per = new perceptron(args[0], args[1],eta, args[3]); //"spambase-train", "spambase-test", 0.01 , "model"
        }
    }

    public perceptron(String trainingSet,String testSet,double eta,String modelFile) {

        String[][] training_data = readFile(trainingSet);
        EntrySet training_set = new EntrySet(training_data);
        String[][] test_data = readFile(testSet);
        EntrySet test_set = new EntrySet(test_data);


        weights=new HashMap<String,Double>();
        weights.put(baseString,0.0);
        Iterator<String> kItr=training_set.getKeyItr();
        while(kItr.hasNext()){
            String key=kItr.next();
            if(!key.equalsIgnoreCase("Spam")) {
                weights.put(key, 0.0);
            }
        }


        //Epochs
        boolean loop=true;
        int epochCount=0;
        double training_accuracy = 100;
        do {
            epochCount++;

            //Train
            Iterator<HashMap<String, Boolean>> itr=training_set.getIterator();
            while(itr.hasNext()){
                HashMap<String, Boolean> example=itr.next();

                //Activate Perceptron
                double[] result=activate(example); //{sum,ans}

                //Calculate error
                double error=result[1]-result[0];

                //Update weights
                kItr=example.keySet().iterator();
                while(kItr.hasNext()) {
                    String key = kItr.next();
                    if (!key.equalsIgnoreCase("Spam")) {
                        double x=0;
                        if(example.get(key)){
                            x=1;
                        }
                        else{
                            x=-1;
                        }
                        double newWeight=weights.get(key)+eta*error*x;
                        weights.remove(key);
                        weights.put(key,newWeight);
                    }
                }

                //Update base
                double newBase=weights.get(baseString)+eta*error*1;
                weights.remove(baseString);
                weights.put(baseString,newBase);
            }


            //Test
            itr =training_set.getIterator();// test_set.getIterator();
            int count = 0;
            training_accuracy = 100;
            while (itr.hasNext()) {
                HashMap<String, Boolean> observation = itr.next();
                double[] result=activate(observation); //{sum,ans}
                if (result[0] == result[1]) {
                    count++;
                }
            }
            training_accuracy = ( (((double) count * 100) / (double) training_set.Size()));

            if(training_accuracy>=100 || epochCount>=100){
                loop=false;
            }

            //Print on screen
           // System.out.println("Epoch : " + epochCount );



        } while (loop);

        //Print file
        String line=weights.get(baseString)+"\n";
        kItr=weights.keySet().iterator();
        while(kItr.hasNext()) {
            String key = kItr.next();
            line+=key+" "+weights.get(key)+"\n";
        }
        writeFile(modelFile,line);

        //Test for test set
        Iterator<HashMap<String, Boolean>> itr =test_set.getIterator();
        int count = 0;
        double test_accuracy = 100;
        String predictions="";
        while (itr.hasNext()) {
            HashMap<String, Boolean> observation = itr.next();
            double[] result=activate(observation); //{sum,ans}
            if (result[0] == result[1]) {
                count++;
            }
            System.out.println(result[0]);
            if(result[0]>0) {
                predictions += 1 + "\n";
            }
            else{
                predictions += 0 + "\n";
            }
        }
        test_accuracy = ( (((double) count * 100) / (double) test_set.Size()));
        writeFile("predictions.txt",predictions);

        System.out.println("Training accuracy : " + training_accuracy + "%");
        System.out.println("Test accuracy : " + test_accuracy + "%");
    }


    private double[] activate(HashMap<String, Boolean> example){
        Iterator<String> kItr=example.keySet().iterator();
        double[] result=new double[2]; //{sum,ans}
        result[0]=weights.get(baseString);
        result[1]=0;
        while(kItr.hasNext()){
            String key=kItr.next();
            if(!key.equalsIgnoreCase("Spam")) {
                if(example.get(key)){
                    result[0]+=weights.get(key);
                }
                else{
                    result[0]-=weights.get(key);
                }
            }
            else{
                if(example.get(key)){
                    result[1]=1;
                }
                else{
                    result[1]=-1;
                }
            }
        }

        //Step function
        if(result[0]>0){
            result[0]=1;
        }
        else{
            result[0]=-1;
        }

        return result;
    }




    public void writeFile(String fileName,String content){
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "utf-8"));
            writer.write(content);
        } catch (IOException ex) {
        } finally {
            try {writer.close();} catch (Exception ex) {}
        }
    }

    public String[][] readFile(String filename) {
        String[][] data=new String[0][0];
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
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

