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
 * Used to provide update information to the calls to the customizeUpdate
 * method of MetadataProvider.
 * Contains an update event for an Item, as dispatched by the Preprocessor
 * in order to be processed by an ItemEventBuffer.
 * All the fields pertaining to the updated Item state are reported,
 * regardless that they are changing or confirmed with the event.
 * The fields can be changed by supplying a new value, either as a String
 * or as a byte array. New fields can be added to the event as well.
 * Fields can also be removed, by setting them to null.
 */
public interface CustomizableItemEvent extends ItemEvent {

    /**
     * Sets the value of a named Field as a String object (null is a legal
     * value too).
     * <BR>If a value is supplied as a String, it will be received as an
     * equivalent string object by the client. This, however, involves
     * an internal conversion to a byte array in order to send the update;
     * this makes supplying values as String less efficient than supplying
     * them as byte arrays, though the latter method is restricted to the
     * ISO-8859-1 (ISO-LATIN-1) character set.
     *
     * @param  fieldName  A Field name.
     * @param  value  a String containing a new value for the Field, or null.
     */
    void setValueAsString(@Nonnull String fieldName, @Nullable String value);

    /**
     * Sets the value of a named Field as a byte array (null is a legal
     * value too).
     * <BR>If a value is supplied as a byte array, it will be considered
     * as an ISO-8859-1 (ISO-LATIN-1) representation of a string, so that
     * the clients will be able to receive the value as a string object.
     * A special case is that of binary representations of AMF objects,
     * which can be received by the Flex Client Library in binary form.
     * As another special case, the mandatory fields for COMMAND Mode,
     * named "key" and "command", cannot be supplied as byte arrays.
     * 
     * @param  fieldName  A Field name.
     * @param  value  a byte array representing a new value for the Field,
     *          or null.
     */
    void setValueAsByteArray(@Nonnull String fieldName,  @Nullable byte[] value);

}
