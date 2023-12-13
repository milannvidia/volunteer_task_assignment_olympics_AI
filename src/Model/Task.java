package Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Task {
    public String id;
    public Location location;
    public int demand;
    public int days;
//    public String taskTypeId;
    public int taskTypeIndex;
    public SkillRequirement[] skillRequirements;
    public ArrayList<Volunteer> volunteers;
    public ArrayList<Volunteer>[] qualPerSkill;
    public int[] qual_per_skill_assigned;

    public int[] neededPerSkill;
    public Task(String id, Location location, int demand, int days, String taskTypeId,
                HashMap<String, SkillRequirement> reqArray,
                ArrayList<String> skills, ArrayList<String> taskTypes) {
        this.id=id;
        this.location=location;
        this.demand=demand;
        this.days=days;
//        this.taskTypeId=taskTypeId;
        skillRequirements=new SkillRequirement[skills.size()];
        qualPerSkill=new ArrayList[skills.size()];
        neededPerSkill =new int[skills.size()];
        for (int i = 0; i < skills.size(); i++) {
            skillRequirements[i]=reqArray.get(skills.get(i));
            if(skillRequirements[i]==null)continue;
            if(!skillRequirements[i].isHard)continue;
            qualPerSkill[i]=new ArrayList<>();
            neededPerSkill[i]=(int)Math.ceil(skillRequirements[i].proportion*this.demand);
        }
        taskTypeIndex=taskTypes.indexOf(taskTypeId);
        volunteers=new ArrayList<>(demand);
        qual_per_skill_assigned =new int[skills.size()];


    }

    public boolean addVolunteer(Volunteer v,boolean skillcheck) {
        if(this.volunteers.size()>=this.demand)return false;
        if(skillcheck){
            if(!skillcheck(v))return false;
        }
        if(!v.assignTask(this))return false;
        this.volunteers.add(v);
        for (int s = 0; s < skillRequirements.length; s++) {
            SkillRequirement skillRequirement = this.skillRequirements[s];
            if (skillRequirement == null) continue;
            if (!skillRequirement.isHard) {
                continue;
            }
            if (v.skills[s] >= skillRequirement.minProficiency) {
                qual_per_skill_assigned[s]++;
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
            if (v.skills[s] < skillRequirement.minProficiency) {
                if (skillRequirement.proportion > (double) (qual_per_skill_assigned[s]) / (this.volunteers.size() + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean skillcheckV2(Volunteer V){


        return false;
    }

    public boolean skillcheck(boolean print) {
        if(volunteers.isEmpty())return true;
        for(int s=0;s<skillRequirements.length;s++){
            if(skillRequirements[s]==null)continue;
            if(!skillRequirements[s].isHard)continue;
            if(qual_per_skill_assigned[s]<(skillRequirements[s].proportion*volunteers.size())) {
                if(print) System.out.println(this.id+" is insufficient in "+ skillRequirements[s].skill_id);
                else return false;
            }
        }
        return true;
    }

    public void reset() {
        Arrays.fill(qual_per_skill_assigned,0);
        for (Volunteer v:volunteers) {
            v.assignment=null;
        }
        volunteers.clear();
    }

    public void sortVolunteer(Volunteer volunteer) {
        for (int s = 0; s < skillRequirements.length; s++) {
            if(skillRequirements[s]==null)continue;
            if(!skillRequirements[s].isHard)continue;
            if(skillRequirements[s].minProficiency>volunteer.skills[s])continue;
            qualPerSkill[s].add(volunteer);
        }

    }
    public int scoreVolunteer(Volunteer v){
        int score=0;
        for (int s = 0; s < skillRequirements.length; s++) {
            if(skillRequirements[s]==null)continue;
            if(!skillRequirements[s].isHard)continue;
            if(skillRequirements[s].minProficiency>v.skills[s])continue;
            score++;
        }
        return score;
    }

    public int getCountVolunteerLeast(boolean onlyPresourced) {
        int score=Integer.MAX_VALUE;
        if(onlyPresourced){
            for (int i = 0; i < qualPerSkill.length; i++) {
                if(qualPerSkill[i]==null)continue;
                int needed=(int)Math.ceil(skillRequirements[i].proportion*this.demand);
                needed-=qual_per_skill_assigned[i];
                if(needed<=0)continue;

                int count=0;
                for (Volunteer v:qualPerSkill[i]) {
                    if(v.isPresourced)count++;
                }
                if(count<score)score=count;
            }
        }else{
            for (int i = 0; i < qualPerSkill.length; i++) {
                if(qualPerSkill[i]==null)continue;
                int needed=(int)Math.ceil(skillRequirements[i].proportion*this.demand);
                needed-=qual_per_skill_assigned[i];
                if(needed<=0)continue;

                if(qualPerSkill[i].size()<score)score=qualPerSkill[i].size();
            }
        }

        return score;
    }

    public boolean assignMostNeeded(boolean onlyPresourced,boolean skillcheck) {
        int index=-1;
        int score=Integer.MAX_VALUE;
        //first check for all volunteers that need two or more skills because demand too low

        //todo
        //check for the skill with least possible but skip the skills which are filled
        for (int i = 0; i < qualPerSkill.length; i++) {
            if(qualPerSkill[i]==null)continue;
            int needed=neededPerSkill[i];
            needed-=qual_per_skill_assigned[i];
            if(needed<=0)continue;

            if(qualPerSkill[i].size()<score) {
                score = qualPerSkill[i].size();
                index=i;
            }
        }

        if(index<0) {
            return false;
        }
        int needed=neededPerSkill[index];
        needed-=qual_per_skill_assigned[index];

        qualPerSkill[index].sort(Comparator.comparingInt((Volunteer a)->-1*this.scoreVolunteer(a)));
        for (Volunteer v:qualPerSkill[index]) {
            if(!v.isPresourced&&onlyPresourced)continue;
            if(this.addVolunteer(v,skillcheck)){
                needed--;
            }
            if(needed<=0)return true;
        }

        return false;
    }
}
