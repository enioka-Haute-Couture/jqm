package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Dependencies
{
	private ArrayList<String> list = new ArrayList<String>();

	public Dependencies(String path) {

	File fXmlFile = new File(path);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
	String dep = "";
	try
	{
		dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

		NodeList nList = doc.getElementsByTagName("dependency");

		//System.out.println("----------------------------");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			//System.out.println("\nCurrent Element :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				dep += eElement.getElementsByTagName("groupId").item(0).getTextContent().toString() + ":";
				dep += eElement.getElementsByTagName("artifactId").item(0).getTextContent().toString() + ":";
				dep += eElement.getElementsByTagName("version").item(0).getTextContent().toString();

				list.add(dep);
				dep = "";
			}
		}

	} catch (ParserConfigurationException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

	public void print()
	{
		for (int i = 0; i < list.size(); i++)
		{
			System.out.println("Dependency " + i + ": " + list.get(i));
		}
	}

	/**
	 * @return the list
	 */
	public ArrayList<String> getList()
	{
		return list;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(ArrayList<String> list)
	{
		this.list = list;
	}
}
