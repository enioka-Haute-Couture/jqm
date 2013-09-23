package mcd;

public class ExecParameter {

	private String key;
	private String value;
	private Integer submittedJobId;


	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public Integer getSubmittedJobId()
	{
		return submittedJobId;
	}

	public void setSubmittedJobId(Integer submittedJobId)
	{
		this.submittedJobId = submittedJobId;
	}
}
