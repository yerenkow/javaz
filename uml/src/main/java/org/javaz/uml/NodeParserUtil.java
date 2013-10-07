package org.javaz.uml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class NodeParserUtil
{
    public static String getNodeAttribute(Node node, String s)
    {
        NamedNodeMap attributes = node.getAttributes();
        for (int k = 0; k < attributes.getLength(); k++)
        {
            Node nodeAttr = attributes.item(k);
            if (nodeAttr.getNodeName().equals(s))
            {
                return nodeAttr.getNodeValue();
            }
        }
        return null;
    }

    public static String getNodeAttributeDeep(NodeList nodelList, String nodeName, String nodeAttr)
    {
        for (int i = 0; i < nodelList.getLength(); i++)
        {
            Node node = nodelList.item(i);
            if (node.getNodeName().equals(nodeName))
            {
                return getNodeAttribute(node, nodeAttr);
            }
            if (node.getChildNodes().getLength() > 0)
            {
                String result = getNodeAttributeDeep(node.getChildNodes(), nodeName, nodeAttr);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    public static Node getNodeChildDeepStatic(NodeList nodelList, String nodeName)
    {
        for (int i = 0; i < nodelList.getLength(); i++)
        {
            Node node = nodelList.item(i);
            if (node.getNodeName().equals(nodeName))
            {
                return node;
            }
            if (node.getChildNodes().getLength() > 0)
            {
                Node result = getNodeChildDeepStatic(node.getChildNodes(), nodeName);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    public static String getNodeValueByAttribute(Node parent, String propName, String value, String what)
    {
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            try
            {
                if (node.getAttributes() != null && node.getAttributes().getNamedItem(propName).getNodeValue().equals(value))
                {
                    Node nodeChildDeepStatic = NodeParserUtil.getNodeChildDeepStatic(node.getChildNodes(), what);
                    return nodeChildDeepStatic.getFirstChild().getNodeValue();
                }
            }
            catch (Exception e)
            {
            }
        }

        return "";
    }


}
