package Model;

import java.io.IOException;
import java.util.*;

public class Instance {
    private static double travelDistanceWeight;
    private static double genderBalanceWeight;
    private static double taskTypeAdequacyWeight;
    private static ArrayList<String> skills;
    private static ArrayList<Task> tasks;
    private static ArrayList<Volunteer> volunteers;
    private static boolean presourcing;
    private static int S;

//    private int BestScore0 = 0;
//    private int BestScore1 = Integer.MAX_VALUE;

    public Instance(int travelDistanceWeight, int genderBalanceWeight, int taskTypeAdequacyWeight, ArrayList<String> skills, ArrayList<String> taskTypes, HashMap<String, Location> locations, ArrayList<Task> tasks, ArrayList<Volunteer> volunteers, Boolean presourcing) {
        Instance.travelDistanceWeight = travelDistanceWeight;
        Instance.genderBalanceWeight = genderBalanceWeight;
        Instance.taskTypeAdequacyWeight = taskTypeAdequacyWeight;

        Instance.skills = skills;
        Instance.tasks = tasks;
        Instance.volunteers = volunteers;
        Instance.presourcing = presourcing;
        S = skills.size();
        for (Volunteer v : volunteers) {
            task:
            for (Task t : tasks) {
                //qualified continue so qualified stays 0
                //if is preferred location
                if (!v.preferredLocationIds.contains(t.location)) {
                    continue;
                }

                //not feasible if not experienced in tasktype
                int volunteer_tasktype_skill = v.taskTypes[t.taskTypeIndex];
                if (volunteer_tasktype_skill == 0) {
                    continue;
                }
                //can the volunteer work all days
                if (v.availableDays < t.days) {
                    continue;
                }
                //check for each skill that is proportion 100 if qualifies
                for (int s = 0; s < S; s++) {
                    SkillRequirement skillTask = t.skillRequirements[s];
                    if (skillTask == null) continue;
                    int skillVolunteer = v.skills[s];

                    if (skillTask.proportion == 1) {
                        if (skillTask.minProficiency >= skillVolunteer) {
                            continue task;
                        }
                    }
                }

                v.addQualified(t);
            }

        }

    }

    public Instance() {


    }

    private double distance(Location location, Location location1) {
        return distance(location.lon, location.lat, location1.lon, location1.lat);
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


    private void feasibleSolutionNoPresourcing() {
        double males = 0;
        double total = 0;

        volunteer:
        for (Volunteer v : volunteers) {
            if (total > 20) {
                if (v.isMale) {
                    if (((males+1) / (total+1)) > 0.55) {
                        continue;
                    }
                }else{
                    if ((males / (total+1)) < 0.45) {
                        continue;
                    }
                }
            }
            for (Task t : v.qualifiedTasks) {
                if (t.addVolunteer(v)) {
                    total += 1;
                    if (v.isMale) males += 1;
                    continue volunteer;
                }
            }
        }
    }


    private boolean assignArraylistRecursiveV2(ArrayList<Volunteer> allPre){
        ArrayList<Volunteer> presourcedLeft=new ArrayList<>(allPre);

        int amounLeft = 0;
        while (amounLeft != presourcedLeft.size()) {
            amounLeft = presourcedLeft.size();
            volunteer:
            for (Iterator<Volunteer> it = presourcedLeft.iterator(); it.hasNext(); ) {
                Volunteer v = it.next();
                for (Task t : v.qualifiedTasks) {
                    if (t.addVolunteerNoSkill(v)) {
                        it.remove();
                        continue volunteer;
                    }
                }
            }
        }


        return true;
    }


    private double[] assignArraylistRecursive(ArrayList<Volunteer> initVol){
        boolean repeat=true;
//        while(repeat) {
            double males = 0;
            double total = 0;

            ArrayList<Volunteer> presourcedLeft = new ArrayList<>(initVol);
            ArrayList<Volunteer> toAddAfterwards = new ArrayList<>();
            ArrayList<Volunteer> newInit = new ArrayList<>();

            int amounLeft = 0;
            while (amounLeft != presourcedLeft.size()) {
                amounLeft = presourcedLeft.size();
                volunteer:
                for (Iterator<Volunteer> it = presourcedLeft.iterator(); it.hasNext(); ) {
                    Volunteer v = it.next();
                    for (Task t : v.qualifiedTasks) {
                        if (t.addVolunteer(v)) {
                            total += 1;
                            if (v.isMale) males += 1;
                            toAddAfterwards.add(v);
                            it.remove();
                            continue volunteer;
                        }
                    }
                }
            }

            if (presourcedLeft.isEmpty()) return new double[]{males, total};


            //if fails focus on smaller group
            for (Task t : tasks) {
                t.reset();
            }
            double[] gender = assignArraylistRecursive(presourcedLeft);

            amounLeft = 0;
            while (amounLeft != toAddAfterwards.size()) {
                amounLeft = toAddAfterwards.size();
                volunteer:
                for (Iterator<Volunteer> it = toAddAfterwards.iterator(); it.hasNext(); ) {
                    Volunteer v = it.next();
                    for (Task t : v.qualifiedTasks) {
                        if (t.addVolunteer(v)) {
                            gender[1] += 1;
                            if (v.isMale) gender[0] += 1;
//                            newInit.add(v);
                            it.remove();
                            continue volunteer;
                        }
                    }
                }
            }

            if (toAddAfterwards.isEmpty()) return gender;
            else {
                for (Task t : tasks) {
                    t.reset();
                }

//                Collections.shuffle(newInit);
//                Collections.shuffle(toAddAfterwards);
//                Collections.shuffle(presourcedLeft);

                newInit.addAll(toAddAfterwards);
                newInit.addAll(presourcedLeft);
//                Collections.reverse(newInit);
                Collections.shuffle(newInit);

                double[] test=assignArraylistRecursive(newInit);
                System.out.println(test);
            }

//        }

        return new double[]{0, 0};

    }

    private void feasibleSolutionPresourcing() {

        ArrayList<Volunteer> presourcedLeft=new ArrayList<>();
        for(Volunteer v:volunteers){
            if(v.isPresourced)presourcedLeft.add(v);
        }

        assignArraylistRecursiveV2(presourcedLeft);

        presourcedLeft.clear();
        for(Volunteer v:volunteers){
            if(v.assignment!=null)continue;
            if(v.isPresourced)presourcedLeft.add(v);
        }

        System.out.println(presourcedLeft.size());
//        int amountleft=0;
//        while(presourcedLeft.size()!=amountleft){
//            amountleft=presourcedLeft.size();
//            volunteer:
//            for (Iterator<Volunteer> it=presourcedLeft.iterator(); it.hasNext();){
//                Volunteer v=it.next();
//                for(Task t:v.qualifiedTasks){
//                    if(t.makeSpace(v)){
//                        if(v.isMale)gender[0]++;
//                        gender[1]++;
//                        it.remove();
//                        continue volunteer;
//                    }
//                }
//
//            }
//        }

//        while(gender[0]/gender[1]>0.55){
//            volunteer:
//            for (Volunteer v:volunteers) {
//                if(v.isMale)continue;
//                if(v.isPresourced)continue;
//                if(v.assignment!=null)continue;
//                for (Task t : v.qualifiedTasks) {
//                    if (t.addVolunteer(v)) {
//                        gender[1] += 1;
//                        break volunteer;
//                    }
//                }
//            }
//        }
//        while(gender[0]/gender[1]<0.45){
//            volunteer:
//            for (Volunteer v:volunteers) {
//                if(!v.isMale)continue;
//                if(v.isPresourced)continue;
//                if(v.assignment!=null)continue;
//                for (Task t : v.qualifiedTasks) {
//                    if (t.addVolunteer(v)) {
//                        gender[1] += 1;
//                        gender[0] += 1;
//                        break volunteer;
//                    }
//                }
//            }
//        }
    }


    public void feasibleSolution() {
        int[] assignment_matrix;
        if (presourcing) {
            System.out.println("Problem has presourced volunteers");
            feasibleSolutionPresourcing();
        } else {
            System.out.println("Problem has no presourced volunteers");
            feasibleSolutionNoPresourcing();
        }
        if (isValid(true)) {
            System.out.println("klopt");
        }

    }

    public boolean isValid(boolean print) {
        double males = 0;
        double total = 0;
        //qualified check not necessary because volunteer only has qualified jobs
        for (Volunteer v : volunteers) {
            if (v.isPresourced && v.assignment == null) {
                if (print) System.out.println("Volunteer " + v + " is presourced and didnt get job");
                else return false;
            }
            if (v.assignment != null) {
                if (v.isMale) males++;
                total++;
            }
        }
        double gender=(males / total);
        if (gender > 0.55) {
            System.out.println("too many men: "+ gender);
            return false;
        }
        if (gender < 0.45) {
            System.out.println("too many women: "+gender);
            return false;
        }
        for (Task t : tasks) {
            if (!t.skillcheck()) {
                if (print) System.out.println("task " + t + " wrong skills");
                else return false;
            }
        }
        if(print)return false;
        return true;
    }

//    public boolean isValidInt(int[] assigments) {
//        boolean print = true;
//        int males = 0;
//        int total = 0;
//        int[] vol_per_task = new int[T];
//        int[][] qual_per_skill = new int[T][S];
//
//        for (int v = 0; v < V; v++) {
//            Volunteer volunteer = volunteers[v];
//            //presourced is required job
//            if (volunteer.isPresourced && assigments[v] == -1) {
//                if (print) System.out.println("Volunteer " + v + " is presourced and didnt get job");
//                else return false;
//            }
//            if (assigments[v] != -1) {
//                total++;
//                males += volunteer.isMale ? 1 : 0;
//
//                vol_per_task[assigments[v]]++;
//                //qualified is 1 if: distance in prefered, tasktype>0, enough days, if skill for proportion 1 is met
//                if (qualified[v][assigments[v]] == 0) {
//                    if (print) System.out.println("volunteer " + v + " isn't qualified");
//                    else return false;
//                }
//                //hard req
//                for (int s = 0; s < S; s++) {
//                    SkillRequirement skillRequirement = tasks[assigments[v]].skillRequirements[s];
//                    if (skillRequirement == null) continue;
//                    if (!skillRequirement.isHard) {
//                        continue;
//                    }
//                    if (volunteer.skills[s] >= skillRequirement.minProficiency) {
//                        qual_per_skill[assigments[v]][s] += 1;
//                    }
//                }
//            }
//        }
//
//        for (int t = 0; t < T; t++) {
//            SkillRequirement[] taak = tasks[t].skillRequirements;
//            for (int s = 0; s < S; s++) {
//                if (taak[s] == null) continue;
//                if (!taak[s].isHard) continue;
//                if (taak[s].proportion > (double) qual_per_skill[t][s] / vol_per_task[t]) {
//                    if (print) {
//                        System.out.println("task " + t);
//                        System.out.println("skillproportion " + skills.get(s) + " requires proportion " + taak[s].proportion + " but only has " + (double) qual_per_skill[t][s] / vol_per_task[t]);
//                    } else return false;
//                }
//            }
//        }
//
//        //gender distributie
//        if ((double) males / total > 0.55 || (double) males / total < 0.45) {
//            if (print) System.out.println("Solution gender distribution not correct: " + (double) males / total);
//            else return false;
//        }
//        //max aantal per taak
//        for (int t = 0; t < T; t++) {
//            if (vol_per_task[t] > max_volunteers_per_job[t]) {
//                if (print) System.out.println("Job " + t + " is overflowing");
//                else return false;
//            }
//        }
//        return true;
//    }

    public int optFunction0() {
        return 0;
    }

    public int optFunction1() {
        int score = 0;
        int total = 0;
        int man = 0;
        for (Volunteer v : volunteers) {
            if (v.assignment != null) {
                total++;
                if (v.isMale) man++;
                Task taak = v.assignment;

                score += (int) (travelDistanceWeight * 2 * taak.days * distance(v.location, taak.location));
                score -= (int) (taskTypeAdequacyWeight
                        * v.taskTypes[taak.taskTypeIndex]);

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


    public void write(instanceReader reader) throws IOException {
        int total = optFunction0();
        int score = optFunction1();
        reader.startSolution(total, score);
        for (Task t : tasks) {
            for (Volunteer v : t.volunteers) {
                reader.addAssignment(v.id, t.id);
            }
        }
        reader.writeSolution();
    }
}

