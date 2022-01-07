import ch.epfl.biop.wrappers.cellpose.ij2commands.Cellpose_SegmentImgPlusAdvanced;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;

public class SimpleIJLaunch {

    final public static void main(String... args) {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();

        ImagePlus imp = IJ.openImage("https://imagej.net/images/blobs.gif");
        imp.show();

        ij.command().run(Cellpose_SegmentImgPlusAdvanced.class, true);

    }
}
