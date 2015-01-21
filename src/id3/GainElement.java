package id3;

/**
 * Created by Nisansa on 15/01/19.
 */
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
        return 0;
    }
}
