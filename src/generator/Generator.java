package generator;
import java.util.Random;

public abstract class Generator {
	
	public int generate(int lowerBound, int upperBound) {
		Random randomNum = new Random();
		return lowerBound + randomNum.nextInt(upperBound);
	}

}
