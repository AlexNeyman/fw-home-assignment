# Introduction

Guy was assigned a ticket in YouTrack. The task was to implement the following specification:

```
Write a program in Scala that uses HTTP4S and circe to expose a web service with the following endpoints:

Endpoint A:
Path: /fareplace/flightExists
Method: POST
Payload: { origArp: "TLV", destArp: "BER", date: "2022-04-01", flightNum: "1"}
Response: true if the flight exists, otherwise return false.

The list of flights is kept on disk as a CSV file can be quite large, so avoid loading it into memory. When the file changes, the program should reflect them dynamically. 
```

Guy asked ChatGPT to solve it for him, created a PR request based on the output, and then quickly left the office,
heading for a week-long vacation in Greece.

You are expected to review "his" work and fix the code if it needs to be.

How does this code fare in terms of scalability, performance, and consistency? Feel free to suggest as many
architectural changes as you believe are necessary, and then afterward implement the two issues you deem the most
important.

# Changes

## Project itself

I didn't manage to run the project as it was. SBT simply couldn't deal with it for an incomprehensible reason.

So I created a new project and copied the code from the old one. I also had to change the Scala version.

## Performance

The way the service worked originally is it reads the file on every request, and seeks for a match line by line. This is
not efficient, because it has time complexity of O(n), where n is the number of lines in the file.

A time-efficient approach is to read the file on startup and populate a lookup-efficient data structure by of all
existing flights.
The first idea was to use a set, where a row from the file becomes its item. A simple concatenation of all four flight
parameters gives us around 1.1 MB for 12k (test file) flights. We can improve it a bit by hashing the resulting string
and taking only a part of the hash. If we use 8 hash chars per flight, our 12k flights will cost us around 900 KB of
memory, and we still have a good collision resistance.

Though, pre-COVID numbers of 40 million flights annually (plus the growing trend) may require us to look for a more
efficient solution if we want to cover multiple years of flights while using commodity hardware.

At one point I thought about a Bloom filter, but it may return false positives. The Bloom filter algorithm uses a bit
array under the hood, so I thought about utilizing this technique somehow. I found interesting the idea of mapping the
(from, to, num) triplet to a bitmap of available dates. Just to keep things simple (I still don't know Scala), in the
code I used boolean array instead of bitmap.

The last approach gives the same time complexity of O(1) for lookups, but it requires only a negligible amount of
memory: just 16 KB for 12k flights. There is still a room for optimization; for example, replacing boolean arrays with
bitmaps will reduce the memory usage by a factor of 8.

The downside of it is that we need to know the range of dates in advance, so we can calculate the size of the dates
array. If our files are sorted, we can easily find the range by looking at the first and the last line. In the code I
simply hardcoded the range that covers the test file flights.

## Notes

* DTO
* DB file location
* Class files
* Check everything
