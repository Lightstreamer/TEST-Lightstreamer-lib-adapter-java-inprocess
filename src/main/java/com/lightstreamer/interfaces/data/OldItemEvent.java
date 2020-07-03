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


package com.lightstreamer.interfaces.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Enumeration;

/**
 * Provides to the Data Adapter a special interface for creating ItemEvents
 * in order to send updates to Lightstreamer Kernel.
 * If the external feed supplies Item data through old style objects,
 * like java.util.Hashtable or JMS MapMessage objects, that provide a
 * java.util.Enumeration to the Fields, then wrapping it into an OldItemEvent
 * can be simpler than wrapping it into an ItemEvent.
 * <BR>
 * <BR>All implementation methods must execute fast and must be nonblocking.
 * All information needed to extract data must be provided at object
 * construction. If the implementation were slow, the whole update delivery
 * process, even for different sessions, would be slowed down.
 *
 * @author          ...
 * last author:     $Author: Aalinone $
 * @version         $Revision: 53184 $
 * last modified:   $Modtime: 17/01/07 16.14 $
 * last check-in:   $Date: 17/01/07 16.15 $
 *
 * @see ItemEventListener
 * @see ItemEvent
 */
public interface OldItemEvent {

    /**
     * Returns an iterator to browse the names of the supplied Fields,
     * expressed as String.
     * If the Item to which this ItemEvent refers has been subscribed
     * by setting the needsIterator flag as false, the method can return
     * a null value.
     *
     * @return an iterator, or null.
     * @see DataProvider
     */
	@Nullable
    Enumeration getNames();

    /**
     * Returns the value of a named Field (null is a legal value too). Returns
     * null if the Field is not reported in the ItemEvent.
     * <BR>The value can be expressed as either a String or a byte array;
     * see {@link ItemEvent#getValue(String)} for details.
     *
     * @param  name  A Field name.
     * @return  a String or a byte array containing the Field value, or null.
     */
	@Nullable
    Object getValue(@Nonnull String name);
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
