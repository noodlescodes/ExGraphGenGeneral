import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class EdgeCounter {
	private int numberOfEdges;
	private String file;

	public EdgeCounter(String file) {
		this.file = file;
		numberOfEdges = 0;
	}

	public int countEdges() {
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			String line;
			while((line = br.readLine()) != null) {
				if(line.length() > 0) {
					String[] linesplit = line.split(",");
					for(int i = 0; i < linesplit.length; i++) {
						int num = Integer.parseInt(linesplit[i]);
						if(num == 1) {
							numberOfEdges++;
						}
					}
				}
			}
			br.close();
		}
		catch(FileNotFoundException e) {
		}
		catch(IOException e) {
		}
		
		return numberOfEdges / 2;
	}
}
