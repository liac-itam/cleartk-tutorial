package mx.itam.liac.cleartk.tutorial.ner;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

public class MASCTextFileFilter
        implements IOFileFilter
{
    @Override
    public boolean accept(File file) {
        return file.getPath().endsWith(".txt");
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".txt");
    }
}