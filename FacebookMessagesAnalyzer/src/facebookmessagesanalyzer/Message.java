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
public class Message implements Comparable<Message>{
    String message;
    Participant sender;
    GregorianCalendar date;

    @Override
    public int compareTo(Message o) {
        return date.compareTo(o.date);
    }

    @Override
    public String toString() {
        return sender + "\n" + message + "\n" + date.getTime().toString();
    }
    
    
}
