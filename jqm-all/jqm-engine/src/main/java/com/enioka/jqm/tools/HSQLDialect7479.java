package com.enioka.jqm.tools;

import org.hibernate.dialect.HSQLDialect;

public class HSQLDialect7479 extends HSQLDialect
{
	@Override
	public String getForUpdateString()
	{
		return " for update";
	}
}
