// This file is imported from https://github.com/scala/scala/blob/v2.13.3/src/library/scala/util/matching/Regex.scala.

package codes.quine.labo.re2s

import scala.collection.AbstractIterator

import com.google.re2j.Matcher
import com.google.re2j.Pattern

class Regex private[re2s] (val pattern: Pattern, groupNames: String*) extends Serializable { self =>
  import Regex._

  def this(regex: String, groupNames: String*) = this(Pattern.compile(regex), groupNames: _*)

  def unapplySeq(s: CharSequence): Option[List[String]] = {
    val m = pattern.matcher(s)
    if (runMatcher(m)) Some(List.tabulate(m.groupCount())(i => m.group(i + 1)))
    else None
  }

  def unapplySeq(c: Char): Option[List[Char]] = {
    val m = pattern.matcher(c.toString)
    if (runMatcher(m))
      if (m.groupCount > 0) Some(m.group(1).toList) else Some(Nil)
    else None
  }

  def unapplySeq(m: Match): Option[List[String]] =
    if (m.matched == null) None
    else if (m.matcher.pattern == this.pattern) Regex.extractGroupsFromMatch(m)
    else unapplySeq(m.matched)

  protected def runMatcher(m: Matcher): Boolean = m.matches()

  def findAllIn(source: CharSequence): MatchIterator = new MatchIterator(source, this, groupNames)

  def findAllMatchIn(source: CharSequence): Iterator[Match] = {
    val matchIterator = findAllIn(source)
    new AbstractIterator[Match] {
      def hasNext: Boolean = matchIterator.hasNext
      def next(): Regex.Match = {
        matchIterator.next()
        new Match(matchIterator.source, matchIterator.matcher, matchIterator.groupNames).force
      }
    }
  }

  def findFirstIn(source: CharSequence): Option[String] = {
    val m = pattern.matcher(source)
    if (m.find()) Some(m.group) else None
  }

  def findFirstMatchIn(source: CharSequence): Option[Match] = {
    val m = pattern.matcher(source)
    if (m.find()) Some(new Match(source, m, groupNames)) else None
  }

  def findPrefixOf(source: CharSequence): Option[String] = {
    val m = pattern.matcher(source)
    if (m.lookingAt()) Some(m.group) else None
  }

  def findPrefixMatchOf(source: CharSequence): Option[Match] = {
    val m = pattern.matcher(source)
    if (m.lookingAt()) Some(new Match(source, m, groupNames)) else None
  }

  def matches(source: CharSequence): Boolean =
    runMatcher(pattern.matcher(source))

  def replaceAllIn(target: CharSequence, replacement: String): String = {
    val m = pattern.matcher(target)
    m.replaceAll(replacement)
  }

  def replaceAllIn(target: CharSequence, replacer: Match => String): String = {
    val it = new MatchIterator(target, this, groupNames).replacementData
    it.foreach(m => it.replace(replacer(m)))
    it.replaced
  }

  def replaceSomeIn(target: CharSequence, replacer: Match => Option[String]): String = {
    val it = new MatchIterator(target, this, groupNames).replacementData
    for (m <- it; replacement <- replacer(m)) it.replace(replacement)
    it.replaced
  }

  def replaceFirstIn(target: CharSequence, replacement: String): String = {
    val m = pattern.matcher(target)
    m.replaceFirst(replacement)
  }

  def split(toSplit: String): Array[String] =
    pattern.split(toSplit)

  def unanchored: UnanchoredRegex = new Regex(pattern, groupNames: _*) with UnanchoredRegex {
    override def anchored: Regex = self
  }
  def anchored: Regex = this

  def regex: String = pattern.pattern

  override def toString: String = regex
}

trait UnanchoredRegex extends Regex {
  override protected def runMatcher(m: Matcher): Boolean = m.find()
  override def unanchored: UnanchoredRegex = this
}

object Regex {
  trait MatchData {
    protected def matcher: Matcher

    val source: CharSequence

    val groupNames: Seq[String]

    def groupCount: Int

    def start: Int

    def start(i: Int): Int

    def end: Int

    def end(i: Int): Int

    def matched: String =
      if (start >= 0) source.subSequence(start, end).toString
      else null

    def group(i: Int): String =
      if (start(i) >= 0) source.subSequence(start(i), end(i)).toString
      else null

    def subgroups: List[String] = (1 to groupCount).toList.map(group(_))

    def before: CharSequence =
      if (start >= 0) source.subSequence(0, start)
      else null

    def before(i: Int): CharSequence =
      if (start(i) >= 0) source.subSequence(0, start(i))
      else null

    def after: CharSequence =
      if (end >= 0) source.subSequence(end, source.length)
      else null

    def after(i: Int): CharSequence =
      if (end(i) >= 0) source.subSequence(end(i), source.length)
      else null

    private[this] lazy val nameToIndex: Map[String, Int] = ("" :: groupNames.toList).zipWithIndex.toMap

    def group(id: String): String =
      if (groupNames.isEmpty) matcher.group(id)
      else nameToIndex.get(id).fold(matcher.group(id))(group(_))

    override def toString: String = matched
  }

  class Match(val source: CharSequence, protected[re2s] val matcher: Matcher, val groupNames: Seq[String])
      extends MatchData {
    val start: Int = matcher.start
    val end: Int = matcher.end

    def groupCount: Int = matcher.groupCount

    private[this] lazy val starts: Array[Int] = Array.tabulate(groupCount + 1)(matcher.start(_))
    private[this] lazy val ends: Array[Int] = Array.tabulate(groupCount + 1)(matcher.end(_))

    def start(i: Int): Int = starts(i)
    def end(i: Int): Int = ends(i)

    def force: this.type = { starts; ends; this }
  }

  object Match {
    def unapply(m: Match): Some[String] = Some(m.matched)
  }

  @inline private def extractGroupsFromMatch(m: Match): Option[List[String]] =
    Some(List.tabulate(m.groupCount)(i => m.group(i + 1)))

  object Groups {
    def unapplySeq(m: Match): Option[List[String]] =
      if (m.groupCount > 0) extractGroupsFromMatch(m) else None
  }

  class MatchIterator(val source: CharSequence, val regex: Regex, val groupNames: Seq[String])
      extends AbstractIterator[String]
      with Iterator[String]
      with MatchData { self =>
    protected[Regex] val matcher: Matcher = regex.pattern.matcher(source)

    // 0 = not yet matched, 1 = matched, 2 = advanced to match, 3 = no more matches
    private[this] var nextSeen = 0

    def hasNext: Boolean = {
      nextSeen match {
        case 0     => nextSeen = if (matcher.find()) 1 else 3
        case 1 | 3 => ()
        case 2     => nextSeen = 0; hasNext
      }
      nextSeen == 1
    }

    def next(): String = {
      nextSeen match {
        case 0 => if (!hasNext) throw new NoSuchElementException; next()
        case 1 => nextSeen = 2
        case 2 => nextSeen = 0; next()
        case 3 => throw new NoSuchElementException
      }
      matcher.group
    }

    override def toString: String = super[AbstractIterator].toString

    private[this] def ensure(): Unit = nextSeen match {
      case 0     => if (!hasNext) throw new NoSuchElementException
      case 1 | 2 => ()
      case 3     => throw new NoSuchElementException
    }

    def start: Int = { ensure(); matcher.start }
    def start(i: Int): Int = { ensure(); matcher.start(i) }
    def end: Int = { ensure(); matcher.end }
    def end(i: Int): Int = { ensure(); matcher.end(i) }

    def groupCount: Int = { ensure(); matcher.groupCount }

    private[re2s] def replacementData = new AbstractIterator[Match] with Replacement {
      def matcher = self.matcher
      def hasNext = self.hasNext
      def next() = { self.next(); new Match(source, matcher, groupNames).force }
    }
  }

  private[re2s] trait Replacement {
    protected def matcher: Matcher

    private def sb: java.lang.StringBuilder = new java.lang.StringBuilder

    def replaced: String = {
      val newsb = new java.lang.StringBuilder(sb)
      matcher.appendTail(newsb)
      newsb.toString
    }

    def replace(rs: String): Matcher = matcher.appendReplacement(sb, rs)
  }

  def quote(s: String): String = Pattern.quote(s)
  def quoteReplacement(s: String): String = Matcher.quoteReplacement(s)
}
