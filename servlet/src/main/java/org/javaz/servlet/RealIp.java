package org.javaz.servlet;

/**
 *
 */
public class RealIp
{
    public static final String HEADER_IP_1 =
            System.getProperty("org.javaz.servlet.RealIp.HEADER_IP_1", "X-Forwarded-For");

    public static final String HEADER_IP_2 =
            System.getProperty("org.javaz.servlet.RealIp.HEADER_IP_2", "X-Real-IP");

    public static String getRealIp(javax.servlet.http.HttpServletRequest request)
    {
        String s = request.getHeader(HEADER_IP_1);
        if (s == null)
        {
            s = request.getHeader(HEADER_IP_2);
        }
        if (s == null)
        {
            return request.getRemoteAddr();
        }
        if (s.contains(" "))
        {
            s = s.substring(s.lastIndexOf(" ")).trim();
        }
        return s;
    }

}
