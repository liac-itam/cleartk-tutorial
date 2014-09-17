package mx.itam.liac.creatk.tutorial;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

import java.util.Collection;

/**
 * Este anotador itera sobe las anotaciones 'Sentence' y las
 * anotaciones que contiene cada "Sentence", que son
 * anotaciones 'Token'
 */
public class DumpTweetPOS
    extends JCasAnnotator_ImplBase
{
    @Override
    public void process(JCas jCas)
            throws AnalysisEngineProcessException
    {
        Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
        for(Sentence s: sentences) {
            Collection<Token> tokens = JCasUtil.selectCovered(jCas,Token.class, s);
            //System.out.printf("Sentence{%d, %d}:", s.getBegin(), s.getEnd());
            for(Token t: tokens) {
                //System.out.printf(" [%d,%d]", t.getBegin(), t.getEnd());
            }
            //System.out.println();
        }
    }
}
