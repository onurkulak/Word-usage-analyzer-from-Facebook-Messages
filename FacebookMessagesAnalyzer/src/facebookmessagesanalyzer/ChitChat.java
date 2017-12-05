/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;

import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author onur
 */
public class ChitChat {

    ArrayList<Message> messages;
    HashMap<Participant, HashMap<String, FrequencyInstance>> wordFrequencies;
    GregorianCalendar startDate, endDate;
    Conversation parentConservation;

    public ChitChat(ArrayList<Message> m) {
        messages = m;
    }

    @Override
    public String toString() {
        String s = "This conversation is lasted from:\n";
        s += startDate.getTime().toString() + '\n';
        s += endDate.getTime().toString() + '\n';
        s = messages.stream().map((message) -> message.sender.name + ":\n" + message.date.getTime() + '\n' + message.message + '\n').reduce(s, String::concat);
        return s;
    }

    int[] wordsByPerParticipant() {
        int[] t = new int[parentConservation.participants.length];
        messages.stream().forEach((m) -> {
            t[getParticipantIndex(m.sender)] += countWords(m.message, 2);
        });
        return t;
    }

    private int getParticipantIndex(Participant sender) {
        for (int i = 0; i < parentConservation.participants.length; i++) {
            if (sender == parentConservation.participants[i]) {
                return i;
            }
        }
        System.out.println(sender);
        System.out.println(Arrays.toString(parentConservation.participants));
        System.out.println(this);
        return -1;
    }

    private int countWords(String m, int l) {
        String[] words = m.split("(\t|\n| )+");
        int totalWords = 0;
        for (String word : words) {
            if (word.length() > l) {
                totalWords++;
            }
        }
        return totalWords;

    }
}
