package Model;

public class SkillRequirement {
    public String skill_id;
    public int minProficiency;
    public boolean isHard;
    public double proportion;
    public double weight;

    public SkillRequirement(String skill_id,int minProficiency, boolean isHard, double proportion, double weight) {
        this.skill_id=skill_id;
        this.minProficiency=minProficiency;
        this.isHard=isHard;
        this.proportion=proportion;
        this.weight=weight;
    }
}
