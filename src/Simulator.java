import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulator {
    List<Element> eleList;
    List<ExecRules> ruleList;
    Map<String, Element> eleMap;
    Map<Integer, List<ExecRules>> rankRuleLst;

    public Simulator() {
	eleList = new ArrayList<Element>();
	ruleList = new ArrayList<ExecRules>();
	eleMap = new HashMap<String, Element>();
	rankRuleLst = new HashMap <Integer, List<ExecRules>>();
    }

    public static void main(String[] args) {

	if (args.length != 6) {
	    System.out
		    .println("Usage: java Simulator <input file> <type: ra | ca> <runs> <cycle> <output file> <isRank>"); //TODO add isRank
	    return;
	}
	Simulator sim = new Simulator();
	String infn = args[0];
	String type = args[1];
	int run = Integer.parseInt(args[2]);
	int cycles = Integer.parseInt(args[3]);
	String outfn = args[4];
	boolean isRank = args[5].equals("yes") ? true : false;
	

	sim.readFile(infn);

	PrintWriter out;

	try {
	    out = new PrintWriter(outfn, "UTF-8");
	    out.println(outfn + " succeeded with " + run + " runs of " + cycles + " Cycles each.");
	    out.println();
	    for (int i = 0; i < run; i++) {
		System.out.println("Run #" + i);
		// run simulation
		if (type.equals("ra")) {
		    sim.raSim(cycles);
		} else if (type.equals("ca")) {
		    sim.caSim(cycles, isRank);
		} else {
		    System.err.println("Wrong type!");
		    return;
		}
		sim.writeOutRun(i, out); // write to the output
		if (i != run - 1)
		    sim.resetEle(); // reset element
	    }
	    sim.printSummary(out);
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public void resetEle() {
	for (Element element : eleList)
	    element.reset();
    }

    public void printSummary(PrintWriter out) {
	out.println("Frequency Summary:");
	for (Element element : eleList) {
	    StringBuilder line = new StringBuilder();
	    line.append(element.getName()).append(" ");
	    List<Integer> accumSeries = element.getAccSeries();
	    for (Integer val : accumSeries)
		line.append(val).append(" ");
	    out.println(line.substring(0, line.length() - 1));
	}
    }
    
    public void writeOutRun(int run, PrintWriter out) {
	out.println("Run #" + run);
	for (Element element : eleList) {
	    StringBuilder line = new StringBuilder();
	    line.append(element.getName()).append(" ");
	    List<Integer> roundSeries = element.getRoundSeries();
	    for (Integer val : roundSeries)
		line.append(val).append(" ");
	    out.println(line.substring(0, line.length() - 1));
	}
    }

    public void raSim(int cycles) {
	Utility.raSimulation(cycles, eleList, ruleList, eleMap);
    }
    
    public void caSim(int cycles, boolean isRank) {
	Utility.caSimulation(cycles, isRank, eleList, ruleList, eleMap, rankRuleLst);
    }

    public void readFile(String infn) {
	try {
	    BufferedReader br = new BufferedReader(new FileReader(infn));
	    Utility.parseElements(eleList, br, eleMap);
	    Utility.parseRules(ruleList, br, rankRuleLst);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }
}
