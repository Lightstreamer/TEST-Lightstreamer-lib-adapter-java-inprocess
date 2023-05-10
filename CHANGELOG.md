# Lightstreamer Changelog - SDK for Java In-Process Adapters


## [8.0.0] (xx-xx-xxxx)

*Compatible with Lightstreamer Server since 7.4.*
*May not be compatible with code developed with the previous version; see compatibility notes below.*

**New Features**

- Moved to an asynchronous interface for the most critical callbacks, that is, the two overloads of notifyUser and notifyUserMessage in the MetadataProvider interface.
This was done by actually modifying the signatures of these methods, which now return a CompletionStage.
The possibility for the implementations to operate inline and return directly (either a null value or an exception) has been left, but now it is only limited to the case of fast non-blocking execution.<br/>
**COMPATIBILITY NOTE:** *Adapter source code implementing the affected callbacks has to be ported. Only in case of fast non-blocking execution, the addition of a return of null is enough. Otherwise, the processing should be rewritten to perform asynchronously and a CompletionStage, to which the successful or exceptional termination will be notified, should be immediately returned.*<br/>
*Only Adapter source code inheriting from LiteralBasedProvider or MetadataProviderAdapter which doesn't override any of the affected callbacks can be left unchanged.*<br/>
**COMPATIBILITY NOTE:** *Existing Adapter binaries built with the previous library version are still supported by the Server, but for a corner case: If an Adapter inherits from LiteralBasedProvider or MetadataProviderAdapter and invokes the no-op "super" implementation of any of the affected callbacks, it will incur in a runtime error. In this case, the Adapter should be ported and rebuilt (or you may contact Lightstreamer support for alternative workarounds).*

- Introduced the possibility for the notifyUserMessage callback to provide a response message, through the new CompletionStage return value.
**COMPATIBILITY NOTE:** *Adapter source code implementing notifyUserMessage, after being ported to comply with the new return type, doesn't need further changes. A null response, in fact, is always allowed.*<br/>
*Adapter source code inheriting from LiteralBasedProvider or MetadataProviderAdapter which doesn't override notifyUserMessage can be left unchanged.*<br/>
**COMPATIBILITY NOTE:** *Existing Adapter binaries built with the previous library version are still supported by the Server and meant to provide a null response, when successful.*

**Improvements**

- Removed from LiteralBasedProvider and MetadataProviderAdapter the 2-arguments overload of notifyUser, which was introduced to handle backward compatibility with very old Metadata Adapters.<br/>
**COMPATIBILITY NOTE:** *Adapter source code inheriting from LiteralBasedProvider or MetadataProviderAdapter which overrides only the 2-arguments overload should override the 3-arguments overload instead.*<br/>
**COMPATIBILITY NOTE:** *Existing Adapter binaries built with the previous library version are still supported by the Server, but for a corner case: If an Adapter inherits from LiteralBasedProvider or MetadataProviderAdapter and invokes the no-op "super" implementation of the 2-arguments overload of notifyUser, it will incur in a runtime error. In this case, the Adapter should be ported and rebuilt (or you may contact Lightstreamer support for alternative workarounds).*


## [7.4.1] (18-11-2022)

*Compatible with Lightstreamer Server since 7.3.*  
*May not be compatible with code developed with the previous version; see compatibility notes below.*  

**Improvements**

- Extended the LiteralBasedProvider implementation to take advantage of the request's Data Adapter information, supplied since 7.4.0, in modeMayBeAllowed.
Now, new parameters in adapters.xml of the form data_adapter_for_item_family_xxx are supported to refine the definition of the various "item families".
The 2-argument version of modeMayBeAllowed, which is left for backward compatibilty, now throws an exception.<br/>
**COMPATIBILITY NOTE:** *Old Adapters inheriting from LiteralBasedProvider and implementing the old 2-argument version of modeMayBeAllowed will still override the basic implementation.*<br/>
*However, they can no longer refer to the "super" implementation of the 2-argument version.*<br/>


## [7.4.0] (19-09-2022)

*Compatible with Lightstreamer Server since 7.3.*  
*May not be compatible with code developed with the previous version; see compatibility notes below.*  

**New Features**

- Extended all the MetadataProvider interface methods in which the arguments include an item name, by adding a further argument that specifies the name of the related Data Adapter.
The change affects a large part of the interface and it is actually a fix for the lack of this information, since items with the same name can be supplied by different Data Adapters.
Note that the new "dataAdapter" argument always follows immediately the "item" argument, so it has not always been added as the last one.<br/>
Likewise, extended the getItems and getSchema methods to specify the name of the Data Adapter included in the client subscription request.<br/>
On the other hand, the MetadataProviderAdapter and LiteralBasedProvider classes have been extended by adding the new method versions and keeping the old ones, whereas the new versions discard the new dataAdapter argument and invoke the old ones.<br/>
**COMPATIBILITY NOTE:** *Adapter source code has to be ported, by adding the new argument on all the modified methods. In most cases, the new argument can be ignored; in some cases it may be leveraged to remove existing workarounds to handle item name conflicts.*<br/>
*However, Adapter source code inheriting from LiteralBasedProvider doesn't need to be changed; likewise, Adapter source code inheriting from MetadataProviderAdapter needs to be changed only with reference to getItems and getSchema. Anyway, the porting is always recommended.*<br/>
**COMPATIBILITY NOTE:** *Existing Adapter binaries built with the previous library version are still supported by the Server.*<br/>
In order to ensure binary-level backward compatibility, the Adapter is now inspected to look for either the old or the new version of the modified methods.
However, if both versions are found in custom code, the Adapter loading (and the Server startup) will fail, as being ambiguous.
This restriction is generalized as a fully-fledged interface contract extension for the MetadataProvider interface, although not always enforced.<br/>
**COMPATIBILITY NOTE:** *When porting Adapter source code, the old versions of the modified methods cannot be left, or cannot be declared as public.*<br/>
**COMPATIBILITY NOTE:** *Existing Adapter binaries built with the previous library version and which happen to include a public overload that matches one of the new methods will not be supported.*

- Introduced the declareFieldDiffOrder and smartDeclareFieldDiffOrder methods in the ItemEventListener interface.
Together with the new DiffAlgorithm class, they allow a Data Adapter to specify which algorithms, and in which order,
the Server should try, in order to compute the difference between a value and the previous one in order to send the client
this difference, for "delta delivery" purpose.<br/>
**COMPATIBILITY NOTE:** *Invoking the new method is optional and by default no algorithm is tried by the Server; hence there are no backward compatibility issues.*<br/>
**COMPATIBILITY NOTE:** *Adapter source code is not expected to implement the ItemEventListener interface, hence no source compatibility issues are expected, unless the Adapter uses this interface for its own purposes.*<br/>
Currently, the following options are available:
	- JSON Patch, which the Server can use when the involved values are valid JSON representations.
	- Google's "diff-match-patch" algorithm (the result is then serialized with the custom "TLCP-diff" format).<br/>


## [7.3.1] (08-04-2022)

*Compatible with Lightstreamer Server since 7.1.*  
*Compatible with code developed with the previous version.*  

**Improvements**

- Removed the provided template of adapters.xml. For this template we now refer to the one provided by LS Server.

- Added the source code of the included LiteralBasedProvider class; previously, this code was taken from a separate project.

- Removed the use of @Nonnull for primitive types, which could have caused compilation issues.

- Added clarifications in the javadoc; in particular, revised the descriptions of the SmartDataProvider interface and of getMinSourceFrequency in the MetadataProvider interface.


## [7.3.0] (10-07-2020)

*Compatible with Lightstreamer Server since 7.1.*  
*May not be compatible with code developed with the previous version; see compatibility notes below.*  

**New Features**

- Made the library available on the public Maven repository, at the following address:<br/>
https://mvnrepository.com/artifact/com.lightstreamer/ls-adapter-inprocess<br/>
Previous releases were included in Lightstreamer distribution packages, up to Lightstreamer version 7.1.1.

- Made the library open source and available on GitHub at the following address:<br/>
https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess

**Improvements**
   
- Added the null annotations (according to JSR 305) in the class files of public classes, to better support library use with Kotlin and any other language which leverages JSR 305.<br/>
**COMPATIBILITY NOTE:** *Existing code written in Kotlin and similar languages may no longer compile and should be aligned with the new method signatures. No issues are expected for existing Java code.*


## [7.2.0] (24-01-2020)

*Compatible with Lightstreamer Server since 7.1.*  
*Compatible with code developed with the previous version.*  

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
**COMPATIBILITY NOTE:** *Existing Data Adapters don't need to be recompiled.*<br/>
Extended some demo source code to show how the new methods can be invoked.

- Introduced separate ClassLoaders for loading the resources related with the various Adapter Sets. As a consequence, classes pertaining to different Adapter Sets can no longer see each other, though they can still share any classes defined in the "shared" folder.
By the way, note that any classes found in "lib" and "classes" under the Adapter Set folder are now added to the Adapter Set ClassLoader, even if all the Adapters declare a different dedicated <install_dir>.<br/>
**COMPATIBILITY NOTE:** *Existing Adapter code that leans on class sharing between the Adapter Sets may fail; however, the old behavior can be restored by simply placing all jars and classes in the "shared" folder. Moreover, introduced the possibility of loading the classes of single Metadata or Data Adapters in dedicated ClassLoaders (still inheriting from the ClassLoader of the Adapter Set);*
*see the new \<classloader\> configuration element in the sample adapters.xml for details.*

- As a consequence of the introduction of separate ClassLoaders for the loading of the external libraries used by Lightstreamer Server internally, all these libraries are no longer visible from Adapter code.<br/>
**COMPATIBILITY NOTE:** *Existing Adapters that lean on libraries included by Lightstreamer Server should now include these libraries explicitly. Sharing of library state is no longer possible, but it was not supposed to be leveraged anyway.*
*The only exception is for logging. If any Adapter leans on the instance of slf4j/logback included by the Server (perhaps in order to share the log configuration), it can be configured to share these libraries through the new \<classloader\> setting in adapters.xml (see the sample adapters.xml for details). In particular, this is the case for Proxy Adapters.*
*However, note that the slf4j and logback libraries have been updated; hence, if any custom Adapter has to keep sharing these libraries, check out the slf4j and logback changelogs for any compatibility issues.*

- Embedded the binaries of the basic LiteralBasedProvider in *ls-adapter-interface.jar*. As a consequence, the *ls-generic-adapters.jar* file is no longer provided. Note that this library was also predeployed in the "shared" folder.<br/>
**COMPATIBILITY NOTE:** *If an existing Adapter installation is based on the LiteralBasedProvider and includes the ls-generic-adapters.jar file within its own folder, the jar should be removed to avoid confusion.*<br/>
Also note that the removed library also included the sample FileBasedProvider, which is no longer provided.<br/>
**COMPATIBILITY NOTE:** *In the unlikely case of an existing Adapter installation based on the FileBasedProvider, the old ls-generic-adapters.jar should be left; if it had been left in the "shared" folder, it is recommended to place it within the Adapter's own folder.*<br/>
Extended the interface Javadocs to include the LiteralBasedProvider, previously documented under the "examples" folder.

- Added identification tags at the beginning of the various sample adapters.xml files provided. By keeping them in your own adapter configuration files, future upgrades of these files may be automated.

**Improvements**

- Changed the initialization order for Metadata Adapters and Data Adapters. By default the Metadata Adapter is initialised before any Data Adapter of the same Adapter Set. 
The optional configuration parameter "metadata_adapter_initialised_first" has been added to the adapters.xml configuration file to initialise the Metadata Adapter in parallel with the Data Adapters.<br/>
**COMPATIBILITY NOTE:** *Existing Adapter Sets relying on a concurrent initialization of Data and Metadata Adapters (may be the case for Remote Adapters) should use the "metadata_adapter_initialised_first" configuration parameter to restore the parallel initialization.*<br/>
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
**COMPATIBILITY NOTE:** *In some cases, the relative order of data and notification events may change, but only when different tables are involved, in which case no order specification has never been claimed.*<br/>
This also fixes an issue in the support of the Adapter Remoting Infrastructure, in which the implementation of "notifyNewTables" can't but be potentially blocking.

- Fixed a typo in the documentation of the init method of both adapters, where "adapter_conf.id" was reported instead of "adapters_conf.id".
