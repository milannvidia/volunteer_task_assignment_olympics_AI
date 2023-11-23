package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Task {
    public String id;
    public String locationId;
    public int demand;
    public int days;
    public String taskTypeId;
    public int taskTypeIndex;
    public SkillRequirement[] skillRequirements;

    public Task(String id, String locationId, int demand, int days, String taskTypeId,
                HashMap<String, SkillRequirement> reqArray,
                ArrayList<String> skills, ArrayList<String> taskTypes) {
        this.id=id;
        this.locationId=locationId;
        this.demand=demand;
        this.days=days;
        this.taskTypeId=taskTypeId;
        skillRequirements=new SkillRequirement[skills.size()];
        for (int i = 0; i < skills.size(); i++) {
            skillRequirements[i]=reqArray.get(skills.get(i));
        }
        taskTypeIndex=taskTypes.indexOf(taskTypeId);

    }
}
