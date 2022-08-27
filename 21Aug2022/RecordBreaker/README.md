# Record Breaker

[Download the Starter Code!](https://codejam.googleapis.com/dashboard/get_file/AQj_6U0BBLsEvCxA4x4uDPMac4yfY3Z-Y-Ap8YdKHpr_uIYJ29oijl3ei1RqgYPBzxBp/starter_code.zip?dl=1)

Problem
---
Isyana is given the number of visitors at her local theme park on N consecutive days. The number of visitors on the i-th day is Vi. A day is record breaking if it satisfies both of the following conditions:

* Either it is the first day, or the number of visitors on the day is strictly larger than the number of visitors on each of the previous days.
* Either it is the last day, or the number of visitors on the day is strictly larger than the number of visitors on the following day.

Note that the very first day could be a record breaking day!

Please help Isyana find out the number of record breaking days.

Input
---
The first line of the input gives the number of test cases, T. T test cases follow. Each test case begins with a line containing the integer N. The second line contains N integers. The i-th integer is Vi and represents the number of visitors on the i-th day.

Output
---
For each test case, output one line containing Case #x: y, where x is the test case number (starting from 1) and y is the number of record breaking days.

Limit \
Time limit: 20 seconds.  
Memory limit: 1 GB.  
1 ≤ T ≤ 100.  
0 ≤ Vi ≤ 2 × 10^5, for all i.

Test Set 1 \
1 ≤ N ≤ 1000.

Test Set 2 \
1 ≤ N ≤ 2 × 10^5, for at most 10 test cases.  
For the remaining cases, 1 ≤ N ≤ 1000.

Sample
<table>
<tr>
<th>Sample Input</th>
<th>Sample Output</th>
</tr>
<tr>
<td>
  
```
4
8
1 2 0 7 2 0 2 0
6
4 8 15 16 23 42
9
3 1 4 1 5 9 2 6 5
6
9 9 9 9 9 9
```
  
</td>
<td>

```
Case #1: 2
Case #2: 1
Case #3: 3
Case #4: 0
```

</td>
</tr>
</table>

In Sample Case #1, the underlined numbers in the following represent the record breaking days: 1 <ins>2</ins> 0 <ins>7</ins> 2 0 2 0.

In Sample Case #2, only the last day is a record breaking day: 4 8 15 16 23 <ins>42<ins>.

In Sample Case #3, the first, the third, and the sixth days are record breaking days: <ins>3</ins> 1 <ins>4</ins> 1 5 <ins>9</ins> 2 6 5.
  
In Sample Case #4, there is no record breaking day: 9 9 9 9 9 9.

## Coding Practice with Kick Start Session #3. [Source](https://codingcompetitions.withgoogle.com/kickstart/round/00000000008f49d7/0000000000bcf2ed)
