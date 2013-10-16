/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.parser.odf.OpenDocumentParser;


/**
 * @see TikaAutoMetadataExtracter
 * 
 * @author Nick Burch
 */
public class TikaAutoMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private static Log logger = LogFactory.getLog(TikaAutoMetadataExtracterTest.class);
    
    private TikaAutoMetadataExtracter extracter;
    private static final QName TIKA_MIMETYPE_TEST_PROPERTY =
       QName.createQName("TikaMimeTypeTestProp");

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        TikaConfig config = (TikaConfig)ctx.getBean("tikaConfig");
        extracter = new TikaAutoMetadataExtracter(config);
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
        
        // Attach some extra mappings, using the Tika
        //  metadata keys namespace
        // These will be tested later
        HashMap<String, Set<QName>> newMap = new HashMap<String, Set<QName>>(
              extracter.getMapping()
        );
        
        Set<QName> tlaSet = new HashSet<QName>();
        tlaSet.add(TIKA_MIMETYPE_TEST_PROPERTY);
        newMap.put( Metadata.CONTENT_TYPE, tlaSet );
        
        extracter.setMapping(newMap);
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        ArrayList<String> mimeTypes = new ArrayList<String>();
        for (Parser p : new Parser[] {
                 new OfficeParser(), new OpenDocumentParser(),
                 new Mp3Parser(), new OOXMLParser()
        }) {
           Set<MediaType> mts = p.getSupportedTypes(new ParseContext());
           for (MediaType mt : mts) 
           {
              mimeTypes.add(mt.toString());
           }
        }
        
        for (String mimetype : mimeTypes)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    /**
     * Test several different files
     * Note - doesn't use extractFromMimetype
     */
    public void testSupportedMimetypes() throws Exception
    {
        String[] testFiles = new String[] {
              ".doc", ".docx", ".xls", ".xlsx",
              ".ppt", ".pptx", 
              //".vsd", // Our sample file lacks suitable metadata
              "2010.dwg",
              "2003.mpp", "2007.mpp",
              ".pdf",
              ".odt",
        };
           
        AutoDetectParser ap = new AutoDetectParser();
        for (String fileBase : testFiles)
        {
           String filename = "quick" + fileBase;
           URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("quick/" + filename);
           File file = new File(url.getFile());
           
           // Cheat and ask Tika for the mime type!
           Metadata metadata = new Metadata();
           metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
           MediaType mt = ap.getDetector().detect(TikaInputStream.get(file), metadata);
           String mimetype = mt.toString();
           
           if (logger.isDebugEnabled())
           {
              logger.debug("Detected mimetype " + mimetype + " for quick test file " + filename);
           }

           // Have it processed
           Map<QName, Serializable> properties = extractFromFile(file, mimetype);
           
           // check we got something
           assertFalse("extractFromMimetype should return at least some properties, " +
           		"none found for " + mimetype + " - " + filename,
              properties.isEmpty());
           
           // check common metadata
           testCommonMetadata(mimetype, properties);
           // check file-type specific metadata
           testFileSpecificMetadata(mimetype, properties);
        }
    }
    
    @Override
    protected boolean skipAuthorCheck(String mimetype) { return true; }

    @Override
    protected boolean skipDescriptionCheck(String mimetype) 
    {
       if(mimetype.endsWith("/ogg")) 
       {
          return true;
       }
       return false; 
    }

   /**
    * We also provide the creation date - check that
    */
   protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) 
   {
      
      // Check for extra fields
      // Author isn't there for the OpenDocument ones
      if(mimetype.indexOf(".oasis.") == -1 && !mimetype.endsWith("/ogg") && !mimetype.endsWith("dwg")) 
      {
         assertEquals(
               "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
               "Nevin Nollop",
               DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
      }
      
      // Ensure that we can also get things which are standard
      //  Tika metadata properties, if we so choose to
      assertTrue( 
            "Test Property " + TIKA_MIMETYPE_TEST_PROPERTY + " not found for mimetype " + mimetype,
            properties.containsKey(TIKA_MIMETYPE_TEST_PROPERTY)
      );
      assertEquals(
            "Test Property " + TIKA_MIMETYPE_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
            mimetype,
            DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(TIKA_MIMETYPE_TEST_PROPERTY)));
      
      // Extra media checks for music formats
      if(mimetype.startsWith("audio"))
      {
         assertEquals(
               "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
               "Hauskaz",
               DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
         QName artistQ = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "artist"); 
         assertEquals(
               "Property " + artistQ + " not found for mimetype " + mimetype,
               "Hauskaz",
               DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(artistQ)));
      }
   }

   /**
    * We don't have explicit extractors for most image and video formats.
    * Instead, these will be handled by the Auto Tika Parser, and
    *  this test ensures that they are
    */
   @SuppressWarnings("deprecation")
public void testImageVideo() throws Throwable {
      Map<String, Serializable> p;
      
      // Image
      p = openAndCheck(".jpg", "image/jpeg");
      assertEquals("409 pixels", p.get("Image Width"));
      assertEquals("92 pixels", p.get("Image Height"));
      assertEquals("8 bits", p.get("Data Precision"));
      
      p = openAndCheck(".gif", "image/gif");
      assertEquals("409", p.get("width"));
      assertEquals("92", p.get("height"));
      
      p = openAndCheck(".png", "image/png");
      assertEquals("409", p.get("width"));
      assertEquals("92", p.get("height"));
      assertEquals("8 8 8", p.get("Data BitsPerSample"));
      assertEquals("none", p.get("Transparency Alpha"));
      
      p = openAndCheck(".bmp", "image/bmp");
      assertEquals("409", p.get("width"));
      assertEquals("92", p.get("height"));
      assertEquals("8 8 8", p.get("Data BitsPerSample"));
      
      
      // Geo tagged image
      p = openAndCheck("GEO.jpg", "image/jpeg");
      // Check raw EXIF properties
      assertEquals("100 pixels", p.get("Image Width"));
      assertEquals("68 pixels", p.get("Image Height"));
      assertEquals("8 bits", p.get("Data Precision"));
      // Check regular Tika properties
      assertEquals(QUICK_TITLE, p.get(Metadata.COMMENT));
      assertEquals("canon-55-250", p.get(Metadata.SUBJECT));
      // Check namespace'd Tika properties
      assertEquals("12.54321", p.get("geo:lat"));
      assertEquals("-54.1234", p.get("geo:long"));
      assertEquals("100", p.get("tiff:ImageWidth"));
      assertEquals("68", p.get("tiff:ImageLength"));
      assertEquals("Canon", p.get("tiff:Make"));
      assertEquals("5.6", p.get("exif:FNumber"));
      
      // Map and check
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
      ContentReader reader = new FileContentReader(open("GEO.jpg"));
      reader.setMimetype("image/jpeg");
      extracter.extract(reader, properties);
      // Check the geo bits
      assertEquals(12.54321, properties.get(ContentModel.PROP_LATITUDE));
      assertEquals(-54.1234, properties.get(ContentModel.PROP_LONGITUDE));
      // Check the exif bits
      assertEquals(100, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "pixelXDimension")));
      assertEquals(68, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "pixelYDimension")));
      assertEquals(0.000625, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "exposureTime")));
      assertEquals(5.6, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "fNumber")));
      assertEquals(false, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "flash")));
      assertEquals(194.0, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "focalLength")));
      assertEquals("400", properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "isoSpeedRatings")));
      assertEquals("Canon", properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "manufacturer")));
      assertEquals("Canon EOS 40D", properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "model")));
      assertEquals("Adobe Photoshop CS3 Macintosh", properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "software")));
      assertEquals(null, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "orientation")));
      assertEquals(240.0, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "xResolution")));
      assertEquals(240.0, properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "yResolution")));
      assertEquals("Inch", properties.get(QName.createQName(NamespaceService.EXIF_MODEL_1_0_URI, "resolutionUnit")));
   }
   private File open(String fileBase) throws Throwable {
      String filename = "quick" + fileBase;
      URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("quick/" + filename);
      File file = new File(url.getFile());
      assertTrue(file.exists());
      return file;
   }
   private Map<String, Serializable> openAndCheck(String fileBase, String expMimeType) throws Throwable {
      // Get the mimetype via the MimeTypeMap 
      // (Uses Tika internally for the detection)
      File file = open(fileBase);
      ContentReader detectReader = new FileContentReader(file);
      String mimetype = mimetypeMap.guessMimetype(fileBase, detectReader);

      assertEquals("Wrong mimetype for " + fileBase, mimetype, expMimeType);
      
      // Ensure the Tika Auto parser actually handles this
      assertTrue("Mimetype should be supported but isn't: " + mimetype, extracter.isSupported(mimetype));
      
      // Now create our proper reader
      ContentReader sourceReader = new FileContentReader(file);
      sourceReader.setMimetype(mimetype);
      
      // And finally do the properties extraction
      return extracter.extractRaw(sourceReader);
   }
}
