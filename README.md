# Mybatis-typehandlers-json
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

mybatis-typehandlers-json 提供了mybatis的json字段的TypeHandler。

## 使用

### Maven

在pom.xml中加入nexus资源库

```xml
<repositories>
    <repository>
        <id>nexus</id>
        <name>nexus</name>
        <url>http://maven.zhuangjinjin.cn/repository/public</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

在pom.xml中加入依赖

```xml
<dependency>
   <groupId>io.github.ukuz</groupId>
   <artifactId>mybatis-typehandlers-json</artifactId>
   <version>0.0.1</version>
</dependency>
```

### Gradle

在build.gradle中加入nexus资源库

```groovy
repositories {
    mavenLocal()
    maven {url 'http://maven.zhuangjinjin.cn/repository/public'}
    mavenCentral()
}
```

在build.gradle加入依赖

```groovy
dependencies {
    ...
    compile 'io.github.ukuz:mybatis-typehandlers-json:0.0.1'
}
```

### 代码

在应用启用的代码中加入扫描实体类的包，如下代码：

```java
Bootstrap bootstrap = new Bootstrap.Builder().build();
bootstrap.scanEntityPackages(new String[]{"xxx.xxx.entity"});
```

在Entity类中需要转成json字符串字段加上`@JsonString`注解，如下：

```java
public class UserEntity {

    private long id;
    private String name;
    private int age;
    @JsonString
    private List<Email> email; //如果这边是要当成一个json字段存入的话，加上@JsonString
}
```

### Demo

Demo地址：
