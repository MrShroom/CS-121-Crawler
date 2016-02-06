package ir.assignments.three;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class DataBaseCrawlerFunctions 
{
	
	private static int currentRun;
	
	public static Connection setMySQLDB()
	{
		String url = "jdbc:mysql://shaunmcthomas.me:3306/cs121DB";
		String user = "dbuser";
        String password = "password";
        Connection dBConnects = null;
        try {
        	dBConnects = DriverManager.getConnection(url, user, password);
        	
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Crawler.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        }
        return dBConnects;
	}
	
	public static void recover(boolean resumable)
	{
		Connection dBConnects = setMySQLDB();
		Statement st = null;
        ResultSet rs = null;
        
		String statement = "INSERT INTO Timing (RunTime) VALUES ( 0 );";
		try 
		{
			if (!resumable)
			{
				dBConnects.createStatement().executeUpdate("TRUNCATE TABLE Visited_URL;");
				dBConnects.createStatement().executeUpdate("TRUNCATE TABLE WordFreq;");
				dBConnects.createStatement().executeUpdate("TRUNCATE TABLE Timing;");
			}
			dBConnects.createStatement().executeUpdate(statement);
			statement = "SELECT max(RunNumber) FROM Timing;";
			
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			if (rs.next()) 
			{
				currentRun = rs.getInt(1);				
			}
			dBConnects.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
	}

	public static void writePageDataToDB(String url, String subDomain, int numberOfWords, HtmlParseData htmlParseData, long startTime )
	{
		Connection dBConnects = setMySQLDB();
		
		String statement = "INSERT INTO Visited_URL (Url, SubDomain, NumberOfWords, WebText, Html )" +
							" VALUES ( ?, ?, " + numberOfWords + ", ?, ? );";
		try
		{
			java.sql.PreparedStatement preparedStatement = dBConnects.prepareStatement(statement);
			preparedStatement.setString(1, url);
			preparedStatement.setString(2, subDomain);
			preparedStatement.setString(3, htmlParseData.getText());
			preparedStatement.setString(4, htmlParseData.getHtml());
			preparedStatement.executeUpdate();

			dBConnects.createStatement().executeUpdate("UPDATE Timing SET RunTime = " + (System.currentTimeMillis()-startTime)
														+ " WHERE RunNumber=" + currentRun +";" );
			dBConnects.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public static int writeSubDomainsToFile()
	{
		Connection dBConnects = setMySQLDB();
		Statement st = null;
        ResultSet rs = null;
        int subDomains = 0;
        String statement = " SELECT SubDomain, COUNT(*) " +
        					"FROM Visited_URL "+
        					"GROUP BY SubDomain " + 
        					"ORDER BY SubDomain; ";
        try 
        {
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			PrintWriter out;
			out = new PrintWriter("Subdomains.txt");

			while(rs.next())
			{
				out.println(rs.getString(1) + ", " + rs.getInt(2));
				
				subDomains++;
			}
			out.close();
			dBConnects.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return subDomains;
        
	}
	public static String getTotalTime()
	{
		Connection dBConnects = setMySQLDB();
			Statement st = null;
	        ResultSet rs = null;
		String statement = "SELECT SUM( RunTime )  AS TotalTime FROM Timing";
		int total = 0;
		try
		{			
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			if (rs.next()) {
				total = rs.getInt("TotalTime")/1000;
				dBConnects.close();
				return ( total/60/60 )+ ":" + (total/60)%60 + ":" + (total%60%60);
			}
			
			dBConnects.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
			
		}
		
		return null;		
	}

	public static int getTotalCrawled()
	{
		Connection dBConnects = setMySQLDB();
			Statement st = null;
	        ResultSet rs = null;
		String statement = "SELECT COUNT( * )  AS TotalCrawled FROM Visited_URL";
		
		try
		{			
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			if (rs.next()) 
				return rs.getInt("TotalCrawled");
			dBConnects.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
			
		}
		return 0;		
	}
	
	public static String getLongestPage(int[] numOfWords)
	{
		Connection dBConnects = setMySQLDB();
		Statement st = null;
	    ResultSet rs = null;
		String statement = "SELECT Url, NumberOfWords" +
						   " FROM Visited_URL" +
						   " WHERE NumberOfWords=(SELECT MAX(NumberOfWords) FROM Visited_URL);";
		
		try
		{			
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			if (rs.next()) {
				numOfWords[0] = rs.getInt("NumberOfWords");
				return rs.getString("Url");
			}
			dBConnects.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
			
		}
		return null;		
	}
	
	public static boolean checkifPageWasSeen(String url)
	{
		Connection dBConnects = setMySQLDB();
		Statement st = null;
	    ResultSet rs = null;
		String statement = " SELECT EXISTS(SELECT * FROM Visited_URL WHERE url =\'" + url +"\');";
		
		try
		{			
			st = dBConnects.createStatement();
			rs = st.executeQuery(statement);
			if (rs.next()) {
				return rs.getBoolean(1);
			}
			dBConnects.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
			
		}
		return false;		
	}
	
}