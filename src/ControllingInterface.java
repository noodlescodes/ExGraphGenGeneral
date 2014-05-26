import java.io.File;

import javax.sound.sampled.LineUnavailableException;

import lpsolve.LpSolveException;

public class ControllingInterface {
	public static void main(String[] args) {
		int deltaL = 5;
		int deltaU = deltaL + 1;
		int nVertices = 31;
		int iteration = 0;

		Model model;
		OutputParser op;
		CreateSquareConstraints csc;
		EdgeCounter ec;
		File f = new File("models/" + nVertices);
		f.mkdir();
		long time = System.currentTimeMillis();
		String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS").format(new java.util.Date(time));
		System.out.println("Start time:" + date + "\n");

		try {
			do {
				long timeIt = System.currentTimeMillis();
				date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS").format(new java.util.Date(timeIt));
				System.out.println("Starting iteration #" + iteration + ". Time started: " + date);
				model = new Model("models/" + nVertices + "/squares.dat", deltaL, deltaU, nVertices, iteration, false);
				op = new OutputParser("models/" + nVertices + "/ExSolution" + iteration + ".dat", nVertices);
				csc = new CreateSquareConstraints("models/" + nVertices + "/mat.dat", "models/" + nVertices + "/squares.dat", nVertices);
				int ret = model.execute();
				if (ret != 0) {
					System.out.println("No solution");
					break;
				}
				op.read();
				op.write("models/" + nVertices + "/mat.dat");
				csc.createConstraints();
				System.out.println("Number of squares after iteration #" + iteration + ": " + csc.getNumberOfSquares());
				System.out.println("Time spent on iteration #" + (iteration++) + ": " + (System.currentTimeMillis() - timeIt));
				System.out.println("Total time so far: " + (System.currentTimeMillis() - time) + "\n");
				Tone.sound(7000, 100, 1.0);
			} while (csc.getNumberOfSquares() > 0);
			if(model.getRet() == 0) {
				System.out.println("Solution found.");
				ec = new EdgeCounter("models/" + nVertices + "/mat.dat");
				System.out.println("Number of edges: " + ec.countEdges());
				Tone.sound(8000, 200, 1.0);
				Tone.sound(9000, 200, 1.0);
				Tone.sound(10000, 200, 1.0);
			}
			System.out.println("Time taken: " + (System.currentTimeMillis() - time));
		} catch (LpSolveException | IllegalArgumentException | LineUnavailableException e) {
			e.printStackTrace();
		}		
	}
}