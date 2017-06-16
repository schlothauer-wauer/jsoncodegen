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
* ??

##Additional attributes for properties
* since - since what version the attribute is uses
* visKey - this attribute works as an visual communication key - for instance name
* unique - This attribute contains unique values