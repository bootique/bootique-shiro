[![Build Status](https://travis-ci.org/bootique/bootique-shiro.svg)](https://travis-ci.org/bootique/bootique-shiro)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.shiro/bootique-shiro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.shiro/bootique-shiro/)

# bootique-shiro

_since Bootique 0.22_

## Overview

This is a set of modules that help to integrate [Apache Shiro](http://shiro.apache.org/) security engine in Bootique apps. 
Quick description of the provided modules:

* `bootique-shiro` - creates a standalone Shiro stack with user-configured security Realms. Includes factories for 
"Ini" realm (that supports in-place definition of user accounts) and ActiveDirectory realm. Provides injectable 
`SubjectManager` service for creating new Subjects, that are not thread-bound by default. 

* `bootique-shiro-web` - a module that stands up the Shiro stack and attaches it to a special servlet Filter. Supports
a powerful Shiro feature - [path matching with filters](https://shiro.apache.org/web.html#urls-).

* `bootique-shiro-jdbc` - Provides configurable JDBC realm. Can be used with any of the above modules.

## Usage

Here is a `bootique-shiro-web` configuration example. First you need to include the module:
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
This will start Shiro and install Shiro filter to match all URLs. Now you will need to configure your realms and 
security filters.  A few words on configuration. If you have used Apache Shiro outside Bootique,  you may be familiar 
with its `.ini` file-based configuration mechanism. We have ported it to a much more flexible Bootique approach that 
is a combination of true dependency injection (DI) with a unified config mechanism (YAML and friends).
 
So you might create a `.yml` file similar to this (you may recognize some of the configs below that replaced Shiro's 
`[users]`, `[roles]` and `[urls]` sections; `[main]` is mostly handled by DI) :

```yaml
shiro:
  realms:
    - users:
        adminuser: "password, admin, user"
        user: "password, user"
      roles:
        admin: "admin"
        
shiroweb:
  urls:
    "/admin" : perms[\"admin\"]
    "/pub"   : anon
```

_Hint: use `-H` flag to run your app to see configuration docs in details._
