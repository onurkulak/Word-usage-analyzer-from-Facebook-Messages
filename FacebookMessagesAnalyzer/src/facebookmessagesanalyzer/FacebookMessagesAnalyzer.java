/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookmessagesanalyzer;
import org.jfree.chart.util.ExportUtils;
import java.io.IOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.ui.RefineryUtilities;
import java.io.File;
/**
 *
 * @author onur
 */
public class FacebookMessagesAnalyzer {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        /*Analyzer analci = new Analyzer(args[0]);
        DataSetMaker johnyBravo = new DataSetMaker(analci);
        JFreeChart pp = ChartFactory.createPieChart("Selo'nun sık kullandığı kelimeler",
                johnyBravo.getMostUsedWords("Selahattin Can Uğuzeş", 80));
        */
        MainFrame mFrame = new MainFrame();
        mFrame.startProgram();
    }
    
}
