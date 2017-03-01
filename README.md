[![Build Status](https://travis-ci.org/bootique/bootique-shiro.svg)](https://travis-ci.org/bootique/bootique-shiro)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.shiro/bootique-shiro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.shiro/bootique-shiro/)

# bootique-shiro

_since Bootique 0.22_

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
            <version>0.22</version>
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
            <version>0.22</version>
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
    Subject subject = SecurityUtils.subject();
    subject.checkPermission("A");
    subject.checkPermission("B");
    ...
}

```