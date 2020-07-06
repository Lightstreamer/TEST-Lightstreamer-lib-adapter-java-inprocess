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

/**
 * Provides an extended interface to be implemented by a Data Adapter in order
 * to attach a Data Provider to Lightstreamer Kernel.
 * Data Adapters that implement this interface can relieve the Kernel from
 * finding item data by item name at every update call.
 * <BR>By implementing this interface, a Data Adapter will receive item
 * subscription requests by an extended subscribe call, which supplies handles
 * to item data in the Kernel. These handles can be used, instead of item
 * names, to identify items in update calls, through the various smartUpdate
 * and the smartEndOfSnapshot methods.
 * <BR>Using this extended interface is natural in many situations, where an
 * internal lookup for item data by item name is already performed in the
 * Data Adapter. In these cases, a second lookup in the Kernel can be saved.
 * This also saves a global synchronization, allowing different update calls
 * issued in different threads to be managed concurrently, provided that calls
 * for the same item are issued in the same thread. 
 * <BR>Lightstreamer Kernel always uses different handles across subsequent
 * subscriptions of the same item. This gives an additional benefit: if
 * spurious trailing updates are sent, through smartUpdate, after an item has
 * been unsubscribed and then immediately subscribed again, these trailing
 * updates cannot conflict with the new updates sent against the new
 * subscription and are just ignored by Lightstreamer Kernel.
 *
 * @see DataProvider
 * @see ItemEventListener
 */
public interface SmartDataProvider extends DataProvider {

    /**
     * Called by Lightstreamer Kernel to request data for an Item. If the
     * request succeeds, the Data Adapter can start sending an ItemEvent
     * to the listener for any update in the Item value. Before sending the
     * updates, the Data Adapter may optionally send one or more ItemEvents
     * to supply the current Snapshot.
     * <BR>Both item name and item handle can be used to identify the item,
     * but using the latter is far more efficient.
     * <BR>
     * <BR>The method should perform as fast as possible. See the notes for
     * {@link DataProvider#subscribe(String, boolean)}.
     *
     * @param itemName  Name of an Item.
     * @param itemHandle  Object to be used to identify the item in update
     * calls. Lightstreamer Kernel always uses different handles across
     * subsequent subscriptions of the same item. 
     * @param needsIterator  Signals that the getNames method will be called
     * on the ItemEvents received for this Item. If this flag is set to false,
     * the ItemEvent objects sent for this Item need not implement the method.
     * @throws SubscriptionException  if the request cannot be satisfied.
     * A failed subscription is not notified to the clients; it just causes
     * the clients not to receive data. Upon a failed subscription, the
     * related {@link #unsubscribe(String)} call will not be issued.
     * @throws FailureException  if the method execution has caused a severe
     * problem that can compromise future operation of the Data Adapter.
     * This causes the whole Server to exit, so that an external recovery
     * mechanism may come into action.
     * @see ItemEventListener
     * @see ItemEvent
     * @see DataProvider#subscribe
     */
    public void subscribe(
        @Nonnull String itemName, @Nonnull Object itemHandle, boolean needsIterator)
            throws SubscriptionException, FailureException;
    // Pertaining to DATA pool.

    /**
     * Inherited by the base interface DataProvider but never called in this
     * case, because the extended version will always be called in its place.
     * The method can be safely left blank or return an exception.
     *
     * @param itemName  Not to be used in this context.
     * @param needsIterator  Not to be used in this context.
     * @throws SubscriptionException  Can always be thrown in this context.
     * @throws FailureException  Not to be thrown in this context.
     */
    public void subscribe(@Nonnull String itemName, boolean needsIterator)
        throws SubscriptionException, FailureException;
    // Pertaining to DATA pool.
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
