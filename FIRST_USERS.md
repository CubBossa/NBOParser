# NBO Configuration

---

## Content
- Basic Structure
- Declaring Variables
- Maps vs Lists
- Imports
- Link multiple NBO Files

---

## Basic Structure

The basic idea of the NBO file format is to simplify the configuration
process for administrators with plugins that use many smaller objects, which might be
in relation to each other.
NBO allows you to create an object of any type as a variable and reference it
throughout the whole script afterwards.

The whole language is based on the json-like NBT format, that is used by minecraft.
Therefore you might be familiar with json formatting and the file structure.

Example:
```NBO
# Create a variable containing the value 10.
simple-cooldown := 10

# Reference it from anywhere afterwards with the & reference symbol
vote-reward := {
    message: ...,
    sound: ...,
    cooldown: &simple-cooldown # <- reference
}
```

In general, NBT files have the following rules:
- Curly brackets represent an ordered map:<br>`{key: value, key: value, key: value, ...}`
- Square brackets represent an ordered list:<br>`[value, value, value, ...]`
- Values are the following primitives:
  - Strings: `"require quotation marks"`
  - Booleans: `true`, `false`, `1b`, `0b`
  - Bytes (127 to -128): `<value>b`, e.g. `32b`
  - Shorts ($2^16-1$ to $-2^16$): `<value>s`, e.g. `10000s`
  - Integers ($2^32-1$ to $-2^32$): `<value>[i]`, e.g. `10`
  - Longs ($2^64-1$ to $-2^64$): `<value>l`, e.g. `0xffffffffl`

## Declaring Variables

Variables / Constants can be declared by using the assign operator ':='.

They can only be used in the "root layer" of your script and not within a block '{...}'
This leads to a script structure of many following constants:

```
a := xy
b := {...}
c := [...]
```

They must not be separated by a comma and can simply be put in the script.
You don't have to worry about spaces, tabs and line breaks, as they will be removed
before parsing the script.

Every value type can be declared as a variable and be used as reference later.