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

import java.io.File;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides an interface to be implemented by a Metadata Adapter in order
 * to attach a Metadata Provider to Lightstreamer Kernel.
 * The configuration of an Adapter instance requires the following items:
 * <UL>
 * <LI> a unique identification for the instance; </LI>
 * <LI> the Adapter class name; </LI>
 * <LI> optional configuration parameters. </LI>
 * </UL>
 * A Metadata Provider is used by Lightstreamer Kernel in combination with
 * one or multiple Data Providers, uniquely associated with it.
 * The configuration is accomplished through the "adapters.xml" configuration
 * files, by which one or more "Adapter Sets" can be installed into the Server,
 * where each Adapter Set is made up of exactly one Metadata Adapter and one
 * or multiple Data Adapters.
 * <BR>A single instance of each configured Metadata Adapter is created by
 * Lightstreamer Kernel at startup, then its init method is called, providing
 * it with the configuration information. For this purpose, any Metadata
 * Adapter must provide a void constructor.
 * <BR>A Metadata Provider is consulted by Lightstreamer Kernel in order
 * to manage the push Requests intended for the associated Data Providers.
 * A Metadata Provider supplies information for several different goals:
 * <UL>
 * <LI> the resolution of the Group/Schema names used in the Requests; </LI>
 * <LI> the check of the User accessibility to the requested Items; </LI>
 * <LI> the check of the resource level granted to the User. </LI>
 * <LI> the request for specific characteristics of the Items. </LI>
 * </UL>
 * Note: Each Item may be supplied by one or more of the associated Data
 * Adapters and each client Request must reference to a specific Data Adapter.
 * However, in the current version of the interface, no Data Adapter
 * information is supplied to the Metadata Adapter methods. Hence, the Item
 * names must provide enough information for the methods to give an answer.
 * As a consequence, for instance, the frequency, snapshot length and other
 * characteristics of an item are the same regardless of the Data Adapter
 * it is requested from. More likely, for each item name defined, only one
 * of the Data Adapters in the set is responsible for supplying that item.
 *
 * @author              Dario Crivelli
 * last author:         $Author: Dcrivel $
 * @version             $Revision: 75867 $
 * last modified:       $Modtime: 8/02/08 10.20 $
 * last check-in:       $Date: 8/02/08 10.22 $
 */
public interface MetadataProvider {
	
    /**
     * Called by Lightstreamer Kernel to provide initialization information
     * to the Metadata Adapter.
     * <BR>
     * <BR>
     * The call must not be blocking; any polling cycle or similar must be
     * started in a different thread. Any delay in returning from this call
     * will in turn delay the Kernel initialization.
     * If an exception occurs in this method, Lightstreamer Kernel can't
     * complete the startup and must exit.
     *
     * @param params A Map-type value object that contains name-value pairs
     * corresponding to the "param" elements supplied in the Metadata Adapter
     * configuration file under the "metadata_provider" element.
     * Both names and values are represented as String objects. <BR>
     * In addition, the following entries are added by the Server:
     * <ul>
     * <li>"adapters_conf.id" - the associated value is a string which reports
     * the name configured for the Adapter Set, i&#46;e&#46; the name specified for
     * the "id" attribute of the &lt;adapters_conf&gt; element.</li>
     * </ul>
     * @param configDir  The path of the directory on the local disk
     * where the Metadata Adapter configuration file resides.
     * @throws MetadataProviderException if an error occurs that prevents
     * the correct behavior of the Metadata Adapter. This causes the Server
     * not to complete the startup and to exit.
     */
    public void init(@Nonnull Map params, @Nonnull File configDir)
        throws MetadataProviderException;
    
    /**
     * Called by Lightstreamer Kernel to provide a listener to receive
     * requests about sessions and any asynchronous severe error notification.
     * If these features are not needed, the method can be left unimplemented
     * (sticking to its default implementation).
     * The listener is set after init and before any other method is called
     * and it is never changed.
     *
     * @param listener a listener.
     */
    public default void setListener(@Nonnull MetadataControlListener listener) {
        // we will do without the listener
    }

    /**
     * Called by Lightstreamer Kernel as a preliminary check that a user is
     * enabled to make Requests to any of the related Data Providers.
     * It is invoked upon each session request and it is called prior to any
     * other session-related request. So, any other method with a User
     * argument can assume that the supplied User argument has already been
     * checked.
     * <BR>The User authentication should be based on the user and password
     * arguments supplied by the client. The full report of the request HTTP
     * headers is also available; they could be used in order to gather
     * information about the client, but should not be used for authentication,
     * as they may not be under full control by client code. See also the
     * discussion about the &lt;use_protected_js&gt; Server configuration
     * element, if available.
     * <BR>
     * <BR>The method should perform as fast as possible. If the implementation
     * is slow because of complex data gathering operations, it might delay the
     * client session activation.
     * In that case, configuring a dedicated "AUTHENTICATION" thread pool for
     * authentication requests on this Adapter Set is recommended, in order not
     * to block operations for different Adapter Sets or different operations
     * for this Adapter Set.
     * Also consider that a slow implementation may cause a "connection timeout"
     * posed on the client side to expire, with a consequent new attempt;
     * it is advisable that the second invocation can take advantage of the
     * work already performed during the first invocation.
     *
     * @param user A User name.
     * @param password A password optionally required to validate the User.
     * @param httpHeaders A Map-type value object that contains a name-value
     * pair for each header found in the HTTP request that originated the call.
     * The header names are reported in lower-case form. <BR>
     * For headers defined multiple times, a unique name-value pair is reported,
     * where the value is a concatenation of all the supplied header values,
     * separated by a ",".
     * @throws AccessException if the User name is not known or the supplied
     * password is not correct.
     * <BR>If the User credentials cannot be validated because of a temporary
     * lack of resources, then a {@link ResourceUnavailableException} can be
     * thrown. This will instruct the client to retry in short time.
     * @throws CreditsException if the User is known but is not enabled to
     * make further Requests at the moment.
     * 
     * @see #notifyUser(String, String, Map, String)
     */
    public void notifyUser(@Nullable String user, @Nullable String password, @Nonnull Map httpHeaders)
        throws AccessException, CreditsException;

    /**
     * Called by Lightstreamer Kernel, instead of calling the 3-arguments
     * version, in case the Server has been instructed to acquire the client
     * principal from the client TLS/SSL certificate through the &lt;use_client_auth&gt;
     * configuration flag.
     * <BR>Note that the above flag can be set for each listening port
     * independently (and it can be set for TLS/SSL ports only), hence, both
     * overloads may be invoked, depending on the port used by the client.
     * <BR>Also note that in case client certificate authentication is not
     * forced on a listening port through &lt;force_client_auth&gt;, a client
     * request issued on that port may not be authenticated, hence it may
     * have no principal associated. In that case, if &lt;use_client_auth&gt;
     * is set, this overload will still be invoked, with null principal.
     * <BR>See the base 3-arguments version for other notes.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>https is an optional feature, available depending
     * on Edition and License Type.
     * To know what features are enabled by your license, please see the License
     * tab of the Monitoring Dashboard (by default, available at /dashboard).
     *
     * @param user A User name.
     * @param password A password optionally required to validate the User.
     * @param httpHeaders A Map-type value object that contains a name-value
     * pair for each header found in the HTTP request that originated the call.
     * @param clientPrincipal the identification name reported in the client
     * TLS/SSL certificate supplied on the socket connection used to issue the
     * request that originated the call; it can be null if client has not
     * authenticated itself or the authentication has failed.
     * @throws AccessException if the User name is not known or the supplied
     * password is not correct.
     * <BR>If the User credentials cannot be validated because of a temporary
     * lack of resources, then a {@link ResourceUnavailableException} can be
     * thrown. This will instruct the client to retry in short time.
     * @throws CreditsException if the User is known but is not enabled to
     * make further Requests at the moment.
     */
    public void notifyUser(@Nullable String user, @Nullable String password, @Nonnull Map httpHeaders,  @Nonnull String clientPrincipal)
        throws AccessException, CreditsException;

    /**
     * Called by Lightstreamer Kernel to resolve an Item Group name (or Item List specification) supplied in
     * a Request. The names of the Items in the Group must be returned.
     * For instance, the client could be allowed to specify the "NASDAQ100"
     * Group name and, upon that, the list of all items corresponding to the
     * stocks included in that index could be returned.
     * <BR>Possibly, the content of an Item Group may be dependent on the User
     * who is issuing the Request or on the specific Session instance.
     * <BR>
     * <BR>When an Item List specification is supplied, it is made of a space-separated
     * list of the names of the items in the List. This convention is used
     * by some of the subscription methods provided by the various client
     * libraries. The specifications for these methods require that
     * "A LiteralBasedProvider or equivalent Metadata Adapter is needed
     * on the Server in order to understand the Request".
     * <BR>When any of these interface methods is used by client code accessing
     * this Metadata Adapter, the supplied "group" argument should be inspected
     * as a space-separated list of Item names and an array with these names
     * in the same order should be returned.
     * <BR>
     * <BR>Another typical case is when the same Item has different contents
     * depending on the User that is issuing the request. On the Data Adapter
     * side, different Items (one for each User) can be used; nevertheless, on
     * the client side, the same name can be specified in the subscription
     * request and the actual user-related name can be determined and returned
     * here. For instance:
     * <pre>
     * {@code
     * if (group.equals("portfolio")) {
     *     String itemName = "PF_" + user;
     *     return new String[] { itemName };
     * } else if (group.startsWith("PF_")) {
     *     // protection from unauthorized use of user-specific items
     *     throw new ItemsException("Unexpected group name");
     * }
     * }
     * </pre>
     * Obviously, the two above techniques can be combined, hence any
     * element of an Item List can be replaced with a decorated or alternative
     * Item name: the related updates will be associated to the original name
     * used in the supplied Item List specification by client library code.
     * <BR>
     * <BR>The method should perform fast. External information needed to
     * execute it should have been previously gathered and cached by
     * {@link #notifyUser(String, String, Map)}. If the implementation
     * is slow, it might delay the subscription request management.
     * In that case, configuring a dedicated "SET" thread pool for requests
     * on this Adapter Set is recommended, in order not to block operations
     * for different Adapter Sets. If the delay affects requests for a
     * specific Data Adapter, then configuring a dedicated "DATA" thread pool
     * for that Data Adapter is a better option.
     *
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param group An Item Group name (or Item List specification).
     * @return An array with the names of the Items in the Group.
     * @throws ItemsException if the supplied Item Group name (or Item List specification) is not recognized.
     */
    @Nonnull
    public String[] getItems(@Nullable String user, @Nonnull String sessionID, @Nonnull String group) throws ItemsException;

    /**
     * Called by Lightstreamer Kernel to resolve a Field Schema name (or Field List specification) supplied in
     * a Request. The names of the Fields in the Schema must be returned.
     * <BR>Possibly, the content of a Field Schema may be dependent on the User
     * who is issuing the Request, on the specific Session instance or on the
     * Item Group (or Item List) to which the Request is related.
     * <BR>
     * <BR>When a Field List specification is supplied, it is made of a space-separated
     * list of the names of the Fields in the Schema. This convention is used
     * by some of the subscription methods provided by the various client
     * libraries. The specifications for these methods require that
     * "A LiteralBasedProvider or equivalent Metadata Adapter is needed
     * on the Server in order to understand the Request".
     * <BR>When any of these interface methods is used by client code accessing
     * this Metadata Adapter, the supplied "schema" argument should be inspected
     * as a space-separated list of Field names and an array with these names
     * in the same order should be returned;
     * returning decorated or alternative Field names is also possible:
     * they will be associated to the corresponding names used in the
     * supplied Field List specification by client library code.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getItems(String, String, String)}.
     *
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param group The name of the Item Group (or specification of the Item List)
     * whose Items the Schema is to be applied to.
     * @param schema A Field Schema name (or Field List specification).
     * @return An array with the names of the Fields in the Schema.
     * @throws ItemsException if the supplied Item Group name (or Item List specification) is not recognized.
     * @throws SchemaException if the supplied Field Schema name (or Field List specification) is not recognized.
     */
    @Nonnull
    public String[] getSchema(@Nullable String user, @Nonnull String sessionID, @Nonnull String group, @Nonnull String schema)
        throws ItemsException, SchemaException;

    /**
     * Called by Lightstreamer Kernel to ask for the bandwidth level to be
     * allowed to a User for a push Session.
     * <BR>
     * <BR>The method should perform fast. External information needed to
     * execute it should have been previously gathered and cached by
     * {@link #notifyUser(String, String, Map)}. If the implementation
     * is slow, it might delay the client session activation.
     * In that case, configuring a dedicated "SET" thread pool for requests
     * on this Adapter Set is recommended, in order not to block operations
     * for different Adapter Sets.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>Bandwidth Control is an optional feature,
     * available depending on Edition and License Type.
     * To know what features are enabled by your license, please see the License
     * tab of the Monitoring Dashboard (by default, available at /dashboard).
     *
     * @param user A User name.
     * @return The allowed bandwidth, in Kbit/sec. A zero return value means
     * an unlimited bandwidth.
     */
    @Nonnull
    public double getAllowedMaxBandwidth(@Nullable String user);

    /**
     * Called by Lightstreamer Kernel to ask for the ItemUpdate frequency
     * to be allowed to a User for a specific Item.
     * An unlimited frequency can also be specified.
     * <BR>
     * Such filtering applies only to Items requested with publishing Mode
     * MERGE, DISTINCT and COMMAND (in the latter case, the frequency
     * limitation applies to the UPDATE events for each single key).
     * If an Item is requested with publishing Mode MERGE, DISTINCT or
     * COMMAND and unfiltered dispatching has been specified,
     * then returning any limited maximum frequency will cause the refusal
     * of the request by the Kernel.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getItems(String, String, String)}.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>A further global frequency limit could also be
     * imposed by the Server, depending on Edition and License Type; this
     * specific limit also applies to RAW mode and to unfiltered dispatching.  
     * To know what features are enabled by your license, please see the License
     * tab of the Monitoring Dashboard (by default, available at /dashboard).
     *
     * @param user A User name.
     * @param item An Item Name.
     * @return The allowed Update frequency, in Updates/sec. A zero return
     * value means no frequency restriction.
     */
    @Nonnull
    public double getAllowedMaxItemFrequency(@Nullable String user, @Nonnull String item);

    /**
     * Called by Lightstreamer Kernel to ask for the maximum size allowed
     * for the buffer internally used to enqueue subsequent ItemUpdates
     * for the same Item.
     * If this buffer is more than 1 element deep, a short burst of ItemEvents
     * from the Data Adapter can be forwarded to the Client without losses,
     * though with some delay.
     * <BR>
     * The buffer size is specified in the Request. Its maximum allowed size
     * can be different for different Users.
     * <BR>
     * Such buffering applies only to Items requested with publishing Mode
     * MERGE or DISTINCT. However, if the Item has been requested with
     * unfiltered dispatching, then the buffer size is always unlimited
     * and buffer size settings are ignored.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getItems(String, String, String)}.
     *
     * @param user A User name.
     * @param item An Item Name.
     * @return The allowed buffer size. A zero return value means a potentially
     * unlimited buffer.
     */
    public int getAllowedBufferSize(@Nullable String user, @Nonnull String item);

    /**
     * Called by Lightstreamer Kernel to ask for the allowance of a publishing
     * Mode for an Item. A publishing Mode can or cannot be allowed depending
     * on the User.
     * <BR>
     * The Metadata Adapter should ensure that conflicting Modes are not
     * both allowed for the same Item (even for different Users),
     * otherwise some Requests will be eventually refused by Lightstreamer
     * Kernel.
     * The conflicting Modes are MERGE, DISTINCT and COMMAND.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getItems(String, String, String)}.
     *
     * @param user A User name.
     * @param item An Item name.
     * @param mode A publishing Mode.
     * @return true if the publishing Mode is allowed.
     */
    public boolean isModeAllowed(@Nullable String user, @Nonnull String item, @Nonnull Mode mode);

    /**
     * Called by Lightstreamer Kernel to ask for the allowance of a publishing
     * Mode for an Item (for at least one User).
     * The Metadata Adapter should ensure that conflicting Modes are not
     * both allowed for the same Item.
     * The conflicting Modes are MERGE, DISTINCT and COMMAND.
     * <BR>
     * <BR>The method should perform fast. If the implementation is slow,
     * it might delay the subscription request management.
     * In that case, configuring a dedicated "SET" thread pool for requests
     * on this Adapter Set is recommended, in order not to block operations
     * for different Adapter Sets. If the delay affects requests for a
     * specific Data Adapter, then configuring a dedicated "DATA" thread pool
     * for that Data Adapter is a better option.
     *
     * @param item An Item name.
     * @param mode A publishing Mode.
     * @return true if the publishing Mode is allowed.
     */
    public boolean modeMayBeAllowed(@Nonnull String item, @Nonnull Mode mode);

    /**
     * Called by Lightstreamer Kernel to ask for the allowance of a Selector
     * for an Item. Typically, a Selector is intended for one Item or for
     * a specific set of Items with some characteristics. Moreover, a Selector
     * can or cannot be allowed depending on the User.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getItems(String, String, String)}.
     *
     * @param user A User name.
     * @param item An Item name.
     * @param selector A selector name.
     * @return true if the Selector is allowed.
     */
    public boolean isSelectorAllowed(@Nullable String user, @Nonnull String item, @Nonnull String selector);

    /**
     * Called by Lightstreamer Kernel in order to filter events pertaining
     * to an ItemEventBuffer, if the related Item was requested within a Table
     * (i&#46;e&#46;: Subscription) with an associated Selector.
     * If the return value is true, the event is dispatched to the
     * ItemEventBuffer; otherwise, it is filtered out.
     * <BR>
     * If any consistency rule for the update flow is guaranteed by the Server
     * for the Item (e.g. mandatory snapshot in MERGE mode or ADD-UPDATE-DELETE
     * sequencing in COMMAND mode), it is a responsibility of the isSelected
     * method implementation to ensure that these consistency rules keep being
     * honored.
     * <BR>Upon a {@link com.lightstreamer.interfaces.data.ItemEventListener#clearSnapshot}
     * request issued by the Data Adapter for an item in COMMAND mode, a special
     * event is associated, with "key" field null and "command" field valued
     * with "DELETEALL"; this event is equivalent to a DELETE event for each
     * active key and it is shown to allow for keeping the item state and to
     * help obeying the consistency rules, but it cannot be filtered out.
     * <BR>
     * <BR>The method must perform fast. External information needed to
     * execute it must have been previously gathered and cached by
     * {@link #notifyUser(String, String, Map)}. If the implementation
     * were slow, the whole update delivery process, even for different
     * sessions, would be slowed down.
     *
     * @param user A User name.
     * @param item An Item name.
     * @param selector A selector name.
     * @param event An update event for the Item.
     * @return true if the event is to be processed by the ItemEventBuffer.
     */
    public boolean isSelected(@Nullable String user, @Nonnull String item, @Nonnull String selector, @Nonnull ItemEvent event);

    /**
     * Called by Lightstreamer Kernel to know whether the Metadata Adapter
     * must or must not be given a chance to modify the values carried by the
     * updates for a supplied Item in a push Session owned by a supplied User.
     * If this method returns true, the customizeUpdate method will be called
     * for each update for these Item and User. If it returns false, the
     * customizeUpdate method will never be called for these Item and User,
     * saving some processing time.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getItems(String, String, String)}.
     *
     * @param user A User name.
     * @param item An Item name.
     * @return true if the Metadata Adapter must be notified any time
     * an update for the Item is received in a Session owned by the User.
     */
    public boolean enableUpdateCustomization(@Nullable String user, @Nonnull String item);
    
    /**
     * Called by Lightstreamer Kernel in order to customize events pertaining
     * to an ItemEventBuffer, if such customization has been requested
     * through the enableUpdateCustomization method.
     * <BR>
     * The supplied event can be changed inside this method before being
     * processed by the ItemEventBuffer. Note that the applied changes only
     * affect one ItemEventBuffer instance. This means that impacts on overall
     * performance may be significant if customization for some items applies
     * to all sessions. 
     * <BR>
     * If any consistency rule for the update is guaranteed by the Server
     * for the Item, it is a responsibility of the customizeUpdate method
     * implementation to ensure that these consistency rules keep being
     * honoured; for example, "key" and "command" fields for an Item to be
     * requested in COMMAND mode should not be altered.
     * <BR>Upon a {@link com.lightstreamer.interfaces.data.ItemEventListener#clearSnapshot}
     * request issued by the Data Adapter for an item in COMMAND mode, a special
     * event is associated, with "key" field null and "command" field valued
     * with "DELETEALL"; this event is equivalent to a DELETE event for each
     * active key and it is shown to allow for keeping the item state, but it
     * cannot be customized in any way.
     * <BR>
     * <BR>The method must perform fast. External information needed to
     * execute it must have been previously gathered and cached by
     * {@link #notifyUser(String, String, Map)}. If the implementation
     * were slow, the whole update delivery process, even for different
     * sessions, would be slowed down.
     *
     * @param user A User name.
     * @param item An Item name.
     * @param event An update event for the Item, ready to be changed.
     */
    public void customizeUpdate(@Nullable String user, @Nonnull String item, @Nonnull CustomizableItemEvent event);

    /**
     * Called by Lightstreamer Kernel to ask for the minimum ItemEvent
     * frequency from the supplier Data Adapter at which the events for an Item are
     * guaranteed to be delivered to the Clients without loss of information.
     * In case of an incoming ItemEvent frequency greater than the specified
     * frequency, Lightstreamer Kernel may prefilter the events flow down to
     * this frequency.
     * Such prefiltering applies only for Items requested with publishing Mode
     * MERGE or DISTINCT.
     * <BR>
     * The frequency set should be greater than the ItemUpdate frequencies
     * allowed to the different Users for that Item. Moreover, because this
     * filtering is made without buffers, the frequency set should be far
     * greater than the ItemUpdate frequencies allowed for that Item for which
     * buffering of event bursts is desired.
     * If an Item is requested with publishing Mode MERGE or DISTINCT and
     * unfiltered dispatching, then specifying any limited source frequency
     * will cause the refusal of the request by the Kernel.
     * <BR>
     * This feature is just for ItemEventBuffers protection against Items with
     * a very fast flow on the supplier Data Adapter and a very slow flow allowed
     * to the Clients. If this is the case, but just a few Clients need a fast
     * or unfiltered flow for the same MERGE or DISTINCT Item, the use of two
     * differently named Items that receive the same flow from the Data Adapter
     * is suggested.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #modeMayBeAllowed(String, Mode)}.
     *
     * @param item An Item Name.
     * @return The minimum ItemEvent frequency that must be processed without
     * loss of information, in ItemEvents/sec. A zero return value indicates
     * that incoming ItemEvents must not be prefiltered. If the ItemEvents
     * frequency for the Item is known to be very low, returning zero allows
     * Lightstreamer Kernel to save any prefiltering effort.
     */
    @Nonnull
    public double getMinSourceFrequency(@Nonnull String item);

    /**
     * Called by Lightstreamer Kernel to ask for the maximum allowed length
     * for a Snapshot of an Item that has been requested with publishing Mode
     * DISTINCT. In fact, in DISTINCT publishing Mode, the Snapshot for an Item
     * is made by the last events received for the Item and the Client can
     * specify how many events it would like to receive. Thus, Lightstreamer
     * Kernel must always keep a buffer with some of the last events received
     * for the Item and the lenght of the buffer is limited by the value
     * returned by this method. The maximum Snapshot size cannot be unlimited.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #modeMayBeAllowed(String, Mode)}.
     *
     * @param item An Item Name.
     * @return The maximum allowed length for the Snapshot; a zero return
     * value means that no Snapshot information should be kept.
     */
    public int getDistinctSnapshotLength(@Nonnull String item);

    /**
     * Called by Lightstreamer Kernel to forward a message received by a User.
     * The interpretation of the message is up to the Metadata Adapter.
     * A message can also be refused.
     * <BR>
     * <BR>The method should perform fast and the message processing should
     * be done asynchronously. If the implementation is slow, it might
     * propagate the delay to other operations and other sessions.
     * In that case, configuring a dedicated "MSG" thread pool for message
     * management on the related Adapter Set is recommended, in order not
     * to block other types of operations.
     *
     * @param user A User name.
     * @param  sessionID  The ID of a Session owned by the User.
     * @param  message  A non-null string. 
     * @throws CreditsException if the User is not enabled to send the
     * message or the message cannot be correctly managed.
     * @throws NotificationException if something is wrong in the parameters,
     * such as a nonexistent Session ID.
     */
    public void notifyUserMessage(@Nullable String user, @Nonnull String sessionID, @Nonnull String message)
        throws CreditsException, NotificationException;

    /**
     * Called by Lightstreamer Kernel to check that a User is enabled to open
     * a new push Session.
     * If the check succeeds, this also notifies the Metadata Adapter that
     * the Session is being assigned to the User.
     * <BR>Request context information is also available; this allows for
     * differentiating group, schema and message management based on specific
     * Request characteristics.
     * <BR>
     * <BR>The method should perform as fast as possible. External information
     * needed to execute it should have been previously gathered and cached by
     * {@link #notifyUser(String, String, Map)}. If the implementation
     * is slow because of complex data gathering operations, it might delay the
     * client session activation.
     * In that case, configuring a dedicated "SET" thread pool for this Adapter Set
     * is recommended, in order not to block operations for different Adapter Sets.
     *
     * @param user A User name.
     * @param  sessionID  The ID of a new Session.
     * @param clientContext A Map-type value object that contains information
     * about the request context. Unless specified, the values are supplied as strings.
     * Information related to a client connection refers to the HTTP request
     * that originated the call. Available keys are:
     * <ul>
     * <li>"REMOTE_IP" - string representation of the remote IP related to the
     * current connection; it may be a proxy address</li>
     * <li>"REMOTE_PORT" - string representation of the remote port related
     * to the current connection</li>
     * <li>"USER_AGENT" - the user-agent as declared in the current connection
     * HTTP header</li>
     * <li>"FORWARDING_INFO" - the comma-separated list of addresses forwarded
     * by intermediaries, obtained from the X-Forwarded-For HTTP header,
     * related to the current connection; intermediate proxies usually set
     * this header to supply connection routing information.
     * Note that if the number of forwards to be considered local to the Server
     * environment has been specified through the &lt;skip_local_forwards&gt;
     * configuration element, in order to better determine the remote address,
     * then these forwards will not be included in the list.</li>
     * <li>"LOCAL_SERVER" - the name of the specific server socket that handles
     * the current connection, as configured through the &lt;http_server&gt;
     * or &lt;https_server&gt; element</li>
     * <li>"CLIENT_TYPE" - the type of client API in use. The value may be null
     * for some old client APIs</li>
     * <li>"CLIENT_VERSION" - the signature, including version and build number,
     * of the client API in use. The signature may be only partially complete,
     * or even null, for some old client APIs and for some custom clients</li>
     * <li>"HTTP_HEADERS" - the same Map object that has just been supplied
     * to {@link #notifyUser(String, String, Map)} for the current client
     * request instance; note that any authorization information that was
     * supplied by the backend through the authentication request could be
     * added to the Map in {@link #notifyUser(String, String, Map)} and can
     * be got back here. This allows for using local authentication-related
     * details for the authorization task.</li>
     * </ul>
     * @throws CreditsException if the User is not enabled to open the new
     * Session.
     * <BR>If it's possibile that the User would be enabled as soon as another
     * Session were closed, then a {@link ConflictingSessionException} can be
     * thrown, in which the ID of the other Session must be specified.
     * In this case, a second invocation of the method with the same
     * "HTTP_HEADERS" Map and a different Session ID will be received.
     * @throws NotificationException if something is wrong in the parameters,
     * such as the ID of a Session already open for this or a different User.
     */
    public void notifyNewSession(@Nullable String user, @Nonnull String sessionID,  @Nonnull Map clientContext)
        throws CreditsException, NotificationException;

    /**
     * Called by Lightstreamer Kernel to ask for an optional time-to-live setting
     * for a session just started.
     * If this setting is not needed, the method can be left unimplemented
     * (sticking to its default implementation, which poses no limit).
     * <BR>If the session is terminated due to this setting, the originating
     * client will receive the notification of the termination according with the API
     * in use, together with a proper cause code.
     * <BR>
     * <BR>The method should perform fast. External information needed to
     * execute it should have been previously gathered and cached by
     * {@link #notifyNewSession(String, String, Map)}. If the implementation
     * is slow, it might delay the client session activation.
     * In that case, configuring a dedicated "SET" thread pool for requests
     * on this Adapter Set is recommended, in order not to block operations
     * for different Adapter Sets.
     * 
     * @param user A User name.
     * @param session A session ID.
     * @return The time-to-live setting to be applied to the specified session,
     * as a positive number of seconds. If zero or negative, no time-to-live
     * limit will be applied.
     */
    public default int getSessionTimeToLive(@Nullable String user, @Nonnull String session) {
        return 0;
    }

    /**
     * Called by Lightstreamer Kernel to notify the Metadata Adapter that
     * a push Session has been closed.
     * After this invocation, no more calls to {@link #notifyNewTables} and
     * {@link #notifyTablesClose} for this sessionID are possible.
     * On the other hand, trailing invocations of methods related with the
     * validation of client requests, like {@link #getItems}, are still possible
     * on parallel threads and accepting them would have no effect.
     * However, if the method may have side-effects on the Adapter, like
     * {@link #notifyUserMessage}, the Adapter is responsible for checking
     * if the session is still valid.
     * <BR>
     * <BR>The method must perform fast. A slow implementation could propagate
     * delays also on different sessions.
     *
     * @param sessionID A Session ID.
     * @throws NotificationException if something is wrong in the parameters,
     * such as the ID of a Session that is not currently open.
     */
    public void notifySessionClose(@Nonnull String sessionID) throws NotificationException;

    /**
     * Called by Lightstreamer Kernel to know whether the Metadata Adapter
     * must or must not be notified any time a Table (i&#46;e&#46;: Subscription)
     * is added or removed from a push Session owned by a supplied User.
     * If this method returns false, the methods {@link #notifyNewTables} and
     * {@link #notifyTablesClose} will never be called for this User, saving some
     * processing time. In this case, the User will be allowed to add to his
     * Sessions any Tables (i&#46;e&#46;: Subscriptions) he wants.
     * <BR>
     * <BR>The method should perform fast. See the notes for
     * {@link #getAllowedMaxBandwidth(String)}.
     *
     * @param user A User name.
     * @return true if the Metadata Adapter must be notified any time a Table
     * (i&#46;e&#46;: Subscription) is added or removed from a Session owned by the User.
     */
    public boolean wantsTablesNotification(@Nullable String user);

    /**
     * Called by Lightstreamer Kernel to check that a User is enabled to add
     * some Tables (i&#46;e&#46;: Subscriptions) to a push Session.
     * If the check succeeds, this also notifies the Metadata Adapter that
     * the Tables are being added to the Session.
     * <BR>The method is invoked only if enabled for the User through
     * {@link #wantsTablesNotification}.
     * <BR>
     * <BR>The method should perform fast. Any complex data gathering
     * operation (like a check on the overall number of subscribed Items)
     * should have been already performed asynchronously. See the notes
     * for {@link #getItems(String, String, String)} for details; but see
     * the &lt;sequentialize_table_notifications&gt; parameter available
     * in adapters.xml as well.
     *
     * @param user A User name.
     * @param sessionID  The ID of a Session owned by the User.
     * @param tables An array of TableInfo instances, each of them containing
     * the details of a Table (i&#46;e&#46;: Subscription) to be added to the Session.
     * The elements in the array represent Tables (i&#46;e&#46;: Subscriptions) whose
     * subscription is requested atomically by the client. A single element
     * should be expected in the array, unless clients based on a very old
     * version of a client library or text protocol may be in use.
     * @throws CreditsException if the User is not allowed to add the
     * specified Tables (i&#46;e&#46;: Subscriptions) to the Session.
     * @throws NotificationException if something is wrong in the parameters,
     * such as the ID of a Session that is not currently open or inconsistent
     * information about a Table (i&#46;e&#46;: Subscription).
     */
    public void notifyNewTables(@Nullable String user, @Nonnull String sessionID, @Nonnull TableInfo[] tables)
        throws CreditsException, NotificationException;

    /**
     * Called by Lightstreamer Kernel to notify the Metadata Adapter that
     * some Tables (i&#46;e&#46;: Subscriptions) have been removed from a push Session.
     * <BR>The method is invoked only if enabled for the User through
     * {@link #wantsTablesNotification}.
     * <BR>
     * <BR>The method must perform fast. A slow implementation could propagate
     * delays also on different sessions.
     *
     * @param sessionID A Session ID.
     * @param tables An array of TableInfo instances, each of them containing
     * the details of a Table (i&#46;e&#46;: Subscription) that has been removed from the Session.
     * The supplied array is in 1:1 correspondance with the array supplied by
     * {@link #notifyNewTables} in a previous call;
     * the correspondance can be recognized by matching {@link TableInfo#getWinIndex}
     * as returned by the included objects (if multiple objects are included,
     * it must be the same for all of them).
     * @throws NotificationException if something is wrong in the parameters,
     * such as the ID of a Session that is not currently open or a Table
     * (i&#46;e&#46;: Subscription) that is not contained in the Session.
     */
    public void notifyTablesClose(@Nonnull String sessionID, @Nonnull TableInfo[] tables)
        throws NotificationException;
    
    /**
     * Called by Lightstreamer Kernel to check that a User is enabled to access
     * the specified MPN device. The success of this method call is
     * a prerequisite for all MPN operations, including the activation of a
     * subscription, the deactivation of a subscription, the change of a device
     * token, etc. Some of these operations have a subsequent specific notification,
     * i.e. {@link #notifyMpnSubscriptionActivation} and {@link #notifyMpnDeviceTokenChange}. 
     * <BR>
     * <BR>Take particular precautions when authorizing device access, if
     * possible ensure the user is entitled to the specific platform, 
     * device token and application ID.
     * <BR>
     * <BR>The method should perform fast. External information needed to
     * execute it should have been previously gathered and cached by
     * {@link #notifyUser(String, String, Map)}. If the implementation
     * is slow, it might delay the notification request management.
     * In that case, configuring a dedicated "MPN_REQUESTS" thread pool for
     * MPN-related requests on this Adapter Set is recommended, in order not
     * to block operations for different Adapter Sets or different operations
     * for this Adapter Set.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>Push Notifications is an optional feature, available
     * depending on Edition and License Type.
     * To know what features are enabled by your license, please see the License
     * tab of the Monitoring Dashboard (by default, available at /dashboard).
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param device specifies an MPN device.
     * @throws CreditsException if the User is not allowed to access the
     * specified MPN device in the Session.
     * @throws NotificationException if something is wrong in the parameters,
     * such as inconsistent information about the device.
     */
    public void notifyMpnDeviceAccess(@Nullable String user, @Nonnull String sessionID, @Nonnull MpnDeviceInfo device)
            throws CreditsException, NotificationException;

    /**
     * Called by Lightstreamer Kernel to check that a User is enabled 
     * to activate a Push Notification subscription.
     * If the check succeeds, this also notifies the Metadata Adapter that
     * Push Notifications are being activated.
     * <BR>
     * <BR>Take particular precautions when authorizing subscriptions, if
     * possible check for validity the trigger expression reported by 
     * {@link MpnSubscriptionInfo#getTrigger}, as it may contain maliciously 
     * crafted code. The MPN notifiers configuration file contains a first-line 
     * validation mechanism based on regular expression that may also be used 
     * for this purpose.
     * <BR> 
     * <BR>The method should perform fast. Any complex data gathering
     * operation (like a check on the overall number of Push Notifications
     * activated) should have been already performed asynchronously.
     * See the notes for {@link #notifyMpnDeviceAccess} for details.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>Push Notifications is an optional feature, available
     * depending on Edition and License Type.
     * To know what features are enabled by your license, please see the License
     * tab of the Monitoring Dashboard (by default, available at /dashboard).
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User. The session ID is
     * provided for a thorough validation of the Table information, but Push 
     * Notification subscriptions are persistent and survive the session. Thus,
     * any association between this Session ID and this Push Notification
     * subscription should be considered temporary. 
     * @param table A TableInfo instance, containing the details of a Table 
     * (i.e.: Subscription) for which Push Notification have to be activated.
     * @param mpnSubscription An MpnSubscriptionInfo instance, containing the
     * details of a Push Notification to be activated.
     * @throws CreditsException if the User is not allowed to activate the
     * specified Push Notification in the Session.
     * @throws NotificationException if something is wrong in the parameters,
     * such as inconsistent information about a Table (i.e.: Subscription) or 
     * a Push Notification.
     */
    public void notifyMpnSubscriptionActivation(@Nullable String user, @Nonnull String sessionID, @Nonnull TableInfo table, @Nonnull MpnSubscriptionInfo mpnSubscription)
            throws CreditsException, NotificationException;
    
    /**
     * Called by Lightstreamer Kernel to check that a User is enabled to change
     * the token of an MPN device. 
     * If the check succeeds, this also notifies the Metadata Adapter that future
     * client requests should be issued by specifying the new device token.
     * <BR>
     * <BR>Take particular precautions when authorizing device token changes,
     * if possible ensure the user is entitled to the new device token.
     * <BR>
     * <BR>The method should perform fast. Any complex data gathering
     * operation (like a check on the devices currently served) should have been
     * already performed asynchronously.
     * See the notes for {@link #notifyMpnDeviceAccess} for details.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>Push Notifications is an optional feature, available
     * depending on Edition and License Type.
     * To know what features are enabled by your license, please see the License
     * tab of the Monitoring Dashboard (by default, available at /dashboard).
     * 
     * @param user A User name.
     * @param sessionID The ID of a Session owned by the User.
     * @param device specifies an MPN device.
     * @param newDeviceToken The new token being assigned to the device.
     * @throws CreditsException if the User is not allowed to change the
     * specified device token.
     * @throws NotificationException if something is wrong in the parameters,
     * such as inconsistent information about the device.
     */
    public void notifyMpnDeviceTokenChange(@Nullable String user, @Nonnull String sessionID, @Nonnull MpnDeviceInfo device, @Nonnull String newDeviceToken)
            throws CreditsException, NotificationException;
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
