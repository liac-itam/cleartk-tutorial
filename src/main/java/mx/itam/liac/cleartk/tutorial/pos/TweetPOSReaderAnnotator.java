package mx.itam.liac.cleartk.tutorial.pos;

import com.google.common.base.Splitter;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

/**
 * Esta clase-Anotador lee los datos del material especial del tutorial: Tweets con anotaciones Part-Of-Speech
 */
public class TweetPOSReaderAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_READ_POS_TAGS = "readPOSTags";
    @ConfigurationParameter(
            name = PARAM_READ_POS_TAGS,
            mandatory = false,
            description = "Specifies whether or not to populate tokens with POS tags.  Default = true")
    protected Boolean readPOSTags = true;

    public static final String PARAM_INPUT_VIEW = "inputView";
    public static final String TWEET_POS_CONLL_VIEW = "tweet_pos_conll_view";
    @ConfigurationParameter(
            name = PARAM_INPUT_VIEW,
            mandatory = false,
            description = "View containing twpos CONLL formatted data",
            defaultValue = TWEET_POS_CONLL_VIEW)
    protected String inputView;

    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        JCas conllView;
        try {
            conllView = jCas.getView(this.inputView);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        StringBuilder builder = new StringBuilder();
        int sentenceBegin = builder.length();
        int sentenceEnd;

        //System.out.println(conllView.getDocumentText());

        for(String line: Splitter.on("\n").split(conllView.getDocumentText())) {
            if(line.matches("^$")) {
                sentenceEnd = builder.length();
                if(sentenceEnd > sentenceBegin) {
                    Sentence sentence = new Sentence(jCas, sentenceBegin, sentenceEnd);
                    sentence.addToIndexes();
                    builder.append("\n");
                    sentenceBegin = builder.length();
                }
            }
            else {
                // Normal lines are token and POS tag
                String[] parts = line.split("\\t");
                String tokenText = parts[0];
                String tokenPos = parts[1];
                int tokenBegin = builder.length();
                int tokenEnd = tokenBegin + tokenText.length();
                Token token = new Token(jCas, tokenBegin, tokenEnd);
                if(this.readPOSTags) {
                    token.setPos(tokenPos);
                }
                token.addToIndexes();
                builder.append(tokenText);
                builder.append(' '); //faster
            }
        }
    }
}
