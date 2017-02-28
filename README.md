[![Build Status](https://travis-ci.org/bootique/bootique-shiro.svg)](https://travis-ci.org/bootique/bootique-shiro)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.bootique.shiro/bootique-shiro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.bootique.shiro/bootique-shiro/)

# bootique-shiro

_since Bootique 0.22_

## Overview

This is a set of modules that help to integrate [Apache Shiro](http://shiro.apache.org/) security engine in Bootique apps. 
Quick description of the provided modules:

* `bootique-shiro` - a basic module that helps to configure a set of security Realms (available as injectable 
[Realms](https://github.com/bootique/bootique-shiro/blob/master/bootique-shiro/src/main/java/io/bootique/shiro/realm/Realms.java))
object). While it can be used standalone, it normally serves as a basis for environment-specific Shiro integrations described
below. Also includes factories for "Ini" realm (that supports in-place definition of user accounts) and ActiveDirectory 
realm.

* `bootique-shiro-static` - a module that stands up Shiro stack and stores it in Shiro-provided static singletons. This 
allows the app to use `ShiroUtils` without extra setup. The obvious shortcoming of this approach is that the framework 
is no longer embeddable (there can only eb one Shiro stack), which is quite appropriate for many apps.

* `bootique-shiro-web` - a module that stands up the Shiro stack and attaches it to a special servlet Filter. Supports
a powerful Shiro feature - [path matching with filters](https://shiro.apache.org/web.html#urls-).

* `bootique-shiro-jdbc` - Provides configurable JDBC realm. Can be used with any of the modules above.

## Usage

Here is a web configuration example. Include ```bootique-shiro-web```:
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
security filters. You might create a `.yml` file similar to this:

```yaml
shiro:

shiroweb:
  urls:
    "/admin" : perms[\"admin\"]
    "/pub"   : anon
    
shiro:
  realms:
    - users:
        adminuser: "password, admin, user"
        user: "password, user"
      roles:
        admin: "admin"
```

_Hint: use `-H` flag to run your app to see configuration docs in details._

`bootique-shiro` replaces `.ini` files with real dependency injection plus Bootique configuration system. If you 
have used Apache Shiro outside Bootique, you may recognize some of the configs above that replace Shiro's `[users]`,
`[roles]` and `[urls]` sections. 