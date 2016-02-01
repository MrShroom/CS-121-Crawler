package ir.assignments.three;

import ir.assignments.three.Frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Counts the total number of words and their frequencies in a text file.
 */
public final class WordFrequencyCounter {
	
	private	Map<String, Frequency> holder = new HashMap<String, Frequency>();//a Map to hold pairing of word to Frequency
	
	/**
	 * This class should be instantiated.
	 */
	public WordFrequencyCounter() {}
	
	
	/**
	 * Takes the input list of words and processes it, returning a list
	 * of {@link Frequency}s.
	 * 
	 * This method expects a list of lowercase alphanumeric strings.
	 * If the input list is null, an empty list is returned.
	 * 
	 * There is one frequency in the output list for every 
	 * unique word in the original list. The frequency of each word
	 * is equal to the number of times that word occurs in the original list. 
	 * 
	 * The returned list is ordered by decreasing frequency, with tied words sorted
	 * alphabetically.
	 * 
	 * The original list is not modified.
	 * 
	 * Example:
	 * 
	 * Given the input list of strings 
	 * ["this", "sentence", "repeats", "the", "word", "sentence"]
	 * 
	 * The output list of frequencies should be 
	 * ["sentence:2", "the:1", "this:1", "repeats:1",  "word:1"]
	 *  
	 * @param words A list of words.
	 * @return A list of word frequencies, ordered by decreasing frequency.
	 */
	public void addOrIncrementCounters(List<String> words) 
	{
		
		for(String word : words)
		{
			if(holder.containsKey(word))
			{
				holder.get(word).incrementFrequency();
			}
			else
			{
				holder.put(word,  new Frequency(word,1));
			}
		}
	}


	public List<Frequency> returnSortedCounts() {
		
		if(holder == null || holder.isEmpty())//handle empty lists
			return new ArrayList<Frequency>();
		
		List<Frequency> output = new ArrayList<Frequency>( holder.values());
		class FrequencyComparer implements Comparator<Frequency>
		{
			@Override
			public int compare(Frequency o1, Frequency o2) 
			{
				if(o2.getFrequency() != o1.getFrequency())
					return o2.getFrequency()-o1.getFrequency();
				return o1.getText().compareTo(o2.getText());
			}
		
		}
		Collections.sort(output,new FrequencyComparer ());//sort output 
		
		return output;
	}

}


