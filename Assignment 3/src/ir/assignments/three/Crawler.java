package ir.assignments.three;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {
	/**
	 * This method is for testing purposes only. It does not need to be used to
	 * answer any of the questions in the assignment. However, it must function
	 * as specified so that your crawler can be verified programatically.
	 * 
	 * This methods performs a crawl starting at the specified seed URL. Returns
	 * a collection containing all URLs visited during the crawl.
	 */
		
	//taken from https://www.ics.uci.edu/~djp3/classes/2014_01_INF141/Discussion/Discussion_03.pdf
	private final static Pattern FILTERS = Pattern.compile(".*calendar.*"+
											"|(.*(\\.(css|js|gif|jpe?g|png|mp2|mp3|zip|gz|exe|dll"+
											"|bin|tar|pdf|mid|wav|avi|mov|mpeg|ram|m4v|rm|smil"+
											"|wmv|swf|wma|rar|bmp|tiff?|pptx?|docx?|jemdoc|odp|ps|"+
											"uai|thmx|xmlx?|mso|))$)");
	
	private final static String CRAWL_STORAGE_FOLDER = "data";//path to to store data, made final for easy changing
	private final static int NUMBER_OF_CRAWLERS = 1;//only need one crawler because we don't leave the ICS domain
	private final static Pattern replaceRegexPattern = Pattern.compile("[^A-Za-z0-9]+");//Pre-compile Regex for small speed up
	private final static Pattern singleQoute = Pattern.compile("\'|`");//Pre-compile Regex for small speed up
	
	private static Set<String> hiturls; 
	private static Map<String, Integer> subDomains;
	private static WordFrequencyCounter myCounter;
	private static HashSet<String> stopwords = new HashSet<String>();
	private static String mostWordsUrl = "None";
	private static int bestWordCount = 0;
	private static PrintWriter indexToUrl; 

	public static void main(String [] args)
	{
		try 
		{
			//Create set of stop words from file "stopwords"
			Scanner in = new Scanner(new File("stopwords"));
			while(in.hasNext())
			{
				stopwords.add(singleQoute.matcher(in.nextLine().trim().toLowerCase()).replaceAll(""));
			}
			stopwords.add("");
			in.close();
			
			
			long startTime = System.currentTimeMillis();
			myCounter = new WordFrequencyCounter();
			
			PrintWriter out = new PrintWriter("Visited.txt");
			indexToUrl = new PrintWriter("UrlMapIndex.txt");
			for(String s : crawl("http://www.ics.uci.edu"))
			{
				out.println(s);
			}
			out.close();
			indexToUrl.close();
			
			out = new PrintWriter("Subdomains.txt");
			ArrayList <String> keys = new ArrayList<String>(subDomains.keySet());
			Collections.sort(keys);
			for(String key : keys)
			{
				out.println(key + ", " + subDomains.get(key));
			}
			out.close();
			
			
			out = new PrintWriter("answers.txt");
			out.println("1. It took " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds to crawl the domain." );
			out.println("2. There are " + hiturls.size() + " unique url crawled in this domain." );
			out.println("3. There are " + subDomains.size() + " unique subdomains crawled(see \"Subdomains.txt\").");
			out.println("4. Longest Page: " + mostWordsUrl + " with " + bestWordCount + " words");
			out.println("5. Please see file\"CommonWords.txt\" ");
			out.close();
			
			List<Frequency> sortedFreqCount = myCounter.returnSortedCounts();
			out = new PrintWriter("CommonWords.txt");
			
			for(int i = 0; i< 500 && i < sortedFreqCount.size(); i++)
				out.println(sortedFreqCount.get(i).toString().replaceAll(",", "\n").replace("[", "").replace("]", ""));
			out.close();
		
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	//majority of code taken from https://www.ics.uci.edu/~djp3/classes/2014_01_INF141/Discussion/Discussion_03.pdf
	public static Collection<String> crawl(String seedURL) throws Exception 
	{
		hiturls = new HashSet<String>();
		subDomains = new HashMap<String, Integer>();

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(CRAWL_STORAGE_FOLDER);
        
        //Specific Settings for Project
        config.setPolitenessDelay(600);
        config.setUserAgentString("UCI Inf141-CS121 crawler 82425468 24073320 13828643");
        config.setMaxPagesToFetch(-1);
        config.setMaxDepthOfCrawling(32767);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed(seedURL);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(Crawler.class, NUMBER_OF_CRAWLERS);
		
		return hiturls;
	}


	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		
		/** TODO DELETE DEBUG */ 
		System.out.println("<!--- SITE TO VISIT " + href + " --->" + "AND BOOLEAN TOVISIT= " 
		+ (!FILTERS.matcher(href).matches() && href.contains("ics.uci.edu") && href.contains("http://") 
		&& !hiturls.contains(href)));
		
		return !FILTERS.matcher(href).matches()//skip file that match preset filters 
				&& href.contains("ics.uci.edu")//stay in ics.uci.edu
				&& href.contains("http://")//only follow http://(i.e.  avoid https, ftp, file,...)
				&& !hiturls.contains(href);//skip seen sites. 
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	
	

	
	//taken from https://www.ics.uci.edu/~djp3/classes/2014_01_INF141/Discussion/Discussion_03.pdf
	@Override
	public void visit(Page page) {

		WebURL currentUrl = page.getWebURL();
		System.out.println("############## Current URL!:: "+ currentUrl.getURL());
		hiturls.add(currentUrl.getURL());
		String subDomain = currentUrl.getSubDomain();
		
		if(!subDomains.containsKey(subDomain))
			subDomains.put(subDomain, 1);
		else
		{
//			System.out.println(subDomain + " Hit! count :" +  (subDomains.get(subDomain)+1));
			subDomains.put(subDomain, subDomains.get(subDomain)+1) ; 
		}	

		if (page.getParseData() instanceof HtmlParseData) 
		{
			try {
				PrintWriter out = new PrintWriter("data/pageText/"+hiturls.size() + ".txt");
				indexToUrl.println(hiturls.size() + "\t," + currentUrl.getURL());
				indexToUrl.flush();
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				String text = htmlParseData.getText();
				out.append(text);
				int wordCount = tokenizeText(text);
				if(wordCount > bestWordCount){
					mostWordsUrl = currentUrl.getURL();
					bestWordCount = wordCount;
				}
				out.close();
			} catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
	
	}
	
	public static int tokenizeText(String input) {
		input = singleQoute.matcher(input).replaceAll("").trim();
		input = replaceRegexPattern.matcher(input.toLowerCase()).replaceAll(" ").trim();//Change case to lower and remove all non word charters
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(input.split(" ")));//Create new Array list to hold tokens
		int ctr = tokens.size();
		tokens.removeAll(stopwords);
//		for(String s : input.split(" ")){
//			if(s.length()<=0)
//				continue;
//			++ctr;
//			if(!stopwords.contains(s))
//				tokens.add(s);
//		}
		myCounter.addOrIncrementCounters(tokens);
		return ctr;
	}
}
