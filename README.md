# Lightstreamer Java In-Process Adapter SDK
 
This project includes the source code of the Lightstreamer Java In-process Adapter API specification.
This resource is needed to write Data Adapters and Metadata Adapters for Lightstreamer Server in Java. The adapters will run in process with the Lightstreamer Server.
Each Lightstreamer session requires the presence of an Adapter Set, which is made up of one Metadata Adapter and one or multiple Data Adapters. Multiple Adapter Sets can be plugged onto Lightstreamer Server.
Please refer to [General Concepts](https://lightstreamer.com/docs/ls-server/latest/General%20Concepts.pdf) document for further details about the role of the Adapters in Lightstreamer.

Each Adapter Set is defined by a configuration file called `adapters.xml` placed in a specific subfolder of `/adapters`. Please refer to the WELCOME Adapter Set pre-installed in the factory donwload of Lightstreamer as a reference of Adapters deploy and to [the provided template of adapters.xml](https://lightstreamer.com/docs/ls-server/7.1.1/sdk_adapter_java_inprocess/doc/adapter_conf_template/adapters.xml) for a complete description of all the parameters configurable for an Adapter Set.

In case you want to use Eclipse for developing and running your own Adapter Set, please refer to the instructions provided in this [thread](http://forums.lightstreamer.com/showthread.php?4875-Developing-amp-Running-an-Adapter-Set-Using-Eclipse) of our [Java Adapter API](http://forums.lightstreamer.com/forumdisplay.php?6-Java-Adapter-API) support forum.

![architecture](architecture.png)

## Compatibility

The library is compatible with Lightstreamer Server since 7.1.

## Using the API

Since the API is available from the Maven Central Repository, to setup your development environment add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.lightstreamer</groupId>
    <artifactId>ls-adapter-inprocess</artifactId>
    <version>7.3.0</version>
</dependency>
```

## External Links

- [Maven repository](https://mvnrepository.com/artifact/com.lightstreamer/ls-adapter-inprocess)

- [Examples](https://demos.lightstreamer.com/?p=lightstreamer&t=adapter&a=javaadapter)

- [API Reference](https://sdk.lightstreamer.com/ls-adapter-inprocess/7.3.0/api/index.html)

## Other GitHub projects using this library

- [Lightstreamer - Reusable Metadata Adapters - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-ReusableMetadata-adapter-java)

## Support

For questions and support please use the [Official Forum](https://forums.lightstreamer.com/). The issue list of this page is **exclusively** for bug reports and feature requests.

## License

[Apache 2.0](https://opensource.org/licenses/Apache-2.0)
