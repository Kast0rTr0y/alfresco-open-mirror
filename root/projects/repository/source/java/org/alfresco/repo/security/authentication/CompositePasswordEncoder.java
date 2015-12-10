/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * A configurable password encoding that delegates the encoding to a Map of
 * configured encoders.
 *
 * @Author Gethin James
 */
public class CompositePasswordEncoder
{
    private static Log logger = LogFactory.getLog(CompositePasswordEncoder.class);
    private Map<String,Object> encoders;
    private String preferredEncoding;
    private String legacyEncoding;
    private String legacyEncodingProperty;

    public void setLegacyEncoding(String legacyEncoding)
    {
        this.legacyEncoding = legacyEncoding;
    }

    public void setLegacyEncodingProperty(String legacyEncodingProperty)
    {
        this.legacyEncodingProperty = legacyEncodingProperty;
    }

    public String getPreferredEncoding()
    {
        return preferredEncoding;
    }

    public String getLegacyEncoding()
    {
        return legacyEncoding;
    }

    public String getLegacyEncodingProperty()
    {
        return legacyEncodingProperty;
    }

    public void setPreferredEncoding(String preferredEncoding)
    {
        this.preferredEncoding = preferredEncoding;
    }

    public void setEncoders(Map<String, Object> encoders)
    {
        this.encoders = encoders;
    }

    /**
     * Is this the preferred encoding ?
     * @param encoding a String representing the encoding
     * @return true if is correct
     */
    public boolean isPreferredEncoding(String encoding)
    {
        return preferredEncoding.equals(encoding);
    }

    /**
     * Basic init method for checking mandatory properties
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "encoders", encoders);
        PropertyCheck.mandatory(this, "preferredEncoding", preferredEncoding);
        PropertyCheck.mandatory(this, "legacyEncoding", legacyEncoding);
        PropertyCheck.mandatory(this, "legacyEncodingProperty", legacyEncodingProperty);
    }

    /**
     * Encode a password
     * @param rawPassword mandatory password
     * @param salt optional salt
     * @param encodingChain mandatory encoding chain
     * @return the encoded password
     */
    public String encodePassword(String rawPassword, Object salt, List<String> encodingChain) {

        ParameterCheck.mandatoryString("rawPassword", rawPassword);
        ParameterCheck.mandatoryCollection("encodingChain", encodingChain);
        String encoded = new String(rawPassword);
        for (String encoderKey : encodingChain)
        {
            encoded = encode(encoderKey, encoded, salt);

        }
        if (encoded == rawPassword) throw new AlfrescoRuntimeException("No password encoding specified. "+encodingChain);
        return encoded;
    }

    /**
     *  Encode a password using the specified encoderKey
     * @param encoderKey the encoder to use
     * @param rawPassword  mandatory password
     * @param salt optional salt
     * @return the encoded password
     */
    protected String encode(String encoderKey, String rawPassword, Object salt)
    {
       ParameterCheck.mandatoryString("rawPassword", rawPassword);
       ParameterCheck.mandatoryString("encoderKey", encoderKey);
       Object encoder = encoders.get(encoderKey);
       if (encoder == null) throw new AlfrescoRuntimeException("Invalid encoder specified: "+encoderKey);
       if (encoder instanceof net.sf.acegisecurity.providers.encoding.PasswordEncoder)
       {
           net.sf.acegisecurity.providers.encoding.PasswordEncoder pEncoder = (net.sf.acegisecurity.providers.encoding.PasswordEncoder) encoder;
           if (logger.isDebugEnabled()) {
               logger.debug("Encoding using acegis PasswordEncoder: "+encoderKey);
           }
           return pEncoder.encodePassword(rawPassword, salt);
       }
       if (encoder instanceof org.springframework.security.crypto.password.PasswordEncoder)
       {
           org.springframework.security.crypto.password.PasswordEncoder passEncoder = (org.springframework.security.crypto.password.PasswordEncoder) encoder;
           if (logger.isDebugEnabled()) {
               logger.debug("Encoding using spring PasswordEncoder: "+encoderKey);
           }
           return passEncoder.encode(rawPassword);
       }

       throw new AlfrescoRuntimeException("Unsupported encoder specified: "+encoderKey);
    }

    /**
     * Does the password match?
     * @param rawPassword  mandatory password
     * @param encodedPassword mandatory hashed version
     * @param salt optional salt
     * @param encodingChain mandatory encoding chain
     * @return true if they match
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword, Object salt, List<String> encodingChain)
    {
        ParameterCheck.mandatoryString("rawPassword", rawPassword);
        ParameterCheck.mandatoryString("encodedPassword", encodedPassword);
        ParameterCheck.mandatoryCollection("encodingChain", encodingChain);
        if (encodingChain.size() > 1)
        {
            String lastEncoder = encodingChain.get(encodingChain.size() - 1);
            String encoded = encodePassword(rawPassword,salt, encodingChain.subList(0,encodingChain.size()-1));
            return matches(lastEncoder,encoded,encodedPassword,salt);
        }

        if (encodingChain.size() == 1)
        {
            return matches(encodingChain.get(0), rawPassword, encodedPassword, salt);
        }
        return false;
    }

    /**
     * Does the password match?
     * @param encoderKey the encoder to use
     * @param rawPassword  mandatory password
     * @param encodedPassword mandatory hashed version
     * @param salt optional salt
     * @return true if they match
     */
    protected boolean matches(String encoderKey, String rawPassword, String encodedPassword, Object salt)
    {
        ParameterCheck.mandatoryString("rawPassword", rawPassword);
        ParameterCheck.mandatoryString("encodedPassword", encodedPassword);
        ParameterCheck.mandatoryString("encoderKey", encoderKey);
        Object encoder = encoders.get(encoderKey);
        if (encoder == null) throw new AlfrescoRuntimeException("Invalid matches encoder specified: "+encoderKey);
        if (encoder instanceof net.sf.acegisecurity.providers.encoding.PasswordEncoder)
        {
            net.sf.acegisecurity.providers.encoding.PasswordEncoder pEncoder = (net.sf.acegisecurity.providers.encoding.PasswordEncoder) encoder;
            if (logger.isDebugEnabled()) {
                logger.debug("Matching using acegis PasswordEncoder: "+encoderKey);
            }
            return pEncoder.isPasswordValid(encodedPassword, rawPassword, salt);
        }
        if (encoder instanceof org.springframework.security.crypto.password.PasswordEncoder)
        {
            org.springframework.security.crypto.password.PasswordEncoder passEncoder = (org.springframework.security.crypto.password.PasswordEncoder) encoder;
            if (logger.isDebugEnabled()) {
                logger.debug("Matching using spring PasswordEncoder: "+encoderKey);
            }
            return passEncoder.matches(rawPassword, encodedPassword);
        }
        throw new AlfrescoRuntimeException("Unsupported encoder for matching: "+encoderKey);
    }
}