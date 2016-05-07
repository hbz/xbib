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
package org.xbib.etl.marc.dialects.mab;

import org.xbib.marc.Field;
import org.xbib.marc.FieldList;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.BlankMemoryResource;

import java.io.IOException;

public class MABDirectQueue extends MABEntityQueue {

    public MABDirectQueue(String packageName, int workers, String... paths) throws Exception {
        super(packageName, workers, paths);
    }

    @Override
    public MABDirectWorker newWorker() {
        return new MABDirectWorker();
    }

    private class MABDirectWorker extends MABWorker {

        @Override
        public void build(FieldList fields) throws IOException {
            if (fields == null) {
                return;
            }
            String tag = fields.iterator().next().tag();
            Resource tagResource = new BlankMemoryResource();
            for (Field field : fields) {
                String data = field.data();
                if (data == null || data.isEmpty()) {
                    continue;
                }
                if ("001".equals(field.tag())) {
                    getWorkerState().setRecordIdentifier(data);
                }
                if (field.isControlField()) {
                    tagResource.add("_", data);
                } else {
                    if (field.indicator() != null) {
                        // with indicator
                        tagResource.newResource(field.indicator().replaceAll("\\s", "_")).add(field.isSubField() ? field.subfieldId() : "_", data);
                    } else {
                        // without indicator
                        tagResource.add(field.isSubField() ? field.subfieldId() : "_", data);
                    }
                }
            }
            getWorkerState().getResource().add(tag, tagResource);
        }
    }

}