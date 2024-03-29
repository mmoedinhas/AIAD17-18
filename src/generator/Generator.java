package generator;
import java.util.Random;

public class Generator {
	
	public int generate(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
	
	public double generate(double min, double max) {
		Random rand = new Random();
		return rand.nextDouble()*(max - min) + min;
	}
	
	public long generate(long min, long max) {
		Random rand = new Random();
		return rand.nextLong()*(max - min) + min;
	}

}
