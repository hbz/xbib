/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.query.cql;

/**
 *  Abstract syntax tree of CQL - Index
 *  The Index consists of <b>context<b> and <b>name<b>
 *  The default context is "cql" and is of the same concept like a namespace.
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Index extends AbstractNode {

    private String context;
    private String name;

    public Index(String name) {
        this.name = name;
        int pos = name.indexOf('.');
        if (pos > 0) {
            this.context = name.substring(0, pos);
            this.name = name.substring(pos + 1);
        }
    }

    public Index(SimpleName name) {
        this(name.getName());
    }

    /**
     *
     * @return the context of the index
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the name of the index
     *
     * @return the name of the index
     */
    public String getName() {
        return name;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return context != null ? context + "." + name : name;
    }

}