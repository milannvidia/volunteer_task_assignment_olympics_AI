package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Volunteer {
    public int[] taskTypes;
    public int[] skills;
    public String id;
    public boolean isMale;
    public boolean isPresourced;
    public Location location;
    public ArrayList<Location> preferredLocationIds;
    public int availableDays;

    public ArrayList<Task> qualifiedTasks=new ArrayList<>();

    public Task assignment=null;

    public Volunteer(String id, boolean isMale, boolean isPresourced, Location location, ArrayList<Location> prefLoc, int availableDays,
                     HashMap<String, Integer> hashSkills, HashMap<String, Integer> taskSkills,
                     ArrayList<String> skills, ArrayList<String> taskTypes) {
        this.id=id;
        this.isMale=isMale;
        this.isPresourced=isPresourced;
        this.location =location;
        this.preferredLocationIds=prefLoc;
        this.availableDays=availableDays;

        this.skills=new int[skills.size()];
        for (int i = 0; i < skills.size(); i++) {
            this.skills[i]=hashSkills.get(skills.get(i))==null?0:hashSkills.get(skills.get(i));
        }

        this.taskTypes=new int[taskTypes.size()];
        for (int i = 0; i < taskTypes.size(); i++) {
            this.taskTypes[i]=taskSkills.get(taskTypes.get(i));
        }
    }


    public void addQualified(Task t) {
        qualifiedTasks.add(t);
    }
    public boolean assignTask(Task t){
        if(assignment==null){
            assignment=t;
            return true;
        }else{
            return false;
        }
    }

//    public boolean reassign(Task task) {
//        for (Task t: qualifiedTasks) {
//            if(t==task)continue;
//            if(t.addVolunteer(this)){
//                return true;
//            }
//
//        }
//        return false;
//    }

//    public boolean solveProblem(boolean checkSkill) {
//        for (Task t:qualifiedTasks) {
//            if(t.solve(this,checkSkill))return true;
//        }
//        return false;
//    }

//    public boolean reassign(Task task, boolean checkSkill) {
//        for (Task t: qualifiedTasks) {
//            if(t==task)continue;
//            if(checkSkill){
//                if(t.addVolunteer(this)){
//                    return true;
//                }
//            }else{
//                if(t.addVolunteerNoSkill(this)){
//                    return true;
//                }
//            }
//
//        }
//        return false;
//    }
}
