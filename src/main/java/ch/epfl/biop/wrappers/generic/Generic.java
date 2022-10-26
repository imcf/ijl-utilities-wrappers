package ch.epfl.biop.wrappers.generic;


import ij.IJ;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.io.File.separatorChar;

public class Generic {

    static void execute(String setEnvDir, List<String> commandTool, Consumer<InputStream> outputHandler) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        List<String> start_cmd = null ;

        // start terminal
        if (IJ.isWindows()) {
            start_cmd=  Arrays.asList("cmd.exe", "/C");
         } else if ( IJ.isMacOSX() || IJ.isLinux()) {
            start_cmd = Arrays.asList("bash", "-c");
        }
        cmd.addAll( start_cmd );

        List<String> activate_cmd = null;
        if (IJ.isWindows()) {
            // Activate the conda env
            activate_cmd = Arrays.asList("CALL", "conda.bat", "activate", setEnvDir);
            cmd.addAll(activate_cmd);
            // After starting the env we can now use cellpose
            cmd.add("&");// to have a second command
            // input options
            cmd.addAll(commandTool);

        } else if ( IJ.isMacOSX() || IJ.isLinux()) {
            // instead of conda activate (so much headache!!!) specify the python to use
            String python_path = setEnvDir+separatorChar+"bin"+separatorChar+"python";
            List<String> cellpose_args_cmd = new ArrayList<>(Arrays.asList( python_path ));
            activate_cmd.addAll(commandTool);

            // convert to a string
            activate_cmd = activate_cmd.stream().map(s -> {
                if (s.trim().contains(" "))
                    return "\"" + s.trim() + "\"";
                return s;
            }).collect(Collectors.toList());
            // The last part needs to be sent as a single string, otherwise it does not run
            String cmdString = activate_cmd.toString().replace(",","");

            // finally add to cmd
            cmd.add(cmdString.substring(1, cmdString.length()-1));
        }


        System.out.println(cmd.toString().replace(",", ""));
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);

        Process p = pb.start();
        Thread t = new Thread(Thread.currentThread().getName() + "-" + p.hashCode()) {
            @Override
            public void run() {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
                try {
                    for (String line = stdIn.readLine(); line != null; ) {
                        System.out.println(line);
                        line = stdIn.readLine();// you don't want to remove or comment that line! no you don't :P
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        t.setDaemon(true);
        t.start();

        p.waitFor();
        int exitValue = p.exitValue();

        if (exitValue != 0) {
            System.out.println("Runner " + setEnvDir + " exited with value " + exitValue + ". Please check output above for indications of the problem.");
        } else {
            System.out.println(setEnvDir + " run finished");
        }

    }

    /*
    public static void execute(String singleCommand) throws IOException, InterruptedException {
        ArrayList<String> cmdList = new ArrayList<>();
        cmdList.add(singleCommand);
        execute(cmdList, null);
    }*/

}
