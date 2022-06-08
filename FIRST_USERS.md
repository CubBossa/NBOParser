# NBO Configuration

---

## Content

- [Basic Structure](#Basic_Structure)
- [Declaring Variables](#Declaring_Variables)
- [Maps vs Lists](#Maps_and_Lists)
- [Imports](#Imports)
- [Link multiple NBO Files](#Link_multiple_NBO_Files)
---

## Basic Structure

The basic idea of the NBO file format is to simplify the configuration process for administrators with plugins that use
many smaller objects, which might be in relation to each other. NBO allows you to create an object of any type as a
variable and reference it throughout the whole script afterwards.

The whole language is based on the json-like NBT format, that is used by minecraft. Therefore you might be familiar with
json formatting and the file structure.

Example:

<pre>
<span style="color:#529755"># Create a variable containing the value 10.</span>
<span style="color:orange">simple-cooldown</span> := 10

<span style="color:#529755"># Reference it from anywhere afterwards with the & reference symbol</span>
vote-reward := {
    message: <span style="color:#529755">...</span>,
    sound: <span style="color:#529755">...</span>,
    cooldown: <span style="color:orange">&simple-cooldown</span> <span style="color:#529755"># <- reference</span>
}
</pre>

In general, NBT files have the following rules:

- Curly brackets represent an ordered map:<br>`{key: value, key: value, key: value, ...}`
- Square brackets represent an ordered list:<br>`[value, value, value, ...]`
- Values are the following primitives:
    - Strings: `"require quotation marks"`
    - Booleans: `true`, `false`, `1b`, `0b`
    - Bytes (127 to -128): `<value>b`, e.g. `32b`
    - Shorts ($2^(16)-1$ to $-2^(16)$): `<value>s`, e.g. `10000s`
    - Integers ($2^(32)-1$ to $-2^(32)$): `<value>[i]`, e.g. `10`
    - Longs ($2^(64)-1$ to $-2^(64)$): `<value>l`, e.g. `0xffffffffl`
    - Floats (32 bit): `<value>f`, e.g. `3f`, `0.12354`, `.1f`
    - Doubles (64 bit): `<value>d`, e.g. `3d`, `0.12354d`, `.1d`
- Key Value declarations are to be made with a colon, `key: value`
- Entries are separated with a comma: `[value, value, ...]`

## Declaring Variables

Variables / Constants can be declared by using the assign operator ':='.

They can only be used in the "root layer" of your script and not within a block '{...}' This leads to a script structure
of many following constants:

<pre>
a := xy
b := {<span style="color:#529755">...</span>}
c := [<span style="color:#529755">...</span>]
<span style="color:#529755">...</span>
</pre>

They must not be separated by a comma and can simply be put in the script. You don't have to worry about spaces, tabs
and line breaks, as they will be removed before parsing the script.

Every value type can be declared as a variable and be used as reference later.

## Maps and Lists

Objects are represented as lists `[...]` or maps `{...}` and have a specifier at the opening bracket. So an ItemStack
object in Minecraft would be declared like so:

<pre>
my-item := ItemStack {
    id: 'minecraft:apple',
    Count: 3b,
    <span style="color:#529755">...</span>
}
</pre>

This might seem very familiar when having some experience with datapacks or command blocks, as the item stack simply
represents valid NBT, with additional type definition before the block begins.

Keep in mind, that objects can only be deserialized if the creator of the corresponding plugin enabled it in code. If
something does not meet your expectations, please contact the plugins author.

If no type definition is placed before a map or a list block, the map will simply be interpreted as ordered map of key
value pairs. Lists will be interpreted as ordered lists. Ordered means, that the 'insertion' order will be preserved.

# Imports

The previous example of an item stack declaration was not exactly correct. Instead, it would have to look like so:
<pre>
my-item := <span style="color:orange">org.bukkit.inventory.ItemStack</span> {
    id: 'minecraft:apple',
    Count: 3b,
    <span style="color:#529755">...</span>
}
</pre>

'org.bukkit.inventory.ItemStack' is the precise name of the class that the following block will represent. This is not
exactly intuitive and easy to understand for beginners, especially with no programming experience. Therefore, you can
simply create shortcuts via imports.

By adding the line

```
<with ItemStack as org.bukkit.inventory.ItemStack>
```

at the top of your script, you can then always use the keyword
'ItemStack' to reference an org.bukkit.inventory.ItemStack class.
Good plugin developers provide you an import file with all imports that you could possibly need.
If not, contact the plugin author and he or she might help you out.

## Link multiple NBO Files

The more objects you declare in your NBO script, the messier it gets. To deal with this issue,
you can simply move object declarations and imports to other files.

Let's say, you have a plugin to create custom weapons. Your weapons.nbo will be loaded from the weapons plugin.
Create a new file and call it whatever you like, e.g. 'swords.nbo'.

Go to the first line of your weapons.nbo script (not necessarily the first, but before the first import)
and insert the following line.
```
<include plugins/weaponPlugin/swords.nbo>
```
Of course you must make sure to have the right path to the file, but with this line set all objects and imports
of the swords.nbo file will be loaded first. This even means, that you can move all imports to a separate
imports.nbo file and put the 'include' tag at the first line of every other script.
