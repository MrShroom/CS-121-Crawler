Some assumptions made:
1.	The time to crawl includes some text processing time, therefore this measurement is not that accurate. 
2.	The Unique URLs crawled do not include any URL that was skipped for any reason (e.g. filtered out by file extension).
3.	The crawled subdomains do not included calendar or duttgroup subdomains. These were filtered out to avoid traps.
4.	Initial Crawl included “.mat” binary file that are indexed into database and into word frequencies. This added large number of random chars and numbers to be included into the counts. Because of the amount of work to either develop a method to remove them or re-crawl the entire domain, these numbers and characters will be left in the word frequencies. This should not affect the data too much if we filter out word that start with numbers.
5.	Two CommonWords.txt file included. One of them filters out the words that start with numbers. 
  
