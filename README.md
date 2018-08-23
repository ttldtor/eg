eg - simple event generator

Usage:
```console
eg --help
eg --sample <sample file name>
eg [[<host>] [<DateTime {yyyy-mm-dd}> or now] [<interval>] [<type: int or double>] [<minValue>] [<maxValue>]]
```
    
Examples:
```console
eg localhost now 5 int 0 5
eg localhost 2018-08-11 1 double 500.0 2000.0
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