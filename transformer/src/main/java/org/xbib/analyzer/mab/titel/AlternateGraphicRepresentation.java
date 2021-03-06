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
package org.xbib.analyzer.mab.titel;

import org.xbib.etl.marc.dialects.mab.MABEntity;

import java.util.Map;

public class AlternateGraphicRepresentation extends MABEntity {

    public AlternateGraphicRepresentation(Map<String, Object> params) {
        super(params);
    }

    /*
                   "OtherScript": [
                  {
                     "6": "245-01/Cyrl",
                     "a": "Офiцiйний вiсник України",
                     "b": "щотижневий збiник актiв законодавства офiцiйне видання  = Официальный вестник Украины",
                     "c": "Мiнicтерство  Юстицiї України, Головне Державне Обьʹєднання Правової Iнформацiї та Пропаганди Правових Знань Мiнicтерства Юстицiї  України"
                  },
                  {
                     "0": [
                        "(DE-588)4061496-7",
                        "(DE-101)040614964"
                     ],
                     "4": "aut",
                     "6": "110-02/Cyrl",
                     "a": "Украïна"
                  },
                  {
                     "0": [
                        "(DE-588)5099225-9",
                        "(DE-101)940438542"
                     ],
                     "6": "710-03/Cyrl",
                     "a": "Уkpaїнa",
                     "b": "Мiнiстерство Юстицiї"
                  },
                  {
                     "6": "260-04/Cyrl",
                     "a": "Київ",
                     "b": "Голос"
                  }
               ],
     */
}
