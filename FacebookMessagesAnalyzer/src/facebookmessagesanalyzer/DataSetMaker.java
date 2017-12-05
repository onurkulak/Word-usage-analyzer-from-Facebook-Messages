/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;
import java.util.ArrayList;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.DefaultPieDataset;
/**
 *
 * @author onur
 */
class DataSetMaker {

    Analyzer data;
    
    DataSetMaker(Analyzer anl) {
        data = anl;
    }

    PieDataset getMostUsedWords(String userNameOrID, int wordCount) {
        DefaultPieDataset dp = new DefaultPieDataset();
        ArrayList<StringIntPair> orderedWordCounts = data.getWordCounts(userNameOrID);
        int totalWords = data.getTotalWordsOf(userNameOrID);
        int i = 0;
        for(; i<wordCount&&i<orderedWordCounts.size(); i++){
            dp.insertValue(i, '\"' + orderedWordCounts.get(i).s + "\""+ ":\t" + orderedWordCounts.get(i).i, orderedWordCounts.get(i).i);
            totalWords -= orderedWordCounts.get(i).i;
        }
        if(i==0)
            dp.insertValue(i, "You did not speak at all:\t" + totalWords, 1);
        else if(totalWords!=0)
            dp.insertValue(i, "Other:\t" + totalWords, totalWords);
        return dp;
    }
    
}
