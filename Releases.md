## 0.8.3
- ignoreTag, neededTag are handled now as lists for multi-file-generators
- rename 'pmta' command line switch to specify a required attribute for main types to 'mta'
- add 'tmt' command line switch to add automatically a 'mainType' tag to all main types

## 0.8.2
- needed fix in 'pmta' command line switch

## 0.8.1
- add to types schemaPath and schemaFileName attributes (to finally detect what are the interesting main types of a model)
- add a isMainType function to model type
- add command line switches pro print only main types

## 0.8.0
- command line switches for type black- and white-lists
- command line switch to add tags to types
- command line switch to remove a tag from types
- command line switch to remove a tag from all types
- enforce camel-case type names in models
- add guidTypeColor generator parameter for plantuml
- remove printTags generator parameter for plantuml
- add printTypeTags generator parameter for plantuml
- add printPropTags generator parameter for plantuml

## 0.7.5
- deep copy functions for types
- introduce pure array type to handle definitions where arrays contain only arrays, f.e. geoJson Polygons 

```json
...
    "FeatureAreaGeometry": {
      "type": "object",
      "properties": {
        "type": {
          "type": "string"
        },
        "coordinates": {
          "type": "array",
          "items": {
            "type": "array",
            "items": {
              "type": "array",
              "items": {
                "type": "number"
              }
            }
          }
        },
        "projection": {
          "type": "string"
        }
      },
      "__tags": ["additional"]
    },
...
```

## 0.7.4
- add attributes `selfReference` and `selfContainment` to model property type