import Model.Instance;
import Model.instanceReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {

//        instanceReader reader=new instanceReader(new File("problems/i0_200t_5000v.json"));
//        instanceReader reader=new instanceReader(new File("problems/i1_600t_40000v.json"));
//        instanceReader reader=new instanceReader(new File("problems/i2_781t_40000v.json"));
//        instanceReader reader=new instanceReader(new File("problems/i3_781t_100000v.json"));
//        instanceReader reader=new instanceReader(new File("problems/i4_781t_140765v.json"));
//        instanceReader reader=new instanceReader(new File("problems/p0_200t_5000v.json"));
//        instanceReader reader=new instanceReader(new File("problems/p2_781t_40000v.json"));
        instanceReader reader=new instanceReader(new File("problems/p4_781t_140765v.json"));
//        instanceReader reader=new instanceReader(new File("toy_problem.json"));
        Instance instance= reader.getInstance();
//        instance.solveGurobi(reader);
//        BufferedWriter writer = new BufferedWriter(new FileWriter("toy_problem_solution.json"));
//        writer.write(reader.solution.toString());
//        writer.close();

        int[] initialSolution=instance.feasibleSolution();

        instance.write(reader,initialSolution);


    }
}