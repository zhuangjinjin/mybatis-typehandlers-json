# Mybatis-typehandlers-json
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

mybatis-typehandlers-json 提供了mybatis的json字段的TypeHandler。

## 环境

JDK环境：JDK1.8+

项目依赖：

* mybatis-3.5.1
* fastjson-1.2.58
* slf4j-1.7.25

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

我这边就用`${entityPackage}`代表实体类的包路径，我们就先假设`${entityPacakge}`为`a.b.c.entity`我们需要在应用启动的地方手动扫描实体类的包路径。代码如下：

```java
Bootstrap bootstrap = new Bootstrap.Builder().build();
bootstrap.scanEntityPackages(new String[]{"a.b.c.entity"});
```

在Entity类中需要转成json字符串字段加上`@JsonString`注解，如下：

```java
package a.b.c.entity;

public class UserEntity {

    private long id;
    private String name;
    private int age;
    @JsonString
    private List<Email> email; //如果这边是要当成一个json字段存入的话，加上@JsonString
}
```

### 配置

在`mybatis-config.xml`中加入动态生成的`TypeHandler`，如下：

```xml
<typeHandlers>
  <typeHandler handler="io.github.ukuz.mybatis.type.json.type.ListTypeHandler$Email"/>
</typeHandlers>
```

规则：如果字段是`List`类型，则typeHandler的类名是**'io.github.ukuz.mybatis.type.json.type.ListTypeHandler' **+ '$' + **'泛型类型'**，上例的泛型类型是`Email`。

在`UserMapper.xml`中加入如下：

```xml
<resultMap id="user" type="a.b.c.UserEntity">
  <result column="email" typeHandler="io.github.ukuz.mybatis.type.json.type.ListTypeHandler$Email" property="email" />
</resultMap>
  
<select id="selectById" resultMap="user">
  select * from user where id = #{id}
</select>
  
<insert id="save" parameterType="a.b.c.UserEntity">
  INSERT INTO user (id, name, age, email) VALUES (#{id}, #{name}, #{age}, #{email, typeHandler = io.github.ukuz.mybatis.type.json.type.ListTypeHandler$Email})
</insert>
```

### Demo

Demo地址：
