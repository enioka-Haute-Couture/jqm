package com.enioka.jqm.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;

public class InputFile
{
    private Integer id;
    private String filePath;
    private String fileFamily;
    private Integer jobId;
    private String originalFileName;
    private Integer nodeId;

    public Integer getId()
    {
        return id;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getFileFamily()
    {
        return fileFamily;
    }

    public Integer getJobId()
    {
        return jobId;
    }

    public String getOriginalFileName()
    {
        return originalFileName;
    }

    public Integer getNodeId()
    {
        return nodeId;
    }

    public static List<InputFile> select(DbConn cnx, String query_key, Object... args)
    {
        List<InputFile> res = new ArrayList<InputFile>();
        ResultSet rs = null;
        try
        {
            rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                InputFile tmp = new InputFile();

                tmp.id = rs.getInt(1);
                tmp.fileFamily = rs.getString(2);
                tmp.filePath = rs.getString(3);
                tmp.jobId = rs.getInt(4);
                tmp.originalFileName = rs.getString(5);
                tmp.nodeId = rs.getInt(6);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            cnx.closeQuietly(rs);
        }
        return res;
    }
}
