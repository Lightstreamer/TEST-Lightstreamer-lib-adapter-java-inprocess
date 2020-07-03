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
import javax.annotation.Nullable;

/**
 * Used to provide update information to the calls to the isSelected method
 * of MetadataProvider.
 * Contains an update event for an Item, as dispatched by the Preprocessor
 * in order to be processed by an ItemEventBuffer.
 * All the fields pertaining to the updated Item state are reported,
 * regardless that they are changing or confirmed with the event.
 *
 * @author          ...
 * last author:     $Author: Aalinone $
 * @version         $Revision: 53184 $
 * last modified:   $Modtime: 17/01/07 16.14 $
 * last check-in:   $Date: 17/01/07 16.15 $
 */
public interface ItemEvent {

    /**
     * Returns the value of a named Field as a String object (null is a legal
     * value too). Returns null also if the Field is not part of the current
     * Item state.
     * <BR>Note that, in case the Data Adapter had supplied the Field value
     * as a byte array, getting the String value of the Field involves
     * a conversion operation through ISO-8859-1 encoding.
     *
     * @param  fieldName  A Field name.
     * @return  a String containing the Field value, or null.
     */
	@Nullable
    String getValueAsString(@Nonnull String fieldName);

    /**
     * Returns the value of a named Field as a byte array (null is a legal
     * value too). Returns null also if the Field is not part of the current
     * Item state.
     * <BR>Note that, in case the Data Adapter had supplied the Field value
     * as a String, getting the byte array value of the Field involves
     * a conversion operation. Application code should avoid a similar case;
     * anyway, UTF-8 encoding is used to this purpose.
     *
     * @param  fieldName  A Field name.
     * @return  a byte array containing the Field value, or null.
     */
	@Nullable
    byte[] getValueAsByteArray(@Nonnull String fieldName);

    /**
     * Allows to distinguish between events that carry the Item initial state
     * (the Snapshot) and events that carry state Updates.
     *
     * @return  true if the event carries initial state information.
     */
    boolean isSnapshot();

}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
