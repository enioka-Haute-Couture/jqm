package mcd;


import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author pierre.coppee
 */
public class JobDefinition {

	public Integer parentID;
	public boolean canBeRestarted = true;
	public String applicationName;
	public String sessionID;
	public String application;
	public String module;
	public String other1;
	public String other2;
	public String other3;
	public Map<String, String> parameters = new HashMap<String, String>();

	public void addParameters(String key, String value) {

	}

	public void delParameters(String param) {

	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public boolean getCanBeRestarted() {
		return canBeRestarted;
	}

	public void setCanBeRestarted(int canBeRestart) {
		this.canBeRestarted = canBeRestarted;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getOther1() {
		return other1;
	}

	public void setOther1(String other1) {
		this.other1 = other1;
	}

	public String getOther2() {
		return other2;
	}

	public void setOther2(String other2) {
		this.other2 = other2;
	}

	public String getOther3() {
		return other3;
	}

	public void setOther3(String other3) {
		this.other3 = other3;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void setCanBeRestarted(boolean canBeRestarted)
	{
		this.canBeRestarted = canBeRestarted;
	}

}
