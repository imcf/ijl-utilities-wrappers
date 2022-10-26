package ch.epfl.biop.wrappers.generic;

import ch.epfl.biop.wrappers.cellpose.CellposeTaskSettings;

public class GenericTaskSettings {

    String setEnvDir = "";
    String datasetDir = "";
    String flags = "";


    public GenericTaskSettings setEnvDir(String setEnvDir) {
        this.setEnvDir = setEnvDir;
        return this;
    }

    public GenericTaskSettings setDatasetDir(String datasetDir) {
        this.datasetDir = datasetDir;
        return this;
    }

    public GenericTaskSettings setAdditionalFlags(String flags) {
        this.flags = flags;
        return this;
    }
}
