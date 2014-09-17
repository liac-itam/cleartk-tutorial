package mx.itam.liac.creatk.tutorial;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

public class CopySentenceAndTokenAnnotations
        extends JCasAnnotator_ImplBase
{

    public static final String PARAM_SOURCE_VIEW_NAME = "sourceViewName";
    @ConfigurationParameter(
            name = PARAM_SOURCE_VIEW_NAME,
            mandatory = true,
            description = "Name of the view to copy annotations to")
    String sourceViewName;

    public static final String PARAM_DESTINATION_VIEW_NAME = "destinationViewName";
    @ConfigurationParameter(
            name = PARAM_DESTINATION_VIEW_NAME,
            mandatory = true,
            description = "Name of the view to copy annotations to")
    String destinationViewName;

    public static final String PARAM_COPY_POS_TAGS = "copyPosTags";
    @ConfigurationParameter(
            name = PARAM_COPY_POS_TAGS,
            mandatory = false,
            description = "Specifies whether or not to copy POS tags field for token.  Default = true")
    protected Boolean copyPosTags = true;


    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        JCas srcView;
        try {
            srcView = jCas.getView(sourceViewName);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        JCas dstView = ViewCreatorAnnotator.createViewSafely(jCas, destinationViewName);

        for (Sentence sentence : JCasUtil.select(srcView, Sentence.class)) {
            Sentence sentenceCopy = new Sentence(dstView, sentence.getBegin(), sentence.getEnd());
            sentenceCopy.addToIndexes();
        }

        for (Token token : JCasUtil.select(srcView, Token.class)) {
            Token tokenCopy = new Token(dstView, token.getBegin(), token.getEnd());
            if (this.copyPosTags) {
                tokenCopy.setPos(token.getPos());
            }
            tokenCopy.addToIndexes();
        }

    }

}