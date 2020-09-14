# RE2/S

> Scala flavored [RE2/J][].

In other words, this library provides [`scala.util.matching.Regex`][`matching.Regex`]-like APIs for [RE2/J][].

[RE2/J]: https://github.com/google/re2j
[`matching.Regex`]: https://www.scala-lang.org/api/current/scala/util/matching/Regex.html

## Example

To use this instead of [`matching.Regex`][], you should replace `.r` with `.r2`.
It's easy!

```scala
scala> import codes.quine.labo.re2s._
import codes.quine.labo.re2s._

scala> val date = raw"""(\d{4})-(\d{2})-(\d{2})""".r2
date: Regex = (\d{4})-(\d{2})-(\d{2})

scala> "1955-11-12" match {
     |   case date(year, month, day) => s"He backed to the future at $year/$month/$day"
     | }
res0: String = He backed to the future at 1955/11/12

scala> "We alive over 2015-11-21." match {
     |   case date.unanchored(year, month, day) => s"$year/$month/$day"
     | }
res1: String = 2015/11/21
```

Of course, other APIs of [`matching.Regex`][] are supported.

## TODO

- [ ] add a test covering all functions.
- [ ] add useful scaladoc comments.

## License

MIT License.

2020 (C) TSUYUSATO "MakeNowJust" Kitsune
