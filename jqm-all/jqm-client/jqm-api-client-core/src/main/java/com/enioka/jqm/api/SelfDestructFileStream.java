package com.enioka.jqm.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class SelfDestructFileStream extends FileInputStream
{
    File f = null;

    SelfDestructFileStream(File file) throws FileNotFoundException
    {
        super(file);
        this.f = file;
    }

    @Override
    public void close() throws IOException
    {
        super.close();

        try
        {
            f.delete();
        }
        catch (Exception e)
        {
            // Nothing
        }
        f = null;
    }
}
