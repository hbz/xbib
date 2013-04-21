/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.rdf.io.xml;

import java.util.Stack;
import javax.xml.namespace.QName;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.Factory;

/**
 * The XML resource handler can create nested RDF resources from arbitrary XML.
 */
public abstract class XmlResourceHandler extends AbstractXmlHandler {

    protected final Factory factory = Factory.getInstance();
    private Stack<Resource> stack = new Stack();

    public XmlResourceHandler(ResourceContext resourceContext) {
        super(resourceContext);
    }

    @Override
    public void openResource() {
        super.openResource();
        stack.push(resourceContext.resource());
    }

    @Override
    public void closeResource() {
        super.closeResource();
        stack.clear();
    }

    @Override
    public void openPredicate(QName parent, QName name, int level) {
        // nested resource creation
        // always create newResource, even if there will be only a single literal. We will compact later.
        Resource r = stack.peek().newResource(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        stack.push(r);
    }

    @Override
    public void addToPredicate(String content) {
    }

    @Override
    public void closePredicate(QName parent, QName name, int level) {
        Property p = (Property) factory.asPredicate(makePrefix(name.getPrefix()) + ":" + name.getLocalPart());
        Resource r = stack.pop();
        if (level < 0) {
            // it's a newResource
            if (!stack.isEmpty()) {
                stack.peek().add(p, r);
            }
        } else {
            // it's a property
            String s = content();
            if (s != null) {
                r.add(p, factory.newLiteral(toLiteral(name, s)));
                // compact because it has only a single value
                if (!stack.isEmpty()) {
                    stack.peek().compactPredicate(p);
                }
            }
        }
    }

    protected Object toLiteral(QName name, String content) {
        return content;
    }
}
