package id3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Nisansa on 15/01/19.
 */
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
            entries.get(i).remove(attr);      //See if this works as a reference!
        }
    }

    public Iterator<String> getKeyItr(){
        return entries.get(0).keySet().iterator();
    }

    public double calculateEntropy(){
        int[] pn=getPNcount();
        //System.out.println(pn[0]);
        //System.out.println(pn[1]);
        return(calculateEntropy(pn[0], pn[1]));
    }

    public int[] getPNcount(){
        int[] counts=new int[2];
       // System.out.println("L"+entries.get(0).keySet().contains("Class"));
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
