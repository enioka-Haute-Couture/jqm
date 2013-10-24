package com.enioka.jqm.testpackages;

import java.util.Enumeration;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import com.enioka.jqm.api.JobBase;
import com.ibm.msg.client.jms.JmsQueueConnectionFactory;

public class SuperTestPayload extends JobBase
{

	@Override
	public void start()
	{
		try
		{
			System.out.println("Thread context class loader is: " + Thread.currentThread().getContextClassLoader());
			System.out.println("Class class loader used for loading test class is: " + this.getClass().getClassLoader());
			Object o = NamingManager.getInitialContext(null).lookup("jms/qcf");
			System.out.println(o.getClass());
			JmsQueueConnectionFactory qcf = (JmsQueueConnectionFactory) o;
			String propName = "";
			Enumeration<?> names = qcf.getPropertyNames();
			while (names.hasMoreElements())
			{
				propName = (String) names.nextElement();
				System.out.println("Property name: " + propName + " - " + qcf.getStringProperty(propName));
			}

			Object p = NamingManager.getInitialContext(null).lookup("jms/testqueue");
			System.out.println(p.getClass());

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
