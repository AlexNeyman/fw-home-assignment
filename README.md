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

The way the service worked originally is it read the file on every request, and sought for a match line by line. This is
not efficient, because:

1) Even though that reading a file may be pretty fast with the right combination of OS, filesystem and hardware, it is
   still not free.
2) Looking for a match line by line has time complexity of O(N), where N is the number of lines in the file.

A time-efficient approach is to read the file on startup and populate some lookup-efficient data structure with all
existing flights.

The first idea was to use a set, where a row from the file becomes its item. A simple concatenation of all four flight
parameters gives us around 1.1 MB for 12k (test file) flights. We can improve it a bit by hashing the resulting string
and taking only a part of the hash. If we use 8 hash chars per flight, our 12k flights will cost us around 900 KB of
memory, and we still have a good collision resistance.

Though, pre-COVID numbers of 40 million flights annually (plus the growing trend) may require us to look for a more
efficient solution if we want to cover multiple years of flights while using commodity hardware.

At one point I thought about a Bloom filter, but it may return false positives. The Bloom filter algorithm uses a bit
array under the hood, so I thought about utilizing this technique somehow. I found interesting the idea of mapping the
`(from, to, num)` triplet to a bitmap of available dates. Just to keep things simple, in the code I used a boolean
vector instead of a bitmap. This gives us the same time complexity of O(1) for lookups, but it requires only a
negligible amount of memory: just 62 KB for 12k flights.

There is still a room for optimization. For example, initially I used boolean arrays, that required to know the first
and last flight dates in advance to set their length, but with arrays 12k flights consumed only 16 KB of memory. And
finally, by replacing arrays with bitmaps we will reduce memory usage further by a factor of 8.

Initially I used Arrays instead of

## DB updates

The original code didn't have to bother with file updates, because it simply re-read the file on every request.

Together with the "database" I tried my best to implement a file watcher based on `cats.effect`. The thing is that after
just a couple of days of playing with Scala I cannot truly validate how idiomatic and efficient this code is.

Btw, the `FlightDB` is written in a partially mutable manner. I'm not sure how far idiomatic Scala prefers going with
immutability when we are talking about millions of objects.

## Scalability

On my local machine in the dev mode with logs enabled and without any JVM tuning the service performs like this:

```
Summary:
  Total:        1.5058 secs
  Slowest:      0.0308 secs
  Fastest:      0.0001 secs
  Average:      0.0007 secs
  Requests/sec: 66408.9598
```

In order to scale the service, we can run multiple instances of it behind a load balancer. I don't think there may be a
need to shard the data between instances, because the data structure is very compact and can be easily kept in memory
entirely.

## Other stuff

* I left the hardcoded file path, listen address and port, but it definitely should be configurable.
* There are unhandled errors here and there for operations like parsing and file reading.
* There is definitely a room for tests. I wrote only a couple of unit tests for the `FlightDB` class.
* Service should be more HTTP-compliant. For example, it should only accept `Content-Type: application/json` requests,
  unless there is a good reason to do otherwise.
