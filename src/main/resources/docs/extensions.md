# What are the extensions to the standard JSON schema
## Replace of standard version definition in a multi type schema
Original Version property
```
...
  "type": "object",
  "properties": {
    "model_version": {
      "description": "version of the current model",
      "type": "number",
      "enum": [
        1
      ],
      "maximum": 1,
      "minimum": 1
    },
    ...
  }

```

Additional short version property
```
  "type": "object",
  "version" 1,
  ...
```
The 'type' attribute has always an integer type

##Additional attributes for types
* __tags - string array with additional tags/keywords that can be used select/deselect desired types for code generation

##Additional attributes for properties
* __since - since what version the attribute is uses
* __visKey - this attribute works as an visual communication key - for instance name
* __unique - This attribute contains unique values
* __ref - implicit reference of an ID type to another type
* __tags - string array with additional tags/keywords that can be used select/deselect desired types for code generation
