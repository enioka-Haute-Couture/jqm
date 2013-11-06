package com.enioka.jqm.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
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

					NodeList nl = e.getElementsByTagName("jobDefinition");

					for (int i = 0; i < nl.getLength(); i++)
					{
						em.getTransaction().begin();
						Element ee = (Element) nl.item(i);

						canBeRestarted = (ee.getElementsByTagName("canBeRestarted").item(0).getTextContent().equals("true")) ? true : false;
						javaClassName = ee.getElementsByTagName("javaClassName").item(0).getTextContent();
						filePath = ee.getElementsByTagName("filePath").item(0).getTextContent();
						queue = em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true", Queue.class).getSingleResult();
						maxTimeRunning = Integer.parseInt(ee.getElementsByTagName("maxTimeRunning").item(0).getTextContent());

						JobDef jd;

						TypedQuery<JobDef> q = em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :n", JobDef.class);
						q.setParameter("n", ee.getElementsByTagName("name").item(0).getTextContent());

						try
						{
							jd = q.getSingleResult();
							jd.getParameters().clear();
						} catch (NoResultException x)
						{
							jd = new JobDef();
						}

						applicationName = ee.getElementsByTagName("name").item(0).getTextContent();
						application = ee.getElementsByTagName("application").item(0).getTextContent();
						module = ee.getElementsByTagName("module").item(0).getTextContent();
						other1 = ee.getElementsByTagName("other1").item(0).getTextContent();
						other2 = ee.getElementsByTagName("other2").item(0).getTextContent();
						other3 = ee.getElementsByTagName("other3").item(0).getTextContent();
						highlander = (ee.getElementsByTagName("highlander").item(0).getTextContent().equals("true")) ? true : false;
						jarPath = e.getElementsByTagName("path").item(0).getTextContent();

						NodeList l = ee.getElementsByTagName("parameter");

						for (int j = 0; j < l.getLength(); j++)
						{
							Element t = (Element) l.item(j);

							JobDefParameter jdp = new JobDefParameter();
							jdp.setKey(t.getElementsByTagName("key").item(0).getTextContent());
							jdp.setValue(t.getElementsByTagName("value").item(0).getTextContent());
							jd.getParameters().add(jdp);
						}

						jd.setCanBeRestarted(canBeRestarted);
						jd.setJavaClassName(javaClassName);
						// jd.setParameters(parameters);
						jd.setFilePath(filePath);
						jd.setQueue(queue);
						jd.setMaxTimeRunning(maxTimeRunning);
						jd.setApplicationName(applicationName);
						jd.setApplication(application);
						jd.setModule(module);
						jd.setOther1(other1);
						jd.setOther2(other2);
						jd.setOther3(other3);
						jd.setHighlander(highlander);
						jd.setJarPath(jarPath);

						em.persist(jd);
						em.getTransaction().commit();

						jqmlogger.debug("XML parsed");
					}
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
