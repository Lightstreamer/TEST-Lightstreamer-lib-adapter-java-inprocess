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

import java.util.Iterator;

/**
 * Provides to the Data Adapter a base interface for creating ItemEvents
 * in order to send updates to Lightstreamer Kernel.
 * An ItemEvent object contains the new values and, in some cases, the current
 * values of the Fields of an Item.
 * The interfaces OldItemEvent, IndexedItemEvent and java.util.Map may also
 * be used to define events. Events of all these kinds may be freely mixed,
 * even if they belong to the same Item.
 * <BR>
 * <BR>All implementation methods must execute fast and must be nonblocking.
 * All information needed to extract data must be provided at object
 * construction. If the implementation were slow, the whole update delivery
 * process, even for different sessions, would be slowed down.
 *
 * @author          ...
 * last author:     $Author: Aalinone $
 * @version         $Revision: 67138 $
 * last modified:   $Modtime: 17/01/07 16.14 $
 * last check-in:   $Date: 17/01/07 16.15 $
 *
 * @see ItemEventListener
 * @see OldItemEvent
 * @see IndexedItemEvent
 */
public interface ItemEvent {

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
    Iterator getNames();

    /**
     * Returns the value of a named Field (null is a legal value too). Returns
     * null also if the Field is not reported in the ItemEvent.
     * <BR>Any value can be expressed as either a byte array or a String:
     * <UL>
     * <LI>If a value is supplied as a byte array, it will be considered
     * as an ISO-8859-1 (ISO-LATIN-1) representation of a string, so that
     * the clients will be able to receive the value as a string object.
     * A special case is that of binary representations of AMF objects,
     * which can be received by the Flex Client Library in binary form.
     * As another special case, the mandatory fields for COMMAND Mode,
     * named "key" and "command", cannot be supplied as byte arrays.</LI> 
     * <LI>If a value is supplied as a String, it will be received as an
     * equivalent string object by the client. This, however, involves
     * an internal conversion to a byte array in order to send the update;
     * this makes supplying values as String less efficient than supplying
     * them as byte arrays, though the latter method is restricted to the
     * ISO-8859-1 (ISO-LATIN-1) character set.</LI>
     * </UL>
     * Lightstreamer Kernel will call this method at most once for each Field
     * (unless events logging is enabled) and may not call this method at all
     * for some Fields. So, if performing any data conversion is required
     * in order to extract Field values, it may be convenient to do it on
     * demand rather than doing it in advance.
     *
     * @param  name  A Field name.
     * @return  a String or a byte array containing the Field value, or null.
     * @see ItemEventListener
     */
	@Nullable
    Object getValue(@Nonnull String name);
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
