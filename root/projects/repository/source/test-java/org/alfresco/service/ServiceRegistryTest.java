/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.service;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ServiceRegistryTest
{
    protected static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    protected ServiceRegistry serviceRegistry;

    @Before
    public void before() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
    }

    @Test
    public void testServiceRegistryGetters() throws Exception
    {
        Method[] methods = serviceRegistry.getClass().getMethods();
        for (Method method : methods)
        {
            if (method.getName().startsWith("get") && (method.getParameterTypes().length == 0))
            {
                try
                {
                    method.invoke(serviceRegistry, null);
                }
                catch (java.lang.reflect.InvocationTargetException i)
                {
                    if (i.getTargetException() instanceof UnsupportedOperationException)
                    {
                        continue;
                    }
                    fail("Failed to invoke " + method.getName() + " : " + i.getTargetException().getMessage());
                }
                catch (Exception e)
                {
                    fail("Failed to invoke " + method.getName() + " : " + e.getMessage());
                }
                
            }
        }
    }
}
