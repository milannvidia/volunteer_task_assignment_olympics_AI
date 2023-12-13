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
            for (Task t : Instance.tasks) {
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
                        if (skillTask.minProficiency > skillVolunteer) {
                            continue task;
                        }
                    }
                }
                v.addQualified(t);
            }
            v.sortTasks();
        }
        //clean up tasks and remove in volunteer qualified tasks
        for (Iterator<Task> it = Instance.tasks.iterator(); it.hasNext(); ) {
            if (it.next().getCountVolunteerLeast(false) == 0) it.remove();
        }
        for (Volunteer v : volunteers) {
            for (Iterator<Task> it = v.qualifiedTasks.iterator(); it.hasNext(); ) {
                if (!tasks.contains(it.next())) it.remove();
            }
        }

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
                    if (((males + 1) / (total + 1)) > 0.55) {
                        continue;
                    }
                } else {
                    if ((males / (total + 1)) < 0.45) {
                        continue;
                    }
                }
            }
            for (Task t : v.qualifiedTasks) {
                if (t.addVolunteer(v, true)) {
                    total += 1;
                    if (v.isMale) males += 1;
                    continue volunteer;
                }
            }
        }
    }

    private double[] assignArraylistRecursive(ArrayList<Volunteer> initVol) {
        boolean repeat = true;
//        while(repeat) {
        double males = 0;
        double total = 0;

        ArrayList<Volunteer> presourcedLeft = new ArrayList<>(initVol);
        ArrayList<Volunteer> toAddAfterwards = new ArrayList<>();
//        ArrayList<Volunteer> newInit = new ArrayList<>();

        int amounLeft = 0;
        while (amounLeft != presourcedLeft.size()) {
            amounLeft = presourcedLeft.size();
            volunteer:
            for (Iterator<Volunteer> it = presourcedLeft.iterator(); it.hasNext(); ) {
                Volunteer v = it.next();
                for (Task t : v.qualifiedTasks) {
                    if (t.addVolunteer(v, false)) {
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
                    if (t.addVolunteer(v, false)) {
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
        }

        return new double[]{0, 0};

    }

    private void feasibleSolutionPresourcing() {
        ArrayList<Volunteer> presourced = new ArrayList<>();
        for (Volunteer v : volunteers) {
            if (v.isPresourced) presourced.add(v);
        }

        //
        while (!presourced.isEmpty()) {
            int amountleft=0;
            while(presourced.size()!=amountleft){
                amountleft=presourced.size();

                tasks.sort(Comparator.comparingInt((Task a) -> a.getCountVolunteerLeast(true)));
                for (Task t : tasks) {
                    t.assignMostNeeded(true,true);
                }

                presourced.removeIf(v -> v.assignment != null);
            }
            volunteer:
            for(Iterator<Volunteer> it=presourced.iterator(); it.hasNext();){
                Volunteer v=it.next();
                for (Task t:v.qualifiedTasks) {
                    if(t.addVolunteer(v,true)){
                        it.remove();
                        continue volunteer;
                    }
                }
            }
        }
        double males=0;
        double total=0;
        for(Volunteer v:volunteers){
            if(v.assignment==null)continue;
            if(v.isMale)males++;
            total++;
        }
        while(males/total>0.55||males/total<0.45){
            volunteer:
            for (Volunteer v : volunteers) {
                if(v.assignment!=null)continue;
                if (v.isMale) {
                    if (((males + 1) / (total + 1)) > 0.55) {
                        continue;
                    }
                } else {
                    if ((males / (total + 1)) < 0.45) {
                        continue;
                    }
                }

                for (Task t : v.qualifiedTasks) {
                    if (t.addVolunteer(v, true)) {
                        total += 1;
                        if (v.isMale) males += 1;
                        continue volunteer;
                    }
                }
            }
        }

//        boolean check=true;
//        while(check){
//            tasks.sort(Comparator.comparingInt((Task a) -> a.getCountVolunteerLeast(false)));
//            for (Task t : tasks) {
//                if(t.volunteers.isEmpty())continue;
//                t.assignMostNeeded(false,false);
//            }
//            check=false;
//            for (Task t:tasks) {
//                if(!t.skillcheck(false)){
////                    check=true;
//                    break;
//                }
//            }
//        }
    }


    public void feasibleSolution() {
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
        double gender = (males / total);
        if (gender > 0.55) {
            if(print)System.out.println("too many men: " + gender);
            else return false;
        }
        if (gender < 0.45) {
            if(print)System.out.println("too many women: " + gender);
            else return false;
        }
        for (Task t : tasks) {
            if (!t.skillcheck(false)) {
                if (print) t.skillcheck(true);
                else return false;
            }
        }
        if (print) return false;
        return true;
    }

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

