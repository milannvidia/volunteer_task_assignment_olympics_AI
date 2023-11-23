package Model;

import java.io.IOException;
import java.util.*;

public class Instance {
    private static double travelDistanceWeight;
    private static double genderBalanceWeight;
    private static double taskTypeAdequacyWeight;
    private static ArrayList<String> skills;
    private static Task[] tasks;
    private static Volunteer[] volunteers;
    private static boolean presourcing;
    private static int[][] qualified;
    private static int[] max_volunteers_per_job;
    private static int[][] distance_matrix;
    private static int V;
    private static int T;
    private static int S;

    //non static
    private int[] vol_per_task;
    private int[] assignment_matrix;
    private int[] qual_per_job;
    private int males = 0;
    private int total = 0;
    private int[][] qual_per_skill;

//    private int BestScore0 = 0;
//    private int BestScore1 = Integer.MAX_VALUE;

    public Instance(int travelDistanceWeight, int genderBalanceWeight, int taskTypeAdequacyWeight, ArrayList<String> skills, ArrayList<String> taskTypes, HashMap<String, Location> locations, Task[] tasks, Volunteer[] volunteers, Boolean presourcing) {
        Instance.travelDistanceWeight = travelDistanceWeight;
        Instance.genderBalanceWeight = genderBalanceWeight;
        Instance.taskTypeAdequacyWeight = taskTypeAdequacyWeight;

        Instance.skills = skills;
        Instance.tasks = tasks;
        Instance.volunteers = volunteers;
        Instance.presourcing = presourcing;

        V = volunteers.length;
        T = tasks.length;
        S = skills.size();

        qualified = new int[V][T];
        max_volunteers_per_job = new int[T];
        distance_matrix = new int[V][T];

        vol_per_task = new int[T];
        assignment_matrix = new int[V];
        qual_per_skill = new int[T][S];
        Arrays.fill(assignment_matrix, -1);
        qual_per_job=new int[T];

        for (int t = 0; t < T; t++) {
            max_volunteers_per_job[t] = tasks[t].demand;
        }
        for (int v = 0; v < V; v++) {
            Volunteer volunteer = volunteers[v];
            task:
            for (int t = 0; t < T; t++) {
                Task task = tasks[t];

                //distance
                Location lv = locations.get(volunteer.locationId);
                Location lt = locations.get(task.locationId);
                if (volunteer.preferredLocationIds.contains(task.locationId)) {
                    distance_matrix[v][t] = Instance.distance(lv.lon, lv.lat, lt.lon, lt.lat);
                } else {
                    distance_matrix[v][t] = -1;
                }

                //qualified continue so qualified stays 0
                //if is preferred location
                if (distance_matrix[v][t] < 0) {
                    continue;
                }

                //not feasible if not experienced in tasktype
                int volunteer_tasktype_skill = volunteer.taskTypes[task.taskTypeIndex];
                if (volunteer_tasktype_skill == 0) {
                    continue;
                }
                //can the volunteer work all days
                if (volunteer.availableDays < task.days) {
                    continue;
                }
                //check for each skill that is proportion 100 if qualifies
                for (int s = 0; s < S; s++) {
                    SkillRequirement skillTask = task.skillRequirements[s];
                    if (skillTask == null) continue;
                    int skillVolunteer = volunteer.skills[s];

                    if (skillTask.proportion == 1) {
                        if (skillTask.minProficiency >= skillVolunteer) {
                            continue task;
                        }
                    }
                }

                qualified[v][t] = 1;
                qual_per_job[t]+=1;
            }

        }

    }

    public Instance() {

        vol_per_task = new int[T];
        assignment_matrix = new int[V];
        qual_per_skill = new int[T][S];
        Arrays.fill(assignment_matrix, -1);

    }

    public static int distance(double lon1, double lat1, double lon2, double lat2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double dLat = Math.toRadians(lat2 - lat1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        int r = 6371;
        return (int) Math.ceil(c * r);
    }

    private boolean genderCheck(int v) {
        if (total > 25) {
            if (volunteers[v].isMale) {
                return !((double) (males + 1) / (total + 1) > 0.55);
            } else {
                return !((double) males / (total + 1) < 0.45);
            }
        }
        return true;
    }

    private boolean skillCheck(int t, int v) {
        Volunteer volunteer = volunteers[v];
        SkillRequirement[] skillrequirements = tasks[t].skillRequirements;
        for (int s = 0; s < S; s++) {
            SkillRequirement skillRequirement = skillrequirements[s];
            if (skillRequirement == null) continue;
            if (!skillRequirement.isHard) {
                continue;
            }
            if (volunteer.skills[s] <= skillRequirement.minProficiency) {
                if (skillRequirement.proportion > (double) (qual_per_skill[t][s]) / (vol_per_task[t] + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

//    private int[] getSkillCheck(int t, int v) {
//        //1 if adding it would give problem
//        Volunteer volunteer = volunteers[v];
//        ArrayList<SkillRequirement> skillrequirements = tasks[t].skillRequirements;
//        int[] skills = new int[S];
//        for (int s = 0; s < S; s++) {
//            SkillRequirement skillRequirement = skillrequirements.get(s);
//            if (skillRequirement == null) continue;
//            if (!skillRequirement.isHard) {
//                continue;
//            }
//            if (volunteer.skills[s] <= skillRequirement.minProficiency) {
//                if (skillRequirement.proportion > (double) (qual_per_skill[t][s]) / (vol_per_task[t] + 1)) {
//                    skills[s]=1;
//                }
//            }
//        }
//        return skills;
//    }
//
    private void addingToArrays(int t, int v) {
        Volunteer volunteer = volunteers[v];
        SkillRequirement[] skillrequirements = tasks[t].skillRequirements;
        for (int s = 0; s < S; s++) {
            SkillRequirement skillRequirement = skillrequirements[s];
            if (skillRequirement == null) continue;
            if (!skillRequirement.isHard) {
                continue;
            }
            if (volunteer.skills[s] > skillRequirement.minProficiency) {
                qual_per_skill[t][s] += 1;
            }
        }
        males += volunteer.isMale ? 1 : 0;
        total += 1;
        assignment_matrix[v] = t;
        vol_per_task[t] += 1;
    }
//
//    private void removingFromArrays(int t, int v) {
//        Volunteer volunteer = volunteers[v];
//        ArrayList<SkillRequirement> skillrequirements = tasks[t].skillRequirements;
//        for (int s = 0; s < S; s++) {
//            SkillRequirement skillRequirement = skillrequirements.get(s);
//            if (skillRequirement == null) continue;
//            if (!skillRequirement.isHard) {
//                continue;
//            }
//            if (volunteer.skills[s] > skillRequirement.minProficiency) {
//                qual_per_skill[t][s] -= 1;
//            }
//        }
//        males -= volunteer.isMale ? 1 : 0;
//        total -= 1;
//        assignment_matrix[v] = -1;
//        vol_per_task[t] -= 1;
//    }
//
//    private boolean freeUpSpace(int jobIndex) {
//
//        for (int v = 0; v < V; v++) {
//            if (assignment_matrix[v] != jobIndex) continue;
//
//            for (int t_pre = jobIndex + 1; t_pre < T + jobIndex + 1; t_pre++) {
//                int t = t_pre % T;
//
//                if (qualified[v][t] != 1) continue;
//                if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) continue;
//                if (!skillCheck(t, v)) continue;
//                removingFromArrays(jobIndex, v);
//                addingToArrays(t, v);
//                return true;
//            }
//
//        }
//        return false;
//    }
//
//    private boolean reallocateUnskilled(int jobIndex, int[] skills) {
//        for (int v = 0; v < V; v++) {
//            if (assignment_matrix[v] != jobIndex) continue;
//            if(vol_per_task[jobIndex]<=1)return false;
//            //check if removing unskilled doesnt remove a skill point from other skill
//            //skills is 1 if it is a problem
//            boolean remove = true;
//            for (int s = 0; s < S; s++) {
//                if (skills[s] == 0) continue;
//                ArrayList<SkillRequirement> skillRequirements = tasks[jobIndex].skillRequirements;
//                if (volunteers[v].skills[s] > skillRequirements.get(s).minProficiency) {
//                    if (skillRequirements.get(s).proportion > (double) ((qual_per_skill[jobIndex][s] - 1) / (vol_per_task[jobIndex] - 1))) {
//                        remove = false;
//                    }
//                }
//            }
//            if (remove) {
//                for (int t_pre = jobIndex + 1; t_pre < T + jobIndex + 1; t_pre++) {
//                    int t = t_pre % T;
//
//                    if (qualified[v][t] != 1) continue;
//                    if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) continue;
//                    if (!skillCheck(t, v)) continue;
//                    removingFromArrays(jobIndex, v);
//                    addingToArrays(t, v);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    private boolean AddSkilled(Integer jobIndex, int[] skillsNotOkay) {
//        volunteer:
//        for (int v = 0; v < V; v++) {
//            //gender check
//            if(assignment_matrix[v]!=-1)continue;
//            if (qualified[v][jobIndex] != 1) continue;
//
//            if (vol_per_task[jobIndex] + 1 > max_volunteers_per_job[jobIndex]) {
//                return false;
//            }
//
//            Volunteer volunteer = volunteers[v];
//            ArrayList<SkillRequirement> skillrequirements = tasks[jobIndex].skillRequirements;
//            boolean goodVolunteer=true;
//            for (int s = 0; s < S; s++) {
//                if(skillsNotOkay[s]==0)continue;
//                SkillRequirement skillRequirement = skillrequirements.get(s);
//
//                if (volunteer.skills[s] < skillRequirement.minProficiency) {
//                    goodVolunteer=false;
//                }
//            }
//            if (!goodVolunteer) {
//                continue;
//            }
//            return true;
//
//        }
//        return false;
//    }
    private int[] feasibleSolutionNoPresourcing() {
        volunteer:
        for (int v = 0; v < V; v++) {
            //gender check
            if (!genderCheck(v)) continue;
            for (int t = 0; t < T; t++) {
                if (qualified[v][t] != 1) continue;
                if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) continue;
                if (!skillCheck(t, v)) continue;

                addingToArrays(t, v);
                continue volunteer;
            }
        }
        return assignment_matrix;
    }

//    private int[] feasibleSolutionPresourcing() {
//
//        ArrayList<Integer> presourcedLeft = new ArrayList<>();
//
//
//        for (int v = 0; v < V; v++) {
//            Volunteer volunteer = volunteers[v];
//            if (!volunteer.isPresourced) continue;
//            presourcedLeft.add(v);
//        }
//
//        //first assignment
//        int amountLeft;
//        do {
//            amountLeft = presourcedLeft.size();
//            volunteer:
//            for (int v : new ArrayList<>(presourcedLeft)) {
////                if (!genderCheck(v)) continue;
//                for (int t = 0; t < T; t++) {
//                    if (qualified[v][t] != 1) continue;
//                    if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) continue;
//                    if (!skillCheck(t, v)) continue;
//                    addingToArrays(t, v);
//                    presourcedLeft.remove(Integer.valueOf(v));
//                    continue volunteer;
//                }
//            }
//        } while (presourcedLeft.size() != amountLeft);
//
//        //for each volunteer an arraylist
//
//        //1-> fillingsporblems
//        //2-> skillissues
//        HashMap<Integer, ArrayList<ArrayList<Integer>>> problems = new HashMap<>();
//
//        //now check why not possible
//        volunteer:
//        for (int v : presourcedLeft) {
//            ArrayList<ArrayList<Integer>> problemsVolunteer = new ArrayList<>();
//
//            ArrayList<Integer> fullProblem = new ArrayList<>();
//            ArrayList<Integer> skillIssue = new ArrayList<>();
//
//            for (int t = 0; t < T; t++) {
//                if (qualified[v][t] != 1) continue;
//                if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) {
//                    fullProblem.add(t);
//                }
//                if (!skillCheck(t, v)) {
//                    skillIssue.add(t);
//                }
//            }
//            problemsVolunteer.add(0, fullProblem);
//            problemsVolunteer.add(1, skillIssue);
//            problems.put(v, problemsVolunteer);
//        }
//
//        //solver
//        while (!presourcedLeft.isEmpty()) {
//            amountLeft = presourcedLeft.size();
//            System.out.printf("%d   %d%n", total, amountLeft);
//            solver:
//            for (Integer volunteerIndex : new ArrayList<>(presourcedLeft)) {
//                ArrayList<ArrayList<Integer>> problemsVolunteer = problems.get(volunteerIndex);
//                //full and skill
//                if (!problemsVolunteer.get(0).isEmpty()&&!problemsVolunteer.get(1).isEmpty()) {
//                    for(Iterator<Integer> it=problemsVolunteer.get(0).iterator(); it.hasNext();){
//                        int jobIndex=it.next();
//
//                        if (!problemsVolunteer.get(1).contains(jobIndex)) continue;
//
//                        int[] skillsNotOkay = getSkillCheck(jobIndex, volunteerIndex);
//                        if (reallocateUnskilled(jobIndex, skillsNotOkay)) {
//                            boolean saveTOadd=true;
//                            //cant arise
//                            if (vol_per_task[jobIndex] + 1 > max_volunteers_per_job[jobIndex]) {
//                                if (!problemsVolunteer.get(0).contains(jobIndex))
//                                    problemsVolunteer.get(0).add(jobIndex);
//                                saveTOadd=false;
//                            }else{
//                                it.remove();
//                            }
//                            //cant arise
//                            if (!skillCheck(jobIndex, volunteerIndex)) {
//                                if (!problemsVolunteer.get(1).contains(jobIndex))
//                                    problemsVolunteer.get(1).add(jobIndex);
//                                saveTOadd=false;
//                            }else{
//                                problemsVolunteer.get(1).remove(Integer.valueOf(jobIndex));
//                            }
//                            if(!saveTOadd)continue;
//                            addingToArrays(jobIndex, volunteerIndex);
//                            problems.remove(volunteerIndex);
//                            presourcedLeft.remove(Integer.valueOf(volunteerIndex));
//                            continue solver;
//
//                        }
//
//                    }
//                }
//                //only skill
//                if(!problemsVolunteer.get(1).isEmpty()){
//                    for(Iterator<Integer> it=problemsVolunteer.get(1).iterator();it.hasNext();){
//                        int jobIndex=it.next();
//
//                        int[] skillsNotOkay = getSkillCheck(jobIndex, volunteerIndex);
//                        if (AddSkilled(jobIndex, skillsNotOkay)) {
//                            boolean saveTOadd=true;
//                            //cant arise
//                            if (vol_per_task[jobIndex] + 1 > max_volunteers_per_job[jobIndex]) {
//                                if (!problemsVolunteer.get(0).contains(jobIndex))
//                                    problemsVolunteer.get(0).add(jobIndex);
//                                saveTOadd=false;
//                            }else{
//                                problemsVolunteer.get(0).remove(Integer.valueOf(jobIndex));
//                            }
//                            //cant arise
//                            if (!skillCheck(jobIndex, volunteerIndex)) {
//                                if (!problemsVolunteer.get(1).contains(jobIndex))
//                                    problemsVolunteer.get(1).add(jobIndex);
//                                saveTOadd=false;
//                            }else{
//                                it.remove();
//                            }
//                            if(!saveTOadd)continue;
//                            addingToArrays(jobIndex, volunteerIndex);
//                            presourcedLeft.remove(Integer.valueOf(volunteerIndex));
//                            problems.remove(volunteerIndex);
//                            continue solver;
//
//                        }else{
//                            if (vol_per_task[jobIndex] + 1 > max_volunteers_per_job[jobIndex]) {
//                                if (!problemsVolunteer.get(0).contains(jobIndex)) problemsVolunteer.get(0).add(jobIndex);
//                                continue;
//                            }else{
//                                System.out.println("cant add someone to cover");
//                            }
//                        }
//
//                    }
//
//                }
//                //only full problem
//                if (!problemsVolunteer.get(0).isEmpty()) {
//                    for(Iterator<Integer> it=problemsVolunteer.get(0).iterator(); it.hasNext();){
//                        int jobIndex=it.next();
//                        //skip if also skill issue
//                        if (problemsVolunteer.get(1).contains(jobIndex)) continue;
//                        if (freeUpSpace(jobIndex)) {
//                            boolean saveTOadd=true;
//                            //cant arise
//                            if (vol_per_task[jobIndex] + 1 > max_volunteers_per_job[jobIndex]) {
//                                if (!problemsVolunteer.get(0).contains(jobIndex))
//                                    problemsVolunteer.get(0).add(jobIndex);
//                                saveTOadd=false;
//                            }else{
//                                it.remove();
//                            }
//                            //cant arise
//                            if (!skillCheck(jobIndex, volunteerIndex)) {
//                                if (!problemsVolunteer.get(1).contains(jobIndex))
//                                    problemsVolunteer.get(1).add(jobIndex);
//                                saveTOadd=false;
//                            }else{
//                                problemsVolunteer.get(1).remove(Integer.valueOf(jobIndex));
//                            }
//                            if(!saveTOadd)continue;
//                            addingToArrays(jobIndex, volunteerIndex);
//                            problems.remove(volunteerIndex);
//                            presourcedLeft.remove(Integer.valueOf(volunteerIndex));
//                            continue solver;
//                        }
//                    }
//                }
//            }
//
////            if (amountLeft != presourcedLeft.size()) continue;
////            for (int i = 0; i < 500; i++) {
////
////
////                volunteer:
////                for (int v = 0; v < V; v++) {
////                    if (assignment_matrix[v] != -1) continue;
////                    for (int t = 0; t < T; t++) {
////                        if (qualified[v][t] != 1) continue;
////                        if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) continue;
////                        if (!skillCheck(t, v)) continue;
////
////                        addingToArrays(t, v);
////                        break volunteer;
////                    }
////                }
////            }
//        }
//        while((double)males/total>0.55 ||(double)males/total<0.45){
//            volunteer:
//            for (int v = 0; v < V; v++) {
//                if (!((double) males / total > 0.55) && !((double) males / total < 0.45)) break ;
//                if(assignment_matrix[v]!=-1)continue;
//                //gender check
//                if (!genderCheck(v)) continue;
//                for (int t = 0; t < T; t++) {
//                    if (qualified[v][t] != 1) continue;
//                    if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) continue;
//                    if (!skillCheck(t, v)) continue;
//
//                    addingToArrays(t, v);
//                    continue volunteer;
//                }
//            }
//            return assignment_matrix;
//        }
//
//        return assignment_matrix;
//    }
//
//    private int[] feasibleSolutionPresourcingV2(){
//        //sort jobs on peaple who can work there
//        ArrayList<Integer> jobs=new ArrayList<>(T);
//        for (int t = 0; t < T; t++) {
//            jobs.add(t);
//        }
//        ArrayList<Integer> presourcedLeft = new ArrayList<>();
//        for (int v = 0; v < V; v++) {
//            if (!volunteers[v].isPresourced) continue;
//            presourcedLeft.add(v);
//        }
//        jobs.sort((o1,o2)-> Integer.compare(qual_per_job[o1], qual_per_job[o2]));
//        presourcedLeft.sort(Comparator.comparingInt(o -> Arrays.stream(qualified[o]).sum()));
//
//        int amountLeft;
//        restart:
//        do {
//            amountLeft = presourcedLeft.size();
//            System.out.println(presourcedLeft.size());
//            job:
//            for (Iterator<Integer> itJobs = jobs.iterator(); itJobs.hasNext(); ) {
//                int t = itJobs.next();
//                if (t == 0) {
//                    itJobs.remove();
//                    continue;
//                }
//                if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) {
//                    itJobs.remove();
//                    continue;
//                }
//                for (Iterator<Integer> it = presourcedLeft.iterator(); it.hasNext(); ) {
//                    int v = it.next();
//                    if (qualified[v][t] == 0) {
//                        continue;
//                    }
//                    if (!skillCheck(t, v)) {
//                        continue;
//                    }
//
//                    addingToArrays(t, v);
//                    it.remove();
//                    if (vol_per_task[t] + 1 > max_volunteers_per_job[t]) {
//                        itJobs.remove();
//                        continue restart;
//                    }
//                    continue;
//                }
//            }
//        } while (presourcedLeft.size()!=amountLeft);
//
//        return assignment_matrix;
//    }

    private int[] feasibleSolutionPresourcingV3(){
        return null;
    }

    public int[] feasibleSolution() {
        int[] assignment_matrix;
        if (presourcing) {
            System.out.println("Problem has presourced volunteers");
            assignment_matrix = feasibleSolutionPresourcingV3();
        } else {
            System.out.println("Problem has no presourced volunteers");
            assignment_matrix = feasibleSolutionNoPresourcing();
        }
        if (!isValidInt(assignment_matrix)) {
            System.out.println("solution isn't feasible");

        }
        return assignment_matrix;
    }

    public boolean isValidInt(int[] assigments) {
        boolean print = true;
        int males = 0;
        int total = 0;
        int[] vol_per_task = new int[T];
        int[][] qual_per_skill = new int[T][S];

        for (int v = 0; v < V; v++) {
            Volunteer volunteer = volunteers[v];
            //presourced is required job
            if (volunteer.isPresourced && assigments[v] == -1) {
                if (print) System.out.println("Volunteer " + v + " is presourced and didnt get job");
                else return false;
            }
            if (assigments[v] != -1) {
                total++;
                males += volunteer.isMale ? 1 : 0;

                vol_per_task[assigments[v]]++;
                //qualified is 1 if: distance in prefered, tasktype>0, enough days, if skill for proportion 1 is met
                if (qualified[v][assigments[v]] == 0) {
                    if (print) System.out.println("volunteer " + v + " isn't qualified");
                    else return false;
                }
                //hard req
                for (int s = 0; s < S; s++) {
                    SkillRequirement skillRequirement = tasks[assigments[v]].skillRequirements[s];
                    if (skillRequirement == null) continue;
                    if (!skillRequirement.isHard) {
                        continue;
                    }
                    if (volunteer.skills[s] >= skillRequirement.minProficiency) {
                        qual_per_skill[assigments[v]][s] += 1;
                    }
                }
            }
        }

        for (int t = 0; t < T; t++) {
            SkillRequirement[] taak = tasks[t].skillRequirements;
            for (int s = 0; s < S; s++) {
                if (taak[s] == null) continue;
                if (!taak[s].isHard) continue;
                if (taak[s].proportion > (double) qual_per_skill[t][s] / vol_per_task[t]) {
                    if (print) {
                        System.out.println("task " + t);
                        System.out.println("skillproportion " + skills.get(s) + " requires proportion " + taak[s].proportion + " but only has " + (double) qual_per_skill[t][s] / vol_per_task[t]);
                    } else return false;
                }
            }
        }

        //gender distributie
        if ((double) males / total > 0.55 || (double) males / total < 0.45) {
            if (print) System.out.println("Solution gender distribution not correct: " + (double) males / total);
            else return false;
        }
        //max aantal per taak
        for (int t = 0; t < T; t++) {
            if (vol_per_task[t] > max_volunteers_per_job[t]) {
                if (print) System.out.println("Job " + t + " is overflowing");
                else return false;
            }
        }
        return true;
    }

    public int optFunction0() {
        return total;
    }

    public int optFunction1() {
        int score = 0;
        int total = 0;
        int man = 0;
        for (int v = 0; v < V; v++) {
            if (assignment_matrix[v] > 0) {
                total++;
                if (volunteers[v].isMale) man++;
                Task taak = tasks[assignment_matrix[v]];

                score += (int) (travelDistanceWeight * 2 * taak.days * distance_matrix[v][assignment_matrix[v]]);
                score -= (int) (taskTypeAdequacyWeight
                        * volunteers[v].taskTypes[taak.taskTypeIndex]);

                for (int s = 0; s < S; s++) {
                    SkillRequirement skill = taak.skillRequirements[s];
                    if (skill == null) continue;
                    if (skill.isHard) continue;
                    score += (int) skill.weight;
                }
            }
        }
        score += (int) (genderBalanceWeight * Math.abs(total - 2 * man));


        return score;
    }

    public void write(instanceReader reader, int[] initialSolution) throws IOException {
        int total = optFunction0();
        int score = optFunction1();
        reader.startSolution(total, score);
        for (int i = 0; i < V; i++) {
            if (assignment_matrix[i] < 0) continue;
            reader.addAssignment(volunteers[i].id, tasks[assignment_matrix[i]].id);
        }
        reader.writeSolution();
    }
}

