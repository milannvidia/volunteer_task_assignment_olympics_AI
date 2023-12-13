package Model;

import org.json.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class instanceReader {

    private File instance;
    private String outputPath;
    private boolean writeproblem;

    public instanceReader(String input, String output,boolean writeproblem) {
        instance = new File(input);
        outputPath=output;
        this.writeproblem=writeproblem;
    }
    public JSONObject solution;

    public void startSolution(double value, double value1) {
        solution=new JSONObject();
        solution.put("assignedVolunteers",value);
        solution.put("assignmentCost",value1);
        solution.put("assignments", new JSONArray());
    }

    public void addAssignment(String id, String id1) {
        JSONObject assignment=new JSONObject();
        assignment.put("volunteerId",id);
        assignment.put("taskId",id1);
        solution.getJSONArray("assignments").put(assignment);
    }
    public void writeSolution() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("solutionCheck/solution.json"));
        writer.write(solution.toString());
        writer.close();
    }
    public void makeProblem(String problem) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("solutionCheck/problem.json"));
        writer.write(problem);
        writer.close();
    }

    public Instance getInstance() throws IOException {
        String JsonString= Files.readString(instance.toPath());
        if(writeproblem) makeProblem(JsonString);

        JSONObject objTarget= new JSONObject(JsonString);
        JSONObject weights=objTarget.getJSONObject("weights");

        //skills
        JSONArray JSONskills= objTarget.getJSONArray("skills");
        ArrayList<String> skills=new ArrayList<>();
        for (Object y:JSONskills) {
            skills.add(y.toString());
        }

        //tasktypes
        JSONArray JSONtaskTypes= objTarget.getJSONArray("taskTypes");
        ArrayList<String> taskTypes=new ArrayList<>();
        for (Object y:JSONtaskTypes) {
            taskTypes.add(y.toString());
        }

        //locations
        JSONArray JSONlocations= objTarget.getJSONArray("locations");
        HashMap<String, Location> locations=new HashMap<>();
        for (int i = 0; i < JSONlocations.length(); i++) {
            JSONObject x=JSONlocations.getJSONObject(i);
            locations.put(x.getString("id"),new Location(
                    x.getDouble("lat"),
                    x.getDouble("lon")
            ));
        }

        //tasks
        JSONArray JSONtasks= objTarget.getJSONArray("tasks");
        ArrayList<Task> tasks=new ArrayList<>();
        for (int i = 0; i < JSONtasks.length(); i++) {
            JSONObject x=JSONtasks.getJSONObject(i);

            HashMap<String, SkillRequirement> reqArray=new HashMap<>();
            JSONArray skillreq=x.getJSONArray("skillRequirements");
            for (int j = 0; j < skillreq.length(); j++) {
                JSONObject req =skillreq.getJSONObject(j);
                reqArray.put(req.getString("skillId"),new SkillRequirement(
                        req.getString("skillId"),
                        req.getInt("minProficiency"),
                        req.getBoolean("isHard"),
                        req.getDouble("proportion"),
                        req.getDouble("weight")
                ));

            }
            tasks.add(new Task(
                    x.getString("id"),
                    locations.get(x.getString("locationId")),
                    x.getInt("demand"),
                    x.getInt("days"),
                    x.getString("taskTypeId"),
                    reqArray,
                    skills,
                    taskTypes
            ));
        }

        //volunteers
        JSONArray JSONvolunteers= objTarget.getJSONArray("volunteers");
        ArrayList<Volunteer> volunteers=new ArrayList<>(JSONvolunteers.length());
        Boolean presourcing=false;
        for (int i = 0; i < JSONvolunteers.length(); i++) {

            JSONObject volunteerJSON= JSONvolunteers.getJSONObject(i);
            ArrayList<Location> prefLoc=new ArrayList<>();
            for (Object y:volunteerJSON.getJSONArray("preferredLocationIds")) {
                prefLoc.add(locations.get(y.toString()));
            }

            HashMap<String, Integer> volunteerSkills=new HashMap<String, Integer>();
            JSONObject JSONVolunteerSkills = volunteerJSON.getJSONObject("skills");
            Iterator<?> keys=JSONVolunteerSkills.keys();
            while(keys.hasNext()){
                String key= (String) keys.next();
                volunteerSkills.put(key,JSONVolunteerSkills.getInt(key));
            }

            HashMap<String, Integer> taskSkills=new HashMap<String, Integer>();
            JSONObject JSONVolunteerTask = volunteerJSON.getJSONObject("taskTypes");
            Iterator<?> keysTask=JSONVolunteerTask.keys();
            while(keysTask.hasNext()){
                String key= (String) keysTask.next();
                taskSkills.put(key,JSONVolunteerTask.getInt(key));
            }
            if(volunteerJSON.getBoolean("isPresourced")) presourcing=true;
            volunteers.add(new Volunteer(
                    volunteerJSON.getString("id"),
                    volunteerJSON.getBoolean("isMale"),
                    volunteerJSON.getBoolean("isPresourced"),
                    locations.get(volunteerJSON.getString("locationId")),
                    prefLoc,
                    volunteerJSON.getInt("availableDays"),
                    volunteerSkills,
                    taskSkills,
                    skills,
                    taskTypes
            ));
        }


        return new Instance(
                weights.getInt("travelDistanceWeight"),
                weights.getInt("genderBalanceWeight"),
                weights.getInt("taskTypeAdequacyWeight"),
                skills,
                taskTypes,
                locations,
                tasks,
                volunteers,
                presourcing

        );





    }

}
