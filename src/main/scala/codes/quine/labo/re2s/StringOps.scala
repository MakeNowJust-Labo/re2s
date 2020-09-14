package codes.quine.labo.re2s

final class StringOps(private val s: String) extends AnyVal {
  def r2: Regex = r2()
  def r2(groupNames: String*): Regex = new Regex(s, groupNames: _*)
}
