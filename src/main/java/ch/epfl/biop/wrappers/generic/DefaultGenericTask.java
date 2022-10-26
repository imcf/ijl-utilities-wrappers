package ch.epfl.biop.wrappers.generic;

import ch.epfl.biop.wrappers.cellpose.Cellpose;

import java.util.ArrayList;

public class DefaultGenericTask extends GenericTask {

    public void run() throws Exception {

        ArrayList<String> commandTool = new ArrayList<>();

        if (settings.flags != "") {
            String[] flagsList = settings.flags.split(" ");

            if (flagsList.length > 1) {
                for (int i = 0; i < flagsList.length; i++) {
                    commandTool.add(flagsList[i].toString().trim());
                }
            } else {
                if (settings.flags.length() > 1) {
                    commandTool.add(settings.flags.trim());
                }
            }
        }

        commandTool.add("--dir");
        commandTool.add("" + settings.datasetDir);

        Generic.execute(settings.setEnvDir, commandTool, null);

    }
}
