package mx.itam.liac.creatk.tutorial;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by fer on 16/09/14.
 */
public class Main
{
    public static void main(String args[])
    throws UIMAException, IOException, URISyntaxException
    {
        URI uri = Main.class.getResource("/twpos-data-v0.2.1/train").toURI();

        CollectionReaderDescription reader =
                UriCollectionReader.getDescriptionFromFiles(
                        Arrays.asList(new File(uri))
                );

        AggregateBuilder builder = new AggregateBuilder();

        builder.add(
                UriToDocumentTextAnnotator.getDescriptionForView(
                        TweetPOSReaderAnnotator.TWEET_POS_CONLL_VIEW));
        builder.add(
                AnalysisEngineFactory.createPrimitiveDescription(
                        TweetPOSReaderAnnotator.class));
        builder.add(
                AnalysisEngineFactory.createPrimitiveDescription(
                        DumpTweetPOS.class));

        SimplePipeline.runPipeline(reader, builder.createAggregateDescription());

        System.out.println("Bye!");
    }
}
