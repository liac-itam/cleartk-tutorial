package mx.itam.liac.cleartk.tutorial.ner;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ne.type.NamedEntityMention;

/**
 * A simple annotator that just prints out any {@link org.cleartk.ne.type.NamedEntityMention}s in the CAS.
 *
 * A real pipeline would probably decide on an appropriate output format and write files instead
 * of printing to standard output.
 */
public class PrintNERAnnotator
        extends JCasAnnotator_ImplBase
{
    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        for (NamedEntityMention mention : JCasUtil.select(jCas, NamedEntityMention.class)) {
            System.out.printf("%s (%s)\n", mention.getCoveredText(), mention.getMentionType());
        }
    }

}