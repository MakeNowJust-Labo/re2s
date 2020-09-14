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

- - -

This library's source code is derived from Scala's standard library.
The original Scala's NOTICE file is quoted at the below.

<details>

  ```
  Scala
  Copyright (c) 2002-2020 EPFL
  Copyright (c) 2011-2020 Lightbend, Inc.

  Scala includes software developed at
  LAMP/EPFL (https://lamp.epfl.ch/) and
  Lightbend, Inc. (https://www.lightbend.com/).

  Licensed under the Apache License, Version 2.0 (the "License").
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  This software includes projects with other licenses -- see `doc/LICENSE.md`.
  ```

</details>
