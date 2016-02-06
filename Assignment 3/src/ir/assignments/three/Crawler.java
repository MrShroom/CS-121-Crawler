package ir.assignments.three;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
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
	private final static Pattern FILTERS = Pattern.compile(".*calendar.*|duttgroup.ics.uci.edu"+
											"|(.*(\\.(css|js|gif|jpe?g|png|mp2|mp3|zip|gz|exe|dll"+
											"|bin|tar|pdf|mid|wav|avi|mov|mpeg|ram|m4v|rm|smil"+
											"|wmv|swf|wma|rar|bmp|tiff?|pptx?|docx?|jemdoc|odp|ps|"+
											"uai|thmx|xmlx?|mso|bx|tgz|7z|bzg|mat))$)");
	
	//path to to store data, made final for easy changing
	private final static String CRAWL_STORAGE_FOLDER = "data";
	
	//only need one crawler because we don't leave the ICS domain
	private final static int NUMBER_OF_CRAWLERS =  Runtime.getRuntime().availableProcessors();
	
	//Pre-compile Regex for small speed up
	private final static Pattern replaceRegexPattern = Pattern.compile("[^A-Za-z0-9]+");
	
	//Pre-compile Regex for small speed up
	private final static Pattern singleQoute = Pattern.compile("\'|`");
	
	//set Politeness in constant so that it's easy to change
	private final static int POLITENESS = 1200;
	
	//set Resumable in constant so that it's easy to change
	private final static boolean RESUMABLE = true;//DONT change! It will wipe DATABASE!!!!!(I already did it....)
	
	//Set of words to remove from BOW
	private static HashSet<String> stopwords = new HashSet<String>();
	
	//private static Set<String> hiturls; 
	private static long startTime; 
	
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
			
			//get current start time
			startTime = System.currentTimeMillis();
			
			//move to next index of timing table or delete it. 
			DataBaseCrawlerFunctions.recover(RESUMABLE);
			
			crawl("http://www.ics.uci.edu/");	
			
			generateCurrentReports.generateNow();
		
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}	
	}	
	
	
	//majority of code taken from https://www.ics.uci.edu/~djp3/classes/2014_01_INF141/Discussion/Discussion_03.pdf
	public static void crawl(String seedURL) throws Exception 
	{

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(CRAWL_STORAGE_FOLDER);
        
        //Specific Settings for Project
        config.setPolitenessDelay(POLITENESS);
        config.setUserAgentString("UCI Inf141-CS121 crawler 82425468 24073320 13828643");
        config.setMaxPagesToFetch(-1);
        config.setMaxDepthOfCrawling(32767);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(RESUMABLE);
        config.setSocketTimeout(60000);
        config.setConnectionTimeout(120000);
        
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
		));
		
		return !FILTERS.matcher(href).matches()//skip file that match preset filters 
				&& href.contains("ics.uci.edu")//stay in ics.uci.edu
				&& href.contains("http://")//only follow http://(i.e.  avoid https, ftp, file,...)				 
				&& href.length() < 767 //smaller than max length for mysql key
				&& !DataBaseCrawlerFunctions.checkifPageWasSeen(href);//url not in DB
	}
	
	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 * taken from https://www.ics.uci.edu/~djp3/classes/2014_01_INF141/Discussion/Discussion_03.pdf
	 * then heavily edited
	 */		
	@Override
	public void visit(Page page) 
	{
		WebURL currentUrl = page.getWebURL();
		System.out.println("############## Current URL!:: "+ currentUrl.getURL());
		//hiturls.add(currentUrl.getURL());
		String subDomain = currentUrl.getSubDomain();
		
		if (page.getParseData() instanceof HtmlParseData) 
		{
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			int wordCount = tokenizeText(text);
			DataBaseCrawlerFunctions.writePageDataToDB(currentUrl.getURL(),subDomain, wordCount, htmlParseData ,startTime );			
		}
		else
		{
			DataBaseCrawlerFunctions.writePageDataToDB(currentUrl.getURL(),subDomain, 0, null , startTime );	
		}
	}
	
	public static int tokenizeText(String input) 
	{
		input = singleQoute.matcher(input).replaceAll("").trim();
		input = replaceRegexPattern.matcher(input.toLowerCase()).replaceAll(" ").trim();//Change case to lower and remove all non word charters
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(input.split(" ")));//Create new Array list to hold tokens
		int ctr = tokens.size();
		tokens.removeAll(stopwords);
		WordFrequencyCounter.addOrIncrementCounters(tokens,DataBaseCrawlerFunctions.setMySQLDB());
		return ctr;
	}
}
