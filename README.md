# Alien Language Exercise

You have landed on a strange planet and discovered the remnants of an alien civilization. You have uncovered evidence of their alien language which uses the same letters as in the English alphabet, but not necessarily in the same order. You would like to deduce the ordering of the alphabet. To aid you in your task, your science team has uncovered an alien lexicon, giving some words in the alien language in sorted order (sorted according to the ordering of the alien alphabet, not necessarily the same ordering as English). Using this lexicon, deduce as much of the ordering of the alien alphabet as you can.

Sample Input:

```
7
3
z
x
y
2
ce
he
3
zc
bc
zd
2
xy
xyz
2
hulu
hul
4
a
a
b
c
3
e
je
jj
```

Sample Output:
```
Case #1
zxy
EXACT
Case #2
ceh
AMBIGUOUS
Case #3
bcdz
INCONSISTENT
Case #4
xyz
AMBIGUOUS
Case #5
hlu
INCONSISTENT
Case #6
abc
EXACT
Case #7
ej
EXACT
```


Explanation:

In case 1, the lexicon gives enough information to determine "zxy" is the ordering of the alphabet. 
In case 2, "che", "ceh", and "ech" are all possible orderings. Of these, "ceh" comes first alphabetically (according to the English alphabet). 
In case 3, the lexicon is not consistently sorted because 'z' comes both before and after 'b'. The output is "bcdz" because that is how the letters in the alien language would be ordered according to the English alphabet's ordering. 
In case 4, "xy" is a prefix of "xyz" so it will come before "xyz" regardless of the ordering of the alphabet. The lexicon gives no information and thus all 6 permutations of "xyz" are possible orderings. Of these, "xyz" is the lexicographically first ordering. 
In case 5, "hul" is a prefix of "hulu", but "hul" comes after "hulu". Thus, the lexicon is inconsistently sorted. 
In case 6, the lexicon contains a duplicate word, "a", but still provides enough information to determine an ordering. 
In case 7, we can unambiguously determine that "ej" is the correct ordering.


Input Format:

Read your input from a file. The first line of the file contains N, the number of test cases. Following this are N test cases. Each test case beings with a line containing a single integer L, the number of words in the alien lexicon for that test case. The next L lines each contain a single word, comprising the alien lexicon. The words are ordered according to the alien alphabet.

Output Format:

For each test case: 
1) On the first line, output 'Case #n' (without quotes), where n is the case number (starting from 1). 
2) On the second line, output a single string giving the ordering of the alien alphabet. If the lexicon does not give enough information to deduce a single ordering, output the lexicographically first ordering (lexicographical according to the English alphabet ordering). If the lexicon is not consistently sorted, output the letters in the alien language ordered by the English alphabet ordering. 
3) On the third line, output either EXACT if the ordering can be unambiguously determined, AMBIGUOUS if the lexicon does not give enough information, or INCONSISTENT if the lexicon is not consistently sorted.


Bounds:

These are assumptions you can make about the input. You do not need to test for them.

1 <= N <= 200 
1 <= L <= 200 
Each word in the lexicon will consist of between 1 and 100, inclusive, lowercase letters 'a' through 'z'.


