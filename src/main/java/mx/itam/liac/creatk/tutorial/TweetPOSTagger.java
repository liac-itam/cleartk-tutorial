package mx.itam.liac.creatk.tutorial;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.*;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.function.*;
import static org.cleartk.ml.feature.function.CharacterNgramFeatureFunction.Orientation.RIGHT_TO_LEFT;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.ml.feature.extractor.CleartkExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un anotador que implementa un POS-tagger basado en los
 * componentes, anotaciones e instanscias de ClearTK
 *
 * NOTA: En este ejemplo, se itera sobre una sola instancia
 */
public class TweetPOSTagger
    extends CleartkAnnotator<String>
{
    /**
     * Extractor de ClearTK que saca features basandose en transformaciones
     * al token original y n-gramas de tamanio 2 y 3
     */
    private FeatureFunctionExtractor<Token> tokenFeatureExtractor;

    /**
     * Extractor de ClearTK que saca features basados en el contexto
     * (palabras alrededor del token
     */
    private CleartkExtractor<Token, Token> contextFeatureExtractor;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        this.tokenFeatureExtractor = new FeatureFunctionExtractor<Token>(
                new CoveredTextExtractor(),
                new LowerCaseFeatureFunction(),
                new CapitalTypeFeatureFunction(),
                new NumericTypeFeatureFunction(),
                new CharacterNgramFeatureFunction(RIGHT_TO_LEFT, 0, 2),
                new CharacterNgramFeatureFunction(RIGHT_TO_LEFT, 0, 3));

        this.contextFeatureExtractor = new CleartkExtractor<Token, Token>(
                Token.class,
                new CoveredTextExtractor<Token>(),
                new CleartkExtractor.Preceding(2),
                new CleartkExtractor.Following(2));
    }

    /**
     * La version de este tutorial es para entrenar una sola instancia a la vez, no todas de jalon
     * @param jCas
     * @throws AnalysisEngineProcessException
     */
    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {

        //Cada oracion forma una instancia para entrenamiento
        for(Sentence sentence: JCasUtil.select(jCas, Sentence.class)) {
            Instance<String> instance = new Instance<String>();

            //Cada token/POS-tag forma un feature para la instancia de entrenamiento
            for(Token token: JCasUtil.selectCovered(jCas, Token.class, sentence)) {
                List<Feature> tokenFeatures = new ArrayList<Feature>();
                //featuregs generadas de los ngrams y preprocesamiento de texto
                instance.addAll(this.tokenFeatureExtractor.extract(jCas, token));
                //features generadas por contexto (palabras alrededor del token)
                instance.addAll(this.contextFeatureExtractor.extract(jCas, token));

                if(this.isTraining()) {
                    instance.setOutcome(token.getPos());
                    this.dataWriter.write(instance);
                }
                else {
                    String POStag = this.classifier.classify(instance.getFeatures());
                    token.setPos(POStag);
                }
            }
        }
    }
}
