# Alien Language Exercise

## Running

```
git clone git@github.com:worace/alien-language.git
cd alien-language
lein test
lein run
```

## Notes

This is an interesting problem and I tried to approach it as generally as possible. Partly I was initially still a
little confused after looking through the relatively small number of examples and explanation provided with
the assignment, so I wanted to try to write an implementation that helped me break down the concepts more generally.

I tried to break the process into 2 high level steps. First, analyze the collections of input words to figure out what
ordering relationships were contained within the given dictionary snippet. Then, take those relationships and determine
whether they were a) contradictory, b) ambiguous, or c) sufficiently detailed to fully determine the ordering of the included
selection of letters.

I designed much of the code around a common data structure for representing the ordering of letters with relation to other letters.
For each letter, you can recognize 2 sets, one of the letters that are observed to _precede_ it in the lexicon and one
of the letters that _follow_ it. For the simple lexicon `z, x, y`, we would see something like:

```clojure
;; #{} makes a Set in clojure; {} makes a map
{"z" {:preceding #{} :following #{"x" "y"}}
 "x" {:preceding #{"z"} :following #{"y"}}
 "y" {:preceding #{"z" "x"} :following #{}}
 }
```

Building these relationships is the tricky part, but once I established them, I was able to then make determinations
about how thorough the information contained was:

1. If any letter has overlap between the letters that precede it and the letters that follow it, that means the ordering is inconsistent
2. If the union of preceding and following letters is not equal to the entire set of letters from the lexicon (minus the current letter itself), that means we don't have enough info to fully determine the order, so it is inconsistent
3. Finally if neither of these are the case, we should have enough info to build a determinant ordering out of the observed order relationships

### Dealing with Stemming

Another tricky part was handling the nested information contained between letters that appear following the same prefix:

```
aa
ab
b
c
```

The important thing about these cases is realizing that it's really 2 separate lexicon snippets: `["a" "a" "b" "c"]` (the first letters) and `["a" "b"]` (the second letters following the prefix `"a"`).

To handle these more consistently, I added an initial step which "flattened" the actual lexicon information contained in a given collection of words. So `["aa" "ab" "b" "c"]` becomes `[["a" "a" "b" "c"] ["a" "b"]]`.

Then I was able to build the ordering relationships between each segment as described above and then finally merge them all together.

So:

```clojure
["aa" "ab" "b" "c"]

becomes:

[["a" "a" "b" "c"] ["a" "b"]]

becomes:

[{"a" {:preceding #{} :following #{"b" "c"}}
  "b" {:preceding #{"a"} :following #{"c"}}
  "c" {:preceding #{"a" "b"} :following #{}}}
  ;;(^^ one set of relationships for the first segment)
 {"a" {:preceding #{} :following #{"b"}}
  "b" {:preceding #{"a"} :following #{}}}
  ;;(^^ another set of relationships for the second)
]
```

In this simple example the second set doesn't contain any new information not already represented in
the first set, but in many cases the nested suffix relationships are necessary to fill in
details on the ordering.

### Trying it out on English

One of the things I struggled with most was envisioning all of the possible cases that could come up.
Things get complicated fast when longer words and lots of nesting is involved, and it was tricky to build
scenarios like this by hand.

However I realized I had one ready-made example on hand -- the process we described for an "alien" language
would actually work just as well for English.

So I wrote up a simple script that would grab random samplings of the english dictionary and attempt to
establish the ordering based on these. If the algorithm is working correctly, I would expect that a) a given
sample from the dictionary should never prove inconsistent and b) with a large enough sample size we should
be able to establish the english alphabet as an exact match.

Empirically I found that I could establish an order some of the time with a 1% or 2% sample, and 5% would produce
an exact match pretty reliably:

```
worace @ alien-language ➸  lein run 
Attempting to order English using 5% sample from the dictionary...
{:status EXACT, :output abcdefghijklmnopqrstuvwxyz}
```

### Missing Pieces

Given more time the big thing I would have liked to focus on is profiling and optimization. With the time allotted
I was playing pretty loose with loops and iterations, and was really focusing more on my data structures and
the concepts of the algorithm I was trying to work out.

This helped me work through the problem and understand things more clearly, but I also think I probably left some
low-hanging fruit as far as optimizations that could be made.

Finally the one case that I couldn't get to work consistently with my approach was out-of-order prefixes (like the
`["hulu" "hul"]` example provided in the prompt). So I ended up having to special case this one to recognize inconsistency.
I would have liked to find a way to make that fit with the rest of the model more consistently.

## Problem:

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


