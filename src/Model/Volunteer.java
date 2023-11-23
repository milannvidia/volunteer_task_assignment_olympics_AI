package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Volunteer {
    public int[] taskTypes;
    public int[] skills;
    public String id;
    public boolean isMale;
    public boolean isPresourced;
    public String locationId;
    public ArrayList<String> preferredLocationIds;
    public int availableDays;

    public Volunteer(String id, boolean isMale, boolean isPresourced, String locationId, ArrayList<String> prefLoc, int availableDays,
                     HashMap<String, Integer> hashSkills, HashMap<String, Integer> taskSkills,
                     ArrayList<String> skills, ArrayList<String> taskTypes) {
        this.id=id;
        this.isMale=isMale;
        this.isPresourced=isPresourced;
        this.locationId=locationId;
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
}
