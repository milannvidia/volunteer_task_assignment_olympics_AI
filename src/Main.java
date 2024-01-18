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
            String[] inputs={
                    "i0_200t_5000v","i1_600t_40000v",
                    "i2_781t_40000v","i3_781t_100000v",
                    "i4_781t_140765v","p0_200t_5000v",
                    "p2_781t_40000v","p4_781t_140765v"};
            for (String input:inputs){
                reader=new instanceReader("problems/"+input+".json","solutionCheck/solution.json",false);
                Instance instance = reader.getInstance();
                Long t0=System.currentTimeMillis();
                instance.feasibleSolution();

                System.out.println(input+" "+(System.currentTimeMillis()-t0)+" "+instance.optFunction()[0]);
                instance.write(reader);
            }
        }else
        {
            reader = new instanceReader(args[0], args[1],false);
            Instance instance = reader.getInstance();
            instance.feasibleSolution();
            instance.write(reader);
        }






    }
}