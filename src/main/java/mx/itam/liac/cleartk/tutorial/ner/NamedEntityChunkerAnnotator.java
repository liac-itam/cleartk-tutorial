package mx.itam.liac.cleartk.tutorial.ner;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instances;
import org.cleartk.ml.chunking.BioChunking;
import org.cleartk.ml.feature.extractor.*;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fer on 17/09/14.
 */
public class NamedEntityChunkerAnnotator
    extends CleartkSequenceAnnotator<String>
{
    private FeatureExtractor1<Token> extractor;

    private CleartkExtractor<Token, Token> contextExtractor;

    /** Extractor para convertir/sacar entidades de NER a token */
    private BioChunking<Token, NamedEntityMention> chunking;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        this.extractor = new CombinedExtractor1<Token>(
                new FeatureFunctionExtractor<Token>(
                        new CoveredTextExtractor<Token>(),
                        new CharacterCategoryPatternFunction<Token>(
                                CharacterCategoryPatternFunction.PatternType.REPEATS_MERGED
                        )
                ),
                new TypePathExtractor<Token>(Token.class, "pos")
        );

        this.contextExtractor = new CleartkExtractor<Token, Token>(
                Token.class,
                this.extractor,
                new CleartkExtractor.Preceding(3),
                new CleartkExtractor.Following(3)
        );

        this.chunking = new BioChunking<Token, NamedEntityMention>(
                Token.class,
                NamedEntityMention.class,
                "mentionType"
        );
    }

    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
            // extract features for each token in the sentence
            List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
            List<List<Feature>> featureLists = new ArrayList<List<Feature>>();
            for (Token token : tokens) {
                List<Feature> features = new ArrayList<Feature>();
                features.addAll(this.extractor.extract(jCas, token));
                features.addAll(this.contextExtractor.extract(jCas, token));
                featureLists.add(features);
            }
            if(this.isTraining()) {
                List<NamedEntityMention> namedEntityMentions = JCasUtil.selectCovered(
                        jCas,
                        NamedEntityMention.class,
                        sentence);

                List<String> outcomes = this.chunking.createOutcomes(jCas, tokens, namedEntityMentions);

                this.dataWriter.write(Instances.toInstances(outcomes,featureLists));
            }
            else {
                // get the predicted BIO outcome labels from the classifier
                List<String> outcomes = this.classifier.classify(featureLists);

                // create the NamedEntityMention annotations in the CAS
                this.chunking.createChunks(jCas, tokens, outcomes);
            }
        }
    }
}
