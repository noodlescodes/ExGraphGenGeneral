import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Model {

	private String squareConsFile;
	private int iteration;

	private LpSolve lp;
	private double[] row;
	private int[] colno;

	private int deltaL;
	private int deltaU;
	private int nVertices;

	private int ret = 0;

	private boolean VERBOSE;

	public Model(String sqareConsFile, int deltaL, int deltaU, int nVertices, int iteration, boolean verbose) throws LpSolveException {
		this.squareConsFile = sqareConsFile;
		this.VERBOSE = verbose;
		this.iteration = iteration;
		this.deltaL = deltaL;
		this.deltaU = deltaU;
		this.nVertices = nVertices;
		colno = new int[nVertices * nVertices];
		row = new double[nVertices * nVertices];
		lp = LpSolve.makeLp(0, nVertices * nVertices);
		if (lp.getLp() == 0) {
			ret = 1;
		}
	}

	public int getRet() {
		return ret;
	}

	private String createString(int i, int j) {
		return "B" + i + ";" + j + "E";
	}

	private int getIndex(int i, int j) {
		return nVertices * (i - 1) + j;
	}

	public int[][] getSubMatrix(int i0, int j0) {
		int[][] submatrix = new int[deltaL][deltaL];

		for (int i = 0; i < deltaL; i++) {
			for (int j = 0; j < deltaL; j++) {
				submatrix[i][j] = getIndex(i + i0, j + j0);
			}
		}

		return submatrix;
	}

	public int execute() throws LpSolveException {
		int j = 0;

		if (ret == 0) {
			if (VERBOSE) {
				System.out.println("Setting up model name.");
			}
			// set model name
			lp.setLpName("ExGraph genetic. Gen " + iteration);

			if (VERBOSE) {
				System.out.println("Setting up variable names.");
			}
			// setup variable names
			int loop = 1;
			for (int i = 1; i <= nVertices; i++) {
				for (int j1 = 1; j1 <= nVertices; j1++) {
					lp.setColName(loop, createString(i, j1));
					lp.setBinary(loop++, true);
				}
			}

			if (VERBOSE) {
				System.out.println("Setting build mode.");
			}
			// setup build mode
			lp.setAddRowmode(true);

			// ---begin adding predefined constraints---
			if (VERBOSE) {
				System.out.println("Adding predefined constraints.");
			}
			// rows two through deltaU
			for (int i = 2; i <= deltaU; i++) {
				for (int j1 = 1; j1 <= deltaL; j1++) {
					lp.setBounds(getIndex(i, j1 + (i - 1) * deltaL + 1), 1, 1.5);
				}
			}
			// first row
			for (int i = 2; i <= deltaU; i++) {
				lp.setBounds(getIndex(1, i), 1, 1.5);
			}
			// identity matrix
			j = deltaL + deltaU + 1;
			for (int i = deltaU + 1; i <= deltaL + deltaU; i++) {
				for (int b = 1; b < deltaL; b++) {
					lp.setBounds(getIndex(i, j + (b - 1) * deltaL), 1, 1.5);
				}
				j++;
			}
			// ---end adding predefined constraints---

			// ---begin a_{i,i}=0 constraints---
			if (VERBOSE) {
				System.out.println("Beginning a_{i,i}=0 constraints");
			}
			j = 0;
			for (int i = 1; i <= nVertices; i++) {
				colno[j] = getIndex(i, i);
				row[j++] = 1;
				lp.addConstraintex(j, row, colno, LpSolve.EQ, 0);
				j = 0;
			}
			// ---end a_{i,i}=0 constraints---

			// ---begin a_{i,j}=a_{j,i} constraints---
			if (VERBOSE) {
				System.out.println("Beginning a_{i,j}=a_{j,i} constraints.");
			}
			j = 0;
			for (int i = 1; i <= nVertices; i++) {
				for (int j1 = 1; j1 <= nVertices; j1++) {
					if (i != j1) {
						colno[j] = getIndex(i, j1);
						row[j++] = 1;
						colno[j] = getIndex(j1, i);
						row[j++] = -1;
						lp.addConstraintex(j, row, colno, LpSolve.EQ, 0);
					}
					j = 0;
				}
			}
			// ---end a_{i,j}=a_{j,i} constraints---

			// ---begin no triangles constraints---
			if (VERBOSE) {
				System.out.println("Beginning no triangles constraints.");
			}
			j = 0;
			for (int i = 1; i <= nVertices; i++) {
				for (int j1 = i + 1; j1 <= nVertices; j1++) {
					for (int k = j1 + 1; k <= nVertices; k++) {
						if (i != j1 && j1 != k && i != k) {
							colno[j] = getIndex(i, j1);
							row[j++] = 1;
							colno[j] = getIndex(j1, k);
							row[j++] = 1;
							colno[j] = getIndex(i, k);
							row[j++] = 1;
							lp.addConstraintex(j, row, colno, LpSolve.LE, 2);
						}
						j = 0;
					}
				}
			}
			// ---end no triangles constraints---

			// ---begin no square constraints---
			if (iteration > 0) {
				if (VERBOSE) {
					System.out.println("Beginning no squares constraitns");
				}
				ReadSquareConstraints rsc = new ReadSquareConstraints(squareConsFile);
				int[][][] squareConstraints = rsc.getConstraints();
				j = 0;
				for (int i = 0; i < squareConstraints.length; i++) {
					for (int j1 = 0; j1 < squareConstraints[i].length; j1++) {
						colno[j] = getIndex(squareConstraints[i][j1][0] + 1, squareConstraints[i][j1][1] + 1);
						row[j++] = 1;
					}
					lp.addConstraintex(j, row, colno, LpSolve.LE, 3);
					j = 0;
				}
			}
			// ---end no square constraints---

			// ---begin correct row sum constraints---
			if (VERBOSE) {
				System.out.println("Beginning correct row sum constraitns");
			}
			// j = 0;
			// for(int i = deltaU + 1; i <= nVertices; i++) {
			// for(int j1 = 1; j1 <= nVertices; j1++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// j = 0;
			// }
			j = 0;
			// first row
			// for(int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(1, i);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaL);
			// row between row 2 and row deltaU
			// j = 0;
			// for(int j1 = 2; j1 <= deltaU; j1++) {
			// for(int i = 1; i <= nVertices; i++) {
			// colno[j] = getIndex(i, j1);
			// row[j++] = 1;
			// }
			// lp.addConstraintex(j, row, colno, LpSolve.EQ, deltaU);
			// }
			// ---end correct row sum constraints---

			// ---begin experimental constraints---
			for (int i = 1; i <= nVertices; i++) {
				for (int j1 = i + 1; j1 <= nVertices; j1++) {
					colno[j] = getIndex(i, j1);
					row[j++] = 1;
				}
			}
			lp.addConstraintex(j, row, colno, LpSolve.LE, 89);
			// ---end experimental constraints---

			if (VERBOSE) {
				System.out.println("Turning off build mode.");
			}
			// turn off build mode
			lp.setAddRowmode(false);

			if (VERBOSE) {
				System.out.println("Beginning objective function.");
			}
			// ---begin objective function---
			j = 0;
			for (int i = deltaU + 1; i <= nVertices; i++) {
				for (int j1 = i; j1 <= nVertices; j1++) {
					colno[j] = getIndex(i, j1);
					row[j++] = 1;
				}
			}

			// j = 0;
			// colno[j] = 1;
			// row[j++] = 0;

			// set the objective function
			lp.setObjFnex(j, row, colno);

			// set the objective direction to maximise
			lp.setMaxim();
			// ---end objective function---

			if (VERBOSE) {
				System.out.println("Writing model to file.");
			}
			// write model to file
			lp.writeLp("models/" + nVertices + "/modelEx" + iteration + ".lp");

			// set message type
			lp.setVerbose(LpSolve.CRITICAL);

			// ---begin solving---
			if (VERBOSE) {
				System.out.println("Presolving.");
			}
			lp.setPresolve(LpSolve.PRESOLVE_BOUNDS | LpSolve.PRESOLVE_COLS | LpSolve.PRESOLVE_ROWS, lp.getPresolveloops());

			if (VERBOSE) {
				System.out.println("Starting to solve the model. This may take a while");
			}
			ret = lp.solve();
			if (ret == LpSolve.OPTIMAL) {
				ret = 0;
			} else {
				ret = 5;
			}
			// ---end solving---
		}
		if (VERBOSE) {
			System.out.println("Beginning output.");
		}
		if (ret == 0) {
			lp.setOutputfile("models/" + nVertices + "/ExSolution" + iteration + ".dat");
			lp.printSolution(1);
			lp.printConstraints(1);
		}

		lp.deleteLp();

		return ret;
	}
}
