import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;

public class CreateSquareConstraints {
	String readFile;
	String writeFile;
	int nVertices;

	int[][] sol;
	int[][][] squaresInGraph;
	int squares = 0;

	static final Comparator<int[][]> CANONICAL_ORDERING = new Comparator<int[][]>() {
		public int compare(int[][] e1, int[][] e2) {
			String s1 = "";
			String s2 = "";
			for (int i = 0; i < e1.length; i++) {
				for (int j = 0; j < e1[i].length; j++) {
					s1 += Integer.toString(e1[i][j]);
					s2 += Integer.toString(e2[i][j]);
				}
			}

			return s1.compareTo(s2);
		}
	};

	static final Comparator<int[]> CANONICAL_CREATE = new Comparator<int[]>() {
		public int compare(int[] e1, int[] e2) {
			String s1 = "";
			String s2 = "";
			for (int i = 0; i < e1.length; i++) {
				s1 += Integer.toString(e1[i]);
				s2 += Integer.toString(e2[i]);
			}

			return s1.compareTo(s2);
		}
	};

	public CreateSquareConstraints(String readFile, String writeFile, int nVertices) {
		this.readFile = readFile;
		this.writeFile = writeFile;
		this.nVertices = nVertices;
		sol = new int[nVertices][nVertices];
	}

	public int getNumberOfSquares() {
		return squares;
	}

	private int countSquares() {
		int count = 0;
		int sum;
		for (int i = 0; i < nVertices; i++) {
			for (int j = 0; j < nVertices; j++) {
				for (int k = 0; k < nVertices; k++) {
					for (int l = 0; l < nVertices; l++) {
						if (i != j && i != k && i != l && j != k && j != l && k != l) {
							sum = sol[i][j] + sol[j][k] + sol[k][l] + sol[l][i];
							if (sum > 3) {
								count++;
							}
						}
					}
				}
			}
		}
		return count;
	}

	private String createString(int[][] e) {
		String str = "";

		for (int i = 0; i < e.length; i++) {
			for (int j = 0; j < e[i].length; j++) {
				str += e[i][j];
			}
		}

		return str;
	}

	private String createOutputString(int[][] e) {
		return "" + e[0][0] + ";" + e[0][1] + ":" + e[1][0] + ";" + e[1][1] + ":" + e[2][0] + ";" + e[2][1] + ":" + e[3][0] + ";" + e[3][1];
	}

	private void setSquares(int numSquares) {
		int count = 0;
		int sum;
		squaresInGraph = new int[numSquares][4][2];

		for (int i = 0; i < nVertices; i++) {
			for (int j = 0; j < nVertices; j++) {
				for (int k = 0; k < nVertices; k++) {
					for (int l = 0; l < nVertices; l++) {
						if (i != j && i != k && i != l && j != k && j != l && k != l) {
							sum = sol[i][j] + sol[j][k] + sol[k][l] + sol[l][i];
							if (sum > 3) {
								squaresInGraph[count][0][0] = i;
								squaresInGraph[count][0][1] = j;
								squaresInGraph[count][1][0] = j;
								squaresInGraph[count][1][1] = k;
								squaresInGraph[count][2][0] = k;
								squaresInGraph[count][2][1] = l;
								squaresInGraph[count][3][0] = l;
								squaresInGraph[count][3][1] = i;
								count++;
							}
						}
					}
				}
			}
		}
	}

	public int[][][] sortIndex(int[][][] cons) {
		int[][][] constraints = cons;

		for (int i = 0; i < constraints.length; i++) {
			for (int j = 0; j < constraints[i].length; j++) {
				if (constraints[i][j][0] > constraints[i][j][1]) {
					int tmp = constraints[i][j][0];
					constraints[i][j][0] = constraints[i][j][1];
					constraints[i][j][1] = tmp;
				}
			}
		}

		return constraints;
	}

	public int[][][] removeDuplicates(int[][][] cons) {
		int dupes = 0;
		for (int i = 0; i < cons.length - 1; i++) {
			String s1 = createString(cons[i]);
			String s2 = createString(cons[i + 1]);
			if (s1.compareTo(s2) == 0) {
				dupes++;
			}
		}

		int[][][] newCons = new int[cons.length - dupes][4][2];
		int count = 0;
		for (int i = 0; i < cons.length; i++) {
			if (i == 0) {
				newCons[count] = cons[i];
				count++;
			} else {
				String s1 = createString(cons[i]);
				String s2 = createString(newCons[count - 1]);
				if (s1.compareTo(s2) != 0) {
					newCons[count] = cons[i];
					count++;
				}
			}
		}

		return newCons;
	}

	public void createConstraints() {
		String file = "models/" + nVertices + "/squares.dat";
		try {
			BufferedReader br = new BufferedReader(new FileReader(readFile));

			String line;
			int count = 0;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					String[] linesplit = line.split(",");
					for (int i = 0; i < linesplit.length; i++) {
						int num = Integer.parseInt(linesplit[i]);
						sol[count][i] = num;
					}
					count++;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		setSquares(countSquares());
		squares = squaresInGraph.length / 8;
		ReadSquareConstraints rsc = new ReadSquareConstraints(file);
		int[][][] oldConstraints = rsc.getConstraints();
		int oldConstraintLength = 0;
		try {
			oldConstraintLength = oldConstraints.length;
		} catch (NullPointerException e) {
		}
		int[][][] newConstraints = new int[oldConstraintLength + squaresInGraph.length][4][2];
		for (int i = 0; i < oldConstraintLength; i++) {
			newConstraints[i] = oldConstraints[i];
		}
		for (int i = oldConstraintLength; i < newConstraints.length; i++) {
			newConstraints[i] = squaresInGraph[i - oldConstraintLength];
		}

		newConstraints = sortIndex(newConstraints);
		for (int i = 0; i < newConstraints.length; i++) {
			Arrays.sort(newConstraints[i], CANONICAL_CREATE);
		}
		Arrays.sort(newConstraints, CANONICAL_ORDERING);
		newConstraints = removeDuplicates(newConstraints);

		String cons = "";
		for (int i = 0; i < newConstraints.length; i++) {
			cons += createOutputString(newConstraints[i]) + "\n";
		}

		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			w.println(newConstraints.length);
			w.print(cons);
			w.close();
		} catch (IOException e) {
		}
	}

	// not used anymore, and can't be guaranteed to be accurate
	public void createConstraints2() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(readFile));

			String line;
			int count = 0;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					String[] linesplit = line.split(",");
					for (int i = 0; i < linesplit.length; i++) {
						int num = Integer.parseInt(linesplit[i]);
						sol[count][i] = num;
					}
					count++;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		String cons = "";
		for (int i = 0; i < nVertices; i++) {
			for (int j = 0; j < nVertices; j++) {
				for (int k = 0; k < nVertices; k++) {
					for (int l = 0; l < nVertices; l++) {
						if (i != j && i != k && i != l && j != k && j != l && k != l) {
							int sum = sol[i][j] + sol[j][k] + sol[k][l] + sol[l][i];
							if (sum > 3) {
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
			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					oldConstraints += line + "\n";
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		try {
			PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
			w.println(squares + num);
			if (oldConstraints.length() > 0) {
				w.print(oldConstraints);
			}
			if (cons.length() > 0) {
				w.print(cons);
			}
			w.close();
		} catch (IOException e) {
		}
		
		squares /= 8;
	}
}
