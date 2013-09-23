package mcd;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 *
 * @author pierre.coppee
 */
@Entity
@Table
public class JobInstance {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    private String user;
    private Integer sessionID;
    private String jobClass;
    private String filePath;
    private String state;
    private String position;

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public int getSessionID() {
        return sessionID;
    }

    public String getJobClass() {
        return jobClass;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getState() {
        return state;
    }

    public String getPosition() {
        return position;
    }

	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * @param sessionID the sessionID to set
	 */
	public void setSessionID(Integer sessionID)
	{
		this.sessionID = sessionID;
	}

	/**
	 * @param jobClass the jobClass to set
	 */
	public void setJobClass(String jobClass)
	{
		this.jobClass = jobClass;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state)
	{
		this.state = state;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(String position)
	{
		this.position = position;
	}

}
