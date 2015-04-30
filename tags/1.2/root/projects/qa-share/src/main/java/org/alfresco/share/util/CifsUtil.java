package org.alfresco.share.util;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;
import org.alfresco.po.share.util.PageUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import javax.swing.ImageIcon;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.Robot;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Olga Antonik
 */
public class CifsUtil extends AbstractUtils implements Transferable, ClipboardOwner
{

    private Image image;
    private static Log logger = LogFactory.getLog(CifsUtil.class);

    /**
     * Method to add document to the Alfresco via CIFS
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param filename
     * @param contents
     * @return boolean
     */
    public static boolean addContent(String shareUrl, String username, String password, String cifsPath, String filename, String contents)
    {

        boolean successful;
        String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
        try
        {
            File file = new File(filename);
            if (!file.exists())
            {
                if (contents != null)
                {
                    FileOutputStream out = null;
                    try
                    {
                        out = new FileOutputStream(file.getAbsolutePath());
                        out.write(contents.getBytes());
                        out.close();
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException(ex.getMessage());
                    }
                    finally
                    {
                        if (out != null)
                            out.close();
                    }
                }
                else
                {
                    file.createNewFile();
                }
            }

            String user = username + ":" + password;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + filename, auth);
            SmbFileOutputStream smbFileOutputStream = null;
            FileInputStream fileInputStream = null;
            try
            {
                smbFileOutputStream = new SmbFileOutputStream(sFile);
                fileInputStream = new FileInputStream(new File(filename));

                byte[] buf = new byte[16 * 1024 * 1024];
                int len;
                while ((len = fileInputStream.read(buf)) > 0)
                {
                    smbFileOutputStream.write(buf, 0, len);
                }
                fileInputStream.close();
                smbFileOutputStream.close();
            }
            catch (Exception ex)
            {
                throw new IllegalAccessException("Seem access denied");
            }
            finally
            {
                if (fileInputStream != null)
                    fileInputStream.close();
                if (smbFileOutputStream != null)
                    smbFileOutputStream.close();
            }

            assertTrue(sFile.exists(), "File isn't added via CIFS");

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        finally
        {
            File file = new File(filename);
            if (file.exists())
            {
                file.delete();
            }
        }

        return successful;
    }

    /**
     * Method to upload document to the Alfresco via CIFS
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param file
     * @return boolean
     */
    public static boolean uploadContent(String shareUrl, String username, String password, String cifsPath, File file)
    {

        boolean successful;
        String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
        try
        {

            String user = username + ":" + password;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + file.getName(), auth);
            SmbFileOutputStream smbFileOutputStream = null;
            FileInputStream fileInputStream = null;
            try
            {
                smbFileOutputStream = new SmbFileOutputStream(sFile);
                fileInputStream = new FileInputStream(file);

                byte[] buf = new byte[1024 * 1024 * 40];
                int len;
                while ((len = fileInputStream.read(buf)) > 0)
                {
                    smbFileOutputStream.write(buf, 0, len);
                }

                fileInputStream.close();
                smbFileOutputStream.flush();
                smbFileOutputStream.close();
            }
            catch (Exception ex)
            {
                throw new IllegalAccessException("Seem access denied");
            }
            finally
            {
                if (fileInputStream != null)
                    fileInputStream.close();
                if (smbFileOutputStream != null)
                    smbFileOutputStream.close();
            }

            assertTrue(sFile.exists(), "File isn't added via CIFS");

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }

        return successful;
    }

    /**
     * Method to verify that item presents in Alfresco
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param item
     * @return boolean
     */
    public static boolean checkItem(String shareUrl, String username, String password, String cifsPath, String item)
    {
        boolean successful;
        try
        {

            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            String user = username + ":" + password;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath, auth);

            String[] listing = sFile.list();
            List<String> list = Arrays.asList(listing);

            successful = list.contains(item);

        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    /**
     * Method to verify that item presents in Alfresco
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param fileName
     * @return boolean
     */
    public static boolean deleteContent(String shareUrl, String username, String password, String cifsPath, String fileName)
    {
        boolean successful;
        try
        {

            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            String user = username + ":" + password;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + fileName, auth);
            SmbFile sFileParent = new SmbFile("smb://" + server + "/" + cifsPath + "/", auth);
            sFile.getPermission();
            if (sFile.exists())
            {
                sFile.delete();
            }
            else
            {
                Assert.fail("Item isn't exist");
            }

            String[] listing = sFileParent.list();
            List<String> list = Arrays.asList(listing);

            assertFalse(list.contains(fileName) && !sFile.exists(), "Item " + fileName + " isn't removed");

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    /**
     * Method to rename document in Alfresco via CIFS
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param oldName
     * @param newName
     * @return boolean
     */
    public static boolean renameItem(String shareUrl, String username, String password, String cifsPath, String oldName, String newName)
    {
        boolean successful;
        try
        {

            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            String user = username + ":" + password;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/", auth);
            SmbFile sFileOld = new SmbFile("smb://" + server + "/" + cifsPath + "/" + oldName, auth);
            SmbFile sFileNew = new SmbFile("smb://" + server + "/" + cifsPath + "/" + newName, auth);

            sFileOld.renameTo(sFileNew);

            String[] listing = sFile.list();
            List<String> list = Arrays.asList(listing);

            Assert.assertTrue(list.contains(newName), "Item " + oldName + " isn't renamed' to " + newName + "'");
            Assert.assertFalse(list.contains(oldName), "Item " + oldName + " isn't renamed' to " + newName + "'");

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    /**
     * Method to rename document in Alfresco via CIFS
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param filename
     * @param contents
     * @return boolean
     */
    public static boolean editContent(String shareUrl, String username, String password, String cifsPath, String filename, String contents)
    {
        boolean successful;

        try
        {
            String user = username + ":" + password;
            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + filename, auth);

            if (sFile.exists() && sFile.isFile() && sFile.canWrite())
            {
                SmbFileOutputStream sfos = null;
                try
                {
                    sfos = new SmbFileOutputStream(sFile);
                    sfos.write(contents.getBytes());
                    sfos.close();
                }
                catch (Exception ex)
                {
                    throw new IllegalAccessException("Seem access denied");
                }
                finally
                {
                    if (sfos != null)
                        sfos.close();
                }
            }
            else
                throw new IllegalAccessException();

            if (sFile.exists() && sFile.isFile() && sFile.canWrite())
            {
                SmbFileInputStream fstream = null;
                try
                {
                    fstream = new SmbFileInputStream(sFile);
                    String text = org.apache.commons.io.IOUtils.toString(fstream);
                    fstream.close();
                    Assert.assertTrue(text.contains(contents), "Expected item isn't edited '" + filename);
                }
                catch (Exception ex)
                {
                    return false;
                }
                finally
                {
                    if (fstream != null)
                        fstream.close();
                }

            }
            else if (sFile.canWrite())
            {
                Assert.fail("Item isn't exist or not file");
            }

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    /**
     * Method to check content of file
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param fileName
     * @param content
     * @return boolean
     */
    public static boolean checkContent(String shareUrl, String username, String password, String cifsPath, String fileName, String content)
    {
        StringBuilder builder;
        BufferedReader reader;
        boolean successful;
        try
        {

            String user = username + ":" + password;
            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + fileName, auth);
            try
            {
                builder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(sFile)));
                String lineReader;
                try
                {
                    while ((lineReader = reader.readLine()) != null)
                    {
                        builder.append(lineReader).append("\n");
                    }
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
                finally
                {
                    try
                    {
                        reader.close();
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                String currentContent = builder.toString();

                Assert.assertTrue(currentContent.contains(content), "Item " + fileName + " wasn't edit");

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    /**
     * Method to create folder via CIFS
     *
     * @param shareUrl
     * @param username
     * @param password
     * @param cifsPath
     * @param spaceName
     * @return boolean
     */
    public static boolean addSpace(String shareUrl, String username, String password, String cifsPath, String spaceName)
    {
        boolean successful;
        try
        {

            String user = username + ":" + password;
            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + spaceName, auth);
            try
            {
                sFile.mkdir();
            }
            catch (SmbException e)
            {
                throw new IllegalAccessException();
            }
            Assert.assertTrue(sFile.exists(), "Folder isn't added via CIFS");

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    public CifsUtil(Image image)
    {
        this.image = image;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {

        if (flavor.equals(DataFlavor.imageFlavor) && image != null)
        {
            return image;
        }
        else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        DataFlavor[] flavors = new DataFlavor[1];
        flavors[0] = DataFlavor.imageFlavor;
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        DataFlavor[] flavors = getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++)
        {
            if (flavor.equals(flavors[i]))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void lostOwnership(Clipboard arg0, Transferable arg1)
    {
        // TODO Auto-generated method stub

    }

    public static boolean copyFolder(String shareUrl, String username, String password, String cifsPath, String destination)
    {
        boolean successful;
        try
        {

            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            String user = username + ":" + password;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFileOld = new SmbFile("smb://" + server + "/" + cifsPath, auth);
            SmbFile sFileNew = new SmbFile("smb://" + server + "/" + destination, auth);

            sFileOld.copyTo(sFileNew);

            successful = true;
        }
        catch (Exception ex)
        {
            successful = false;
        }
        return successful;
    }

    /**
     * Verifies if a folder or file exists each polling time untill the timeout is reached
     * If it finds the item it returns true the moment it is found if not it will return false after the timeout
     *
     * @param timeoutSECONDS
     * @param pollingTimeMILISECONDS
     * @param path
     * @return Boolean
     */
    public static Boolean checkDirOrFileExists(int timeoutSECONDS, int pollingTimeMILISECONDS, String path)
    {
        long counter = 0;
        boolean existence = false;
        while (counter < TimeUnit.SECONDS.toMillis(timeoutSECONDS))
        {
            File test = new File(path);
            if (test.exists())
            {
                existence = true;
                break;
            }
            else
            {
                try
                {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                counter = counter + 200;
            }
        }
        return existence;
    }

    /**
     * Verifies if a folder or file doesn't exists each polling time untill the timeout is reached
     * If it doesn't find the item it returns true instantly if not it will return false after the timeout
     *
     * @param timeoutSECONDS
     * @param pollingTimeMILISECONDS
     * @param path
     * @return Boolean
     */
    public static Boolean checkDirOrFileNotExists(int timeoutSECONDS, int pollingTimeMILISECONDS, String path)
    {
        long counter = 0;
        boolean existence = false;
        while (counter < TimeUnit.SECONDS.toMillis(timeoutSECONDS))
        {
            File test = new File(path);
            if (test.exists())
            {
                try
                {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                counter = counter + 200;

            }
            else
            {
                existence = true;
                break;
            }
        }
        return existence;
    }

    /**
     * Verifies if a hidden file doesn't exists on the specified path each 200ms untill the timeout is reached
     * If it doesn't find the item it returns true instantly if not it will return false after the timeout
     *
     * @param path
     * @param extension
     * @param timeout
     * @return Boolean
     */
    public static Boolean checkTemporaryFileDoesntExists(String path, String extension, int timeout)
    {
        long counter = 0;
        boolean check = false;
        boolean existence = true;
        while (counter < TimeUnit.SECONDS.toMillis(timeout))
        {
            File test = new File(path);
            for (File element : test.listFiles())
            {
                if (element.isHidden() && element.getName().contains(extension))
                {
                    existence = false;
                    break;
                }
            }
            if (existence)
            {
                check = true;
                break;
            }
            else
            {
                try
                {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                counter = counter + 200;
                existence = true;
            }
        }
        return check;
    }

    public static void uploadImageInOffice(String image) throws AWTException
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        ImageIcon icon = new ImageIcon(image);
        CifsUtil clipboardImage = new CifsUtil(icon.getImage());
        clipboard.setContents(clipboardImage, clipboardImage);

        Robot r = new Robot();
        r.keyPress(KeyEvent.VK_CONTROL);
        r.keyPress(KeyEvent.VK_V);
        r.keyRelease(KeyEvent.VK_CONTROL);
        r.keyRelease(KeyEvent.VK_V);
    }

    public static boolean downloadContent(String shareUrl, String username, String password, String cifsPath, String fileName)
    {
        boolean successful = false;
        try
        {

            String user = username + ":" + password;
            String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

            SmbFile sFile = new SmbFile("smb://" + server + "/" + cifsPath + "/" + fileName, auth);

                FileOutputStream outputStream = null;
            SmbFileInputStream smbfileInputStream = null;
            try
            {
                    outputStream = new FileOutputStream(downloadDirectory + fileName);
                smbfileInputStream = new SmbFileInputStream(sFile);

                byte[] buf = new byte[16 * 1024 * 1024];
                int len;
                while ((len = smbfileInputStream.read(buf)) != -1)
                {
                        outputStream.write(buf, 0, len);
                }
                successful = true;
            }
            catch (IOException ex)
            {
                    throw new RuntimeException("Content isn't downloaded", ex);
                }
                finally
                {
                    if (smbfileInputStream != null)
                        smbfileInputStream.close();
                    if (outputStream != null)
                    {
                        outputStream.flush();
                        outputStream.close();
                    }
            }

        }
        catch (IOException ex)
        {
            throw new RuntimeException("Content isn't downloaded", ex);
        }

        return successful;
    }

    /**
     * Authenticate a user.
     *
     * @param login
     * @param password
     * @param shareUrl
     * @return true if the given password matches the password for this user
     */

    public static boolean doLoginCheck(String shareUrl, String login, String password)
    {

        String server = PageUtils.getAddress(shareUrl).replaceAll("(:\\d{1,5})?", "");
        boolean userAuthenticated = false;

        try
        {
            UniAddress dc = UniAddress.getByName(server, true);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(server, login, password);
            SmbSession.logon(dc, auth);
            userAuthenticated = true;
        }
        catch (SmbException | UnknownHostException e)
        {
            logger.error("The network name cannot be found", e);
        }
        return userAuthenticated;
    }
}
