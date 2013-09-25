package mcd;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


/**
 *
 * @author pierre.coppee
 */
@Entity
public class JobInstance {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
	@ManyToOne(fetch=FetchType.LAZY)
	private JobDefinition jd;
	@ManyToOne(fetch=FetchType.LAZY)
	public JobInstance parent;
	@Column(length=50)
    private String user;
    private Integer sessionID;
    @Column(nullable=false, length=50)
    private String state;
    private Integer position;

    public int getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public int getSessionID() {
        return sessionID;
    }
    public JobDefinition getJd()
	{
		return jd;
	}

	public void setJd(JobDefinition jd)
	{
		this.jd = jd;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public Integer getPosition()
	{
		return position;
	}

	public void setPosition(Integer position)
	{
		this.position = position;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public void setSessionID(Integer sessionID)
	{
		this.sessionID = sessionID;
	}

	public JobInstance getParent()
	{
		return parent;
	}

	public void setParent(JobInstance parent)
	{
		this.parent = parent;
	}
}
