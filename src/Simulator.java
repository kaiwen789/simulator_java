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
    List <Double> accProb; // accumulated probabilities 
    List <ExecRules> orderRuleLst; // a list of ExecRules which is align to accProb

    public Simulator() {
	eleList = new ArrayList<Element>();
	ruleList = new ArrayList<ExecRules>();
	eleMap = new HashMap<String, Element>();
	rankRuleLst = new HashMap <Integer, List<ExecRules>>();
    }

    public static void main(String[] args) {

	if (args.length < 7) {
	    System.out.println(
		    "Usage: java Simulator [input file] [type: ra|ca] [runs] [cycle] [output file] [isRank] [probability version] [option: output mode]");
	    return;
	}
	Simulator sim = new Simulator();
	String infn = args[0];
	String type = args[1].toLowerCase();
	int run = Integer.parseInt(args[2]);
	int cycles = Integer.parseInt(args[3]);
	String outfn = args[4];
	boolean isRank = args[5].toLowerCase().equals("yes") ? true : false;
	int probVer = Integer.parseInt(args[6]); // 1 -> version 1, 2 -> version 2, others -> none
	
	/* error handling */
	if (isRank && type.equals("ra")) {
	    System.err.println("Cannot run rank function under ra mode");
	    return;
	} 
	
	/* error handling */
	if (probVer == 1 || probVer == 2) {
	    if (isRank) {
		System.err.println("Cannot have probability when using rank");
		return;
	    } else if (type.equals("ca")) {
		System.err.println("Cannot have probability under ca mode");
		return;
	    }
	}
	

	sim.readFile(infn, isRank, probVer);
	PrintWriter out;

	try {
	    out = new PrintWriter(outfn, "UTF-8");
	    out.println(outfn + " succeeded with " + run + " runs of " + cycles + " Cycles each.");
	    out.println();
	    for (int i = 0; i < run; i++) {
		System.out.println("Run #" + i);
		// run simulation
		if (type.equals("ra")) {
		    sim.raSim(cycles, probVer);
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
	    for (int a = 0; a < accumSeries.size(); a++) {
		line.append(accumSeries.get(a)).append(" ");
	    }
//	    for (Integer val : accumSeries)
//		line.append(val).append(" ");
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

    public void raSim(int cycles, int probVer) {
	Utility.raSimulation(cycles, eleList, ruleList, eleMap, probVer, accProb, orderRuleLst);
    }
    
    public void caSim(int cycles, boolean isRank) {
	Utility.caSimulation(cycles, isRank, eleList, ruleList, eleMap, rankRuleLst);
    }

    public void readFile(String infn, boolean isRank, int probVer) {
	
	if (probVer == 1 || probVer == 2) {
	    accProb = new ArrayList <Double> ();
	    orderRuleLst = new ArrayList <ExecRules>();
	}
	
	try {
	    BufferedReader br = new BufferedReader(new FileReader(infn));
	    Utility.parseElements(eleList, br, eleMap);
	    Utility.parseRules(ruleList, br, rankRuleLst, isRank, probVer, accProb, orderRuleLst);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }
}
