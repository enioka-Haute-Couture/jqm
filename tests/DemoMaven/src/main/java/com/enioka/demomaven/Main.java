package com.enioka.demomaven;


/**
 * Hello world!
 *
 */
public class Main
{
    @SuppressWarnings("static-access")
    public static void main( String[] args )
    {
    	try {
    		Thread.currentThread().sleep(3000);
    		System.out.println("Hello World!");
    	} catch (InterruptedException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
}
