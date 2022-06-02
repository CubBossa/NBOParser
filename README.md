# NBO File Format

The NBO File Format offers a way for minecraft users to keep their configuration files simple. NBO stands for named binary objects and refers to the json like NBT format introduced by minecraft. NBO extends this format with object references. Some ressources i made require many simple objects in relation to each other. This might be easy in code but quite hard to setup for yml users. With NBO, one can simply declare NBT trees and reference them in other trees.

Example:
```
sound := Sound{
  sound: "minecraft:entity.firework_rocket.blast", 
  volume: 1f, 
  pitch: 1f
}

effect := Effect{
    particles: [...]
    sounds: [
      &sound, # <---- reference to previously declared sound object
      Sound{sound: "minecraft:entity.firework_rocket.blast", volume: 0.5f, pitch: 1.4f}
    ]
}
```


## Content
- Why prefer NBO to Yml or json
- Installation
- Usage

## Why NBO

Many times when a text file format is required, YML and JSON might do the job. NBO was developed to simplify the process of creating many smaller objects and setting them in relation to each other by using references. E.g. a sound effect can be declared once at the beginning of the NBO file and be referenced in every following object, where no more configuration is required. This makes modifying very simple and dynamic and allows to easily change things later on. But it also makes the file much easier to read.

As NBO was developed for minecraft, it also serializes and deserializes classes that implement ConfigurationSerializable, so no extra code is required to move from YML to NBO.
I would recommend to still consider where to use it carefully. I see little benefit in using it for simple configuration or translation files.

Here you can see YML and NBO in comparison for object (de-)serialisation:

<table>
<tr>
<td> YML </td> <td> NBO </td>
</tr>
<tr>
<td>

```yml
other_effect:
    ==: SoundPlayer
    volume: 1.0
    sound: minecraft:entity.villager.no
    pitch: 1.0

effect_player:
  ==: EffectPlayer
  delay.0:
  - ==: ReferencedEffect   # <--- requires extra code
    reference: other_effect
  - ==: SoundPlayer
    volume: 1.0
    sound: minecraft:entity.villager.no
    pitch: 1.0
  delay.1:
  - ==: SoundPlayer
    volume: 1.0
    sound: minecraft:block.iron_trapdoor.close
    pitch: 1.0
```
  
</td>
<td>
  
```
other_effect := SoundPlayer{
  sound: 'minecraft:entity.villager.no', 
  volume: 1.0, 
  pitch: 1.0
}

effect_player := EffectPlayer{
  delay_0: [
    &other_effect,
    SoundPlayer{
      sound: 'minecraft:entity.villager.no', 
      volume: 1.0, 
      pitch: 1.0
    }
  ]
  delay_1: [
    SoundPlayer{
      sound: 'minecraft:block.iron_trapdoor.close', 
      volume: 1.0, 
      pitch: 1.0
    }
  ]
}
```

</td>
</table>

## Usage

To make use of this file format in your code, you must first register any classes 
that are used in your NBOFile with the NBOSerializer. After that, you can load
your NBOFile and retrieve the values that you need. You can also take a look at the
test cases, where this example originates from and where you can also see the
(de-)serialization methods for this example class as well as the corresponding source nbo
file (`vector_test.nbo`).
```java
// registration must be done for every class that you want to deserialize once before first usage
NBOSerializer.register(
        Vecto3f.class,
        Vector3f::deserialize,  // Function<Map<String, ?>, T>
        Vector3f::serialize     // Function<T, Map<String, Object>>
);
NBOSerializer.register(
        Matrix3f.class,
        Matrix3f::deserialize,
        Matrix3f::serialize
);

File file = /*...*/; // load your file
NBOFile nbo = NBOFile.loadFile(file);

Vector3f vector = nbo.get("vec"); // note that this might throw in a class cast exception
Matrix3f matrix = nbo.get("matrix");
```
