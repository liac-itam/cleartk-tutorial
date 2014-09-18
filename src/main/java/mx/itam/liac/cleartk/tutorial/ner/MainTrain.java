package mx.itam.liac.cleartk.tutorial.ner;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.jar.*;
import org.cleartk.opennlp.tools.PosTaggerAnnotator;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import com.lexicalscope.jewel.cli.CliFactory;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;


/**
 * Created by fer on 17/09/14.
 */
public class MainTrain
{
    public static void main(String args[])
            throws Exception
    {
        Options options = CliFactory.parseArguments(Options.class, args);

        //CollectionReader
        //CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(
        //        Arrays.asList(options.getTextFile()));
        CollectionReaderDescription crd = UriCollectionReader.getDescriptionFromDirectory(
            options.getTrainDirectory(),
            MASCTextFileFilter.class,
            null
        );
        CollectionReader reader = CollectionReaderFactory.createReader(crd);

        //AnalysisEngine
        AggregateBuilder builder = new AggregateBuilder();

        //Saca el text ode las URI y los pone en CAS.documentText
        builder.add(UriToDocumentTextAnnotator.getDescription());

        //Anotador para procesar el texto de las anotaciones de MASC
        builder.add(MASCGoldAnnotator.getDescription());

        //Anotador basado en OpenNLP para hacer POS
        builder.add(PosTaggerAnnotator.getDescription());

        //Nuestro generador de entidades
        builder.add(
                AnalysisEngineFactory.createEngineDescription(
                        NamedEntityChunkerAnnotator.class,
                        CleartkAnnotator.PARAM_IS_TRAINING,
                        true,
                        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
                        options.getModelDirectory(),
                        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
                        MalletCrfStringOutcomeDataWriter.class
                )
        );

        //Run the pipeline
        SimplePipeline.runPipeline(reader, builder.createAggregate());

        Train.main(options.getModelDirectory());
    }
}
