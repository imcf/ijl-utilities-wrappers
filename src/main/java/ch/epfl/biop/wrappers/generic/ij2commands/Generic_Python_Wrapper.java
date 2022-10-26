package ch.epfl.biop.wrappers.generic.ij2commands;

import ch.epfl.biop.wrappers.cellpose.DefaultCellposeTask;
import ch.epfl.biop.wrappers.generic.DefaultGenericTask;
import ch.epfl.biop.wrappers.generic.GenericTaskSettings;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Plugin(type = Command.class, menuPath = "Plugins>BIOP>Generic Wrapper>Generic...")
public class Generic_Python_Wrapper implements Command {

    @Parameter
    ImagePlus imp;

    @Parameter
    File env_path;

    @Parameter
    String input_dir_flag = "-dir"; // "-i"

    @Parameter(required = false, label = "add more parameters")
    String flags = "";

    @Parameter
    String suffix = "_cp_masks";

    @Parameter(type = ItemIO.OUTPUT)
    ImagePlus output_imp;

    @Override
    public void run() {
        Boolean verbose = true;
        Calibration cal = imp.getCalibration();


        // Create settings and define env_path
        GenericTaskSettings settings = new GenericTaskSettings();
        settings.setEnvDir(env_path.toString() );

        // We'll dave the current time-point of the imp in a temp folder
        String tempDir = IJ.getDirectory("Temp");
        // create tempdir
        File genericTempDir = new File(tempDir, "genericTemp");
        genericTempDir.mkdir();

        // when plugin crashes, image file can pile up in the folder, so we make sure to clear everything
        File[] contents = genericTempDir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                f.delete();
            }
        }

        // Add it to the settings
        settings.setDatasetDir(genericTempDir.toString());

        settings.setAdditionalFlags(flags);

        DefaultGenericTask genericTask = new DefaultGenericTask();
        genericTask.setSettings(settings);
        try {
            // can't process time-lapse directly so, we'll save one time-point after another
            int impFrames = imp.getNFrames();

            // we'll use list to store paths of saved input, output masks and outlines
            List<File> t_imp_paths = new ArrayList<>();
            List<File> generic_masks_paths = new ArrayList<>();

            for (int t_idx = 1; t_idx <= impFrames; t_idx++) {
                // duplicate all channels and all z-slices for a defined time-point
                ImagePlus t_imp = new Duplicator().run(imp, 1, imp.getNChannels(), 1, imp.getNSlices(), t_idx, t_idx);
                // and save the current t_imp into the cellposeTempDir
                File t_imp_path = new File(genericTempDir, imp.getShortTitle() + "-t" + t_idx + ".tif");
                FileSaver fs = new FileSaver(t_imp);
                fs.saveAsTiff(t_imp_path.toString());
                if (verbose) System.out.println(t_imp_path.toString());
                // add to list of paths to delete at the end of operations
                t_imp_paths.add(t_imp_path);

                // prepare path of the cellpose mask output
                File generic_imp_path = new File(genericTempDir, imp.getShortTitle() + "-t" + t_idx + suffix + ".tif");
                generic_masks_paths.add(generic_imp_path);
            }

            // RUN CELLPOSE !
            genericTask.run();

            // Open all the cellpose_mask and store each imp within an ArrayList
            ArrayList<ImagePlus> imps = new ArrayList<>(impFrames);
            for (int t_idx = 1; t_idx <= impFrames; t_idx++) {
                ImagePlus generic_t_imp = IJ.openImage(generic_masks_paths.get(t_idx - 1).toString());
                // make sure to make a 16-bit imp
                // (issue with time-lapse, first frame have less than 254 objects and latest have more)
                if (generic_t_imp.getBitDepth() != 16) {
                    if (generic_t_imp.getNSlices() > 1) {
                        generic_t_imp.getStack().setBitDepth(16);
                    } else {
                        generic_t_imp.setProcessor(generic_t_imp.getProcessor().convertToShort(false));
                    }
                }
                imps.add(generic_t_imp.duplicate());
            }
            // Convert the ArrayList to an imp
            // https://stackoverflow.com/questions/9572795/convert-list-to-array-in-java
            ImagePlus[] impsArray = imps.toArray(new ImagePlus[0]);
            output_imp = Concatenator.run(impsArray);
            output_imp.setCalibration(cal);
            output_imp.setTitle(imp.getShortTitle() + "-wrapper");

            // Delete the created files and folder
            for (int t_idx = 1; t_idx <= impFrames; t_idx++) {
                t_imp_paths.get(t_idx - 1).delete();
                generic_masks_paths.get(t_idx - 1).delete();
            }
            genericTempDir.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}