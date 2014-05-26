import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CreateSquareConstraints {
	String readFile;
	String writeFile;
	int nVertices;

	int[][] sol;
	int[][] squaresInGraph;
	int squares = 0;

	public CreateSquareConstraints(String readFile, String writeFile, int nVertices) {
		this.readFile = readFile;
		this.writeFile = writeFile;
		this.nVertices = nVertices;
		sol = new int[nVertices][nVertices];
	}

	public int getNumberOfSquares() {
		return squares / 8;
	}

	public void createConstraints() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(readFile));

			String line;
			int count = 0;
			while((line = br.readLine()) != null) {
				if(line.length() > 0) {
					String[] linesplit = line.split(",");
					for(int i = 0; i < linesplit.length; i++) {
						int num = Integer.parseInt(linesplit[i]);
						sol[count][i] = num;
					}
					count++;
				}
			}
			br.close();
		}
		catch(FileNotFoundException e) {
		}
		catch(IOException e) {
		}

		String cons = "";
		for(int i = 0; i < nVertices; i++) {
			for(int j = 0; j < nVertices; j++) {
				for(int k = 0; k < nVertices; k++) {
					for(int l = 0; l < nVertices; l++) {
						if(i != j && i != k && i != l && j != k && j != l && k != l) {
							int sum = sol[i][j] + sol[j][k] + sol[k][l] + sol[l][i];
							if(sum > 3) {
								squares++;
								cons += i + ";" + j + ":" + j + ";" + k + ":" + k + ";" + l + ":" + l + ";" + i + "\n";
							}
						}
					}
				}
			}
		}
		int num = 0;
		String oldConstraints = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(writeFile));
			num = Integer.parseInt(br.readLine());
			String line;
			while((line = br.readLine()) != null) {
				if(line.length() > 0) {
					oldConstraints += line + "\n";
				}
			}
			br.close();
		}
		catch(FileNotFoundException e) {
		}
		catch(IOException e) {
		}

		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			w.println(squares + num);
			if(oldConstraints.length() > 0) {
				w.print(oldConstraints);
			}
			if(cons.length() > 0) {
				w.print(cons);
			}
			w.close();
		}
		catch(IOException e) {
		}
	}
}
