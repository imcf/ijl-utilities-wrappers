package ch.epfl.biop.wrappers.generic;

abstract public class GenericTask {

    protected GenericTaskSettings settings;

    public void setSettings(GenericTaskSettings settings) {
        this.settings = settings;
    }

    abstract public void run() throws Exception;

}
