import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nisansa on 15/02/15.
 */
public class nb {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Error in arguments");
            nb n = new nb("training_set.csv", "test_set.csv", 1,"model.model"); //"training_set", "test_set",1, "model"
        } else {
            if (!args[0].contains(".csv")) {
                args[0] = args[0] + ".csv";
            }
            if (!args[1].contains(".csv")) {
                args[1] = args[1] + ".csv";
            }
            int beta=Integer.valueOf(args[2]);
            if (!args[3].contains(".model")) {
                args[3] = args[3] + ".model";
            }
            nb n = new nb(args[0], args[1],beta, args[2]); //"training_set", "test_set", "model"
        }
    }


    public nb(String trainingSet,String testSet,int beta,String modelFile) {
        DataSet trainData=new DataSet(readFile(trainingSet),beta);
        DataSet testData=new DataSet(readFile(testSet),beta);
        testData.Test(trainData.getBase(),trainData.getWeights());
        writeFile(modelFile,trainData.toString());
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




    public ArrayList<DataColumn> readFile(String filename) {
        ArrayList<DataColumn> columns=new ArrayList<DataColumn>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();

            while (line != null) {
                String[] values=line.split(",");
                if(columns.size()==0){
                    for (int i = 0; i < values.length; i++) {
                        DataColumn c=new DataColumn(values[i]);
                        columns.add(c);
                    }
                }
                else{
                    for (int i = 0; i < values.length; i++) {
                        columns.get(i).addValue(values[i]);
                    }
                }

                line = br.readLine();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return columns;
    }

    public class DataSet {
        ArrayList<DataColumn> columns=new ArrayList<DataColumn>();
        DataColumn cla=null;
        int beta=0;

        public DataSet(ArrayList<DataColumn> col,int beta) {
            this.beta=beta-1;

            for (int i = 0; i < col.size(); i++) {
                DataColumn temp=col.get(i);
                if(temp.name.equalsIgnoreCase("Class")){
                    cla=temp;
                }
                else{
                    columns.add(temp);
                }
            }
        }

        public String toString(){
            String line=getBase()+"\n";
            HashMap<String, Double> weights=getWeights();
            for (int i = 0; i < columns.size(); i++) {
                String name=columns.get(i).name;
                line+=name+" "+weights.get(name)+"\n";
            }
            return line;
        }


        public void Test(double base,HashMap<String, Double> weights){
            int count=0;
            for (int i = 0; i <cla.size(); i++) {
                double val=base;
                for (int j = 0; j < columns.size(); j++) {
                    DataColumn c= columns.get(j);
                    if(c.get(i)){
                      //  System.out.println(c.name+" "+weights.get(c.name));
                        val+=weights.get(c.name);
                    }
                }

                val=1/(1+Math.exp(-val));

                boolean res=false;
                if(val>=0){
                    res=true;
                }
                if(cla.get(i)== res){
                    count++;
                }

                System.out.println(val);
            }
           // System.out.println("\nTest set Accuracy "+(count*100)/cla.size());
        }


        public double getBase(){
            double base=logRatio(cla.count(true)+beta,cla.count(false)+beta);
            for (int i = 0; i < columns.size(); i++) {
                DataColumn c=columns.get(i);
                ArrayList<Integer> rows=c.getRows(false);
                base+=logRatio(cla.count(true,rows)+beta,cla.count(false,rows)+beta);
            }
            //System.out.println("Base "+base);
            return base;
        }

        public HashMap<String, Double> getWeights(){
            HashMap<String, Double> weights=new HashMap<String, Double>();
            for (int i = 0; i < columns.size() ; i++) {
                DataColumn c=columns.get(i);
                ArrayList<Integer> rows1=c.getRows(true);
                double weight=logRatio(cla.count(true, rows1) + beta, cla.count(false, rows1) + beta);
                ArrayList<Integer> rows0=c.getRows(false);
                weight-=logRatio(cla.count(true,rows0)+beta,cla.count(false,rows0)+beta);
                //System.out.println(c.name+" "+weight);
                weights.put(c.name, weight);
            }
            return weights;
        }

        private double logRatio(int a,int b){
            return Math.log(((double)a)/((double)b));
        }

    }



    public class DataColumn{
        String name;
        ArrayList<Boolean> values=new ArrayList<Boolean>();

        public DataColumn(String name) {
            this.name = name;
        }

        public int size(){
            return values.size();
        }

        public boolean get(int i){
            return values.get(i);
        }

        public void addValue(String sVal){
            int val=Integer.parseInt(sVal);
            if(val>0) {
                values.add(true);
            }
            else{
                values.add(false);
            }
        }

        public ArrayList<Integer> getRows(boolean type){
            ArrayList<Integer> rows=new ArrayList<Integer>();
            for (int i = 0; i <values.size() ; i++) {
                if(type==values.get(i)){
                    rows.add(i);
                }
            }
            return rows;
        }

        public int count(boolean type){
            ArrayList<Integer> rows=new ArrayList<Integer>();
            for (int i = 0; i <values.size() ; i++) {
                rows.add(i);
            }
            return (count(type, rows));
        }

        public int count(boolean type,ArrayList<Integer> rows){
            int count=0;
            for (int i = 0; i <rows.size() ; i++) {
                if(type==values.get(rows.get(i))){
                    count++;
                }
            }
            return count;
        }

    }


}
