package solver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import params.Params;
import problem.AcquisitionWindow;
import problem.CandidateAcquisition;
import problem.PlanningProblem;
import problem.ProblemParserXML;
import problem.Satellite;

/**
 * Acquisition planner which solves the acquisition problem based on a greedy
 * algorithm which tries to plan at each step one additional acquisition, while
 * there are candidate acquisitions left.
 * 
 * @author cpralet
 *
 */
public class BadAcquisitionPlannerGreedy {

	/** Planning problem for which this acquisition planner is used */
	private final PlanningProblem planningProblem;
	/** Data structure used for storing the plan of each satellite */
	private final Map<Satellite, SatellitePlan> satellitePlans; /*
																 * à chaque satellite j'associe un plan qui contiendra
																 * les acquisitions à effectuer
																 */

	/**
	 * Build an acquisition planner for a planning problem
	 * 
	 * @param planningProblem
	 */
	public BadAcquisitionPlannerGreedy(PlanningProblem planningProblem) {
		this.planningProblem = planningProblem;
		satellitePlans = new HashMap<Satellite, SatellitePlan>();
		for (Satellite satellite : planningProblem.satellites) {
			satellitePlans.put(satellite, new SatellitePlan());
		}
	}

	/**
	 * Planning function which uses a greedy algorithm. The latter tries to plan at
	 * each step one additional acquisition (randomly chosen), while there are
	 * candidate acquisitions left. --> il faudrait commencer par les acquisitions
	 * les plus importantes récupérer l'attribut priorite de chaque acquisition et
	 * ordonner la liste avec ça
	 */
	public void planAcquisitions() {

		/* Random rand = new Random(0); */

		List<CandidateAcquisition> candidateAcquisitions = new ArrayList<CandidateAcquisition>(
				planningProblem.candidateAcquisitions);

		// on fait un sort des acquisitions candidates par priorité
		// Collections.sort(candidateAcquisitions, priorityComparator);

		List<CandidateAcquisition> priority0_candidates = new ArrayList<CandidateAcquisition>();
		List<CandidateAcquisition> priority1_candidates = new ArrayList<CandidateAcquisition>();
		for (CandidateAcquisition candidateAcquisition : candidateAcquisitions) {
			if (candidateAcquisition.priority == 0) {
				priority0_candidates.add(candidateAcquisition);
			} else {
				priority1_candidates.add(candidateAcquisition);
			}

		}

		List<AcquisitionWindow> priority0_windows = new ArrayList<AcquisitionWindow>();

		for (CandidateAcquisition candidateAcquisition : priority0_candidates) {
			for (AcquisitionWindow window : candidateAcquisition.acquisitionWindows) {
				priority0_windows.add(window);
			}
		}

		Collections.sort(priority0_windows, cloudprobaComparator);

		List<AcquisitionWindow> priority1_windows = new ArrayList<AcquisitionWindow>();

		for (CandidateAcquisition candidateAcquisition : priority1_candidates) {
			for (AcquisitionWindow window : candidateAcquisition.acquisitionWindows) {
				priority1_windows.add(window);
			}
		}

		Collections.sort(priority1_windows, cloudprobaComparator);

		// ListIterator<CandidateAcquisition> itr =
		// candidateAcquisitions.listIterator();

		int nCandidates = candidateAcquisitions.size();
		int nPlanned = 0;

		List<CandidateAcquisition> plan_acquisitions = new ArrayList<CandidateAcquisition>();

		for (AcquisitionWindow acqWindow : priority0_windows) {
			System.out.println("cloud proba " + acqWindow.cloudProba);
			// System.out.println("cloud proba" + acqWindow.cloudProba);
			CandidateAcquisition a = acqWindow.candidateAcquisition;
			Satellite satellite = acqWindow.satellite;
			SatellitePlan satellitePlan = satellitePlans.get(satellite);
			if (!plan_acquisitions.contains(a)) {
				satellitePlan.add(acqWindow);
				if (satellitePlan.isFeasible()) {
					nPlanned++;
					a.selectedAcquisitionWindow = acqWindow;
					plan_acquisitions.add(a);
					// break;
				} else
					satellitePlan.remove(acqWindow);
			}

		}

		for (AcquisitionWindow acqWindow : priority1_windows) {
			System.out.println("cloud proba " + acqWindow.cloudProba);
			// System.out.println("cloud proba" + acqWindow.cloudProba);
			CandidateAcquisition a = acqWindow.candidateAcquisition;
			Satellite satellite = acqWindow.satellite;
			SatellitePlan satellitePlan = satellitePlans.get(satellite);
			if (!plan_acquisitions.contains(a)) {
				satellitePlan.add(acqWindow);
				if (satellitePlan.isFeasible()) {
					nPlanned++;
					a.selectedAcquisitionWindow = acqWindow;
					plan_acquisitions.add(a);
					// break;
				} else
					satellitePlan.remove(acqWindow);
			}

		}

		// while (!candidateAcquisitions.isEmpty() && itr.hasNext()) {
		// select one candidate acquisition (random selection) --> à changer

		/*
		 * int k = rand.nextInt(candidateAcquisitions.size()); CandidateAcquisition a =
		 * candidateAcquisitions.remove(k);
		 */

		// CandidateAcquisition a = itr.next();

		// pour chaque acquisition candidate a, on fait un sort de a.acquisitionWindows
		// par cloudproba croissante

		// Collections.sort(a.acquisitionWindows, cloudprobaComparator);

		// try to plan one acquisition window for this acquisition (and stop once a
		// feasible acquisition window is found ; on parcourt toutes les fenêtres
		// possibles pour chaque acquisition
		// for (AcquisitionWindow acqWindow : a.acquisitionWindows) {
		// System.out.println("cloud proba " + acqWindow.cloudProba);
		// System.out.println("cloud proba" + acqWindow.cloudProba);
		// Satellite satellite = acqWindow.satellite;
		// SatellitePlan satellitePlan = satellitePlans.get(satellite);
		// satellitePlan.add(acqWindow);
		// if (satellitePlan.isFeasible()) {
		// nPlanned++;
		// a.selectedAcquisitionWindow = acqWindow;
		// break;
		// } else
		// satellitePlan.remove(acqWindow);
		// }
		// System.out.println("------");
		// }
		System.out.println("nPlanned: " + nPlanned + "/" + nCandidates);
	}

	private class SatellitePlan {

		/** Acquisitions to be realized by the satellite */
		private List<AcquisitionWindow> acqWindows;
		/** Map defining the start time of each acquisition in the solution schedule */
		private Map<AcquisitionWindow, Double> startTimes;

		public SatellitePlan() {
			acqWindows = new ArrayList<AcquisitionWindow>();
			startTimes = new HashMap<AcquisitionWindow, Double>();
		}

		public double getStart(AcquisitionWindow aw) {
			return startTimes.get(aw);
		}

		public List<AcquisitionWindow> getAcqWindows() {
			return acqWindows;
		}

		public void add(AcquisitionWindow aw) {
			acqWindows.add(aw);
		}

		public void remove(AcquisitionWindow aw) {
			acqWindows.remove(aw);
			startTimes.remove(aw);
		}

		/**
		 * 
		 * @return true if the list of acquisition windows is evaluated as being
		 *         feasible from a temporal point of view
		 */
		public boolean isFeasible() {

			// sort acquisition windows by increasing start times
			Collections.sort(acqWindows, startTimeComparator);

			// initialize the forward traversal of the acquisition windows by considering
			// the first one
			AcquisitionWindow prevAcqWindow = acqWindows.get(0);
			if (planningProblem.horizonStart > prevAcqWindow.latestStart)
				return false;
			double startTime = Math.max(planningProblem.horizonStart, prevAcqWindow.earliestStart);
			startTimes.put(prevAcqWindow, startTime);
			double prevEndTime = startTime + prevAcqWindow.duration;

			// traverse all acquisition windows and check that each acquisition can be
			// realized (taking into account roll angle transitions)
			for (int i = 1; i < acqWindows.size(); i++) {
				AcquisitionWindow acqWindow = acqWindows.get(i);
				double rollAngleTransitionTime = planningProblem.getTransitionTime(prevAcqWindow, acqWindow);
				startTime = Math.max(prevEndTime + rollAngleTransitionTime, acqWindow.earliestStart);
				if (startTime > acqWindow.latestStart) // sequence of acquisition windows not feasible
					return false;
				startTimes.put(acqWindow, startTime);
				prevEndTime = startTime + acqWindow.duration;
				prevAcqWindow = acqWindow;
			}
			return true;
		}
	}

	/**
	 * Comparator used for sorting acquisition windows by increasing earliest start
	 * time
	 */
	private final Comparator<AcquisitionWindow> startTimeComparator = new Comparator<AcquisitionWindow>() {
		@Override
		public int compare(AcquisitionWindow w0, AcquisitionWindow w1) {
			return Double.compare(w0.earliestStart, w1.earliestStart);
		}
	};

	/**
	 * Création d'un comparateur de priorité pour ordonner la liste des acquisitions
	 * candidates par ordre de priorité croissante
	 */
	private final Comparator<CandidateAcquisition> priorityComparator = new Comparator<CandidateAcquisition>() {
		@Override
		public int compare(CandidateAcquisition a0, CandidateAcquisition a1) {
			return Integer.compare(a0.priority, a1.priority);
		}
	};

	/**
	 * Création d'un comparateur de priorité pour ordonner la liste des fenêtres
	 * d'acquisitions par proba de couverture nuageuse croissante
	 */
	private final Comparator<AcquisitionWindow> cloudprobaComparator = new Comparator<AcquisitionWindow>() {
		@Override
		public int compare(AcquisitionWindow f0, AcquisitionWindow f1) {
			return Double.compare(f0.cloudProba, f1.cloudProba);
		}
	};

	/**
	 * Write the acquisition plan of a given satellite in a file
	 * 
	 * @param satellite
	 * @param solutionFilename
	 * @throws IOException
	 */
	public void writePlan(Satellite satellite, String solutionFilename) throws IOException {
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(solutionFilename, false)));
		SatellitePlan plan = satellitePlans.get(satellite);
		for (AcquisitionWindow aw : plan.getAcqWindows()) {
			double start = plan.getStart(aw);
			writer.write(aw.candidateAcquisition.idx + " " + aw.idx + " " + start + " " + (start + aw.duration) + " "
					+ aw.candidateAcquisition.name + "\n");
		}
		writer.flush();
		writer.close();
	}

	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException {
		ProblemParserXML parser = new ProblemParserXML();
		PlanningProblem pb = parser.read(Params.systemDataFile, Params.planningDataFile);
		pb.printStatistics();
		BadAcquisitionPlannerGreedy planner = new BadAcquisitionPlannerGreedy(pb);
		planner.planAcquisitions();
		for (Satellite satellite : pb.satellites) {
			planner.writePlan(satellite,
					"satellitePlanning/output/solutionAcqPlan_withcloudproba" + satellite.name + ".txt");
		}
		System.out.println("Acquisition planning done");
	}

}
