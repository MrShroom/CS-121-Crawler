/** 
* Shaun McThomas 13828643
 * Sean Letzer 24073320
 * Sean King 82425468 
 */
package ir.assignments.three;

/**
 * Based on Basic class for pairing a word/2-gram/palindrome with its frequency
 * from assignment 1
 *
 */
public final class Frequency {
	private final String word;
	private int frequency;
	
	public Frequency(String word) {
		this.word = word;
		this.frequency = 0;
	}
	
	public Frequency(String word, int frequency) {
		this.word = word;
		this.frequency = frequency;
	}
	
	public String getText() {
		return word;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public void incrementFrequency() {
		frequency++;
	}
	
	@Override
	public String toString() {
		return word + "\t\t\t:" + frequency;
	}
}
