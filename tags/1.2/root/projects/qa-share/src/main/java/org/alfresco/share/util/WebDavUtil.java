/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.share.util;

import org.alfresco.po.share.util.PageUtils;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.*;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;


import java.io.*;
import java.util.ArrayList;

/**
 * Created by olga.lokhach on 7/1/2014.
 */
public class WebDavUtil extends AbstractUtils
{

    private static Log logger = LogFactory.getLog(WebDavUtil.class);
    private static String serverUrl = PageUtils.getProtocol(shareUrl) + PageUtils.getAddress(shareUrl);

    /**
     * Method to init the client
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @return client
     */

    private static HttpClient connectServer(String shareUrl, String userName, String password)

    {
        String server = PageUtils.getAddress(shareUrl);

        try
        {
            HostConfiguration config = new HostConfiguration();
            config.setHost(server);
            HttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
            HttpConnectionManagerParams params = new HttpConnectionManagerParams();
            params.setMaxConnectionsPerHost(config, 10);
            manager.setParams(params);
            HttpClient client = new HttpClient(manager);
            Credentials credentials = new UsernamePasswordCredentials(userName, password);
            client.getState().setCredentials(AuthScope.ANY, credentials);
            return client;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Method to upload a content
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @param contentName
     * @param remoteFolderPath
     * @return true if content is uploaded
     */

    public static boolean uploadContent(String shareUrl, String userName, String password, File contentName, String remoteFolderPath)
    {

        try
        {

            HttpClient client = connectServer(shareUrl, userName, password);
            PutMethod put = new PutMethod(serverUrl + "/" + remoteFolderPath + contentName.getName());
            RequestEntity entity = new InputStreamRequestEntity(new FileInputStream(contentName));
            put.setRequestEntity(entity);
            client.executeMethod(put);
            put.releaseConnection();
            if (put.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
        return false;
    }

    public static boolean downloadContent(String shareUrl, String userName, String password, String remoteContentName, String remoteFolderPath) throws IOException {
        boolean result = false;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        GetMethod getMethod = null;
        try
        {
            HttpClient client = connectServer(shareUrl, userName, password);
            getMethod = new GetMethod(serverUrl + "/" + remoteFolderPath + remoteContentName);
            client.executeMethod(getMethod);
            inputStream = getMethod.getResponseBodyAsStream();
            outputStream = new FileOutputStream(downloadDirectory + remoteContentName);


            if (inputStream != null)
            {

                byte[] buffer = new byte[4096];
                int l;
                while ((l = inputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, l);
                }

                logger.info("Content downloaded!");
                result = true;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Content isn't downloaded", e);
        }
        finally
        {
            if (inputStream != null)
            inputStream.close();
            if (outputStream !=null)
            {
                outputStream.flush();
                outputStream.close();
            }

            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return result;
    }


    /**
     * Method to delete either a folder or a file
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @param remoteName
     * @param remoteFolderPath
     * @return true if item is deleted
     */

    public static boolean deleteItem(String shareUrl, String userName, String password, String remoteName, String remoteFolderPath)
    {

        try
        {
            HttpClient client = connectServer(shareUrl, userName, password);
            DavMethod delete = new DeleteMethod(serverUrl + "/" + remoteFolderPath + remoteName);
            client.executeMethod(delete);
            delete.releaseConnection();
            if (delete.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                return true;
            }
        }

        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
        return false;
    }

    /**
     * Method to create a folder
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @param remoteSpaceName
     * @param remoteFolderPath
     * @return true if folder is created
     */

    public static boolean createFolder(String shareUrl, String userName, String password, String remoteSpaceName, String remoteFolderPath)
    {

        try
        {
            HttpClient client = connectServer(shareUrl, userName, password);
            DavMethod mkdir = new MkColMethod(serverUrl + "/" + remoteFolderPath + remoteSpaceName + "/");
            client.executeMethod(mkdir);
            mkdir.releaseConnection();
            if (mkdir.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
            {
                return true;
            }
        }

        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
        return false;
    }

    /**
     * Method to edit a content via WebDav
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @param remoteContentName
     * @param remoteFolderPath
     * @return true if content is edited
     */

    public static boolean editContent(String shareUrl, String userName, String password, String remoteContentName, String remoteFolderPath)
    {
        if(remoteContentName.contains(" "))
        {
            remoteContentName = remoteContentName.replace(" ","%20");
        }

        try
        {

            HttpClient client = connectServer(shareUrl, userName, password);
            PutMethod edit = new PutMethod(serverUrl + "/" + remoteFolderPath + remoteContentName);
            RequestEntity entity = new InputStreamRequestEntity(new ByteArrayInputStream(userName.getBytes()));
            edit.setRequestEntity(entity);
            client.executeMethod(edit);
            edit.releaseConnection();
            if (edit.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT)
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
        return false;
    }

    /**
     * Method to get list of remote objects from WebDav
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @param remoteObject
     * @param remoteFolderPath
     * @return true if object is exist
     */

    public static boolean isObjectExists(String shareUrl, String userName, String password, String remoteObject, String remoteFolderPath)
    {

        ArrayList<String> list = new ArrayList<String>();

        if(remoteObject.contains(" "))
        {
            remoteObject = remoteObject.replace(" ","%20");
        }
        try
        {

            HttpClient client = connectServer(shareUrl, userName, password);
            DavMethod pFind = new PropFindMethod(serverUrl + "/" + remoteFolderPath,
                DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            client.executeMethod(pFind);
            MultiStatus multiStatus = pFind.getResponseBodyAsMultiStatus();
            MultiStatusResponse[] responses = multiStatus.getResponses();
            pFind.releaseConnection();

            for (MultiStatusResponse response : responses)
            {
                if (!response.getHref().equals(remoteFolderPath) || response.getHref().equals(remoteFolderPath))
                {
                    list.add(response.getHref());
                }
            }
            if (list.contains("/" + remoteFolderPath + remoteObject))
            {
                return true;
            }

        }
        catch (Exception ex)
        {

            throw new RuntimeException(ex.getMessage());
        }

        return false;
    }

    /**
     * Method to get a content
     *
     * @param shareUrl
     * @param userName
     * @param password
     * @param remoteContentName
     * @param remoteFolderPath
     * @return String message
     */

    public static String getContent(String shareUrl, String userName, String password, String remoteContentName, String remoteFolderPath)
    {
        StringBuilder content = new StringBuilder();
        BufferedReader reader;
        String inputLine;

        try
        {
            HttpClient client = connectServer(shareUrl, userName, password);
            GetMethod getMethod = new GetMethod(serverUrl + "/" + remoteFolderPath + remoteContentName);
            client.executeMethod(getMethod);
            InputStream inputStream = getMethod.getResponseBodyAsStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((inputLine = reader.readLine()) != null)
            {
                content.append(inputLine);
            }
            reader.close();
            getMethod.releaseConnection();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
        return content.toString();
    }

    public static ArrayList<String> getObjects(String shareUrl, String userName, String password, String remoteFolderPath)
    {

        ArrayList<String> list = new ArrayList<String>();

        try
        {

            HttpClient client = connectServer(shareUrl, userName, password);
            DavMethod pFind = new PropFindMethod(serverUrl + "/" + remoteFolderPath, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
            client.executeMethod(pFind);
            MultiStatus multiStatus = pFind.getResponseBodyAsMultiStatus();
            MultiStatusResponse[] responses = multiStatus.getResponses();
            pFind.releaseConnection();

            for (MultiStatusResponse response : responses)
            {
                if (!response.getHref().equals(remoteFolderPath) || response.getHref().equals(remoteFolderPath))
                {
                    list.add(response.getHref());
                }
            }

        }
        catch (Exception ex)
        {

            throw new RuntimeException(ex.getMessage());
        }

        return list;
    }

    /**
     * Method to move content
     * 
     * @param shareUrl
     * @param userName
     * @param password
     * @param remotePath
     * @param remoteNewPath
     * @param overwrite
     * @return true if folder is moved
     */
    public static boolean moveContent(String shareUrl, String userName, String password, String remotePath, String remoteNewPath, boolean overwrite)
    {

        try
        {

            HttpClient client = connectServer(shareUrl, userName, password);
            MoveMethod move = new MoveMethod(serverUrl + "/" + remotePath, serverUrl + "/" + remoteNewPath, overwrite);
            client.executeMethod(move);
            move.releaseConnection();
            if (move.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
        return false;
    }

    public static boolean renameItem(String shareUrl, String userName, String password, String remotePath, String remoteNewPath)
    {
        try
        {
            return moveContent(shareUrl, userName, password, remotePath, remoteNewPath, false);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Method to copy content
     * 
     * @param shareUrl
     * @param userName
     * @param password
     * @param remotePath
     * @param copyNewPath
     * @param overwrite
     * @return true if folder is moved
     */
    public static boolean copyContent(String shareUrl, String userName, String password, String remotePath, String copyNewPath, boolean overwrite)
    {

        try
        {

            HttpClient client = connectServer(shareUrl, userName, password);
            CopyMethod copy = new CopyMethod(serverUrl + "/" + remotePath, serverUrl + "/" + copyNewPath, overwrite);
            client.executeMethod(copy);
            copy.releaseConnection();
            if (copy.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
        return false;
    }
}
