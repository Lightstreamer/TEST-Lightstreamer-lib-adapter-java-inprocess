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

import java.util.Map;

/**
 * Used by Lightstreamer Kernel to receive the ItemEvents and any asynchronous
 * severe error notification from the Data Adapter.
 * The listener instance is supplied to the Data Adapter by Lightstreamer
 * Kernel through a setListener call.
 * The listener can manage multiple kinds of ItemEvents: ItemEvent objects,
 * OldItemEvent objects, IndexedItemEvent objects and java.util.Map objects.
 * The common characteristics of all these kinds of ItemEvent objects are that:
 * <UL>
 * <LI>they contain the new values and, in some cases, the current values
 * of the Fields of an Item; the Item name is not directly asked to the object.
 * </LI>
 * <LI>they provide an iterator that supplies the names of all the Fields
 * reported in the ItemEvent; the iterator may not be provided if the related
 * Item has been subscribed by setting the needsIterator flag as false.
 * <LI>they provide a method for getting the value of a Field by name; the
 * value can be expressed as either a String or a byte array.</LI>
 * </UL>
 * When an ItemEvent, of whichever kind, has been sent to the listener, the
 * object instance is totally owned by Lightstreamer Kernel, which may hold it
 * for some time after the listener call has returned.
 * For this reason, the event object should not be used anymore by the Data
 * Adapter. Only provided that the event object is thread safe, the object can
 * still be accessed in a read-only way. In particular, if exactly the same
 * event contents were to be passed multiple times to the listener (for the
 * same or different items), then passing the same event object instance
 * repeatedly would be allowed. However, specific update methods may pose
 * further restrictions.
 * <BR>The same restriction holds for any underlying object referenced to by
 * the ItemEvent object. However, immutable objects are obviously unaffected;
 * in particular, this holds for any objects used to contain values, if they
 * are of String type; but also any byte array objects used to contain values
 * are unaffected and can be reused if they are no longer modified.
 * <BR>If ItemEvents are implemented as wrappers of the data objects received
 * from the external feed (like JMS Messages), all the above has to be
 * carefully considered.
 *
 * @see DataProvider
 */
public interface ItemEventListener {

    /**
     * Called by a Data Adapter to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as an ItemEvent instance.
     * <BR>The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, no more update calls are issued, until requested
     * by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     *
     * @param itemName  The name of the Item whose values are carried by the
     * ItemEvent.
     * @param event  An ItemEvent instance.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see DataProvider
     */
    public void update(@Nonnull String itemName, @Nonnull ItemEvent event, boolean isSnapshot);

    /**
     * Called by a Data Adapter to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as an OldItemEvent instance.
     * <BR>The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, no more update calls are issued, until requested
     * by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     *
     * @param itemName  The name of the Item whose values are carried by the
     * ItemEvent.
     * @param event  An OldItemEvent instance.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see DataProvider
     */
    public void update(@Nonnull String itemName, @Nonnull OldItemEvent event, boolean isSnapshot);

    /**
     * Called by a Data Adapter to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as a java.util.Map instance.
     * <BR>The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, no more update calls are issued, until requested
     * by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     * <BR>If the event object is of type java.util.HashMap, then Lightstreamer
     * Kernel, in order to improve performances, may synchronize on the object
     * lock and modify the object contents while processing it. As a consequence,
     * in this case, the event object, once passed to the listener, cannot be
     * accessed anymore by the Data Adapter, even for reading (it can still be
     * passed multiple times to the listener, if needed).
     *
     * @param itemName  The name of the Item whose values are carried by the
     * ItemEvent.
     * @param event  A java.util.Map instance, in which Field names are
     * associated to Field values. A Field value can be null or missing
     * if the Field is not to be reported in the event. 
     * <BR>Each value can be expressed as either a String or a byte array;
     * see {@link ItemEvent#getValue(String)} for details.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see DataProvider
     */
    public void update(@Nonnull String itemName, @Nonnull Map event, boolean isSnapshot);

    /**
     * Called by a Data Adapter to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as an IndexedItemEvent instance.
     * <BR>The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, no more update calls are issued, until requested
     * by a new subscription for the same Item.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     *
     * @param itemName  The name of the Item whose values are carried by the
     * ItemEvent.
     * @param event  An IndexedItemEvent instance.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see DataProvider
     */
    public void update(@Nonnull String itemName, @Nonnull IndexedItemEvent event,
                       boolean isSnapshot);

    /**
     * Can be called by a Data Adapter that implements the extended interface
     * SmartDataProvider to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as an ItemEvent instance.
     * <BR>This method should not be called while holding custom locks.
     * After that an unsubscribe call for the Item has returned, further
     * calls of this method for the itemHandler received with the last
     * subscription operation are still allowed and will be just discarded.
     *
     * @param itemHandle  Identifies the Item whose values are carried by the
     * ItemEvent. It must be the same object received in the subscribe call.
     * @param event  An ItemEvent instance.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see SmartDataProvider
     */
    public void smartUpdate(@Nonnull Object itemHandle, @Nonnull ItemEvent event,
    		                boolean isSnapshot);

    /**
     * Can be called by a Data Adapter that implements the extended interface
     * SmartDataProvider to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as an OldItemEvent instance.
     * <BR>This method should not be called while holding custom locks.
     * After that an unsubscribe call for the Item has returned, further
     * calls of this method for the itemHandler received with the last
     * subscription operation are still allowed and will be just discarded.
     *
     * @param itemHandle  Identifies the Item whose values are carried by the
     * ItemEvent. It must be the same object received in the subscribe call.
     * @param event  An OldItemEvent instance.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see SmartDataProvider
     */
    public void smartUpdate(@Nonnull Object itemHandle, @Nonnull OldItemEvent event,
    		                boolean isSnapshot);

    /**
     * Can be called by a Data Adapter that implements the extended interface
     * SmartDataProvider to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as a java.util.Map instance.
     * <BR>This method should not be called while holding custom locks.
     * After that an unsubscribe call for the Item has returned, further
     * calls of this method for the itemHandler received with the last
     * subscription operation are still allowed and will be just discarded.
     * <BR>If the event object is of type java.util.HashMap, then Lightstreamer
     * Kernel, in order to improve performances, may synchronize on the object
     * lock and modify the object contents while processing it. As a consequence,
     * in this case, the event object, once passed to the listener, cannot be
     * accessed anymore by the Data Adapter, even for reading (it can still be
     * passed multiple times to the listener, if needed).
     *
     * @param itemHandle  Identifies the Item whose values are carried by the
     * ItemEvent. It must be the same object received in the subscribe call.
     * @param event  A java.util.Map instance, in which Field names are
     * associated to Field values. A Field value can be null or missing
     * if the Field is not to be reported in the event. 
     * <BR>Each value can be expressed as either a String or a byte array;
     * see {@link ItemEvent#getValue(String)} for details.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see SmartDataProvider
     */
    public void smartUpdate(@Nonnull Object itemHandle, @Nonnull Map event, boolean isSnapshot);

    /**
     * Can be called by a Data Adapter that implements the extended interface
     * SmartDataProvider to send an ItemEvent to Lightstreamer Kernel
     * when the ItemEvent is implemented as an IndexedItemEvent instance.
     * <BR>This method should not be called while holding custom locks.
     * After that an unsubscribe call for the Item has returned, further
     * calls of this method for the itemHandler received with the last
     * subscription operation are still allowed and will be just discarded.
     *
     * @param itemHandle  Identifies the Item whose values are carried by the
     * ItemEvent. It must be the same object received in the subscribe call.
     * @param event  An IndexedItemEvent instance.
     * @param isSnapshot  true if the ItemEvent carries the Item Snapshot.
     * @see SmartDataProvider
     */
    public void smartUpdate(@Nonnull Object itemHandle, @Nonnull IndexedItemEvent event,
    		                boolean isSnapshot);

    /**
     * Called by a Data Adapter to signal to Lightstreamer Kernel that no more
     * ItemEvent belonging to the Snapshot are expected for an Item.
     * This call is optional, because the Snapshot completion can also be
     * inferred from the isSnapshot flag in the update and smartUpdate calls.
     * However, this call allows Lightstreamer Kernel to be informed of the
     * Snapshot completion before the arrival of the first non-Snapshot event.
     * Calling this function is recommended if the Item is to be subscribed
     * in DISTINCT mode.
     * In case the Data Adapter returned false to isSnapshotAvailable for the
     * same Item, this function should not be called. 
     * <BR>The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, a possible pending endOfSnapshot call related with
     * the previous subscription request is no longer issued.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     *
     * @param itemName  The name of the Item whose Snapshot has been completed.
     * @see DataProvider
     */
    public void endOfSnapshot(@Nonnull String itemName);

    /**
     * Can be called, instead of endOfSnapshot, by a Data Adapter that
     * implements the extended interface SmartDataProvider, in all cases
     * in which endOfSnapshot can be called.
     * <BR>This method should not be called while holding custom locks.
     * After that an unsubscribe call for the Item has returned, further
     * calls of this method for the itemHandler received with the last
     * subscription operation are still allowed and will be just discarded.
     *
     * @param itemHandle  Identifies an Item whose Snapshot has been completed.
     * @see #endOfSnapshot
     * @see SmartDataProvider
     */
    public void smartEndOfSnapshot(@Nonnull Object itemHandle);

    /**
     * Called by a Data Adapter to signal to Lightstreamer Kernel that the
     * current Snapshot of an Item has suddenly become empty.
     * More precisely:<ul>
     * <li>for subscriptions in MERGE mode, the current state of the Item will
     * be cleared, as though an update with all fields valued as null were issued;</li>
     * <li>for subscriptions in COMMAND mode, the current state of the Item
     * will be cleared, as though a DELETE event for each key were issued;</li>
     * <li>for subscriptions in DISTINCT mode, a suitable notification that
     * the Snapshot for the Item should be cleared will be sent to all the
     * clients currently subscribed to the Item (clients based on some old
     * client library versions may not be notified); at the same time,
     * the current recent update history kept by the Server for the Item
     * will be cleared and this will affect the Snapshot for new subscriptions;</li>
     * <li>for subscriptions in RAW mode, there will be no effect.</li>
     * </ul>
     * Note that this is a real-time event, not a Snapshot event; hence,
     * in order to issue this call, it is not needed that the Data Adapter
     * has returned true to {@link DataProvider#isSnapshotAvailable} for the specified Item;
     * moreover, if invoked while the Snapshot is being supplied, the Kernel
     * will infer that the Snapshot has been completed.
     * <BR>The Adapter should ensure that, after an unsubscribe call for the
     * Item has returned, a possible pending clearSnapshot call related with
     * the previous subscription request is no longer issued.
     * This assures that, upon a new subscription for the Item, no trailing
     * events due to the previous subscription can be received by the Kernel.
     * Note that the method is nonblocking; moreover, it only takes locks
     * to first order mutexes; so, it can safely be called while holding
     * a custom lock.
     *
     * @param itemName  The name of the Item whose Snapshot has become empty.
     * @see DataProvider
     */
    public void clearSnapshot(@Nonnull String itemName);

    /**
     * Can be called, instead of clearSnapshot, by a Data Adapter that
     * implements the extended interface SmartDataProvider, in all cases
     * in which clearSnapshot can be called.
     * <BR>This method should not be called while holding custom locks.
     * After that an unsubscribe call for the Item has returned, further
     * calls of this method for the itemHandler received with the last
     * subscription operation are still allowed and will be just discarded.
     *
     * @param itemHandle  Identifies an Item whose Snapshot has become empty.
     * @see #clearSnapshot
     * @see SmartDataProvider
     */
    public void smartClearSnapshot(@Nonnull Object itemHandle);

    /**
     * Called by a Data Adapter to notify Lightstreamer Kernel of the
     * occurrence of a severe problem that can compromise future operation
     * of the Data Adapter.
     * This causes the whole Server to exit, so that an external recovery
     * mechanism may come into action.
     *
     * @param e  any java.lang.Throwable object, with the description of
     * the problem.
     */
    public void failure(@Nullable Throwable e);
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
