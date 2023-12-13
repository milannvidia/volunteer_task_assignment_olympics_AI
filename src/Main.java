import Model.Instance;
import Model.instanceReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {

        instanceReader reader;
        if (args.length == 0) {
//         reader=new instanceReader("problems/i0_200t_5000v.json","solutionCheck/solution.json",true);
//         reader=new instanceReader("problems/i1_600t_40000v.json","solutionCheck/solution.json",true);
//         reader=new instanceReader("problems/i2_781t_40000v.json","solutionCheck/solution.json",true);
//         reader=new instanceReader("problems/i3_781t_100000v.json","solutionCheck/solution.json",true);
//         reader=new instanceReader("problems/i4_781t_140765v.json","solutionCheck/solution.json",true);
//         reader=new instanceReader("problems/p0_200t_5000v.json","solutionCheck/solution.json",true);
//         reader=new instanceReader("problems/p2_781t_40000v.json","solutionCheck/solution.json",true);
         reader = new instanceReader("problems/p4_781t_140765v.json", "solutionCheck/solution.json",true);
//         instanceReader reader=new instanceReader("problems/toy_problem.json","solutionCheck/solution.json");
        }else
        {
            reader = new instanceReader(args[0], args[1],false);
        }


        Instance instance = reader.getInstance();
        instance.feasibleSolution();
        instance.write(reader);


    }
}