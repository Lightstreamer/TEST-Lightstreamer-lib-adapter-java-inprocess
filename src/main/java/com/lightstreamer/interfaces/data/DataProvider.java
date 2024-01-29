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

import java.io.File;
import java.util.Map;

/**
 * Provides an interface to be implemented by a Data Adapter in order
 * to attach a Data Provider to Lightstreamer Kernel.
 * The configuration of an Adapter instance requires the following items:
 * <UL>
 * <LI> a unique identification for the instance; </LI>
 * <LI> the Adapter class name; </LI>
 * <LI> optional configuration parameters. </LI>
 * </UL>
 * This is accomplished through the "adapters.xml" configuration files.
 * <BR>A single instance of each configured Adapter is created by Lightstreamer
 * Kernel at startup. For this purpose, any Data Adapter must provide a void
 * constructor. After creation, the init method is called, providing the Data
 * Adapter with the configuration information; then, the Kernel sets itself
 * as the Data Adapter listener, by calling the setListener method. <BR>
 * Data Providers are used by Lightstreamer Kernel to obtain all data to be
 * pushed to the Clients. Any Item requested by a Client must refer to one
 * supplied by the configured Data Adapters.
 * <BR>A Data Provider supplies data in a publish/subscribe way. Lightstreamer
 * Kernel asks for data by calling the subscribe and unsubscribe methods for
 * various Items and the Data Adapter sends ItemEvents to its listener
 * in an asynchronous way.
 * <BR>A Data Adapter can also support Snapshot management. Upon subscription
 * to an item, the current state of the Item data can be sent to the Kernel
 * before the updates. This allows the Kernel to maintain the Item state,
 * by integrating the new ItemEvents into the state (in a way that depends
 * on the Item mode) and to make this state available to the Clients.
 * <BR>Note that the interaction between the Kernel and the Data Adapter and the
 * interaction between the Kernel and any Client are independent activities.
 * As a consequence, the very first ItemEvents sent by the Data Adapter to
 * the Kernel for an Item just subscribed to might be processed before the
 * Kernel starts feeding any client, even the client that caused the
 * subscription to the Item to be invoked;
 * then, such events would not be forwarded to any client.
 * If it is desirable that a client receives all the ItemEvents that have
 * been produced for an Item by the Data Adapter since subscription time,
 * then the support for the Item Snapshot can be leveraged.
 * <BR>Lightstreamer Kernel ensures that calls to subscribe and unsubscribe for
 * the same Item will be interleaved, without redundant calls; whenever
 * subscribe throws an exception, the corresponding unsubscribe call is not
 * issued.
 */
public interface DataProvider {

    /**
     * Called by Lightstreamer Kernel to provide initialization information
     * to the Data Adapter.
     * The call must not be blocking; any polling cycle or similar must be
     * started in a different thread. Any delay in returning from this call
     * will in turn delay the Kernel initialization.
     * If an exception occurs in this method, Lightstreamer Kernel can't
     * complete the startup and must exit.
     *
     * @param params  A Map-type value object that contains name-value pairs
     * corresponding to the "param" elements supplied in the Data Adapter
     * configuration file under the "data_provider" element.
     * Both names and values are represented as String objects. <BR>
     * In addition, the following entries are added by the Server:
     * <ul>
     * <li>"adapters_conf.id" - the associated value is a string which reports
     * the name configured for the Adapter Set, i&#46;e&#46; the name specified for
     * the "id" attribute of the &lt;adapters_conf&gt; element;</li>
     * <li>"data_provider.name" - the associated value is a string which reports
     * the name configured for the Data Adapter, i&#46;e&#46; the name specified for
     * the "name" attribute of the &lt;data_providerf&gt; element (or its
     * default, which is "default").</li>
     * </ul>
     * @param configDir  The path of the directory on the local disk
     * where the Data Adapter configuration file resides.
     * @throws DataProviderException if an error occurs that prevents
     * the correct behavior of the Data Adapter. This causes the Server
     * not to complete the startup and to exit.
     */
    public void init(@Nonnull Map params, @Nonnull File configDir)
        throws DataProviderException;
    // Pertaining to server startup.

    /**
     * Called by Lightstreamer Kernel to provide a listener to receive the
     * ItemEvents carrying data and asynchronous error notifications.
     * The listener is set before any subscribe is called and is never changed.
     *
     * @param listener a listener.
     */
    public void setListener(@Nonnull  ItemEventListener listener);
    // Pertaining to server startup.

    /**
     * Called by Lightstreamer Kernel to request data for an Item. If the
     * request succeeds, the Data Adapter can start sending an ItemEvent
     * to the listener for any update of the Item value. Before sending the
     * updates, the Data Adapter may optionally send one or more ItemEvents
     * to supply the current Snapshot.
     * <BR>The general rule to be followed for event dispatching is:
     * <PRE>
     *      if isSnapshotAvailable(itemName) == true
     *           SNAP* [EOS] UPD*
     *      else
     *           UPD*</PRE>
     * where:
     * <UL>
     * <LI>SNAP represents an update call with the isSnapshot flag set to true</LI>
     * <LI>EOS represents an endOfSnapshot call</LI>
     * <LI>UPD represents an update call with the isSnapshot flag set to false;
     * in this case, the special clearSnapshot call can also be issued.</LI>
     * </UL>
     * The composition of the snapshot depends on the Mode in which the item
     * is to be processed. In particular, for MERGE mode, the snapshot
     * consists of one event and the first part of the rule becomes:
     * <PRE>
     *           [SNAP] [EOS] UPD*</PRE>
     * where a missing snapshot is considered as an empty snapshot.
     * <BR>If an item can be requested only in RAW mode, then isSnapshotAvailable
     * should always return false; anyway, when an item is requested in
     * RAW mode, any snapshot is discarded.
     * <BR>Note that calling endOfSnapshot is not mandatory; however, not
     * calling it in DISTINCT or COMMAND mode may cause the server to keep
     * the snapshot and forward it to the clients only after the first
     * non-shapshot event has been received. The same happens for MERGE mode
     * if neither the snapshot nor the endOfSnapshot call are supplied.
     * <BR>Unexpected snapshot events are converted to non-snapshot events
     * (but for RAW mode, where they are ignored); unexpected endOfSnapshot
     * calls are ignored.
     * <BR>
     * <BR>The method should perform as fast as possible. If the implementation
     * is slow because of complex subscription activation operations, it might
     * delay a subsequent unsubscription and resubscription of the same item.
     * In that case, configuring a dedicated "DATA" thread pool for this Data Adapter
     * is recommended, in order not to block operations for different Data Adapters.
     *
     * @param itemName  Name of an Item.
     * @param needsIterator  Signals that the getNames method will be called
     * on the ItemEvents received for this Item. If this flag is set to false,
     * the ItemEvent objects sent for this Item need not implement the method.
     * @throws SubscriptionException if the request cannot be satisfied.
     * A failed subscription is not notified to the clients; it just causes
     * the clients not to receive data. Upon a failed subscription, the
     * related {@link #unsubscribe(String)} call will not be issued.
     * @throws FailureException  if the method execution has caused a severe
     * problem that can compromise future operation of the Data Adapter.
     * This causes the whole Server to exit, so that an external recovery
     * mechanism may come into action.
     * @see ItemEventListener
     * @see ItemEvent
     */
    public void subscribe(@Nonnull String itemName, boolean needsIterator)
        throws SubscriptionException, FailureException;
    // Pertaining to DATA pool.

    /**
     * Called by Lightstreamer Kernel to end a previous request of data for
     * an Item. After the call has returned, no more ItemEvents for the item
     * should be sent to the listener until requested by a new subscription
     * for the same item.
     * <BR>
     * <BR>The method should perform fast. If the implementation is slow
     * because of complex housekeeping operations, it might delay a subsequent
     * subscription of the same item. In that case, configuring a dedicated
     * "DATA" thread pool for this Data Adapter is recommended, in order not
     * to block operations for different Data Adapters.
     *
     * @param itemName  Name of an Item.
     * @throws SubscriptionException  if the request cannot be satisfied.
     * This does not prevent Lightstreamer Kernel from calling
     * {@link #unsubscribe(String)} again for the same item, if needed.
     * @throws FailureException  if the method execution has caused a severe
     * problem that can compromise future operation of the Data Adapter.
     * This causes the whole Server to exit, so that an external recovery
     * mechanism may come into action.
     */
    public void unsubscribe(@Nonnull String itemName)
        throws SubscriptionException, FailureException;
    // Pertaining to DATA pool.

    /**
     * Called by Lightstreamer Kernel to know whether the Data Adapter,
     * after a subscription for an Item, will send some Snapshot ItemEvents
     * before sending the updates.
     * An item Snapshot can be represented by zero, one or more ItemEvents,
     * also depending on the item mode. 
     * The decision whether to supply or not to supply Snapshot information
     * is entirely up to the Data Adapter.
     * <BR>
     * <BR>The method should perform fast. The availability of the snapshot
     * for an item should be a known architectural property. If the snapshot
     * is expected, but then cannot be obtained at subscription time, then
     * it can only be considered as empty.
     * If the implementation is slow, it will delay the subscription. In that
     * case, configuring a dedicated "DATA" thread pool for this Data Adapter is
     * recommended, in order not to block operations for different Data Adapters.
     *
     * @param itemName  Name of an Item.
     * @return true if Snapshot information will be sent for this Item before
     * before the updates. 
     * @throws SubscriptionException  if the Data Adapter is unable to answer
     * to the request. This causes the subscription to fail and the proper
     * {@link #subscribe(String, boolean)} call not to be issued. A failed
     * subscription is not notified to the clients; it just causes the clients
     * not to receive data.
     */
    public boolean isSnapshotAvailable(@Nonnull String itemName)
        throws SubscriptionException;
    // Pertaining to DATA pool.

    /**
     * Constant that can be used as field name for the "key" field in Items
     * to be processed in COMMAND mode.
     */
    @Nonnull
    public static final String KEY_FIELD = "key";

    /**
     * Constant that can be used as field name for the "command" field in Items
     * to be processed in COMMAND mode.
     */
    @Nonnull
    public static final String COMMAND_FIELD = "command";

    /**
     * Constant that can be used as the "ADD" value for the "command" fields
     * of Items to be processed in COMMAND mode.
     */
    @Nonnull
    public static final String ADD_COMMAND = "ADD";

    /**
     * Constant that can be used as the "UPDATE" value for the "command" fields
     * of Items to be processed in COMMAND mode.
     */
    @Nonnull
    public static final String UPDATE_COMMAND = "UPDATE";

    /**
     * Constant that can be used as the "DELETE" value for the "command" fields
     * of Items to be processed in COMMAND mode.
     */
    @Nonnull
    public static final String DELETE_COMMAND = "DELETE";
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
