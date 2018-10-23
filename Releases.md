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