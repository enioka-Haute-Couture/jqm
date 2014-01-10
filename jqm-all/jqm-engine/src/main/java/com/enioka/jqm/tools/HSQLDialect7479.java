package com.enioka.jqm.tools;

import org.hibernate.dialect.HSQLDialect;

/**
 * A dialect for Hibernate + HSQLDB 2.x+ It exists only because of bug HHH-7479 which is solved but was not backported to any JPA 2.0
 * version of Hibernate.
 * 
 * @author Marc-Antoine
 * 
 */
public class HSQLDialect7479 extends HSQLDialect
{
	@Override
	public String getForUpdateString()
	{
		return " for update";
	}
}
