package com.enioka.jqm.api;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 */
public class JobBase {

	protected int parentID;
	protected int canBeRestart;
	protected String applicationName;
	protected String sessionID;
	protected String application;
	protected String module;
	protected String other1;
	protected String other2;
	protected String other3;
	protected Map<String, String> parameters = new HashMap<String, String>();

	public void start() {
	}

	public void stop() {
	}

	public void addDeliverable(String path) {
	}

	public void sendMsg(String msg) {
	}

	// ---------

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getCanBeRestart() {
		return canBeRestart;
	}

	public void setCanBeRestart(int canBeRestart) {
		this.canBeRestart = canBeRestart;
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

}

