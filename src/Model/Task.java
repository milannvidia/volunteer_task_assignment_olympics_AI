package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Task {
    public String id;
    public Location location;
    public int demand;
    public int days;
    public String taskTypeId;
    public int taskTypeIndex;
    public SkillRequirement[] skillRequirements;
    public ArrayList<Volunteer> volunteers;
    public int[] qual_per_skill;
    public Task(String id, Location location, int demand, int days, String taskTypeId,
                HashMap<String, SkillRequirement> reqArray,
                ArrayList<String> skills, ArrayList<String> taskTypes) {
        this.id=id;
        this.location=location;
        this.demand=demand;
        this.days=days;
        this.taskTypeId=taskTypeId;
        skillRequirements=new SkillRequirement[skills.size()];
        for (int i = 0; i < skills.size(); i++) {
            skillRequirements[i]=reqArray.get(skills.get(i));
        }
        taskTypeIndex=taskTypes.indexOf(taskTypeId);
        volunteers=new ArrayList<>(demand);
        qual_per_skill=new int[skills.size()];

    }

    public boolean addVolunteer(Volunteer v,boolean reassign) {
        if(this.volunteers.size()>=this.demand)return false;
        if(!skillcheck(v))return false;
        if(!reassign){
            if(v.assignTask(this)) return false;
        }{

        }


        this.volunteers.add(v);
        for (int s = 0; s < skillRequirements.length; s++) {
            SkillRequirement skillRequirement = this.skillRequirements[s];
            if (skillRequirement == null) continue;
            if (!skillRequirement.isHard) {
                continue;
            }
            if (v.skills[s] >= skillRequirement.minProficiency) {
                qual_per_skill[s]++;
            }
        }
        return true;
    }
    public boolean addVolunteerNoSkill(Volunteer v) {
        if(this.volunteers.size()>=this.demand)return false;
        if(v.assignTask(this)) return false;

        this.volunteers.add(v);
        for (int s = 0; s < skillRequirements.length; s++) {
            SkillRequirement skillRequirement = this.skillRequirements[s];
            if (skillRequirement == null) continue;
            if (!skillRequirement.isHard) {
                continue;
            }
            if (v.skills[s] >= skillRequirement.minProficiency) {
                qual_per_skill[s]++;
            }
        }
        return true;
    }

    public boolean skillcheck(Volunteer v){

        for (int s = 0; s < skillRequirements.length; s++) {
            SkillRequirement skillRequirement = this.skillRequirements[s];
            if (skillRequirement == null) continue;
            if (!skillRequirement.isHard) {
                continue;
            }
            if (v.skills[s] <= skillRequirement.minProficiency) {
                if (skillRequirement.proportion > (double) (qual_per_skill[s]) / (this.volunteers.size() + 1)) {
                    return false;
                }
            }
        }
        return true;

    }

    public boolean skillcheck() {
        if(volunteers.isEmpty())return true;
        for(int s=0;s<skillRequirements.length;s++){
            if(skillRequirements[s]==null)continue;
            if((qual_per_skill[s]/(double)volunteers.size())<skillRequirements[s].proportion)return false;
        }
        return true;
    }

    public void reset() {
        for (Volunteer v:volunteers) {
            v.assignment=null;
        }
        volunteers.clear();
    }

    public boolean makeSpace(Volunteer volunteer) {
        if(this.volunteers.size()==demand){
            for (Volunteer v:this.volunteers) {
                if(v==volunteer)continue;
                if(v.reassign(this))return true;
            }
        }else{
            return true;
        }
        return false;
    }

//    public boolean solve(Volunteer volunteer, boolean checkSkill) {
//        if(this.volunteers.size()==demand){
//            for (Volunteer v:this.volunteers) {
//                if(v==volunteer)continue;
//                if(v.reassign(this,checkSkill))return true;
//            }
//        }
//        return false;
//    }
}
