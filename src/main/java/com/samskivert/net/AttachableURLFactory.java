//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.net;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import java.util.HashMap;

import static com.samskivert.Log.log;

/**
 * Allows other entities in an application to register URLStreamHandler
 * classes for protocols of their own making.
 */
public class AttachableURLFactory implements URLStreamHandlerFactory
{
    /**
     * Register a URL handler.
     *
     * @param protocol the protocol to register.
     * @param handlerClass a Class of type java.net.URLStreamHandler
     */
    public static void attachHandler (
        String protocol, Class<? extends URLStreamHandler> handlerClass)
    {
        // set up the factory.
        if (_handlers == null) {
            _handlers = new HashMap<String,Class<? extends URLStreamHandler>>();

            // There are two ways to do this.

            // Method 1, which is the only one that seems to work under
            // Java Web Start, is to register a factory. This can throw an
            // Error if another factory is already registered. We let that
            // error bubble on back.
            URL.setURLStreamHandlerFactory(new AttachableURLFactory());

            // Method 2 seems like a better idea but doesn't work under
            // Java Web Start. We add on a property that registers this
            // very class as the handler for the resource property. It
            // would be instantiated with Class.forName().
            // (And I did check, it's not dasho that is preventing this
            // from working under JWS, it's something else.)
            /*
            // dug up from java.net.URL
            String HANDLER_PROP = "java.protocol.handler.pkgs";

            String prop = System.getProperty(HANDLER_PROP, "");
            if (!"".equals(prop)) {
            prop += "|";
            }
            prop += "com.threerings";
            System.setProperty(HANDLER_PROP, prop);
            */
        }

        _handlers.put(protocol.toLowerCase(), handlerClass);
    }

    /**
     * Do not let others instantiate us.
     */
    private AttachableURLFactory ()
    {
    }

    // documentation inherited from interface URLStreamHandlerFactory
    public URLStreamHandler createURLStreamHandler (String protocol)
    {
        Class<? extends URLStreamHandler> handler = _handlers.get(protocol.toLowerCase());
        if (handler != null) {
            try {
                return handler.newInstance();
            } catch (Exception e) {
                log.warning("Unable to instantiate URLStreamHandler", "protocol", protocol,
                            "cause", e);
            }
        }
        return null;
    }

    /** A mapping of protocol name to handler classes. */
    protected static HashMap<String,Class<? extends URLStreamHandler>> _handlers;
}
