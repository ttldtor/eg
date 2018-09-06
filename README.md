eg - simple event generator

Usage:
```console
eg --help
eg --sample <sample file name>
```
    
Examples:
```console
eg --sample ./1.sample
```

Sample File Format:
```json
[
  {
    "kpi": "hpsm-1",
    "value1": 0,
    "value2": 30
  },
  {
    "kpi": "internet-rub",
    "value1": 100000,
    "value2": 300000
  },
  {
    "kpi": "lte-users",
    "value1": 1000,
    "value2": 3000
  }
 ]
```

Sample File Format 2:
```json
[
  {
    "kpi": "hpsm-1",
    "value1": 0,
    "value2": 30,
    "activity": {
      "00:00": 0.0,
      "08:30": 0.5,
      "09:00": 1.0,
      "10:30": 0.5,
      "12:00": 1.0,
      "14:30": 0.5,
      "17:00": 1.0,
      "17:30": 0.5,
      "24:00": 0.0
    }
  },
  {
    "kpi": "internet-rub",
    "value1": 100000,
    "value2": 300000,
    "activity": {
      "00:00": 0.0,
      "08:30": 0.5,
      "09:00": 1.0,
      "10:30": 0.5,
      "12:00": 1.0,
      "14:30": 0.5,
      "17:00": 1.0,
      "17:30": 0.5,
      "24:00": 0.0
    }
  },
  {
    "kpi": "lte-users",
    "value1": 1000,
    "value2": 3000,
    "activity": {
      "00:00": 0.0,
      "08:30": 0.5,
      "09:00": 1.0,
      "10:30": 0.5,
      "12:00": 1.0,
      "14:30": 0.5,
      "17:00": 1.0,
      "17:30": 0.5,
      "24:00": 0.0
    }
  }
]
```

Output Format:
```json
{"time": "2018-09-07T01:01:12.0714295", "hpsm-1": 2, "internet-rub": 120825, "lte-users": 1155}
```