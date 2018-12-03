<!--
  Licensed to ObjectStyle LLC under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ObjectStyle LLC licenses
  this file to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->

[![Build Status](https://travis-ci.org/bootique/bootique-shiro.svg)](https://travis-ci.org/bootique/bootique-shiro)
[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.shiro/bootique-shiro.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.shiro/bootique-shiro/)

# bootique-shiro

## Overview

This is a set of modules that help to integrate [Apache Shiro](http://shiro.apache.org/) security engine in Bootique apps. 
Quick description of the provided modules:

* `bootique-shiro` - creates a standalone Shiro stack with user-configured security Realms. Includes factories for 
"Ini" realm (that supports in-place definition of user accounts) and ActiveDirectory realm. Provides injectable 
`SecurityManager`.

* `bootique-shiro-web` - a module that stands up the Shiro stack and attaches it to a special servlet Filter. Supports
a powerful Shiro feature - [path matching with filters](https://shiro.apache.org/web.html#urls-).

* `bootique-shiro-jdbc` - Provides configurable JDBC realm. Can be used with any of the above modules.

## Usage Standalone

Include Bootique BOM and `bootique-shiro` module:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>1.0.RC1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

...

<dependency>
	<groupId>io.bootique.shiro</groupId>
	<artifactId>bootique-shiro</artifactId>
</dependency>
```
Now you will need to configure your realms. If you have used Apache Shiro outside Bootique, you may be familiar with 
its `.ini` file-based configuration mechanism. We have ported it to a much more flexible Bootique approach that 
is a combination of true dependency injection (DI) with a unified config mechanism (YAML and friends). So you might 
create a `.yml` file similar to this (you may recognize some of the configs below that replaced Shiro's 
`[users]`, and `[roles]` sections; `[main]` is mostly handled by DI) :

```yaml
shiro:
  realms:
    - users:
        adminuser: "password, admin, user"
        user: "password, user"
      roles:
        admin: "admin"
```

_Hint: use `-H` flag to run your app to see configuration docs in details._


Finally you are ready to use Shiro:

```java

@Inject
private SecurityManager securityManager;

public void doSomething() {
    new Subject.Builder(securityManager).buildSubject().execute(() -> {
        
        // within 'execute' you can access current Subject using Shiro API
        Subject subject = SecurityUtils.subject();
        subject.checkPermission("A");
        subject.checkPermission("B");
        ...
    });
}

```

## Usage - Web

Include Bootique BOM and `bootique-shiro-web` module:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.bootique.bom</groupId>
            <artifactId>bootique-bom</artifactId>
            <version>1.0.RC1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

...

<dependency>
	<groupId>io.bootique.shiro</groupId>
	<artifactId>bootique-shiro-web</artifactId>
</dependency>
```
Configuring of a web environment includes configuring realms (as described above) as well as URL filters.

```yaml
shiro:
  realms:
    - users:
        adminuser: "password, admin, user"
        user: "password, user"
      roles:
        admin: "admin"
        
shiroweb:
  # These URLs are resolved within ShiroFilter that routes them to the corresponding internal security filters.
  urls:
    "/admin" : perms[\"admin\"]
    "/pub"   : anon
```
Using Shiro within a servlet request or a JAX-RS endpoint is even easier than a standalone app, as all the environment 
is already initialized for you:


```java
@GET
public Response get() {
    Subject subject = SecurityUtils.getSubject();
    subject.checkPermission("A");
    subject.checkPermission("B");
    ...
}

```

## Logging and Integration with MDC

Often you may want to associate application logs with a user who performed the action that generated a given set of logs. 
This helps in investigation of production issues, security audit, etc. This can be achieved using 
[SLF4J MDC](https://logback.qos.ch/manual/mdc.html) (Mapped Diagnostics Context) functionality. `bootique-shiro` 
provides semi-automated MDC integration facilities. You'd usually start by configuring your logger format to include MDC 
in the output. We are specifically interested in the "principal" key, so the format might contain `%X{principal:-?}` pattern:

```yaml
log:
  appenders:
    - logFormat: '%t %X{principal:-?} %-5p %c{1}: %m%n%ex'
```

Now you can initialize and cleanup the MDC as appropriate. `bootique-shiro` provides a class called 
[PrincipalMDC](https://github.com/bootique/bootique-shiro/blob/master/bootique-shiro/src/main/java/io/bootique/shiro/mdc/PrincipalMDC.java) 
that will do that for you. You just call "reset" and "cleanup" methods as needed.

Now this was the manual approach. If your app is a servlet app and is using `bootique-shiro-web`, MDC initialization and cleanup can be automated. To do that add `bootique-shiro-web-mdc` module to your dependencies:
```xml
<dependency>
	<groupId>io.bootique.shiro</groupId>
	<artifactId>bootique-shiro-web-mdc</artifactId>
</dependency>
```
If your app authenticates every request separately and is not using Shiro sessions, this (and appender configuration above) is all you need for user names to appear in the logs. But in case the app logs in each user once, and then keeps the Subject in a session, a bit more configuration is needed. You will need an extra filter called "mdc" placed in each of your authenticated Shiro chains:

```yaml
shiroweb:
  urls:
    "/admin" : perms[\"admin\"], mdc
    "/pub"   : anon
```
