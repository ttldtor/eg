eg - simple event generator
--
Generates random kpi values by current time

#### Usage:
```console
eg --help
eg --sample <sample file name>
```
    
#### Examples:
```console
eg --sample ./1.sample
```

#### Sample File Format (activity by formula):
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
##### Activity formula:
```
A = 0.877976 * (1 / (1 + (9 - hours) ^ 2) + 1 / (1 + (12 - hours) ^ 2) + 1 / (1 + (17 - hours) ^ 2))
hours = [0 .. 24) -- current hours
```
[Wolfram](https://bit.ly/2wPdRjj)

![Graph](https://user-images.githubusercontent.com/3264871/210156374-26643b91-3b00-4293-846e-4b89f48d2600.png)

##### Common activity formula:

```math
\displaylines{F(x, \textbf{b}) = \displaystyle\sum_{1 \leq i \leq n} \frac{1}{1 + (\textbf{b}_{i} + x^2) }\\
\textbf{b}_{i} < \textbf{b}_{i + 1}, (i = 1, \dotsc , n - 1)\\
C(\textbf{b}) = \frac{1}{\displaystyle\max_{1 \leq i \leq n} F(\textbf{b}_{i}, \textbf{b})}\\
A(x, \textbf{b}) = C(\textbf{b}) F(x, \textbf{b})}
```
**b** - activity hours

#### Sample File Format 2 (activity coefficients):
```json
[
  {
    "kpi": "hpsm-1",
    "value1": 0,
    "value2": 30,
    "activity": {
      "00:00": 0.001,
      "08:30": 0.5,
      "09:00": 1.0,
      "10:30": 0.5,
      "12:00": 1.0,
      "14:30": 0.5,
      "17:00": 1.0,
      "17:30": 0.5,
      "24:00": 0.001
    }
  },
  {
    "kpi": "internet-rub",
    "value1": 100000,
    "value2": 300000,
    "activity": {
      "00:00": 0.001,
      "08:30": 0.5,
      "09:00": 1.0,
      "10:30": 0.5,
      "12:00": 1.0,
      "14:30": 0.5,
      "17:00": 1.0,
      "17:30": 0.5,
      "24:00": 0.001
    }
  },
  {
    "kpi": "lte-users",
    "value1": 1000,
    "value2": 3000,
    "activity": {
      "00:00": 0.001,
      "08:30": 0.5,
      "09:00": 1.0,
      "10:30": 0.5,
      "12:00": 1.0,
      "14:30": 0.5,
      "17:00": 1.0,
      "17:30": 0.5,
      "24:00": 0.001
    }
  }
]
```

#### Output Format:
```json
{"time": "2018-09-07T01:01:12.0714295", "hpsm-1": 2, "internet-rub": 120825, "lte-users": 1155}
```
