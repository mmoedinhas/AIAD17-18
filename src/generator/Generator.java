package generator;
import java.util.Random;

public abstract class Generator {
	
	public int generate(int min, int max) {
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}

}
