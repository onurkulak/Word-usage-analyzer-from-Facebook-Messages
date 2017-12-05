/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;

import java.util.GregorianCalendar;
/**
 *
 * @author onur
 */
public class FrequencyInstance {
    String word;
    GregorianCalendar date;
    int instance;
    int totalWords;
    double frequency;

    public FrequencyInstance(String w, GregorianCalendar d, int ins, int tot) {
        word = w;
        date = d;
        instance = ins;
        totalWords = tot;
        frequency = instance*1.0/totalWords;
    }

    public FrequencyInstance() {
        
    }
    
    
    public void addFreq(int occurrence, int count){
        instance += occurrence;
        totalWords += count;
        frequency = instance*1.0/totalWords;
    }

    @Override
    public String toString() {
        return word + '\n' + date.getTime() + '\n' + frequency + '\t' + instance + '\n';
    }
    
    
    
}
