package org.alfresco.share.util;

import org.alfresco.po.share.util.PageUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RemoteUtil extends AbstractUtils
{

    private static Log logger = LogFactory.getLog(RemoteUtil.class);

    private static SshCommandProcessor commandProcessor;

    private static void initConnection()
    {
        commandProcessor = new SshCommandProcessor();
        commandProcessor.connect();
    }

    public static void applyIptables(String dropPocketsNode)
    {
        initConnection();
        String server = JmxUtils.getAddress(dropPocketsNode);
        commandProcessor.executeCommand("service iptables start");
        commandProcessor.executeCommand("iptables -A INPUT -p tcp -s " + server + " -j DROP");
        commandProcessor.executeCommand("iptables -A OUTPUT -p tcp -s " + server + " -j DROP");
        logger.info("Temporarily apply a rule using iptables to drop all packets coming from (outcoming to) " + server + " to " + sshHost);
    }

    public static void applyIptablesAllPorts()
    {
        initConnection();
        commandProcessor.executeCommand("service iptables start");
        commandProcessor.executeCommand("iptables -F");
        commandProcessor.executeCommand("iptables -A INPUT -p tcp -m tcp -m multiport ! --dports " + serverShhPort + " -j DROP");
        logger.info("Set iptables for all ports except port '" + serverShhPort + "' for host " + sshHost);
    }

    public static void removeIpTables(String acceptPocketsNode)
    {

        initConnection();
        String server = PageUtils.getAddress(acceptPocketsNode).replaceAll("(:\\d{1,5})?", "");
        commandProcessor.executeCommand("iptables -D INPUT -s " + server + " -j DROP");
        commandProcessor.executeCommand("iptables -F");
        commandProcessor.executeCommand("service iptables stop");
        logger.info("Turn the filter off iptables to drop all packets coming from " + server + " to " + sshHost);
    }

    public static void stopAlfresco(String alfrescoPath)
    {
        initConnection();
        commandProcessor.executeCommand(alfrescoPath + "/./alfresco.sh stop");
        logger.info("Stop alfresco server " + sshHost);
        logger.info("Execute command: " + alfrescoPath + "/./alfresco.sh stop");
    }

    public static void startAlfresco(String alfrescoPath)
    {
        initConnection();
        commandProcessor.executeCommand(alfrescoPath + "/./alfresco.sh start");
        logger.info("Start alfresco server " + sshHost);
        logger.info("Execute command: " + alfrescoPath + "/./alfresco.sh start");
    }

    public static void waitForAlfrescoStartup(String nodeURL, long starttime)
    {
        long before = System.currentTimeMillis();
        try
        {
            while (!HttpUtil.alfrescoRunning(nodeURL) || ((System.currentTimeMillis() - before) * 0.001) < 2000 * 1000)
            {
                Thread.sleep(5000);
                logger.info("Retrying request to Alfresco login page");
                logger.info((System.currentTimeMillis() - before) / 1000 + " of " + 2000 + " " + " maximum seconds passed after sending start signal");

                if (starttime > 0)
                    if (!(((System.currentTimeMillis() - before) * 0.001) < starttime))
                    {
                        logger.info("Alfresco application isn't up during expected time: " + starttime + " seconds");
                        break;
                    }
                if (HttpUtil.alfrescoRunning(nodeURL))
                {
                    logger.info("Alfresco application is up and running");
                    break;
                }

                if (((System.currentTimeMillis() - before) * 0.001) >= 2000)
                {
                    throw new InterruptedException("Timeout on waiting for Alfresco startup for " + 2000 + " seconds." + System.getProperty("line.separator")
                            + "Try to increase timeout in \"build.properties\" file or inspect \"alfresco.log\"" + " for details");
                }
            }
        }
        catch (Throwable ex)
        {
            logger.info(ex.getMessage());
            System.exit(1);
        }
    }

    public static void waitForAlfrescoShutdown(String nodeURL, long stoptime)
    {
        try
        {
            long before = System.currentTimeMillis();
            logger.info("Waiting for Alfresco instance to shut down");
            while (HttpUtil.alfrescoRunning(nodeURL) || ((System.currentTimeMillis() - before) * 0.001) < 2000)
            {
                Thread.sleep(5000);

                if (stoptime > 0)
                    if (!(((System.currentTimeMillis() - before) * 0.001) < stoptime))
                    {
                        logger.info("Expected shut down time is ended: " + stoptime + " seconds");
                        break;
                    }

                if (!HttpUtil.alfrescoRunning(nodeURL))
                {
                    logger.info("Alfresco application is shut down");
                    break;
                }

                if ((((System.currentTimeMillis() - before) * 0.001) - stoptime) >= 2000)
                {
                    throw new InterruptedException("Timeout on waiting for Alfresco shutdown for " + 2000 + " seconds." + System.getProperty("line.separator")
                            + "Try to increase timeout in \"build.properties\" file or inspect \"alfresco.log\"" + " for details");
                }
            }

        }

        catch (Throwable ex)
        {
            logger.info(ex.getMessage());
            System.exit(1);
        }
    }

    public static String getCygwinPath(String winPath)
    {
        return String.format("`cygpath -u '%s'`", winPath);
    }
}
