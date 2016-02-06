package ir.assignments.three;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class generateCurrentReports {

	public static void main(String[] args) 
	{
		generateNow();		

	}
	
	public static void generateNow()
	{
		try {
			int numOfWords [] = new int[1];
			PrintWriter out;
			out = new PrintWriter("answers.txt");
			out.println("1. It took " + DataBaseCrawlerFunctions.getTotalTime() + " hours:minutes:seconds to crawl the domain." );
			out.println("2. There were " + DataBaseCrawlerFunctions.getTotalCrawled() + " unique url crawled in this domain." );
			out.println("3. There were " + DataBaseCrawlerFunctions.writeSubDomainsToFile() + " unique subdomains crawled(see \"Subdomains.txt\").");
			out.println("4. Longest Page: " + DataBaseCrawlerFunctions.getLongestPage(numOfWords) + " with " + numOfWords[0] + " words");
			out.println("5. Please see file\"CommonWords.txt\" and \"CommonWordsWithoutNumbers.txt\".");
			out.close();
			
			List<Frequency> sortedFreqCount = WordFrequencyCounter.returnSortedCounts(DataBaseCrawlerFunctions.setMySQLDB());
			out = new PrintWriter("CommonWords.txt");
			
			for(int i = 0; i< 500 && i < sortedFreqCount.size(); i++)
				out.println(sortedFreqCount.get(i).toString().replaceAll(",", "\n").replace("[", ""));
			out.close();
			
			sortedFreqCount = WordFrequencyCounter.returnSortedCounts(DataBaseCrawlerFunctions.setMySQLDB(), true);
			out = new PrintWriter("CommonWordsWithoutNumbers.txt");
			
			for(int i = 0; i< 500 && i < sortedFreqCount.size(); i++)
				out.println(sortedFreqCount.get(i).toString().replaceAll(",", "\n").replace("[", ""));
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
