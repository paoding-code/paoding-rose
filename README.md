# paoding-rose

## Introduction

Paoding-rose is an open source framework, which introduces the best way to develop your web applications and RDMS applications.

Paoding-rose has been a main development framework of [renren.com](http://www.renren.com) (人人网), [mi.com](http://www.mi.com) (小米科技), [mocha.cn](http://mocha.cn) (抹茶美妆). 

And we also receive reports that paoding-rose has been chosen in some applicaitons for [qunar.com](http://www.qunar.com/) (去哪儿), [focus.cn](http://www.focus.cn/) (焦点房产).

(如果您的公司也使用paoding-rose, 欢迎联系qieqie.wang at gmail.com) 

## Enable paoding-rose in your java project 

**Add dependencies to pom.xml in your maven project . **

The artifacts have been released to [oss.sonatype.org](https://oss.sonatype.org/content/groups/public/net/paoding/) and synced to [maven central repository](https://repo1.maven.org/maven2/net/paoding/)

```
<dependency>
    <groupId>net.paoding</groupId>
    <artifactId>paoding-rose-jade</artifactId>
    <version>2.0.u01</version>
</dependency>
<dependency>
    <groupId>net.paoding</groupId>
    <artifactId>paoding-rose-web</artifactId>
    <version>2.0.u01</version>
</dependency>
```
TIPs: 2.0.uxx means the xxth update of 2.0.

## Enable SNAPSHOT dependency

If you always want your project using the lastest version, check ```<version>2.0.uxx</version>```to value 2.0-SNAPSHOT as following:
 
1） add snapshot repository to your pom.xml: 

```
 // under project element
 <repositories>
        <repository>
            <id>ossrh-snapshots</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
 </repositories>
```
2) checkout the the SNAPSHOT version：

```
<dependency>
    <groupId>net.paoding</groupId>
    <artifactId>paoding-rose-jade</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>net.paoding</groupId>
    <artifactId>paoding-rose-web</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```

