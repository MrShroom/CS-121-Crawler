package ir.assignments.three;

import ir.assignments.three.Frequency;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Counts the total number of words and their frequencies in a text file.
 */
public final class WordFrequencyCounter {
	
	/**
	 * This class not should be instantiated.
	 */
	private WordFrequencyCounter() {}
	
	
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
	public static void addOrIncrementCounters(List<String> words, Connection dBConnects) 
	{		
			try 
			{				
				for(String word : words)
				{
					String statement = "INSERT INTO WordFreq( Word, Freq) Value (\""+ word + "\",1)"+
							" ON DUPLICATE KEY UPDATE  Freq = Freq + 1;";
					dBConnects.createStatement().executeUpdate(statement);
				}
				dBConnects.close();
			} catch (SQLException e) 
			{
				e.printStackTrace();
			}            
		
	}

	public static List<Frequency> returnSortedCounts(Connection dBConnects) 
	{
		return returnSortedCounts(dBConnects, false);
	
	}
	
	public static List<Frequency> returnSortedCounts(Connection dBConnects, boolean removeNumbers) 
	{
		Statement st = null;
        ResultSet rs = null;
        String statement = " SELECT Word, Freq " +
        					"FROM WordFreq ";
        if(removeNumbers)
        	statement += " Where Word NOT REGEXP '^[0-9]+'";
        	
        statement += "ORDER BY Freq DESC, Word LIMIT 500;";
        
        List<Frequency> output = new ArrayList<Frequency>();
        try {
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			
			while(rs.next())
			{
				output.add(new Frequency(rs.getString("Word"), rs.getInt("Freq")));
			}
			dBConnects.close();
			
		} catch (SQLException e) 
        {
			e.printStackTrace();
		}finally 
        {
            try {
                if (rs != null) 
                {
                    rs.close();
                }
                if (st != null) 
                {
                    st.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(WordFrequencyCounter.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }   
		return output;
	}
}


