package com.enioka.jqm.tools;

public class Main
{
	/**
	 * Startup method for the packaged JAR
	 * 
	 * @param args
	 *            0 is node name
	 */
	public static void main(String[] args)
	{
		JqmEngine engine = new JqmEngine();
		try
		{
			engine.start(args);
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
