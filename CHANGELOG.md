# Lightstreamer Changelog - SDK for Java In-Process Adapters

## [7.2.0] (24-01-2020)

*Compatible with Lightstreamer Server since 7.1.*
*Compatible with code developed with the previous version.*
*Compatible with configuration files for the previous version.*
*Compatible with the deployment structure of the previous version.*

**New Features**

- Extended the MetadataProvider interface with a setListener method. The new listener, provided by the Kernel just after initialization, adds support for operations requested by Metadata Adapter code.
In particular, it is now possible to enforce the termination of a Session and to notify the Kernel of a fatal issue in the Adapter.
Look for the new MetadataControlListener class in the docs for details.<br/>
**COMPATIBILITY NOTE:** *The new method has a default implementation which ignores the listener; so both source and binary compatibility with existing Adapters is guaranteed.*

- Extended the TableInfo bean class with new getters that provide further information on the involved subscription. Added, in particular, getDataAdapter, getSubscribedItems, and getSubscriptionStatistics;
the latter are only available at subscription close. See TableInfo and look for the new SubscriptionStatistics class in the docs for details.
Note that the change also involves the TableInfo constructor.<br/>
**COMPATIBILITY NOTE:** *Existing Adapter code using the constructor would not be compatible with the new jar; however, the constructor is just provided for descriptive purpose and was never meant to be used by Adapter code.*

- Extended the TableInfo bean class with an operation method, named forceUnsubscription, which allows Adapter code to enforce the unsubscription of the involved subscription on behalf of the client. See the TableInfo docs for details.

- Extended the MetadataProvider interface with a getSessionTimeToLive method, which will be invoked by the Kernel upon session creation.
This will allow the Adapter to specify a time-to-live for the session, which will be enforced by the Server.<br/>
**COMPATIBILITY NOTE:** *The new method has a default implementation which doesn't set any time-to-live; so both source and binary compatibility with existing Adapters is guaranteed.*

**Improvements**

- Added the ResourceUnavailableException, as a type of AccessException, to provide the possibility to have the Server instruct the client to retry upon an error which prevents notifyUser from working correctly.

- Extended the clientContext map provided to notifyNewSession, by adding the CLIENT_TYPE and CLIENT_VERSION keys, with information on the client API in use.

## [7.0.1] (28-02-2018)

*Compatible with Lightstreamer Server since 7.0.*
*Compatible with code developed with the previous version.*
*Compatible with configuration files for the previous version.*
*Compatible with the deployment structure of the previous version.*

**Improvements**

- Added clarifications on licensing matters in the docs.

## [7.0.0] (20-12-2017)

*Compatible with Lightstreamer Server since 7.0 b2.*
*May not be compatible with code developed with the previous version; see the compatibility notes below.*
*May not be compatible with configuration files for the previous version; see the compatibility notes below.*
*Compatible with the deployment structure of the previous version.*

**New Features**

- Modified the interface in the part related to the MPN Module, after its full revision. Here is a resume of the changes::
	- Modified the signature of the notifyMpnDeviceAccess and notifyMpnDeviceTokenChange methods of the MetadataProvider interface, to add a session ID argument.<br/>
	**COMPATIBILITY NOTE:** *Existing Metadata Adapter source code has to be ported in order to be compiled with the new jar, unless the Adapter class inherits from the supplied MetadataProviderAdapter or LiteralBasedProvider and the above methods are not defined.<br/>*
	*On the other hand, existing Metadata Adapter binaries are still supported as long as the MPN Module is disabled. Otherwise, they should be recompiled after porting.*
	- Revised the public constants defined in the MpnPlatformType class. The constants referring to the supported platforms have got new names and corresponding new values, whereas the constants for platforms not yet supported have been removed.<br/>
	**COMPATIBILITY NOTE:** *Existing Metadata Adapters explicitly referring to the constants have to be aligned.*
	*Even if just testing the values of the MpnPlatformType objects received from MpnDeviceInfo.getType, they still have to be aligned.*
	- Removed the subclasses of MpnSubscriptionInfo (namely MpnApnsSubscriptionInfo and MpnGcmSubscriptionInfo) that were used by the SDK library to supply the attributes of the MPN subscriptions in notifyMpnSubscriptionActivation. Now, simple instances of
	MpnSubscriptionInfo will be supplied and attribute information can be obtained through the new getNotificationFormat method.
	See the MPN chapter on the General Concepts document for details on the characteristics of the Notification Format.<br/>
	**COMPATIBILITY NOTE:** *Existing Metadata Adapters leveraging notifyMpnSubscriptionActivation and inspecting the supplied MpnSubscriptionInfo have to be ported to the new class contract.*
	- Added equality checks based on the content in MpnDeviceInfo, MpnPlatformType, and MpnSubscriptionInfo.
	- Improved the interface documentation in various parts.
	
**Improvements**

- Changed the default of the <sequentialize_table_notifications> flag available in adapters.xml from Y to N, to better cope with cases in which many subscriptions are performed at once.<br/>
**COMPATIBILITY NOTE:** *If any existing Metadata Adapter relies on the invocations of notifyNewTables and notifyTablesClose for the same session to never overlap, ensure that the flag is explicitly configured as Y for this Adapter.*

- Changed the preconfigured settings for the dedicated thread pools in the adapters.xml template, to leverage the new defaults for the SERVER pool.

- Clarified in the docs for notifySessionClose which race conditions with other methods can be expected.

- Aligned the documentation to comply with current licensing policies.

## [6.0.1] (23-01-2017)

*Compatible with Lightstreamer Server since 6.0.3.*
*Compatible with code developed with the previous version.*
*Compatible with the deployment structure of the previous version.*

**Improvements**

- Extended the configuration template to advertise the variable-expansion feature introduced in Server version 6.1.

- Added meta-information on method argument names for interface classes, so that developer GUIs can take advantage of them.

## [6.0.0] (21-01-2015)

*Compatible with Lightstreamer Server since 6.0.*
*May not be compatible with code developed with the previous version; see the compatibility notes below.*
*May not be compatible with the deployment structure of the previous version; see the compatibility notes below.*

**New Features**

- Extended the MetadataProvider interface to support the new Push Notification Service (aka MPN Module). When enabled, the new methods will be invoked in order to validate client requests related with the service.
See the Javadocs for details (see also the <mpn_pool> element in the sample adapters.xml for Adapter related thread pool configuration).<br/>
**COMPATIBILITY NOTE:** *Existing Metadata Adapter source code has to be extended in order to be compiled with the new jar (the new methods could just throw a NotificationException), unless the Adapter class inherits from one of the supplied FileBasedProvider, LiteralBasedProvider or MetadataProviderAdapter.*
*In the latter case, the Adapter will accept any MPN-related request; however, MPN-related client requests can be satisfied only if the involved "app" has been properly configured.<br/>*
*On the other hand, existing Metadata Adapter binaries are still supported (but for the unlikely case of a name conflict with the new methods) and will refuse any MPN-related request (unless the Adapter class inherits from one of the supplied FileBasedProvider, LiteralBasedProvider or MetadataProviderAdapter, where the above considerations hold).*

- Introduced the "clearSnapshot"/"smartClearSnapshot" operations on the ItemEventListener, for clearing the state of an item in a single step (or, in DISTINCT mode, for notifying compatible clients that the update history should be discarded). See the javadocs for details.<br/>
**COMPATIBILITY NOTE:** *Existing Data Adapters don't need to be recompiled.*
Extended some demo source code to show how the new methods can be invoked.

- Introduced separate ClassLoaders for loading the resources related with the various Adapter Sets. As a consequence, classes pertaining to different Adapter Sets can no longer see each other, though they can still share any classes defined in the "shared" folder.
By the way, note that any classes found in "lib" and "classes" under the Adapter Set folder are now added to the Adapter Set ClassLoader, even if all the Adapters declare a different dedicated <install_dir>.<br/>
**COMPATIBILITY NOTE:** *Existing Adapter code that leans on class sharing between the Adapter Sets may fail; however, the old behavior can be restored by simply placing all jars and classes in the "shared" folder. Moreover, introduced the possibility of loading the classes of single Metadata or Data Adapters in dedicated ClassLoaders (still inheriting from the ClassLoader of the Adapter Set);*
*see the new <classloader> configuration element in the sample adapters.xml for details.*

- As a consequence of the introduction of separate ClassLoaders for the loading of the external libraries used by Lightstreamer Server internally, all these libraries are no longer visible from Adapter code.<br/>
**COMPATIBILITY NOTE:** *Existing Adapters that lean on libraries included by Lightstreamer Server should now include these libraries explicitly. Sharing of library state is no longer possible, but it was not supposed to be leveraged anyway.*
*The only exception is for logging. If any Adapter leans on the instance of slf4j/logback included by the Server (perhaps in order to share the log configuration), it can be configured to share these libraries through the new <classloader> setting in adapters.xml (see the sample adapters.xml for details). In particular, this is the case for Proxy Adapters.*
*However, note that the slf4j and logback libraries have been updated; hence, if any custom Adapter has to keep sharing these libraries, check out the slf4j and logback changelogs for any compatibility issues.*

- Embedded the binaries of the basic LiteralBasedProvider in *ls-adapter-interface.jar*. As a consequence, the *ls-generic-adapters.jar* file is no longer provided. Note that this library was also predeployed in the "shared" folder.<br/>
**COMPATIBILITY NOTE:** *If an existing Adapter installation is based on the LiteralBasedProvider and includes the ls-generic-adapters.jar file within its own folder, the jar should be removed to avoid confusion.*
Also note that the removed library also included the sample FileBasedProvider, which is no longer provided.<br/>
**COMPATIBILITY NOTE:** *In the unlikely case of an existing Adapter installation based on the FileBasedProvider, the old ls-generic-adapters.jar should be left; if it had been left in the "shared" folder, it is recommended to place it within the Adapter's own folder.*
Extended the interface Javadocs to include the LiteralBasedProvider, previously documented under the "examples" folder.

- Added identification tags at the beginning of the various sample adapters.xml files provided. By keeping them in your own adapter configuration files, future upgrades of these files may be automated.

**Improvements**

- Changed the initialization order for Metadata Adapters and Data Adapters. By default the Metadata Adapter is initialised before any Data Adapter of the same Adapter Set. 
The optional configuration parameter "metadata_adapter_initialised_first" has been added to the adapters.xml configuration file to initialise the Metadata Adapter in parallel with the Data Adapters.<br/>
**COMPATIBILITY NOTE:** *Existing Adapter Sets relying on a concurrent initialization of Data and Metadata Adapters (may be the case for Remote Adapters) should use the "metadata_adapter_initialised_first" configuration parameter to restore the parallel initialization.*
Distinct Adapter Sets are initialised in parallel as before.

- Introduced a sample adapters.xml configuration file, which includes the description of all parameters, in the new "adapter_conf_template" folder under "docs". It should be used as the reference for adapters.xml writing. On the other hand, the adapter configuration file of the preinstalled welcome page is no longer meant as a reference.

- Revised the factory configuration of the optional thread pools in adapters.xml; with the new suggested configuration, some adapter-related pools are enabled and will take on some tasks previously falling in the "SERVER" pool.
Clarified the docs with regard to the thread pools involved in the various adapter invocations.

- Added details in the debug log of events received from the Data Adapters.

- Improved the Javadoc, with introductory notes and instructions on the use of the sample LiteralBasedProvider.

- Removed the examples, which are now only hosted on GitHub and managed through the new "demos" site. Provided suitable references to find the examples there.

- Changed the SDK name, which was "SDK for Java Adapters", to avoid confusion with the new SDK for Java Remote Adapters.

**Bug Fixes**

- Fixed a race condition that, in principle, could have caused notifySessionClose to be invoked twice on the same session.

- Fixed an error in the documentation of getWinIndex in the TableInfo class; clarified how the field can be used to match subscription and unsubscription requests.

- Fixed some truncated short descriptions in the javadocs.

## [5.1.0] (20-12-2012)

*Compatible with Lightstreamer Server since 5.1.*
*Compatible with code developed with the previous version.*
*Compatible with the deployment structure of the previous version.*

**New Features**

- Introduced the <sequentialize_table_notifications> tag in the Metadata Adapter configuration, to allow for also relieving the sequentialization constraint on the "notifyNewTables" and "notifyTablesClose" callbacks, so that delays on the callback execution would not even propagate to other subscription requests for the session. See the inline comment in the demos "adapters.xml" for details.

**Improvements**

- Relieved the locking policy on the invocation of the "notifyNewTables" and "notifyTablesClose" callbacks, so that delays on the callback execution no longer propagate to the update flow for the session. As a consequence, blocking implementations of "notifyNewTables" are now allowed. See the javadocs for details.<br/>
**COMPATIBILITY NOTE:** *In some cases, the relative order of data and notification events may change, but only when different tables are involved, in which case no order specification has never been claimed.*
This also fixes an issue in the support of the Adapter Remoting Infrastructure, in which the implementation of "notifyNewTables" can't but be potentially blocking.

- Fixed a typo in the documentation of the init method of both adapters, where "adapter_conf.id" was reported instead of "adapters_conf.id".