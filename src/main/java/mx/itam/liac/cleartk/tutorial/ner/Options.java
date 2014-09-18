package mx.itam.liac.cleartk.tutorial.ner;

import com.lexicalscope.jewel.cli.Option;
import java.io.File;

/**
 * The JewelCLI interface for getting command line options
 */
public interface Options
{
    @Option(
            longName = "model-dir",
            description = "The directory where the model was trained",
            defaultValue = "target/chunking/ne-model")
    public File getModelDirectory();

    @Option(
            longName = "text-file",
            description = "The file to label with named entities",
            defaultValue = "data/MASC-1.0.3/data/written")
    public File getTrainDirectory();
}
