package com.enioka.jqm.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Queue;

public class XmlParser
{
	private static Logger jqmlogger = Logger.getLogger(XmlParser.class);
	private EntityManager em = Helpers.getNewEm();
	File f = null;

	public XmlParser(String path)
	{
		this.f = new File(path);
	}

	public void parse()
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		boolean canBeRestarted = true;
		String javaClassName = null;
		String filePath = null;
		Queue queue = null;
		Integer maxTimeRunning = null;
		String applicationName = null;
		Integer sessionID = null;
		String application = null;
		String module = null;
		String other1 = null;
		String other2 = null;
		String other3 = null;
		boolean highlander = false;
		String jarPath = null;
		List<JobDefParameter> parameters = new ArrayList<JobDefParameter>();

		try
		{
			jqmlogger.debug(f.getPath());
			jqmlogger.debug("Working Directory = " + System.getProperty("user.dir"));

			if (f == null || !f.isFile())
			{
				throw new FileNotFoundException("The XML file " + f + " was not found");
			}

			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("jqm");

			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element e = (Element) nNode;

					if (e.getNodeName().equals("parameters"))
					{
						jqmlogger.debug("TOTOPROUT");
						NodeList nl = doc.getElementsByTagName("parameter");
						for (int i = 0; i < nl.getLength(); i++)
						{

							Node n = nl.item(i);
							Element el = (Element) n;
							JobDefParameter jdp = new JobDefParameter();
							jdp.setKey(el.getTextContent());
							jdp.setValue(el.getTextContent());
							parameters.add(jdp);
						}
					}

					canBeRestarted = (e.getElementsByTagName("canBeRestarted").item(0).getTextContent().equals("true")) ? true : false;
					javaClassName = e.getElementsByTagName("javaClassName").item(0).getTextContent();
					filePath = e.getElementsByTagName("filePath").item(0).getTextContent();
					queue = em.createQuery("SELECT q FROM Queue q WHERE q.name = :n", Queue.class)
							.setParameter("n", e.getElementsByTagName("queue").item(0).getTextContent()).getSingleResult();
					maxTimeRunning = Integer.parseInt(e.getElementsByTagName("maxTimeRunning").item(0).getTextContent());
					applicationName = e.getElementsByTagName("name").item(0).getTextContent();
					sessionID = Integer.parseInt(e.getElementsByTagName("sessionId").item(0).getTextContent());
					application = e.getElementsByTagName("application").item(0).getTextContent();
					module = e.getElementsByTagName("module").item(0).getTextContent();
					other1 = e.getElementsByTagName("other1").item(0).getTextContent();
					other2 = e.getElementsByTagName("other2").item(0).getTextContent();
					other3 = e.getElementsByTagName("other3").item(0).getTextContent();
					highlander = (e.getElementsByTagName("highlander").item(0).getTextContent().equals("true")) ? true : false;
					jarPath = e.getElementsByTagName("path").item(0).getTextContent();
					JobDefParameter jdp = new JobDefParameter();
					jdp.setKey(e.getElementsByTagName("key").item(0).getTextContent());
					jdp.setValue(e.getElementsByTagName("value").item(0).getTextContent());
					parameters.add(jdp);

					JobDef j = new JobDef();
					EntityTransaction transac = em.getTransaction();
					transac.begin();

					j.setCanBeRestarted(canBeRestarted);
					j.setJavaClassName(javaClassName);
					j.setParameters(parameters);
					j.setFilePath(filePath);
					j.setQueue(queue);
					j.setMaxTimeRunning(maxTimeRunning);
					j.setApplicationName(applicationName);
					j.setSessionID(sessionID);
					j.setApplication(application);
					j.setModule(module);
					j.setOther1(other1);
					j.setOther2(other2);
					j.setOther3(other3);
					j.setHighlander(highlander);
					j.setJarPath(jarPath);

					em.persist(j);
					transac.commit();

					jqmlogger.debug("XML parsed");
				}
			}

		} catch (ParserConfigurationException e)
		{
			jqmlogger.error(e);
		} catch (SAXException e)
		{
			jqmlogger.error("Invalid XML architecture. Please, fix correctly the dependencies", e);
		} catch (IOException e)
		{
			jqmlogger.error("Invalid xml. Please check the xml & its filepath", e);
		} catch (Exception e)
		{
			jqmlogger.error("Invalid xml. Please check the xml & its filepath", e);
		}
	}
}
