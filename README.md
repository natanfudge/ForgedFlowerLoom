# ForgedFlowerLoom

ForgedFlowerLoom allows decompiling Minecraft sources in Loom with ForgedFlower instead of FabricFlower.</br>
ForgedFlower (a fork of ForgeFlower) can produce significantly better output than FabricFlower in some cases and reduces compilation errors in resulting code.</br>
A downside is that ForgedFlower is not multithreaded.

## Usage
First, make certain you are using **at least** `Loom 0.4`:
```groovy
    plugins {
    	id 'fabric-loom' version '0.4-SNAPSHOT'
    }
```

Place **at the very top of your `build.gradle`, above `plugins`**:

```groovy
buildscript {
   repositories { jcenter() }
   dependencies {
       classpath("io.github.fudge:forgedflowerloom:1.0.0")
   }
}
```

And then **anywhere after the `plugins` block**, in the same `build.gradle`:

```groovy
minecraft {
   addDecompiler(new ForgedFlowerDecompiler(project))
}
```

From now on, if you wish to decompile using ForgedFlower, use the `genSourcesWithForgedFlower` task (`gradlew genSourcesWithForgedFlower`) instead of `genSources`. A source jar will be generated using ForgedFlower that you can attach as normal.

## Output examples

![Casts](examples/cast.png)

 ![Static Init](examples/static_init.png)

![Baby Shark Do Do Do Do](examples/baby_shark.png)