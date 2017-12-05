/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.File;
import java.io.IOException;
import org.jsoup.select.Elements;
import java.util.GregorianCalendar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author onur
 */
public class Analyzer {

    Document messagesHtml;
    HashMap<String, Participant> usersByID;
    HashMap<String, Participant> usersByName;
    ArrayList<String> months;
    HashMap<String, Conversation> allConversations;
    Participant owner;
    Elements conversations;
    ArrayList<ChitChat> chitChats;

    public Analyzer(File htmlFile, MainFrame callerFrame) throws IOException {

        messagesHtml = Jsoup.parse(htmlFile, "UTF-8", "");
        conversations = messagesHtml.body().getElementsByClass("thread");
        if (!setMonths()) {
            System.out.println("You don't have many friends, do you?");
        }
        Collections.reverse(months);
        usersByID = new HashMap<>();
        usersByName = new HashMap<>();
        createOwner();
        allConversations = new HashMap();
        chitChats = new ArrayList<>();
        findAndPutNames();
        callerFrame.initNameTree(usersByID);
        createBaseObjects();
        calculateFrequencies();
    }

    private boolean setMonths() {
        months = new ArrayList<>();
        for (Element conversation : conversations) {
            ArrayList<String> encounteredMonths = new ArrayList<>();
            Elements metas = conversation.getElementsByClass("meta");
            int speechYear = 0;
            if (!metas.isEmpty()) {
                speechYear = Integer.parseInt(metas.get(0).text().split(" ")[2]);
            }

            for (Element meta : metas) {
                String month = meta.text().split(" ")[1];
                int year = Integer.parseInt(meta.text().split(" ")[2]);
                if (year != speechYear) {
                    if (mergeWithOrder(months, encounteredMonths)) {
                        break;
                    }
                    speechYear = year;
                    encounteredMonths = new ArrayList<>();
                    encounteredMonths.add(month);
                } else if (encounteredMonths.isEmpty() || !month.equals(encounteredMonths.get(encounteredMonths.size() - 1))) {
                    encounteredMonths.add(month);
                }
            }
            if (mergeWithOrder(months, encounteredMonths)) {
                return true;
            }
        }
        return false;
    }

    private void createBaseObjects() {
        
        scanConversations();
        fixThings();
        createChitChats();
    }

    private void scanConversations() {
        conversations.stream().forEach((conversation) -> {
            String header = conversation.ownText();
            String[] nameOrIDS = conversation.ownText().split(", ");
            int pCount = 0;
            for (String nameOrIDS1 : nameOrIDS) {
                if (null != getElementWithIDOrName(nameOrIDS1)) {
                    pCount++;
                }
            }
            Participant[] participants = new Participant[pCount];
            for (int i = 0, j = 0; i < nameOrIDS.length; i++) {
                if(null!=getElementWithIDOrName(nameOrIDS[i]))
                    participants[j++] = getElementWithIDOrName(nameOrIDS[i]);
            }
            Conversation c = new Conversation(participants);
            Elements messages = conversation.children();
            for (int i = messages.size() - 1; i >= 0; i -= 2) {
                Message m = new Message();
                m.message = messages.get(i).text().toLowerCase();
                m.date = parseCalendar(messages.get(i - 1).getElementsByClass("meta").get(0).ownText());
                m.sender = getElementWithIDOrName(messages.get(i - 1).getElementsByClass("user").get(0).ownText());
                
                if (m.sender != null) {
                    m.sender.messages.add(m);
                    c.messages.add(m);
                }
            }
            c.conversationStart = c.messages.get(0).date;
            if (allConversations.containsKey(header)) {
                allConversations.get(header).mergeConversation(c);
            } else {
                allConversations.put(header, c);
                for (Participant p : participants) {
                    p.conversations.add(c);
                    
                }
            }
        });
    }

    private void calculateFrequencies() {
        calculateChitchats();
        calculateParticipants();
        calculateConversations();
    }

    private boolean mergeWithOrder(ArrayList<String> months, ArrayList<String> encounteredMonths) {
        for (int i = 0; i < encounteredMonths.size(); i++) {
            if (months.isEmpty()) {
                months.add(encounteredMonths.get(i));
            } else if (!months.contains(encounteredMonths.get(i))) {
                int index = encounteredMonths.indexOf(months.get(0));
                if (index != -1 && index > i) {
                    months.add(0, encounteredMonths.get(i));
                } else {
                    index = encounteredMonths.indexOf(months.get(months.size() - 1));
                    if (index != -1 && index < i) {
                        months.add(months.size(), encounteredMonths.get(i));
                    } else {
                        int endIndex = -1;
                        index = -1;

                        for (int j = i - 1; j >= 0 && index == -1; j--) {
                            index = months.indexOf(encounteredMonths.get(j));
                        }
                        for (int j = i + 1; j < encounteredMonths.size() && endIndex == -1; j++) {
                            endIndex = months.indexOf(encounteredMonths.get(j));
                        }
                        if (endIndex - index == 1) {
                            months.add(endIndex, encounteredMonths.get(i));
                        }
                    }
                }
            }
        }
        return months.size() == 12;
    }

    private void createOwner() {
        String title = messagesHtml.title();
        int t = title.indexOf('-');
        owner = new Participant(title.substring(0, t - 1));
        for (Element conversation : conversations) {
            if (Participant.isID(conversation.ownText())) {
                owner.setNameOrID(conversation.ownText().substring(conversation.ownText().lastIndexOf(' ') + 1));
                break;
            }
        }
        usersByID.put(owner.id, owner);
        usersByName.put(owner.name, owner);
    }

    private void findAndPutNames() {
        for (Element conversation : conversations) {
            String[] nameOrIDS = conversation.ownText().split(", ");
            String s = nameOrIDS[0];
            if (nameOrIDS.length == 2) {
                String s2 = null;
                Elements users = conversation.getElementsByClass("user");
                for (Element user : users) {
                    String t = user.ownText();
                    if (!t.equals(s) && !t.equals(owner.id) && !t.equals(owner.name)) {
                        s2 = t;
                        break;
                    }
                }
                handleMaps(s, s2);
            }
        }
    }

    private void handleMaps(String s, String s2) {
        Participant t;
        if (null == (t = getElementWithIDOrName(s2))) {
            if (null == (t = getElementWithIDOrName(s))) {
                t = new Participant(s);
            }
            t.setNameOrID(s2);
            putParticipantToMaps(t);
        } else {
            t.setNameOrID(s);
            putParticipantToMaps(t);
        }

    }

    private Participant getElementWithIDOrName(String s) {
        if (s == null) {
            return null;
        }
        Participant p = usersByID.get(s);
        if (p == null) {
            return usersByName.get(s);
        } else {
            return p;
        }
    }

    private void putParticipantToMaps(Participant p) {
        if (p != null) {
            if (p.name != null) {
                usersByName.put(p.name, p);
            }
            if (p.id != null) {
                usersByID.put(p.id, p);
            }
        }
    }

    private GregorianCalendar parseCalendar(String ownText) {
        String[] s = ownText.split(" ");
        int day = Integer.parseInt(s[0]);
        int year = Integer.parseInt(s[2]);
        int month = months.indexOf(s[1]);
        int minute = Integer.parseInt(s[4].split(":")[1]);
        int hour = Integer.parseInt(s[4].split(":")[0]);
        return new GregorianCalendar(year, month, day, hour, minute);
    }

    private void fixThings() {
        usersByID.values().stream().map((p) -> {
            p.messages.sort((Message o1, Message o2) -> o1.compareTo(o2));
            return p;
        }).filter((p) -> (!p.messages.isEmpty())).forEach((p) -> {
            p.conversationStart = p.messages.get(0).date;
        });
    }

    private void createChitChats() {
        Conversation[] cArr = {};
        cArr = allConversations.values().toArray(cArr);
        for (Conversation cArr1 : cArr) {
            ArrayList<Message> tempMessages = new ArrayList<>();
            for (Message message : cArr1.messages) {
                if (tempMessages.isEmpty() || message.date.getTimeInMillis() - tempMessages.get(tempMessages.size() - 1).date.getTimeInMillis() < 300000) {
                    tempMessages.add(message);
                } else if (tempMessages.size() > 10) {
                    ChitChat chit = new ChitChat(tempMessages);
                    chit.endDate = tempMessages.get(tempMessages.size() - 1).date;
                    chit.startDate = tempMessages.get(0).date;
                    chit.parentConservation = cArr1;
                    for (Participant participant : cArr1.participants) {
                        participant.chitChats.add(chit);
                    }
                    chitChats.add(chit);
                    //this might be changed idk?
                    tempMessages = new ArrayList<>();
                } else {
                    tempMessages = new ArrayList<>();
                }
            }
            //code duplication, works but ugly
            if (tempMessages.size() > 10) {
                ChitChat chit = new ChitChat(tempMessages);
                chit.endDate = tempMessages.get(tempMessages.size() - 1).date;
                chit.startDate = tempMessages.get(0).date;
                chit.parentConservation = cArr1;
                for (Participant participant : cArr1.participants) {
                    participant.chitChats.add(chit);
                }
                chitChats.add(chit);
            }
        }
    }

    private void calculateParticipants() {
        usersByID.values().stream().filter((p) -> !(p.conversationStart==null)).forEach((p) -> {
            int hour = p.getDayStart() / 60;
            int minute = p.getDayStart() % 60;
            HashMap<String, Integer> tempWordFrequencies = new HashMap<>();

            GregorianCalendar day = new GregorianCalendar(p.conversationStart.get(GregorianCalendar.YEAR),
                    p.conversationStart.get(GregorianCalendar.MONTH),
                    p.conversationStart.get(GregorianCalendar.DAY_OF_MONTH));
            if (!(p.conversationStart.get(GregorianCalendar.HOUR_OF_DAY) > hour
                    || (p.conversationStart.get(GregorianCalendar.HOUR_OF_DAY) == hour
                    && p.conversationStart.get(GregorianCalendar.MINUTE) > minute))) {
                day.add(GregorianCalendar.DAY_OF_MONTH, -1);
            }
            int totalWords = 0;

            for (Message mes : p.messages) {
                GregorianCalendar curDay = new GregorianCalendar(mes.date.get(GregorianCalendar.YEAR),
                        mes.date.get(GregorianCalendar.MONTH),
                        mes.date.get(GregorianCalendar.DAY_OF_MONTH));
                if (!(mes.date.get(GregorianCalendar.HOUR_OF_DAY) > hour
                        || (mes.date.get(GregorianCalendar.HOUR_OF_DAY) == hour
                        && mes.date.get(GregorianCalendar.MINUTE) > minute))) {
                    curDay.add(GregorianCalendar.DAY_OF_MONTH, -1);
                }
                if (curDay.compareTo(day) == 0) {
                    totalWords += messageScanner(tempWordFrequencies, mes);
                } else {
                    int dayDiff = (int) (TimeUnit.DAYS.convert(curDay.getTimeInMillis() - day.getTimeInMillis(), TimeUnit.MILLISECONDS));
                    p.messageCountsOverTime[0].add(totalWords);
                    p.messageCountsOverTime[1].add(dayDiff);
                    String[] s = {};
                    s = tempWordFrequencies.keySet().toArray(s);
                    Integer[] inte = {};
                    inte = tempWordFrequencies.values().toArray(inte);
                    for (int i = 0; i < s.length; i++) {
                        Integer j = null;
                        if (null == (j = p.totalWords.get(s[i]))) {
                            p.totalWords.put(s[i], inte[i]);
                        } else {
                            p.totalWords.put(s[i], j + inte[i]);
                        }
                        ArrayList<FrequencyInstance> freyja = null;
                        if (null == (freyja = p.wordFrequencies.get(s[i]))) {
                            freyja = new ArrayList<>();
                            p.wordFrequencies.put(s[i], freyja);
                        }
                        FrequencyInstance fr = new FrequencyInstance(s[i], day, inte[i], totalWords);
                        freyja.add(fr);
                    }
                    day = curDay;
                    tempWordFrequencies = new HashMap<>();
                    p.totalWordCount += totalWords;
                    totalWords = messageScanner(tempWordFrequencies, mes);
                }
            }
            p.messageCountsOverTime[0].add(totalWords);
            String[] s = {};
            s = tempWordFrequencies.keySet().toArray(s);
            Integer[] inte = {};
            inte = tempWordFrequencies.values().toArray(inte);
            for (int i = 0; i < s.length; i++) {
                Integer j = null;
                if (null == (j = p.totalWords.get(s[i]))) {
                    p.totalWords.put(s[i], inte[i]);
                } else {
                    p.totalWords.put(s[i], j + inte[i]);
                }
                ArrayList<FrequencyInstance> freyja = null;
                if (null == (freyja = p.wordFrequencies.get(s[i]))) {
                    freyja = new ArrayList<>();
                    p.wordFrequencies.put(s[i], freyja);
                }
                FrequencyInstance fr = new FrequencyInstance(s[i], day, inte[i], totalWords);
                freyja.add(fr);
            }
            p.totalWordCount += totalWords;
        });
    }

    private void calculateChitchats() {
        chitChats.stream().map((chit) -> {
            chit.wordFrequencies = new HashMap<>();
            return chit;
        }).map((chit) -> {
            for (Participant p : chit.parentConservation.participants) {
                chit.wordFrequencies.put(p, new HashMap<>());
            }
            return chit;
        }).forEach((chit) -> {
            int[] wordCountsPerP = chit.wordsByPerParticipant();
            chit.messages.stream().forEach((m) -> {
                HashMap<String, Integer> temp = new HashMap<>();
                messageScanner(temp, m);
                String[] s = {};
                s = temp.keySet().toArray(s);
                Integer[] inte = {};
                inte = temp.values().toArray(inte);
                for (int i = 0; i < s.length; i++) {
                    if (null == chit.wordFrequencies.get(m.sender).get(s[i])) {
                        FrequencyInstance f = new FrequencyInstance();
                        f.date = m.date;
                        f.instance = inte[i];
                        f.word = s[i];
                        chit.wordFrequencies.get(m.sender).put(s[i], f);
                    } else {
                        chit.wordFrequencies.get(m.sender).get(s[i]).instance += inte[i];
                    }
                }
            });

            for (int i = 0; i < wordCountsPerP.length; i++) {
                FrequencyInstance[] f = {};
                f = chit.wordFrequencies.get(chit.parentConservation.participants[i]).values().toArray(f);
                for (FrequencyInstance f1 : f) {
                    f1.addFreq(0, wordCountsPerP[i]);
                }
            }
        });
    }

    private static int messageScanner(HashMap<String, Integer> temp, Message m) {
        String[] words = m.message.split("(\t|\n| )+");
        int totalWords = 0;
        for (String word : words) {
            if (word.length() > 1) {
                totalWords++;
                Integer t;
                if (null == (t = temp.get(word))) {
                    temp.put(word, 1);
                } else {
                    temp.put(word, t + 1);
                }
            }
        }
        return totalWords;
    }

    private void calculateConversations() {
        allConversations.values().stream().forEach((c) -> {
            int hour = c.getDayStart() / 60;
            int minute = c.getDayStart() % 60;
            HashMap<String, Integer> tempWordFrequencies = new HashMap<>();
            GregorianCalendar day = new GregorianCalendar(c.conversationStart.get(GregorianCalendar.YEAR),
                    c.conversationStart.get(GregorianCalendar.MONTH),
                    c.conversationStart.get(GregorianCalendar.DAY_OF_MONTH));
            if (!(c.conversationStart.get(GregorianCalendar.HOUR_OF_DAY) > hour
                    || (c.conversationStart.get(GregorianCalendar.HOUR_OF_DAY) == hour
                    && c.conversationStart.get(GregorianCalendar.MINUTE) > minute))) {
                day.add(GregorianCalendar.DAY_OF_MONTH, -1);
            }
            int totalWords = 0;

            for (Message mes : c.messages) {
                GregorianCalendar curDay = new GregorianCalendar(mes.date.get(GregorianCalendar.YEAR),
                        mes.date.get(GregorianCalendar.MONTH),
                        mes.date.get(GregorianCalendar.DAY_OF_MONTH));
                if (!(mes.date.get(GregorianCalendar.HOUR_OF_DAY) > hour
                        || (mes.date.get(GregorianCalendar.HOUR_OF_DAY) == hour
                        && mes.date.get(GregorianCalendar.MINUTE) > minute))) {
                    curDay.add(GregorianCalendar.DAY_OF_MONTH, -1);
                }
                if (curDay.compareTo(day) == 0) {
                    totalWords += messageScanner(tempWordFrequencies, mes);
                } else {
                    String[] s = {};
                    s = tempWordFrequencies.keySet().toArray(s);
                    Integer[] inte = {};
                    inte = tempWordFrequencies.values().toArray(inte);
                    for (int i = 0; i < s.length; i++) {
                        ArrayList<FrequencyInstance> freyja = null;
                        if (null == (freyja = c.wordFrequencies.get(s[i]))) {
                            freyja = new ArrayList<>();
                            c.wordFrequencies.put(s[i], freyja);
                        }
                        FrequencyInstance fr = new FrequencyInstance(s[i], day, inte[i], totalWords);
                        freyja.add(fr);
                    }
                    day = curDay;
                    tempWordFrequencies = new HashMap<>();
                    totalWords = messageScanner(tempWordFrequencies, mes);
                }
            }
            String[] s = {};
            s = tempWordFrequencies.keySet().toArray(s);
            Integer[] inte = {};
            inte = tempWordFrequencies.values().toArray(inte);
            for (int i = 0; i < s.length; i++) {
                ArrayList<FrequencyInstance> freyja = null;
                if (null == (freyja = c.wordFrequencies.get(s[i]))) {
                    freyja = new ArrayList<>();
                    c.wordFrequencies.put(s[i], freyja);
                }
                FrequencyInstance fr = new FrequencyInstance(s[i], day, inte[i], totalWords);
                freyja.add(fr);
            }
        });
    }

    ArrayList<StringIntPair> getWordCounts(String userNameOrID) {
        Participant p = getElementWithIDOrName(userNameOrID);
        ArrayList<StringIntPair> ord = new ArrayList<>();
        for(String s: p.totalWords.keySet())
            ord.add(new StringIntPair(s));
        int i=0;
        for(Integer inter:p.totalWords.values()){
            ord.get(i).i=inter;
            i++;
        }
        ord.sort((StringIntPair o1, StringIntPair o2)->o2.i-o1.i);
        return ord;
    }

    int getTotalWordsOf(String userNameOrID) {
        return getElementWithIDOrName(userNameOrID).totalWordCount;
    }
}


