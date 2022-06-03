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

import java.util.concurrent.CompletionStage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used by Lightstreamer to provide value objects to the calls
 * to methods {@link MetadataProvider#notifyNewTables},
 * {@link MetadataProvider#notifyTablesClose}, and
 * {@link MetadataProvider#notifyMpnSubscriptionActivation()}.
 * The attributes of every Table (i&#46;e&#46;: Subscription) to be added or removed
 * to a Session have to be written to a TableInfo instance.
 * The object also provides useful queries and operations that can be performed
 * on its specific Table (i&#46;e&#46;: Subscription).
 */
public class TableInfo {
    private final int winIndex;
    private final Mode mode;
    private final int min, max;
    private final String group;
    private final String dataAdapter;
    private final String schema;
    private final String selector;
    private final String[] itemNames;
    private final SubscriptionStatistics[] itemSubscrStats;
    
    /**
     * Used by Lightstreamer to create a TableInfo instance,
     * collecting the various attributes of a Table (i&#46;e&#46;: Subscription).
     *
     * @param winIndex  Unique identifier of the client subscription request within the session.
     * @param mode  Publishing Mode for the Items in the Table (i&#46;e&#46;: Subscription)
     * (it must be the same across all the Table).
     * @param group  The name of the Item Group (or specification of the Item List)
     * to which the subscribed Items belong.
     * @param dataAdapter  The name of the Data Adapter to which the Table
     * (i&#46;e&#46;: Subscription) refers.
     * @param schema  The name of the Field Schema (or specification of the Field List)
     * used for the subscribed Items.
     * @param selector  The name of the optional Selector associated to
     * the table (i&#46;e&#46;: Subscription).
     * @param min  The 1-based index of the first Item in the Group to be
     * considered in the Table (i&#46;e&#46;: Subscription).
     * @param max  The 1-based index of the last Item in the Group to be
     * considered in the Table (i&#46;e&#46;: Subscription).
     * @param itemNames  The array of Item names involved in this Table
     * (i&#46;e&#46;: Subscription).
     * @param itemSubscrStats  An optional array that contains the statistics
     * related with the activity of this subscription.
     */
    public TableInfo(int winIndex, @Nonnull Mode mode, @Nonnull String group,
    		         @Nonnull String dataAdapter, @Nonnull String schema,
    		         int min, int max, @Nullable String selector,
    		         @Nonnull String[] itemNames, @Nullable SubscriptionStatistics[] itemSubscrStats) {
        this.winIndex = winIndex;
        this.mode = mode;
        this.group = group;
        this.dataAdapter = dataAdapter;
        this.schema = schema;
        this.selector = selector;
        this.min = min;
        this.max = max;
        this.itemNames = itemNames;
        this.itemSubscrStats = itemSubscrStats;
    }

    /**
     * Returns a unique identifier of the client subscription request within the session.
     * This allows for matching the corresponding subscription and unsubscription requests.
     * Note that, for clients based on a very old version of a client library
     * or text protocol, subscription requests may involve multiple Tables
     * (i&#46;e&#46;: Subscriptions), hence multiple objects of this type can be supplied
     * in a single array by {@link MetadataProvider#notifyNewTables} and
     * {@link MetadataProvider#notifyTablesClose}. In this case, the value returned
     * is the same for all these objects and the single Tables (i&#46;e&#46;: Subscriptions)
     * can be identified by their relative position in the array.
     *
     * @return a Window identifier.
     */
    public int getWinIndex() {
        return winIndex;
    }

    /**
     * Returns the publishing Mode for the Items in the Table  (i&#46;e&#46;: Subscription)
     * (it must be the same across all the Table).
     *
     * @return a publishing Mode.
     */
    @Nonnull
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the name of the Item Group (or specification of the Item List)
     * to which the subscribed Items belong.
     *
     * @return an Item Group name (or Item List specification).
     */
    @Nonnull
    public String getId() {
        return group;
    }

    /**
     * Returns the name of the Data Adapter to which the Table
     *  (i&#46;e&#46;: Subscription) refers.
     *
     * @return a Data Adapter name.
     */
    @Nonnull
    public String getDataAdapter() {
        return dataAdapter;
    }
    
    /**
     * Returns the name of the Field Schema (or specification of the Field List)
     * used for the subscribed Items.
     *
     * @return a Field Schema name (or Field List specification).
     */
    @Nonnull
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the name of the optional Selector associated to the Table
     *  (i&#46;e&#46;: Subscription).
     *
     * @return a Selector name, or null if no Selector was associated
     * to the Table.
     */
    @Nullable
    public String getSelector() {
        return selector;
    }

    /**
     * Returns the index of the first Item in the Group to be considered in
     * the Table  (i&#46;e&#46;: Subscription).
     * Such restriction can be specified in the client request.
     *
     * @return a 1-based index.
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the index of the last Item in the Group to be considered in
     * the Table  (i&#46;e&#46;: Subscription).
     * Such restriction can be specified in the client request.
     *
     * @return a 1-based index.
     */
    public int getMax() {
        return max;
    }

    /**
     * Returns the array of the Item names involved in this Table
     * (i&#46;e&#46;: Subscription).
     * The sequence of names is the same one returned by
     * {@link MetadataProvider#getItems(String, String, String)}
     * when decoding of the group name, but restricted, in case a first and/or last
     * Item was specified in the client request (see {@link #getMin()} and {@link #getMax()}). 
     *
     * @return an Array of Item names.
     */
    @Nonnull
    public String[] getSubscribedItems() {
        return itemNames;
    }
    
    /**
     * Returns an array that contains the statistics related with the activity
     * of all items involved in this Table (i&#46;e&#46;: Subscription).
     * Each entry refers to one item and the order is the same as returned
     * by {@link #getSubscribedItems()}.
     * These statistics are available only on the objects supplied by calls
     * to {@link MetadataProvider#notifyTablesClose},
     * so that the statistics will refer to the whole life of the subscription.
     *
     * @return an Array of statistics-gathering objects or null.
     */
    @Nullable
    public SubscriptionStatistics[] getSubscriptionStatistics() {
        return itemSubscrStats;
    }
    
    /**
     * Enforces the unsubscription of this Table (i&#46;e&#46;: Subscription).
     * If the unsubscription was already performed, the call does nothing.
     * The latter holds, in particular, when this object has been obtained
     * from an invocation of {@link MetadataProvider#notifyTablesClose}.
     * <BR>The unsubscription is usually requested by the client, but this method
     * can override the client intentions. In this case, the client will just
     * receive the notification of the unsubscription according with the API
     * in use, but with no other information as to the cause.
     * <BR>Invoking this request while still inside {@link MetadataProvider#notifyNewTables}
     * is pointless; however, the request will be held, then carried out
     * after the underlying subscription attempt has finished.
     * <BR>Note: the operation is only available when this object has been
     * obtained from an invocation of {@link MetadataProvider#notifyNewTables}
     * which has provided an array of TableInfo with a single element (this one);
     * otherwise it will do nothing. However, the case of arrays with multiple
     * elements is only possible when extremely old client SDKs are in use.
     * <BR>The operation is also not available on objects obtained from an
     * invocation of {@link MetadataProvider#notifyMpnSubscriptionActivation()}.
     * 
     * @return A CompletionStage that provides the operation outcome.
     * Note that any continuations requested to this CompletionStage
     * without explicitly providing an Executor will be performed in the same
     * thread pool used for {@link MetadataProvider#notifyNewTables},
     * hence they are expected to execute fast.
     */
    @Nonnull
    public CompletionStage<Void> forceUnsubscription() {
        throw new IllegalStateException("Illegal use of a TableInfo");
    }
    
    @Override @Nonnull
    public String toString() {
        return "TableInfo for index " + winIndex + ", group: " + group;
    }
    
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
