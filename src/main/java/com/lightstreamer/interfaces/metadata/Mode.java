/*
 *  Copyright (c) Lightstreamer Srl
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package com.lightstreamer.interfaces.metadata;

import javax.annotation.Nonnull;

/**
 * Used by Lightstreamer to identify a publishing Mode. The different Modes handled by
 * Lightstreamer Kernel can be uniquely identified by the static constants
 * defined in this class.
 * See the technical documents for a detailed description of Modes.
 *
 * @author              Dario Crivelli
 * last author:         $Author: Aalinone $
 * @version             $Revision: 40022 $
 * last modified:       $Modtime: 17/01/07 16.14 $
 * last check-in:       $Date: 17/01/07 16.15 $
 */
public class Mode {

    private String name;

    /**
     * Constructor Mode.
     */
    private Mode(@Nonnull String name) {
        this.name = name;
    }

    /**
     * The RAW Mode.
     */
    @Nonnull
    public static final Mode RAW = new Mode("RAW");

    /**
     * The MERGE Mode.
     */
    @Nonnull
    public static final Mode MERGE = new Mode("MERGE");

    /**
     * The DISTINCT Mode.
     */
    @Nonnull
    public static final Mode DISTINCT = new Mode("DISTINCT");

    /**
     * The COMMAND Mode.
     */
    @Nonnull
    public static final Mode COMMAND = new Mode("COMMAND");

    /**
     * Gets the internal name of the publishing Mode. 
     * 
     * @return the Mode name.
     */
    @Nonnull
    public String toString() {
        return name;
    }

}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
