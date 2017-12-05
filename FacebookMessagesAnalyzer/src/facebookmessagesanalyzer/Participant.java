/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;

import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author onur
 */
public class Participant {
    String name, id;
    ArrayList<Message> messages;
    HashMap<String, ArrayList<FrequencyInstance>> wordFrequencies;
    HashMap<String, Integer> totalWords;
    GregorianCalendar conversationStart;
    ArrayList<Integer>[] messageCountsOverTime;
    ArrayList<Conversation> conversations;
    ArrayList<ChitChat> chitChats;
    int totalWordCount;
    int dayStart;
    boolean dayStartCalculated;
    public Participant(String nameOrID){
        setNameOrID(nameOrID);
        messages = new ArrayList<>();
        wordFrequencies = new HashMap<>();
        totalWords = new HashMap<>();
        conversations = new ArrayList<>();
        totalWordCount = 0;
        chitChats = new ArrayList<>();
        messageCountsOverTime = new ArrayList[2];
        messageCountsOverTime[0] = new ArrayList(); //word count in a day
        messageCountsOverTime[1] = new ArrayList(); //day difference between conversations
        dayStartCalculated = false;
    }
    
    public static boolean isID(String test){
        return test.endsWith("@facebook.com");
    }
    
    public void setNameOrID(String nameOrID){
        if(nameOrID!=null)
            if(isID(nameOrID))
                id = nameOrID;
            else name = nameOrID;
    }
    
    @Override
    public String toString(){
        if(name==null)
            return id;
        else return name;
    }

    int getDayStart() {
        
        if(dayStartCalculated)
            return dayStart;
        dayStartCalculated = true;
        
        dayStart = calculateDayStart(messages);
        dayStartCalculated = true;
        return dayStart;
        
        
    }

    public static int calculateDayStart(ArrayList<Message> messagesAr) {
        int[] messageTimes = new int[24*60];
        for(Message m: messagesAr)
            messageTimes[m.date.get(GregorianCalendar.HOUR_OF_DAY)*60+m.date.get(GregorianCalendar.MINUTE)]++;
        int minVal = 0;
        int minTime = 0;
        for(int i = -30; i<31; i++)
        {
            int index = i%messageTimes.length;
            if(index<0)
                index += messageTimes.length;
            minVal += messageTimes[index];
        }
            
        int previousVal = minVal;
        for(int i = 1; i<messageTimes.length; i++)
        {
            int index = (i-31)%messageTimes.length;
            if(index<0)
                index += messageTimes.length;
            
            int thisVal = previousVal + messageTimes[(i+30)%messageTimes.length] - messageTimes[index];
            if(thisVal<minVal){
                minTime = i;
                minVal = thisVal;
            }
            previousVal = thisVal;
        }
        return minTime;
    }

}
