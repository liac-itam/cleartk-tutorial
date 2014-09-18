package mx.itam.liac.cleartk.tutorial.pos;

import com.google.common.base.Function;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterator;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.jar.*;
import org.cleartk.ml.opennlp.maxent.MaxentStringOutcomeDataWriter;
import org.cleartk.token.type.Token;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by fer on 16/09/14.
 */
public class TweetPOSEval
    extends Evaluation_ImplBase<File, AnnotationStatistics<String>>
{
    private static final String GOLD_VIEW_NAME = "TweetPosGoldView";

    private static final String SYSTEM_VIEW_NAME = CAS.NAME_DEFAULT_SOFA;

    public TweetPOSEval(File baseDirectory)
    {
        super(baseDirectory);
    }

    @Override
    protected CollectionReader getCollectionReader(List<File> files)
            throws Exception
    {
        CollectionReaderDescription reader =
                UriCollectionReader.getDescriptionFromFiles(files);

        return CollectionReaderFactory.createReader(reader);
    }

    @Override
    protected void train(CollectionReader collectionReader, File directory)
            throws Exception
    {
        System.out.println("Training...");
        AggregateBuilder builder = new AggregateBuilder();
        builder.add(
                UriToDocumentTextAnnotator.getDescriptionForView(
                        TweetPOSReaderAnnotator.TWEET_POS_CONLL_VIEW));
        builder.add(
                AnalysisEngineFactory.createEngineDescription(
                        TweetPOSReaderAnnotator.class));
        builder.add(
                AnalysisEngineFactory.createEngineDescription(
                        DumpTweetPOS.class));
        AnalysisEngineDescription posDesc = AnalysisEngineFactory.createEngineDescription(
                TweetPOSTagger.class,
                CleartkAnnotator.PARAM_IS_TRAINING, true,
                DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, directory.getPath(),
                DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, MaxentStringOutcomeDataWriter.class);
        builder.add(posDesc);

        SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

        Train.main(directory);
    }

    @Override
    protected AnnotationStatistics<String> test(CollectionReader reader, File directory)
            throws Exception
    {
        System.out.println("Testing...");
        AggregateBuilder builder = new AggregateBuilder();

        // Read contents of file in CONLL view
        builder.add(
                UriToDocumentTextAnnotator.getDescriptionForView(
                        TweetPOSReaderAnnotator.TWEET_POS_CONLL_VIEW));
        // Ensure views are created
        builder.add(AnalysisEngineFactory.createEngineDescription(
                ViewCreatorAnnotator.class,
                ViewCreatorAnnotator.PARAM_VIEW_NAME, GOLD_VIEW_NAME));

        builder.add(AnalysisEngineFactory.createEngineDescription(
                ViewCreatorAnnotator.class,
                ViewCreatorAnnotator.PARAM_VIEW_NAME, SYSTEM_VIEW_NAME));

        // Parse CONLL text with POS tags into Gold View
        builder.add(AnalysisEngineFactory.createEngineDescription(TweetPOSReaderAnnotator.class),
                CAS.NAME_DEFAULT_SOFA, GOLD_VIEW_NAME);

        // Copy text from gold view into the system view
        builder.add(AnalysisEngineFactory.createEngineDescription(ViewTextCopierAnnotator.class,
                ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, GOLD_VIEW_NAME,
                ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, SYSTEM_VIEW_NAME));

        // Copy sentences and tokens from the gold view into the system view
        builder.add(AnalysisEngineFactory.createEngineDescription(CopySentenceAndTokenAnnotations.class,
                CopySentenceAndTokenAnnotations.PARAM_SOURCE_VIEW_NAME, GOLD_VIEW_NAME,
                CopySentenceAndTokenAnnotations.PARAM_DESTINATION_VIEW_NAME, SYSTEM_VIEW_NAME,
                CopySentenceAndTokenAnnotations.PARAM_COPY_POS_TAGS, false));

        // Create POS tagger configured for classification
        builder.add(AnalysisEngineFactory.createEngineDescription(TweetPOSTagger.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                new File(directory, "model.jar").getPath()));

        // Create an object to keep track of accuracy, precision, recall, etc.
        AnnotationStatistics<String> stats = new AnnotationStatistics<String>();
        Function<Token, ?> getSpan = AnnotationStatistics.annotationToSpan();
        Function<Token, String> getPos = AnnotationStatistics.annotationToFeatureValue("pos");

        // Run aggregate engine over data from reader, and collect evaluation stats
        JCasIterator it = new JCasIterator(reader, builder.createAggregate());
        while(it.hasNext()) {
            JCas jCas = it.next();
            JCas goldView;
            JCas systemView;
            try {
                goldView = jCas.getView(GOLD_VIEW_NAME);
                systemView = jCas.getView(SYSTEM_VIEW_NAME);
            } catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
            Collection<Token> goldTokens = JCasUtil.select(goldView, Token.class);
            Collection<Token> systemTokens = JCasUtil.select(systemView, Token.class);
            stats.add(goldTokens, systemTokens, getSpan, getPos);
        }

        return stats;
    }
}
