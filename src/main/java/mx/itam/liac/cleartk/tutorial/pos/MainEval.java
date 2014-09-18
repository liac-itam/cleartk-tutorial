package mx.itam.liac.cleartk.tutorial.pos;

import org.cleartk.eval.AnnotationStatistics;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fer on 17/09/14.
 */
public class MainEval
{
    public static void main(String[] args)
            throws Exception
    {
        URI uri = MainSimple.class.getResource("/twpos-data-v0.2.1").toURI();
        File dataDir = new File(uri);
        List<File> trainFiles = Arrays.asList(new File(dataDir, "train"));
        List<File> testFiles = Arrays.asList(new File(dataDir, "dev"));

        TweetPOSEval evaluation = new TweetPOSEval(new File("target/models"));

        // Run and Evaluate on Holdout Set
        AnnotationStatistics<String> holdoutStats = evaluation.trainAndTest(trainFiles, testFiles);
        System.err.println("Holdout Set Results:");
        System.err.print(holdoutStats);
        System.err.println();
        String confMatrixString = holdoutStats.confusions().toCSV();
        PrintWriter pw = new PrintWriter("confusion-matrix.csv");
        pw.write(confMatrixString);
        pw.close();
        System.err.println(confMatrixString);
    }
}
