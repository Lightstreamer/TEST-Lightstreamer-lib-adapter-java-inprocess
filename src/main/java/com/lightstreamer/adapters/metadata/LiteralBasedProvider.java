/*
*
* Copyright (c) Lightstreamer Srl
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package com.lightstreamer.adapters.metadata;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lightstreamer.interfaces.metadata.AccessException;
import com.lightstreamer.interfaces.metadata.CreditsException;
import com.lightstreamer.interfaces.metadata.ItemsException;
import com.lightstreamer.interfaces.metadata.MetadataProviderAdapter;
import com.lightstreamer.interfaces.metadata.MetadataProviderException;
import com.lightstreamer.interfaces.metadata.Mode;
import com.lightstreamer.interfaces.metadata.SchemaException;

/**
 * Simple full implementation of a Metadata Adapter, made available
 * in Lightstreamer SDK. The Adapter is not meant for production use,
 * but it can be used as a starting point for real Adapters. <BR>
 * 
 * The class handles Item List specifications, a special case of Item Group name
 * formed by simply concatenating the names of the Items contained in a List
 * in a space separated way. Similarly, the class
 * handles Field List specifications, a special case of Field Schema name
 * formed by concatenating the names of the contained Fields.
 * The handling of Item List and Field List specifications is required by
 * some optional methods in the client APIs that take advantage of Item Lists
 * and Field Lists. <BR>
 * 
 * The resource levels are assigned the same for all Items and Users,
 * according with values that can be supplied together with adapter
 * configuration, inside the "metadata_provider" element that defines the
 * Adapter. <BR>
 * 
 * The return of the getAllowedMaxBandwidth method can be supplied in a
 * "max_bandwidth" parameter; the return of the getAllowedMaxItemFrequency
 * method can be supplied in a "max_frequency" parameter; the return of the
 * getAllowedBufferSize method can be supplied in a "buffer_size" parameter;
 * the return of the getDistinctSnapshotLength method can be supplied
 * in a "distinct_snapshot_length" parameter; the return of the
 * getMinSourceFrequency method can be supplied in a "prefilter_frequency"
 * parameter. All resource limits not supplied are granted as unlimited,
 * but for distinct_snapshot_length, which defaults as 10. <BR>
 * 
 * The return of the modeMayBeAllowed method (i.e. the association of the
 * proper publishing Mode to each Item) can be configured by supplying a list
 * of rules, which define Item families, where all Items in each family share
 * the same set of allowed Modes
 * (but remember that only one out of the MERGE, DISTINCT and COMMAND Modes
 * is supported by the Server for each Item; only the RAW mode is supported
 * without restrictions).
 * Each family is specified by providing a pattern upon which all names
 * of the Items in the family need to match and, optionally, the involved
 * Data Adapter.
 * The description of each family can be supplied with a group of parameters,
 * named "item_family_&lt;n&gt;", "data_adapter_for_item_family_&lt;n&gt;",
 * and "modes_for_item_family_&lt;n&gt;",
 * where &lt;n&gt; is a progressive number, unique for each family.
 * The first parameter specifies the pattern, in java.util.regex.Pattern
 * format, while the second one is optional and specifies the Data Adapter
 * (if missing, the rule applies regardless of the Data Adapter).
 * The third parameter specifies the allowed modes for this family,
 * as a list of names, with commas and spaces as allowed separators.
 * In case more than one rule applies, the one with the smallest progressive
 * is considered and the Item is assigned only to that family.
 * Items that do not belong to any family are not allowed in any Mode;
 * however, if no families are defined at all, then all Items are allowed
 * in all Modes and the Clients should ensure that the same Item cannot be
 * requested in two conflicting Modes. <BR>
 * 
 * There are no access restrictions, but an optional User name check is
 * performed if a comma separated list of User names is supplied in an
 * "allowed_users" parameter. <BR>
 * 
 * The following snippet shows an example of configuration of this Adapter
 * in adapters.xml:
 * <PRE>
 * {@code
<!-- Mandatory. Define the Metadata Provider. -->
<metadata_provider>

    <!-- Mandatory. Java class name of the adapter. -->
    <adapter_class>com.lightstreamer.adapters.metadata.LiteralBasedProvider</adapter_class>

    ......

    <!-- Optional. List of initialization parameters specific for LiteralBasedProvider. -->

    <!-- Optional.
         Define values to be returned in getAllowedMaxBandwidth(),
         getAllowedMaxItemFrequency(), getAllowedBufferSize() and
         getDistinctSnapshotLength() methods, for any User and Item
         supplied. -->
    <!--
    <param name="max_bandwidth">40</param>
    <param name="max_frequency">3</param>
    <param name="buffer_size">30</param>
    <param name="distinct_snapshot_length">10</param>
     -->

    <!-- Optional.
         Define comma-separated list of User names to be checked
         for allowance by the notifyUser() method. -->
    <!--
    <param name="allowed_users">user123,user456</param>
     -->

    <!-- Optional.
         Define how the modeMayBeAllowed method should behave, by
         associating to each item the modes in which it can be managed
         by the Server.
         Each triple of parameters of the form "item_family_<n>",
         "data_adapter_for_item_family_<n> (optional), and
         "modes_for_item_family_<n>" define respectively the item name
         pattern (in java.util.regex.Pattern format),
         the related Data Adapter, and the allowed
         modes (in comma separated format) for a family of items.
         Each item is assigned to the first family that matches its name
         and (if supplied) the related Data Adapter.
         If no families are specified at all, then modeMayBeAllowed
         always returns true, though this is not recommended, because
         the Server does not support more than one mode out of MERGE,
         DISTINCT, and COMMAND for the same item. In such a case, the
         Server would just manage each item in the mode specified by the
         first Client request it receives for the item and would be up to
         the Clients to ensure that the same item cannot be requested in
         two conflicting Modes. -->
    <param name="item_family_1">item.*</param>
    <param name="data_adapter_for_item_family_1">MyDataAdapter</param>
    <param name="modes_for_item_family_1">MERGE</param>
    <!--
    <param name="item_family_2">portfolio.*</param>
    <param name="modes_for_item_family_2">COMMAND</param>
     -->

</metadata_provider>
 * }
 * </PRE>
 * 
 * The class, together with the inherited
 * {@link com.lightstreamer.interfaces.metadata.MetadataProviderAdapter},
 * also provides implementations for old signature versions
 * of some methods (in the form of overloads of the current version)
 * and it forwards the implementations for the current signature
 * versions to the old ones, which involves discarding some arguments
 * (with the exception of modeMayBeAllowed, which behaves in a slightly
 * different way).
 * As a consequence, a custom Metadata Adapter inheriting from this class
 * is allowed to stick to old signature versions for its own implementations,
 * although the use of the current versions is recommended.
 * However, the restrictions related to the
 * {@link com.lightstreamer.interfaces.metadata.MetadataProvider}
 * interface still hold: the custom part of the Adapter is only allowed
 * to implement at most one version for each interface method, otherwise
 * the Adapter can be refused (the exception, here, is notifyUser,
 * which has two current overloaded versions).
 */
public class LiteralBasedProvider extends MetadataProviderAdapter {

    private String[] allowedUsers;

    private double maxBandwidth;
    private double maxFrequency;
    private double prefilterFrequency;
    private int bufferSize;
    private int distinctSnapshotLength;

    private static class ItemFamily {
        private String dataAdapter;
        private Pattern pattern;
        private Set allowedModes;

        private ItemFamily(String dataAdapter, Pattern pattern, Set modes) {
            allowedModes = modes;
            this.dataAdapter = dataAdapter;
            this.pattern = pattern;
        }
    }
    
    private ItemFamily[] families;

    /**
     * Void constructor required by Lightstreamer Kernel.
     */
    public LiteralBasedProvider() {
        // legal empty block
    }

    /**
     * Reads configuration settings for user and resource constraints.
     * If some setting is missing, the corresponding constraint is not set.
     *
     * @param  params  Can contain the configuration settings. 
     * @param  dir  Not used.
     * @throws MetadataProviderException in case of configuration errors.
     */
    @Override
    public void init(Map params, File dir) throws MetadataProviderException {
        String currParam = null;
        try {
            currParam = "allowed_users";
            String users = (String) params.get(currParam);
            if (users != null) {
                if (users.indexOf(",") < 0) {
                    allowedUsers = new String[1];
                    allowedUsers[0] = users;
                } else {
                    StringTokenizer tokenizer = new StringTokenizer(users, ",");
                    allowedUsers = new String[tokenizer.countTokens()];
                    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
                        allowedUsers[i] = tokenizer.nextToken();
                    }
                }
            }
    
            currParam = "max_bandwidth";
            String mb = (String) params.get(currParam);
            if (mb != null) {
                maxBandwidth = Double.parseDouble(mb);
            } else {
                maxBandwidth = 0.0; // unlimited
            }
    
            currParam = "max_frequency";
            String mf = (String) params.get(currParam);
            if (mf != null) {
                maxFrequency = Double.parseDouble(mf);
            } else {
                maxFrequency = 0.0; // unlimited
            }
    
            currParam = "prefilter_frequency";
            String pf = (String) params.get(currParam);
            if (pf != null) {
                prefilterFrequency = Double.parseDouble(pf);
            } else {
                prefilterFrequency = 0.0; // no prefiltering
            }
    
            currParam = "buffer_size";
            String bs = (String) params.get(currParam);
            if (bs != null) {
                bufferSize = Integer.parseInt(bs);
            } else {
                bufferSize = 0; // unlimited
            }
    
            currParam = "distinct_snapshot_length";
            String dsl = (String) params.get(currParam);
            if (dsl != null) {
                distinctSnapshotLength = Integer.parseInt(dsl);
            } else {
                distinctSnapshotLength = 10;
            }
            
            final String familyPrefix = "item_family_";
            Iterator paramNames = params.keySet().iterator();
            int rules = 0;
            int lastFamily = 0;
            while (paramNames.hasNext()) {
                currParam = (String) paramNames.next();
                if (currParam.startsWith(familyPrefix)) {

                    String strNumber = currParam.substring(familyPrefix.length());
                    int familyNumber = Integer.parseInt(strNumber);
                    if (! currParam.equals(familyPrefix + familyNumber)) {
                        throw new Exception("badly formed item family parameter name");
                    }
                    if (familyNumber <= 0) {
                        throw new Exception("non positive item family number found");
                    } else if (familyNumber > lastFamily) {
                        lastFamily = familyNumber;
                    }
                    rules++;
                }
            }

            if (rules > 0) {
                final String modesPrefix = "modes_for_item_family_";
                final String dataAdapterPrefix = "data_adapter_for_item_family_";
                families = new ItemFamily[rules];
                int rule = 0;
                for (int currFamily = 1; currFamily <= lastFamily; currFamily++) {
                    currParam = familyPrefix + currFamily;
                    if (params.containsKey(currParam)) {
                        String strPattern = (String) params.get(currParam);
                        Pattern pattern = Pattern.compile(strPattern);

                        currParam = modesPrefix + currFamily;
                        String strModes = (String) params.get(currParam);
                        if (strModes == null) {
                            strModes = "";
                        }
                        StringTokenizer tokenizer = new StringTokenizer(strModes, ", ");
                        Set modes = new HashSet();
                        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
                            String strMode = tokenizer.nextToken();
                            modes.add(toMode(strMode));
                        }

                        currParam = dataAdapterPrefix + currFamily;
                        String dataAdapter = (String) params.get(currParam);
                            // can be null, i.e. missing

                        families[rule] = new ItemFamily(dataAdapter, pattern, modes);
                        rule++;
                    }
                }
            } else {
                families = null;
            }
        } catch (Exception e) {
            throw new MetadataProviderException("error reading parameter " + currParam + ": " + e);
        }
    }

    private Mode toMode(String strMode) throws Exception {
        if (strMode.equals(Mode.RAW.toString())) {
            return Mode.RAW;
        } else if (strMode.equals(Mode.MERGE.toString())) {
            return Mode.MERGE;
        } else if (strMode.equals(Mode.DISTINCT.toString())) {
            return Mode.DISTINCT;
        } else if (strMode.equals(Mode.COMMAND.toString())) {
            return Mode.COMMAND;
        } else {
            throw new Exception("Invalid items mode: " + strMode);
        }
    }

    private String[] tokenize(String str) {
        StringTokenizer source = new StringTokenizer(str, " ");
        int dim = source.countTokens();
        String[] ret = new String[dim];

        for (int i = 0; i < dim; i++) {
            ret[i] = source.nextToken();
        }
        return ret;
    }

    /**
     * Resolves an Item List specification supplied in a Request. The names of the Items
     * in the List are returned.
     * The operation is deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This also ensures
     * backward compatibility with old adapter classes derived from this one.
     *
     * @param user A User name.
     * @param sessionID A Session ID.
     * @param itemList An Item List specification.
     * @param dataAdapter The name of the Data Adapter to which the Item List is targeted.
     * @return An array with the names of the Items in the List, demanded to the 3-arguments overload.
     * @throws ItemsException demanded to the 3-arguments overload.
     */
    public String[] getItems(String user, String sessionID, String itemList, String dataAdapter) throws ItemsException {
        return getItems(user, sessionID, itemList);
    }

    /**
     * Reduced, backward-compatibility version of the Item List determination method.
     * The operation is deferred to an even more reduced version of the method,
     * where the sessionID argument is discarded. This also ensures
     * backward compatibility with very old adapter classes derived from this one.
     *
     * @param user A User name.
     * @param sessionID A Session ID.
     * @param itemList An Item List specification.
     * @return An array with the names of the Items in the List, demanded to the 2-arguments overload.
     * @throws ItemsException demanded to the 2-arguments overload.
     * 
     * @see #getItems(String, String, String, String)
     */
    public String[] getItems(String user, String sessionID, String itemList) throws ItemsException {
        return getItems(user, itemList);
    }

    /**
     * Reduced, backward-compatibility version of the Item List determination method.
     *
     * @param user A User name. Not used.
     * @param itemList An Item List specification.
     * @return An array with the names of the Items in the List.
     * @throws ItemsException never thrown.
     * 
     * @see #getItems(String, String, String, String)
     */
    public String[] getItems(String user, String itemList) throws ItemsException {
        return tokenize(itemList);
    }

    /**
     * Resolves a Field List specification supplied in a Request. The names of the Fields
     * in the List are returned.
     * The operation is deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This also ensures
     * backward compatibility with old adapter classes derived from this one.
     *
     * @param user A User name.
     * @param sessionID A Session ID.
     * @param itemList The specification of the Item List whose Items the Field List
     * is to be applied to.
     * @param dataAdapter The name of the Data Adapter to which the Item List is targeted.
     * @param fieldList A Field List specification.
     * @return An array with the names of the Fields in the List, demanded to the 4-arguments overload.
     * @throws SchemaException demanded to the 4-arguments overload.
     */
    public String[] getSchema(String user, String sessionID, String itemList, String dataAdapter, String fieldList)
            throws SchemaException {
        return getSchema(user, sessionID, itemList, fieldList);
    }

    /**
     * Reduced, backward-compatibility version of the Field List determination method.
     * The operation is deferred to an even more reduced version of the method,
     * where the sessionID argument is discarded. This also ensures
     * backward compatibility with very old adapter classes derived from this one.
     *
     * @param user A User name.
     * @param sessionID A Session ID.
     * @param itemList The specification of the Item List whose Items the Field List
     * is to be applied to.
     * @param fieldList A Field List specification.
     * @return An array with the names of the Fields in the List, demanded to the 3-arguments overload.
     * @throws SchemaException demanded to the 3-arguments overload.
     * 
     * @see #getSchema(String, String, String, String, String)
     */
    public String[] getSchema(String user, String sessionID, String itemList, String fieldList)
            throws SchemaException {
        return getSchema(user, itemList, fieldList);
    }

    /**
     * Reduced, backward-compatibility version of the Field List determination method.
     *
     * @param user A User name. Not used.
     * @param itemList The specification of the Item List whose Items the Field List
     * is to be applied to. Not used.
     * @param fieldList A Field List specification.
     * @return An array with the names of the Fields in the List.
     * @throws SchemaException never thrown.
     * 
     * @see #getSchema(String, String, String, String, String)
     */
    public String[] getSchema(String user, String itemList, String fieldList)
            throws SchemaException {
        return tokenize(fieldList);
    }

    private boolean checkUser(String user) {
        if ((allowedUsers == null) || (allowedUsers.length == 0)) {
            return true;
        }
        if (user == null) {
            return false;
        }
        for (int i = 0; i < allowedUsers.length; i++) {
            if (allowedUsers[i] == null) {
                continue;
            }
            if (allowedUsers[i].equals(user)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a user is enabled to make Requests to the related Data
     * Providers.
     * Note that, for authentication purposes, only the user and password
     * arguments should be consulted.
     * <BR>In this very simple implementation, a configured set of user names
     * is checked and no password check is done.
     * Even though the processing is immediate, we return a CompletableFuture
     * for demonstration purpose.
     *
     * @param user A User name.
     * @param password An optional password.
     * @param httpHeaders A Map that contains a name-value pair for each
     * header found in the HTTP request that originated the call. Not used.
     * @return a CompletableFuture, that may also be completed exceptionally
     * with an AccessException if a list of User names has been configured
     * and the supplied name does not belong to the list.
     * @throws AccessException never thrown
     * @throws CreditsException never thrown.
     * 
     * @see #notifyUser(String, String, Map, String)
     */
    @Override
    public CompletableFuture<Void> notifyUser(String user, String password, Map httpHeaders)
            throws AccessException, CreditsException {
        CompletableFuture<Void> outcome = new CompletableFuture<Void>();
        // very simple non-blocking implementation;
        // we don't need to implement it asynchronously
        if (!checkUser(user)) {
            outcome.completeExceptionally(new AccessException("Unauthorized user"));
        } else {
            outcome.complete(null);
        }
        return outcome;
    }

    /**
     * Extended version of the User authentication method, invoked by the
     * Server instead of the standard 3-argument one in case it has been
     * instructed (through the &lt;use_client_auth&gt; configuration flag)
     * to acquire the client principal from the client TLS/SSL certificate,
     * if available.
     * <BR>The check is deferred to the standard version of the method,
     * where the clientPrincipal argument is discarded.
     * <BR>
     * <B>Edition Note:</B>
     * <BR>https is an optional feature, available
     * depending on Edition and License Type.
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
     * authenticated itself or the authentication has failed. Not used.
     * @return a CompletableFuture or null, demanded to the 3-arguments overload.
     * @throws AccessException demanded to the 3-arguments overload.
     * @throws CreditsException demanded to the 3-arguments overload.
     */
    @Override
    public CompletableFuture<Void> notifyUser(String user, String password, Map httpHeaders,  String clientPrincipal)
            throws AccessException, CreditsException {
        return notifyUser(user, password, httpHeaders);
    }

    /**
     * Returns the bandwidth level to be allowed to a User for a push Session.
     *
     * @param user A User name. Not used.
     * @return The bandwidth, in Kbit/sec, as supplied in the Metadata
     * Adapter configuration.
     */
    @Override
    public double getAllowedMaxBandwidth(String user) {
        return maxBandwidth;
    }

    /**
     * Returns the ItemUpdate frequency to be allowed to a User for a specific
     * Item.
     * The operation is deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This also ensures
     * backward compatibility with old adapter classes derived from this one.
     *
     * @param user A User name.
     * @param item An Item Name.
     * @param dataAdapter A Data Adapter name.
     * @return A frequency limit, demanded to the 2-arguments overload.
     */
    @Override
    public double getAllowedMaxItemFrequency(String user, String item, String dataAdapter) {
        return getAllowedMaxItemFrequency(user, item);
    }

    /**
     * Reduced, backward-compatibility version of the ItemUpdate frequency
     * authorization method.
     *
     * @param user A User name. Not used.
     * @param item An Item Name. Not used.
     * @return The allowed Update frequency, in Updates/sec, as supplied
     * in the Metadata Adapter configuration.
     * 
     * @see #getAllowedMaxItemFrequency(String, String, String)
     */
    @Override
    public double getAllowedMaxItemFrequency(String user, String item) {
        return maxFrequency;
    }

    /**
     * Returns the size of the buffer internally used to enqueue subsequent
     * ItemUpdates for the same Item.
     * The operation is deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This also ensures
     * backward compatibility with old adapter classes derived from this one.
     *
     * @param user A User name.
     * @param item An Item Name.
     * @param dataAdapter A Data Adapter name.
     * @return A buffer size, demanded to the 2-arguments overload.
     */
    @Override
    public int getAllowedBufferSize(String user, String item, String dataAdapter) {
        return getAllowedBufferSize(user, item);
    }

    /**
     * Reduced, backward-compatibility version of the buffer size authorization method.
     *
     * @param user A User name. Not used.
     * @param item An Item Name. Not used.
     * @return The allowed buffer size, as supplied in the Metadata Adapter
     * configuration.
     * 
     * @see #getAllowedBufferSize(String, String, String)
     */
    @Override
    public int getAllowedBufferSize(String user, String item) {
        return bufferSize;
    }

    /**
     * Returns the minimum ItemEvent frequency from the supplier Data Adapter at which
     * the events for an Item are guaranteed to be delivered to the Clients
     * without loss of information.
     * In case of an incoming ItemEvent frequency greater than the specified
     * frequency, Lightstreamer Kernel may prefilter the events flow down to
     * this frequency.
     * <BR>The operation is deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This also ensures
     * backward compatibility with old adapter classes derived from this one.
     * 
     * @param item An Item Name.
     * @param dataAdapter A Data Adapter name.
     * @return A frequency limit, demanded to the 1-arguments overload.
     */
    @Override
    public double getMinSourceFrequency(String item, String dataAdapter) {
        return getMinSourceFrequency(item);
    }

    /**
     * Reduced, backward-compatibility version of the Prefilter frequency
     * configuration method.
     * 
     * @param item An Item Name. Not used.
     * @return The maximum frequency to be allowed by the prefilter,
     * as supplied in the Metadata Adapter configuration.
     * 
     * @see #getMinSourceFrequency(String, String)
     */
    @Override
    public double getMinSourceFrequency(String item) {
        return prefilterFrequency;
    }

    /**
     * Returns the maximum allowed length for a Snapshot of any Item that
     * has been requested with publishing Mode DISTINCT.
     * The operation is deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This also ensures
     * backward compatibility with old adapter classes derived from this one.
     *
     * @param item An Item Name.
     * @param dataAdapter A Data Adapter name.
     * @return A snapshot length, demanded to the 1-arguments overload.
     */
    @Override
    public int getDistinctSnapshotLength(String item, String dataAdapter) {
        return getDistinctSnapshotLength(item);
    }

    /**
     * Reduced, backward-compatibility version of the Snapshot length
     * configuration method.
     *
     * @param item An Item Name. Not used.
     * @return The maximum allowed length for the Snapshot, as supplied
     * in the Metadata Adapter configuration. In case no value has been
     * supplied, a default value of 10 events is returned, which is thought
     * to be enough to satisfy typical Client requests.
     * 
     * @see #getDistinctSnapshotLength(String, String)
     */
    @Override
    public int getDistinctSnapshotLength(String item) {
        return distinctSnapshotLength;
    }

    /**
     * Called by Lightstreamer Kernel to ask for the allowance of a publishing
     * Mode for an Item.
     * The operation is first deferred to a reduced version of the method,
     * where the dataAdapter argument is discarded. This ensures
     * backward compatibility with old adapter classes derived from this one.
     *
     * @param item An Item name.
     * @param dataAdapter A Data Adapter name.
     * @param mode A publishing Mode.
     * @return true or false, based on a sequence of rules of the general
     * form &lt;pattern,data_adapter,allowed_modes&gt; supplied in the Adapter
     * configuration.
     * If no rule matches the Item name and the Data Adapter, then false is returned;
     * otherwise, the first matching rule is considered and true is returned only if
     * the related allowed_modes contain the specified Mode.
     * However, if no rules are available at all, then true is always returned.
     * In the latter case, as in any case of loose configuration, conflicting
     * Modes may be both allowed for the same Item, so the Clients should
     * ensure that the same Item cannot be requested in two conflicting Modes.
     */
    @Override
    public boolean modeMayBeAllowed(String item, String dataAdapter, Mode mode) {
        try {
            return modeMayBeAllowed(item, mode);
        } catch (RuntimeException e) {
            if (e == myNotImplementedException) {
                return checkItemMode(item, dataAdapter, mode);
            } else {
                throw e;
            }
        }
    }

    /**
     * Reduced, backward-compatibility version of the publishing mode
     * allowance method. This version is left to support overriding by old
     * adapter classes derived from this one. If not overridden, the method
     * delegates back to the standard version.
     *
     * @param item An Item name.
     * @param mode A publishing Mode.
     * @return true or false only if overridden, otherwise throws.
     * 
     * @see #modeMayBeAllowed(String, String, Mode)
     */
    @Override
    public boolean modeMayBeAllowed(String item, Mode mode) {
        throw myNotImplementedException;
    }
    
    private final RuntimeException myNotImplementedException = new RuntimeException();

    private boolean checkItemMode(String item, String dataAdapter, Mode mode) {
        if (families == null) {
            return true;
        }
        for (int i = 0; i < families.length; i++) {
            if (families[i].dataAdapter == null || families[i].dataAdapter.equals(dataAdapter)) {
                Matcher matcher = families[i].pattern.matcher(item);
                if (matcher.matches()) {
                    return families[i].allowedModes.contains(mode);
                }
            }
        }
        return false;
    }

}
