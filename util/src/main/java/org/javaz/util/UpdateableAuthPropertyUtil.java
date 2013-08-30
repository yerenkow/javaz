package org.javaz.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * Simple auth properties file in format
 * User.Password=*.*
 * User2.Password2=Service.*,Service2.method
 * User3.Password3=Service3.method2
 * <p/>
 * Using UpdateableFilePropertyUtil beneath
 */
public class UpdateableAuthPropertyUtil
{
    private UpdateableFilePropertyUtil filePropertyUtil = null;
    private HashMap permissions = new HashMap();
    private String userPasswordSplitExpression = "\\.";
    private String methodsSplitExpression = ",";
    private long fileStampModify = 0l;

    protected static HashMap instances = new HashMap();

    public static UpdateableAuthPropertyUtil getInstance(String file)
    {
        if (!instances.containsKey(file.hashCode()))
        {
            synchronized (UpdateableAuthPropertyUtil.class)
            {
                if (!instances.containsKey(file.hashCode()))
                {
                    UpdateableAuthPropertyUtil util = new UpdateableAuthPropertyUtil(file);
                    util.updateFileIfNeeded();
                    instances.put(file.hashCode(), util);
                }
            }
        }
        return (UpdateableAuthPropertyUtil) instances.get(file.hashCode());
    }

    protected UpdateableAuthPropertyUtil(String file)
    {
        filePropertyUtil = new UpdateableFilePropertyUtil(file);
    }

    public String getUserPasswordSplitExpression()
    {
        return userPasswordSplitExpression;
    }

    public void setUserPasswordSplitExpression(String userPasswordSplitExpression)
    {
        this.userPasswordSplitExpression = userPasswordSplitExpression;
    }

    public String getMethodsSplitExpression()
    {
        return methodsSplitExpression;
    }

    public void setMethodsSplitExpression(String methodsSplitExpression)
    {
        this.methodsSplitExpression = methodsSplitExpression;
    }

    public boolean isAuthorized(String user, String password, String method)
    {
        updateFileIfNeeded();

        {
            HashMap hm = (HashMap) permissions.get("*.*".hashCode());
            if (hm != null)
            {
                if (hm.containsKey(user) && password.equals(hm.get(user)))
                {
                    return true;
                }
            }
        }

        if (method.contains("."))
        {
            String methodAsterisk = method.substring(0, method.indexOf(".") + 1) + "*";
            HashMap hm = (HashMap) permissions.get(methodAsterisk.hashCode());
            if (hm != null)
            {
                if (hm.containsKey(user) && password.equals(hm.get(user)))
                {
                    return true;
                }
            }
        }

        HashMap hm = (HashMap) permissions.get(method.hashCode());
        if (hm != null)
        {
            return hm.containsKey(user) && password.equals(hm.get(user));
        }

        return false;
    }

    protected boolean updateFileIfNeeded()
    {
        filePropertyUtil.updateFileIfNeeded();
        boolean updated = (fileStampModify != filePropertyUtil.getFileStampModify());
        if (updated)
        {
            synchronized (this)
            {
                permissions.clear();

                Properties properties = filePropertyUtil.getPropertiesCopy();
                Enumeration<?> enumeration = properties.propertyNames();
                while (enumeration.hasMoreElements())
                {
                    String userAndPassword = (String) enumeration.nextElement();
                    String methodNames = properties.getProperty(userAndPassword);


                    String[] split = userAndPassword.split(userPasswordSplitExpression);
                    if (split.length >= 2)
                    {
                        String user = split[0];
                        String pass = split[1];
                        String[] methods = methodNames.split(methodsSplitExpression);
                        for (int i = 0; i < methods.length; i++)
                        {
                            String methodName = methods[i];
                            HashMap methodPerms = (HashMap) permissions.get(methodName.hashCode());
                            if (methodPerms == null)
                            {
                                methodPerms = new HashMap();
                                permissions.put(methodName.hashCode(), methodPerms);
                            }
                            methodPerms.put(user, pass);
                        }
                    }
                    else
                    {
                        System.out.println("Unknown format :" + userAndPassword);
                    }
                }
            }
            fileStampModify = filePropertyUtil.getFileStampModify();
        }

        return updated;
    }
}
