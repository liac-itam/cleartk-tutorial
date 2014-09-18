package mx.itam.liac.cleartk.tutorial.ner;

import com.lexicalscope.jewel.cli.CliFactory;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.opennlp.tools.PosTaggerAnnotator;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import java.util.Arrays;

/**
 * Created by fer on 18/09/14.
 */
public class MainTest
{
    public static void main(String args[])
            throws Exception
    {
        Options ops = CliFactory.parseArguments(Options.class, args);
        // a reader that loads the URIs of the text file
        CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(Arrays.asList(ops.getTextFile()));

        // assemble the classification pipeline
        AggregateBuilder aggregate = new AggregateBuilder();

        // an annotator that loads the text from the training file URIs
        aggregate.add(UriToDocumentTextAnnotator.getDescription());

        // annotators that identify sentences, tokens and part-of-speech tags in the text
        aggregate.add(SentenceAnnotator.getDescription());
        aggregate.add(TokenAnnotator.getDescription());
        aggregate.add(PosTaggerAnnotator.getDescription());
        //el NER
        aggregate.add(AnalysisEngineFactory.createEngineDescription(
                NamedEntityChunkerAnnotator.class,
                CleartkSequenceAnnotator.PARAM_IS_TRAINING,
                false,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                JarClassifierBuilder.getModelJarFile(ops.getModelDirectory())));

        //anotador para imprimir los tags de NER
        aggregate.add(AnalysisEngineFactory.createEngineDescription(PrintNERAnnotator.class));

        // run the classification pipeline on the new texts
        SimplePipeline.runPipeline(reader, aggregate.createAggregateDescription());
    }
}
