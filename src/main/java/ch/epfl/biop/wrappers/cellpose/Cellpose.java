package ch.epfl.biop.wrappers.cellpose;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import ij.IJ;
import ij.Prefs;

public class Cellpose {

    public static String keyPrefix = Cellpose.class.getName() + ".";


    //static String defaultExePath = "C:/Users/username/.conda/envs/cellpose";
    static String defaultEnvDirPath = "cellpose";//"C:/Users/username/.conda/envs/cellpose"; ///Users/username/opt/anaconda3/envs/cellpose
    static String defaultEnvType = "conda";
    static boolean defaultUseGpu = true;
    static boolean defaultUseMxnet = false;
    static boolean defaultUseFastMode = false;
    static boolean defaultUseResample = false;
    static String defaultVersion = "0.7";

    public static String envDirPath = Prefs.get(keyPrefix + "envDirPath", defaultEnvDirPath);
    public static String envType = Prefs.get(keyPrefix + "envType", defaultEnvType);
    public static boolean useGpu = Prefs.get(keyPrefix + "useGpu", defaultUseGpu);
    public static boolean useMxnet = Prefs.get(keyPrefix + "useMxnet", defaultUseMxnet);
    public static boolean useFastMode = Prefs.get(keyPrefix + "useFastMode", defaultUseFastMode);
    public static boolean useResample = Prefs.get(keyPrefix + "useResample", defaultUseResample);
    public static String version = Prefs.get(keyPrefix + "version", defaultVersion);

    public static void setEnvDirPath(File f) {
        envDirPath = f.getAbsolutePath();
        Prefs.set(keyPrefix + "envDirPath", envDirPath);
    }

    public static void setEnvType(String envType) {
        Prefs.set(keyPrefix + "envType", envType);
    }

    public static void setUseGpu(boolean useGpu) {
        Prefs.set(keyPrefix + "useGpu", useGpu);
    }

    public static void setUseMxnet(boolean useMxnet) {
        Prefs.set(keyPrefix + "useMxnet", useMxnet);
    }

    public static void setUseFastMode(boolean useFastMode) {
        Prefs.set(keyPrefix + "useFastMode", useFastMode);
    }

    public static void setUseResample(boolean useResample) {
        Prefs.set(keyPrefix + "useResample", useResample);
    }

    public static void setVersion(String version) {
        Prefs.set(keyPrefix + "version", version);
    }

    private static final File NULL_FILE = new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));

    static void execute(List<String> options, Consumer<InputStream> outputHandler) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        List<String> terminal_cmd = new ArrayList<>();

        // Get the prefs about the env type
        String envType = Prefs.get(keyPrefix + "envType", Cellpose.envType);

        // Depending of the env type
        if (envType.equals("conda")) {
            List<String> conda_activate_cmd = null;

            if (IJ.isWindows()) {
                conda_activate_cmd = Arrays.asList("conda", "activate", envDirPath);
                cmd.addAll(conda_activate_cmd);
                // After starting the env we can now use cellpose
                cmd.add("&");// to have a second line

            } else if (IJ.isMacOSX()) {
                // https://docs.conda.io/projects/conda/en/4.6.1/user-guide/tasks/manage-environments.html#id2
                conda_activate_cmd = Arrays.asList("source", "activate", envDirPath); // tried conda activate without success
                cmd.addAll(conda_activate_cmd);
                cmd.add(";");
            } else if (IJ.isLinux()) {
                String username = System.getProperty("user.name");
                //System.out.println("username = " + username);
                conda_activate_cmd = Arrays.asList("source /home/" + username + "/anaconda3/etc/profile.d/conda.sh && conda activate", envDirPath); // something simpler/cleaner might exist but you know it works ;)
                cmd.addAll(conda_activate_cmd);
                cmd.add("&&");
            }
        } else if (envType.equals("venv")) { // venv
            // only windows tested so far
            if (IJ.isWindows()) {
                List<String> venv_activate_cmd = Arrays.asList(new File(envDirPath, "Scripts/activate").toString());
                cmd.addAll(venv_activate_cmd);

            } else if (IJ.isMacOSX() || IJ.isLinux()) {
                System.out.println("venv not supported for Linux or MacOSX (yet)");
                return;
            }
        } else {
            System.out.println("Virtual env type unrecognized!");
            return;
        }

        List<String> cellpose_args_cmd = Arrays.asList("python", "-m", "cellpose");
        cmd.addAll(cellpose_args_cmd);

        // input options
        cmd.addAll(options);

        if (IJ.isWindows()) {
            terminal_cmd.addAll( Arrays.asList("cmd.exe", "/C"));
            terminal_cmd.addAll(cmd);
        } else if ((IJ.isMacOSX() || IJ.isLinux()) && envType.equals("conda")) {
            terminal_cmd.addAll(Arrays.asList("bash", "-c"));
            // accept only one big String after so
            String cmdString = cmd.toString().replace(",", " ");
            terminal_cmd.add(cmdString.substring(1, cmdString.length() - 1));
        }

        //
        System.out.println(terminal_cmd);

        // Now the cmd line is ready
        ProcessBuilder pb = new ProcessBuilder(terminal_cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        //pb.redirectOutput(NULL_FILE);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        if (outputHandler != null) {
            outputHandler.accept(p.getInputStream());
        }
        p.waitFor();
    }

    public static void execute(String singleCommand) throws IOException, InterruptedException {
        ArrayList<String> cmdList = new ArrayList<>();
        cmdList.add(singleCommand);
        execute(cmdList, null);
    }

}
