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