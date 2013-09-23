/**
 *
 */
package dependencies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 *
 */
public class ListDependencies {

	private ArrayList<String> plugins = new ArrayList<String>();
	private static org.jdom.Document doc;
	private static Element rootNode;

	public ListDependencies(String path) {

		SAXBuilder sxb = new SAXBuilder();

		try
		{
			doc = sxb.build(new File(path));
			rootNode = doc.getRootElement();
			List<Element> list = rootNode.getChildren("project");
			String temp = "";
			System.out.println(list.size());

			Iterator i = list.iterator();
			while (i.hasNext())
			{
				Element c = (Element) i.next();
				System.out.println(c.getChild("groupId").getText());
			}
//			for (int i = 0; i < list.size(); i++)
//			{
//				Element node = list.get(i);
//
//					temp += node.getChild("dependency").getChildText("groupId") + ":";
//					temp += node.getChild("dependency").getChildText("artifactId") + ":";
//					temp += node.getChild("dependency").getChildText("version");
//					System.out.println("temp: " + temp);
//
//					if (!temp.equals("")) {
//						plugins.add(temp);
//						temp = "";
//					}
//				}
			}  catch (JDOMException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void printDependencies() {

		for (int i = 0; i < plugins.size(); i++)
		{
			System.out.println("ListDependencies: " + plugins.get(i));
		}
	}

	/**
	 * @return the urls
	 */
	public ArrayList<String> getUrls()
	{
		return plugins;
	}

	/**
	 * @param urls the urls to set
	 */
	public void setUrls(ArrayList<String> plugins)
	{
		this.plugins = plugins;
	}

	/**
	 * @return the doc
	 */
	public static Document getDoc()
	{
		return doc;
	}

	/**
	 * @param doc the doc to set
	 */
	public static void setDoc(Document doc)
	{
		ListDependencies.doc = doc;
	}

	/**
	 * @return the node
	 */
	public static Element getNode()
	{
		return rootNode;
	}

	/**
	 * @param node the node to set
	 */
	public static void setNode(Element node)
	{
		ListDependencies.rootNode = node;
	}

	/**
	 * @return the plugins
	 */
	public ArrayList<String> getPlugins()
	{
		return plugins;
	}
}

//var attributesSons = XMLNode.childNodes;
//var result;
//for (var i = 0; i < XMLNode.childNodes.length; ++i) {
//    var node = XMLNode.childNodes[i];
//    if (node.localName === attributeXMLtag) {
//        if (node.ELEMENT_NODE === node.nodeType) {
//            result = node.textContent;
//            break;
//        }
//        if (node.TEXT_NODE === node.nodeType) {
//            result = node.textContent;
//            break;
//        }
//        if (node.CDATA_SECTION_NODE === node.nodeType) {
//            result = node.nodeValue;
//            break;
//        }
//    }
//}
