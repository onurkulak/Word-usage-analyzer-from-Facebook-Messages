/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;

import static facebookmessagesanalyzer.Participant.calculateDayStart;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.GregorianCalendar;

/**
 *
 * @author onur
 */
public class Conversation {
    Participant[] participants;
    ArrayList<Message> messages;
    GregorianCalendar conversationStart;
    HashMap<String, ArrayList<FrequencyInstance>> wordFrequencies;
    int dayStart;
    boolean dayStartCalculated;
    
    public void mergeConversation(Conversation c){
        if(c.conversationStart.after(conversationStart)){
            messages.addAll(c.messages);
        }
        else{
            c.messages.addAll(messages);
            messages = c.messages;
            conversationStart = c.conversationStart;
        }
    }

    @Override
    public String toString() {
        String s = "";
        for(Participant p:participants)
            s+= p.toString()+'\n';
        s+=conversationStart.getTime().toString();
        return s;
    }
    
    
    
    public Conversation(Participant[] p){
        participants = p;
        messages = new ArrayList<>();
        wordFrequencies = new HashMap<>();
    }

    int getDayStart() {
        
        if(dayStartCalculated)
            return dayStart;
        dayStartCalculated = true;
        
        dayStart = calculateDayStart(messages);
        dayStartCalculated = true;
        return dayStart;
        
        
    }
    
}
